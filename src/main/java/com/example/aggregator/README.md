# 🌐 Aggregator WebFlux

Reactive data aggregator built with **Spring WebFlux**, **Redis**, and **Project Reactor**.  
The app asynchronously fetches data from multiple external APIs, aggregates the results, caches them in Redis, and provides a reactive endpoint for clients.

---

## 🚀 Features

- ⚡ **Reactive architecture** using Spring WebFlux  
- 🧩 **Parallel aggregation** of multiple APIs  
- 💾 **Caching** via Reactive Redis  
- 🧰 **Graceful error handling**  
- 🔧 **Configurable sources** via `application.yaml`  
- 🧪 **Comprehensive testing** with JUnit + Mockito + Reactor Test  

---

## 🧱 Tech Stack

| Layer | Technology |
- **Java 21**
- **Spring Boot 3 (WebFlux + Reactive Redis)**
- **Netty** as the reactive HTTP server
- **Project Reactor** for reactive streams
- **Redis Cloud** for caching
- **JUnit 5 + Mockito** for testing
---

## ⚙️ Configuration

Example `application.yaml`:

```yaml
server:
  port: 8080

spring:
  data:
    redis:
      host: your_host
      port: your_port
      password: your_redis_password

aggregator:
  cache-ttl-minutes: 1
  sources:
    - name: weather
      url: https://api.open-meteo.com/v1/forecast?latitude=51.107883&longitude=17.038538&current_weather=true
    - name: fact
      url: https://uselessfacts.jsph.pl/api/v2/facts/random
    - name: ip
      url: https://api.ipify.org/?format=json
```

---

## ▶️ Run Locally

### Prerequisites
- Java 21+
- Maven 3.9+
- Redis (local or cloud)

### Run the app

```bash
mvn spring-boot:run
```

or build a jar:

```bash
mvn clean package
java -jar target/aggregator-webflux-0.0.1-SNAPSHOT.jar
```

### Run tests

```bash
mvn test
```

---

## 🧩 API Example

**Endpoint:**  
`GET http://localhost:8080/api/aggregate`

**Response:**
```json
{
  "ip": "192.168.0.10",
  "quote": "Be yourself; everyone else is already taken."
}
```

---

## 🧪 Tests

- ✅ Unit tests with Mockito and Reactor Test  
- ✅ API failure fallback verification  
- ✅ Redis caching behavior validation  

Run all tests:

```bash
mvn test
```

---

## 🧠 Future Improvements

- Add OpenAPI (Swagger UI)  
- Implement retry/backoff strategies for API failures  
- Dynamic management of data sources  
- Docker Compose for Redis + App  

---

## 👨‍💻 Author

**Kostiantyn Ananchenko**  
Java Developer 

