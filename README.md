# ecomm-api

Backend REST API for an SMD electronics e-commerce shop.

- Java Spring MVC
- H2 database (local file)
- Swagger / OpenAPI

## Run

Java 21+ is required. This machine can use the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

## URLs

- Health: http://localhost:8080/api/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 console: http://localhost:8080/h2-console

H2 console JDBC URL: `jdbc:h2:file:./data/ecomm-db`

Username: `sa`  
Password: (leave blank)

## Step 1

Scaffold only. Catalog tables (products, SKUs, packaging) come next.
