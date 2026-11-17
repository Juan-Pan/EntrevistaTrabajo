# Product API

API REST para gestionar un catálogo de productos (CRUD) construida con **Java 25** y **Spring Boot 4**.

## Tecnologías

- Spring Boot 4 (Web MVC, Data JPA, Validation)
- H2 en memoria
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito y MockMvc para pruebas

## Arquitectura

El código está organizado por funcionalidad (`product`) y en capas:

```
src/main/java/com/juanpan/productapi
├── ProductApiApplication.java
├── product
│   ├── Product.java                    # Entidad JPA
│   ├── ProductRepository.java          # Acceso a datos (Spring Data JPA)
│   ├── ProductService.java             # Lógica de negocio y transacciones
│   ├── ProductController.java          # Endpoints REST
│   ├── ProductNotFoundException.java
│   └── dto
│       ├── ProductRequest.java         # Entrada validada
│       └── ProductResponse.java        # Salida de la API
└── shared
    └── GlobalExceptionHandler.java     # Errores en formato RFC 9457 (Problem Details)
```

- La entidad nunca se expone directamente: la API recibe `ProductRequest` y responde `ProductResponse`.
- Los errores se devuelven con el estándar **Problem Details** (`application/problem+json`).

## Modelo

| Campo         | Tipo       | Reglas                       |
|---------------|------------|------------------------------|
| `id`          | Long       | Autogenerado                 |
| `name`        | String     | Obligatorio, máx. 100        |
| `description` | String     | Opcional, máx. 500           |
| `price`       | BigDecimal | Obligatorio, mayor que 0     |
| `stock`       | Integer    | Obligatorio, mayor o igual a 0 |
| `createdAt`   | Instant    | Automático                   |
| `updatedAt`   | Instant    | Automático                   |

## Endpoints

| Método | Ruta                 | Descripción             | Respuesta       |
|--------|----------------------|-------------------------|-----------------|
| GET    | `/api/products`      | Listar productos        | 200             |
| GET    | `/api/products/{id}` | Obtener un producto     | 200 / 404       |
| POST   | `/api/products`      | Crear un producto       | 201 / 400       |
| PUT    | `/api/products/{id}` | Actualizar un producto  | 200 / 400 / 404 |
| DELETE | `/api/products/{id}` | Eliminar un producto    | 204 / 404       |

## Cómo ejecutar

Requisitos: JDK 25 o superior. No hace falta instalar Maven (se usa el wrapper).

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

Al arrancar se cargan tres productos de ejemplo.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:productsdb`, usuario `sa`, sin contraseña)

## Ejemplos

```bash
# Crear
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Monitor","description":"27 pulgadas","price":1299000,"stock":7}'

# Listar
curl http://localhost:8080/api/products

# Actualizar
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","description":"32 GB RAM","price":4999000,"stock":4}'

# Eliminar
curl -X DELETE http://localhost:8080/api/products/1
```

Ejemplo de error de validación:

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/products",
  "errors": {
    "name": "no debe estar vacío",
    "price": "debe ser mayor que 0.0"
  }
}
```

## Pruebas

```bash
./mvnw test
```

- `ProductServiceTest`: pruebas unitarias del servicio con Mockito.
- `ProductControllerIntegrationTest`: pruebas de integración de todos los endpoints con MockMvc y H2.
