package com.stocksim.instrument;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instruments")
class InstrumentController {

	private final InstrumentService service;

	InstrumentController(InstrumentService service) {
		this.service = service;
	}

	@GetMapping
	List<InstrumentView> search(@RequestParam("q") String query) {
		return service.search(query);
	}

	@GetMapping("/{symbol}")
	ResponseEntity<InstrumentView> get(@PathVariable String symbol) {
		return ResponseEntity.of(service.get(symbol));
	}
}
