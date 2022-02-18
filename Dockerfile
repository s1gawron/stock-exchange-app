FROM openjdk:11
WORKDIR app
ADD target/stock-app-1.0.jar /app/stock-app-1.0.jar
COPY target/classes/application.properties /app/application.properties
EXPOSE 8080
CMD java -jar stock-app-1.0.jar