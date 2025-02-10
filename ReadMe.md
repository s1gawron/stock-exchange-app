# Stock app

Stock web application - first project to my CV - still a lot to add or improve - Technologies: Spring Boot, JPA, Hibernate, PostgreSQL, Docker, Kubernetes

This app was created to simulate transactions you can do on Stock Exchange with real time listings (supported exchanges: US and LSE).

To run this app you will need to provide finnhub token in application.properties, which you can get after creating an account at: https://finnhub.io/ (Working
on possibility to run the app without creating account)

# Prerequisites:

* Java (this project is using version 17)
* Docker
* Maven

If you would like to run this application in kubernetes cluster, you may also need:

* Minikube
* Kubectl

# Running the app:

### Docker:

After pulling repository from GitHub just enter the directory where the code is and enter the commands:

``
mvn clean install
``

``
docker-compose up
``

and everything will be done for you automatically (don't worry if java application will throw exception about connection refused when you start project for the
first start, this is because database is still initializing).

### Kubernetes:

After setting up minikube, build the docker image (don't forget to be in a proper directory) using:

``
eval $(minikube docker-env)
`` - configuring local environment to use Docker daemon inside Minikube instance (by using this command we can have Docker image locally and there is no need to
push it to the registry)

``
mvn clean install
``

``
docker build -t stock-app:1.0 .
``

When image is done and dusted we can move to the next step, which is setting up Kubernetes components:

``
kubectl apply -f mysql-configmap.yaml
``

``
kubectl apply -f mysql-secret.yaml
``

``
kubectl apply -f stock-app-configmap.yaml
``

Now we can finally create our services:

``
kubectl apply -f mysql.yaml
``

``
kubectl apply -f stock-app.yaml
``

To head to the page where service is running type:

``
minikube service stock-app-service
``

# Endpoints:

To learn about available endpoints head to:

*{host}/swagger-ui/index.html*
