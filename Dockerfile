FROM openjdk:11
ADD target/stock-app-1.0.jar .
EXPOSE 8080
CMD java -jar stock-app-1.0.jar