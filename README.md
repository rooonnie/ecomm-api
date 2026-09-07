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

## Auth

Cart, orders, and rereel jobs require a JWT. Catalog and inventory stay open for now.

1. `POST /api/auth/register` with email, name, password (min 8 chars)
2. Copy `token` from the response
3. In Swagger click **Authorize**, paste the token (no `Bearer` prefix)
4. Call cart / checkout / rereel

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Create customer and return JWT |
| POST | `/api/auth/login` | Login and return JWT |

Old `POST /api/users` still creates a customer if you send a password, but it does not return a token. Use register, then login.

## Cart and orders

Checkout freezes SKU, packaging, qty, and unit price on the order line. Stock is reserved on checkout, deducted when paid, and released if the unpaid order is cancelled.

| Method | Path | Purpose |
| --- | --- | --- |
| POST/GET | `/api/users` | Create / get customer |
| GET/POST | `/api/users/{id}/addresses` | Shipping addresses |
| GET | `/api/users/{id}/cart` | Get or create cart |
| POST | `/api/users/{id}/cart/items` | Add SKU + qty |
| DELETE | `/api/users/{id}/cart/items/{itemId}` | Remove cart line |
| POST | `/api/users/{id}/checkout` | Create order, reserve stock |
| GET | `/api/orders/{id}` | Get order |
| GET | `/api/users/{id}/orders` | List user orders |
| POST | `/api/orders/{id}/pay` | Manual payment, capture stock |
| POST | `/api/orders/{id}/cancel` | Cancel unpaid order, release stock |

## Rereel jobs

Cut tape leftover can be wound onto a mini-reel or rereel. Source stock is reserved when the job is created. Completing the job consumes cut tape and adds the same qty to a target SKU of the same part (created if it does not exist yet).

| Method | Path | Purpose |
| --- | --- | --- |
| GET/POST | `/api/rereel-jobs` | List / request a rereel job |
| GET | `/api/rereel-jobs/{id}` | Get job |
| POST | `/api/rereel-jobs/{id}/complete` | Consume cut tape, add mini-reel stock |
| POST | `/api/rereel-jobs/{id}/cancel` | Cancel job, release reserved cut tape |
