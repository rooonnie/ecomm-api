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

## Catalog API

One **product** is the part (MPN). Each **SKU** is that part plus packaging (cut tape, tape and reel, mini-reel, etc.).

Seeded on startup:

- Packaging types: `CUT_TAPE`, `TAPE_AND_REEL`, `MINI_REEL`, `REREEL`, `TUBE`, `TRAY`, `BULK`
- Category: Chip Resistors
- Manufacturer: Yageo

| Method | Path | Purpose |
| --- | --- | --- |
| GET/POST | `/api/packaging-types` | Packaging lookup |
| GET/POST | `/api/categories` | Categories |
| GET/POST | `/api/manufacturers` | Manufacturers |
| GET/POST | `/api/products` | Electronic parts |
| GET | `/api/products/{id}/skus` | SKUs for one part |
| GET/POST | `/api/skus` | Sellable packaging SKUs |
| GET/PUT | `/api/skus/{skuId}/inventory` | Stock per SKU |
| GET/POST/PUT | `/api/skus/{skuId}/price-breaks` | Volume prices |
| GET | `/api/skus/{skuId}/price-breaks/quote?qty=` | Unit price for a qty |

Stock and price live on the SKU. Cut tape and full reel of the same part can have different qty and different price breaks.

## Cart and orders

Checkout freezes SKU, packaging, qty, and unit price on the order line. Stock is reserved on checkout and deducted when the order is marked paid.

| Method | Path | Purpose |
| --- | --- | --- |
| POST/GET | `/api/users` | Create / get customer |
| GET/POST | `/api/users/{id}/addresses` | Shipping addresses |
| GET | `/api/users/{id}/cart` | Get or create cart |
| POST | `/api/users/{id}/cart/items` | Add SKU + qty |
| DELETE | `/api/users/{id}/cart/items/{itemId}` | Remove cart line |
| POST | `/api/users/{id}/checkout` | Create order, reserve stock |
| GET | `/api/orders/{id}` | Get order |
| POST | `/api/orders/{id}/pay` | Manual payment, capture stock |
