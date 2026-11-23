package com.stocksim;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.stocksim.account.AccountService;
import com.stocksim.ledger.LedgerService;
import com.stocksim.portfolio.PositionService;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

/**
 * Enforces the architecture rules of CLAUDE.md. Needs no database or Docker.
 * <ul>
 * <li>Spring Modulith: module structure, no cycles, no access to other modules' internal (sub-)packages.</li>
 * <li>ArchUnit: layering (SQL only in repositories), entity ("Row") types stay in their module, module-internal types
 * are not used from other modules, only {@code OrderExecutor} changes money.</li>
 * <li>Source scan: every table is read and written only by the repository of the module that owns it, except the
 * documented read-only reporting queries in {@link #REPORTING_EXCEPTIONS}.</li>
 * </ul>
 * Do not add an exception without asking the project owner.
 */
class ArchitectureTest {

	private static final String BASE = "com.stocksim";

	private static final JavaClasses MAIN = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages(BASE);

	/** Table -> owning module (package name below {@code com.stocksim}). */
	static final Map<String, String> TABLE_OWNER = Map.ofEntries(Map.entry("users", "auth"),
			Map.entry("accounts", "account"), Map.entry("ledger_entries", "ledger"), Map.entry("orders", "trading"),
			Map.entry("fills", "trading"), Map.entry("positions", "portfolio"),
			Map.entry("instruments", "instrument"), Map.entry("catalog_imports", "instrument"),
			Map.entry("watchlist_items", "watchlist"), Map.entry("account_snapshots", "performance"),
			Map.entry("benchmark_closes", "performance"), Map.entry("competitions", "competition"),
			Map.entry("competition_members", "competition"));

	/**
	 * DOCUMENTED EXCEPTIONS: read-only reporting queries that JOIN tables of other modules in one SQL query
	 * (for performance). Key: repository file, value: foreign tables it may READ (never write).
	 */
	static final Map<String, Set<String>> REPORTING_EXCEPTIONS = Map.of(
			"performance/LeaderboardRepository.java", Set.of("accounts", "users"),
			"competition/CompetitionStandingsRepository.java",
			Set.of("users", "accounts", "account_snapshots", "ledger_entries"));

	// ---------------------------------------------------------------- Spring Modulith

	@Test
	void modulesAreWellFormedAndHaveNoCycles() {
		ApplicationModules.of(BackendApplication.class).verify();
	}

	@Test
	void modulesAreFreeOfCyclesAlsoByArchUnit() {
		slices().matching(BASE + ".(*)..").should().beFreeOfCycles().check(MAIN);
	}

	// ---------------------------------------------------------------- layering

	@Test
	void jdbcIsUsedOnlyInRepositories() {
		noClasses().that()
			.haveSimpleNameNotEndingWith("Repository")
			.should()
			.dependOnClassesThat(jdbcTypes())
			.because("SQL belongs in *Repository classes only (controllers and services have none)")
			.check(MAIN);
	}

	@Test
	void repositoriesAreAnnotatedAndPackagePrivate() {
		classes().that()
			.haveSimpleNameEndingWith("Repository")
			.and()
			.areTopLevelClasses()
			.should()
			.beAnnotatedWith(Repository.class)
			.andShould()
			.notBePublic()
			.because("repositories are internal to their module; other modules use a service")
			.check(MAIN);
		classes().that()
			.areAnnotatedWith(Repository.class)
			.should()
			.haveSimpleNameEndingWith("Repository")
			.check(MAIN);
	}

	@Test
	void controllersDoNotDependOnRepositoriesOrEntityTypes() {
		noClasses().that()
			.areAnnotatedWith(RestController.class)
			.should()
			.dependOnClassesThat()
			.areAnnotatedWith(Repository.class)
			.orShould()
			.dependOnClassesThat()
			.haveSimpleNameEndingWith("Row")
			.because("Controller -> Service -> Repository; controllers get DTOs from services")
			.check(MAIN);
	}

	// ---------------------------------------------------------------- entities and module API

	@Test
	void entityTypesAreNotPublic() {
		classes().that()
			.haveSimpleNameEndingWith("Row")
			.should()
			.notBePublic()
			.andShould()
			.notBeProtected()
			.because("entity (table row) records stay inside their module")
			.check(MAIN);
	}

	@Test
	void moduleInternalTypesAreUsedOnlyInsideTheirPackage() {
		for (String suffix : List.of("Row", "Repository", "Controller")) {
			classes().that()
				.haveSimpleNameEndingWith(suffix)
				.should(beAccessedOnlyFromOwnPackage())
				.because("*" + suffix + " types are not part of a module's public API")
				.check(MAIN);
		}
	}

	// ---------------------------------------------------------------- money

	@Test
	void onlyOrderExecutorChangesMoney() {
		String executor = BASE + ".trading.OrderExecutor";
		noClasses().that()
			.doNotHaveFullyQualifiedName(executor)
			.should(callMethodOf(AccountService.class, "lock", "updateBalances"))
			.check(MAIN);
		noClasses().that()
			.doNotHaveFullyQualifiedName(executor)
			.should(callMethodOf(PositionService.class, "save", "addReserved"))
			.check(MAIN);
		// AccountService.openAccount writes the starting DEPOSIT; everything else goes through OrderExecutor.
		noClasses().that()
			.doNotHaveFullyQualifiedName(executor)
			.and()
			.doNotHaveFullyQualifiedName(BASE + ".account.AccountService")
			.should(callMethodOf(LedgerService.class, "append"))
			.check(MAIN);
	}

	// ---------------------------------------------------------------- table ownership

	private static final Pattern TABLE_USE = Pattern
		.compile("(?<kw>INSERT\\s+INTO|DELETE\\s+FROM|UPDATE|FROM|JOIN)\\s+(?<table>[a-z_]+)\\b");

	@Test
	void everyTableIsTouchedOnlyByItsOwnersRepository() throws IOException {
		Path root = Path.of("src/main/java/com/stocksim");
		List<String> violations = new ArrayList<>();
		Set<String> exceptionsUsed = new HashSet<>();
		int uses = 0;
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
				String relative = root.relativize(file).toString().replace('\\', '/');
				String module = relative.contains("/") ? relative.substring(0, relative.indexOf('/')) : "";
				boolean repository = file.getFileName().toString().endsWith("Repository.java");
				Matcher m = TABLE_USE.matcher(Files.readString(file));
				while (m.find()) {
					String table = m.group("table");
					String owner = TABLE_OWNER.get(table);
					if (owner == null) {
						continue; // not one of our tables (e.g. a CTE name or prose)
					}
					uses++;
					boolean write = m.group("kw").matches("INSERT\\s+INTO|DELETE\\s+FROM|UPDATE");
					if (table.equals("ledger_entries") && write && !m.group("kw").startsWith("INSERT")) {
						violations.add(relative + ": ledger_entries is append-only, found '" + m.group() + "'");
					}
					if (repository && owner.equals(module)) {
						continue;
					}
					Set<String> readable = REPORTING_EXCEPTIONS.get(relative);
					if (repository && readable != null && readable.contains(table) && !write) {
						exceptionsUsed.add(relative + ":" + table);
						continue;
					}
					violations.add(relative + ": '" + m.group() + "' (table " + table + " belongs to module " + owner
							+ (repository ? "" : "; SQL is allowed in *Repository classes only") + ")");
				}
			}
		}
		assertThat(uses).as("scan found SQL (guards against a vacuous test)").isGreaterThan(40);
		assertThat(violations).as("table ownership violations").isEmpty();

		// A stale exception (no longer needed) must be removed from the list.
		Set<String> declared = new HashSet<>();
		REPORTING_EXCEPTIONS.forEach((file, tables) -> tables.forEach(t -> declared.add(file + ":" + t)));
		assertThat(exceptionsUsed).as("every documented exception is still used").isEqualTo(declared);
	}

	@Test
	void reportingExceptionsAreExactlyTheDocumentedOnes() {
		assertThat(REPORTING_EXCEPTIONS.keySet()).containsExactlyInAnyOrder(
				"performance/LeaderboardRepository.java", "competition/CompetitionStandingsRepository.java");
	}

	// ---------------------------------------------------------------- helpers

	private static DescribedPredicate<JavaClass> jdbcTypes() {
		return DescribedPredicate.describe("JDBC data access types",
				c -> c.getPackageName().startsWith("org.springframework.jdbc")
						|| c.getPackageName().equals("javax.sql")
						|| c.getName().equals("java.sql.ResultSet") || c.getName().equals("java.sql.Connection")
						|| c.getName().equals("java.sql.Statement") || c.getName().equals("java.sql.PreparedStatement"));
	}

	private static ArchCondition<JavaClass> beAccessedOnlyFromOwnPackage() {
		return new ArchCondition<>("be accessed only from their own package") {
			@Override
			public void check(JavaClass target, ConditionEvents events) {
				target.getAccessesToSelf()
					.stream()
					.filter(a -> !a.getOriginOwner().getPackageName().equals(target.getPackageName()))
					.forEach(a -> events.add(SimpleConditionEvent.violated(a, a.getDescription())));
			}
		};
	}

	private static ArchCondition<JavaClass> callMethodOf(Class<?> owner, String... methods) {
		Set<String> names = Set.of(methods);
		return new ArchCondition<>("call " + owner.getSimpleName() + "." + names) {
			@Override
			public void check(JavaClass origin, ConditionEvents events) {
				for (JavaMethodCall call : origin.getMethodCallsFromSelf()) {
					if (call.getTargetOwner().isEquivalentTo(owner) && names.contains(call.getName())) {
						events.add(SimpleConditionEvent.satisfied(call, call.getDescription()));
					}
				}
			}
		};
	}
}
