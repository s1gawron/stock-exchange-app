ARG PROFILE=local

WORKDIR app
ADD target/stock-exchange-app-1.0-SNAPSHOT.jar /app/stock-exchange-app-1.0-SNAPSHOT.jar

COPY target/classes/application.properties /app/application.properties
COPY target/classes/application-${PROFILE}.properties /app/application-${PROFILE}.properties

EXPOSE 8080

CMD java -jar stock-exchange-app-1.0-SNAPSHOT.jar