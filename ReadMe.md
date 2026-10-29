# Stock Exchange App

A web application that simulates trading on a stock exchange. Users can browse real-time listings from the US and LSE exchanges, keep a list of favourite
stocks and buy or sell shares (also fractional quantities) with a virtual wallet. The wallet value is updated at the end of each trading day.

Stock data comes from the [Finnhub](https://finnhub.io/) API.

## Tech stack

* **Backend:** Java 17, Spring Boot 4, Spring Security (JWT), JPA / Hibernate, Flyway
* **Frontend:** React 19, TypeScript, Vite
* **Storage:** PostgreSQL 17, Redis (cache for stock data)
* **Infrastructure:** Docker, Docker Compose, Kubernetes, nginx, Prometheus metrics (Spring Boot Actuator)

## Prerequisites

* A free Finnhub API token: create an account at https://finnhub.io/
* Docker with Docker Compose
* Java 17 and Maven (to build the backend)
* Node.js 22 and npm (only for local frontend development)

## Quick start (Docker Compose)

1. Build the backend image (Jib builds it straight into the local Docker daemon):

   ```bash
   cd backend
   mvn compile jib:dockerBuild
   cd ..
   ```

2. Build the frontend image:

   ```bash
   docker build -t s1gawron/stock-exchange-app-frontend:1.0 frontend
   ```

   The API URL is put into the frontend bundle at build time. The default is `http://localhost:8080`. To use a different URL, add
   `--build-arg VITE_API_SERVICE_SCHEME=...`, `--build-arg VITE_API_SERVICE_HOST=...` and `--build-arg VITE_API_SERVICE_PORT=...`.

3. Start all services with your Finnhub token:

   ```bash
   FINNHUB_TOKEN=<your-token> docker-compose up
   ```

4. Open http://localhost:5173.

The backend starts after the PostgreSQL healthcheck passes.

## Kubernetes (minikube)

The manifests in `kubernetes/` start the same services as Docker Compose. The backend uses the `docker` profile: the Kubernetes services have the same
host names as the Compose containers.

1. Use the Docker daemon of minikube, so that the cluster can use the local images:

   ```bash
   eval $(minikube docker-env)
   ```

2. Build the backend and frontend images in the same terminal, as in steps 1 and 2 of the [quick start](#quick-start-docker-compose).

3. Create the secret with your Finnhub token and apply the manifests:

   ```bash
   kubectl create secret generic finnhub-token --from-literal=token=<your-token>
   kubectl apply -f kubernetes/
   ```

4. Forward the ports. The frontend image calls the API at `http://localhost:8080`, and the backend accepts requests only from `http://localhost:5173`:

   ```bash
   kubectl port-forward service/stock-exchange-app 8080:8080
   kubectl port-forward service/stock-exchange-app-frontend 5173:80
   ```

5. Open http://localhost:5173.

The database password and the JWT secret key are in `kubernetes/secret.yaml`. They are development values. Change them for a shared cluster.

## Local development

1. Start PostgreSQL and Redis:

   ```bash
   docker-compose up database redis
   ```

2. Start the backend at http://localhost:8080:

   ```bash
   cd backend
   FINNHUB_TOKEN=<your-token> mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. Start the frontend dev server at http://localhost:5173:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

Other useful commands:

| Command                           | Directory  | Description                      |
|-----------------------------------|------------|----------------------------------|
| `mvn clean install`               | `backend`  | Build the backend and run tests  |
| `mvn test -Dtest=ClassName`       | `backend`  | Run one test class               |
| `npm run build`                   | `frontend` | Type-check and production build  |
| `npm run lint`                    | `frontend` | Run ESLint                       |

## API documentation

When the backend runs, the Swagger UI is available at http://localhost:8080/swagger-ui/index.html.

Prometheus metrics are available at http://localhost:8080/actuator/prometheus.
