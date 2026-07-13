# Coworking Reservation API

API REST backend para la gestión de espacios de coworking y reservas, desarrollada con **Spring Boot 3** y **Java 21** como parte de una prueba técnica backend.

El objetivo del proyecto es exponer una API lista para ejecución local mediante Docker, con autenticación JWT, autorización por roles, gestión de espacios, reservas, validación externa de pago simulada, notificación asíncrona y reporte de ocupación cacheado.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- PostgreSQL
- Spring Security
- JWT
- Jakarta Validation
- Spring Boot Actuator
- Spring Cache
- Caffeine
- Resilience4j Circuit Breaker
- Springdoc OpenAPI / Swagger
- Docker
- Docker Compose
- WireMock
- JUnit 5
- Mockito
- Testcontainers
- Spring Security Test

---

## Funcionalidades principales

- Registro e inicio de sesión de usuarios.
- Autenticación mediante JWT.
- Autorización basada en roles `ADMIN` y `USER`.
- CRUD de espacios de coworking.
- Creación, consulta y cancelación de reservas.
- Validación para evitar reservas solapadas sobre el mismo espacio.
- Validación externa de pago mediante un servicio simulado con WireMock.
- Circuit Breaker con fallback para manejar fallos del servicio de pago.
- Notificación asíncrona al confirmar una reserva.
- Reporte de ocupación por rango de fechas con caché.
- Manejo centralizado de errores con `@RestControllerAdvice`.
- Documentación OpenAPI/Swagger.
- Endpoints de monitoreo con Actuator.
- Pruebas unitarias e integración.

---

## Arquitectura del proyecto

El proyecto utiliza una arquitectura en capas:

```text
controller
service
repository
entity
dto
mapper
config
security
exception
event
```

Responsabilidades principales:

- `controller`: expone los endpoints REST.
- `service`: contiene la lógica de negocio.
- `repository`: acceso a datos con Spring Data JPA.
- `entity`: modelo persistente JPA.
- `dto`: objetos de entrada y salida de la API.
- `mapper`: conversión entre entidades y DTOs.
- `security`: configuración y componentes JWT.
- `exception`: excepciones de negocio y manejo centralizado.
- `event`: eventos de dominio y listeners asíncronos.
- `config`: configuración general de la aplicación.

No se exponen entidades JPA directamente desde los controladores. La API utiliza DTOs para separar el contrato externo del modelo persistente.

---

## Modelo de dominio

### UserAccount

Representa a un usuario del sistema.

Campos principales:

```text
id
name
email
password
role
```

El campo `email` tiene una restricción única porque se utiliza como identificador de autenticación.

Roles disponibles:

```text
ADMIN
USER
```

---

### Space

Representa un espacio de coworking.

Campos principales:

```text
id
name
type
capacity
location
hourlyRate
active
```

Tipos de espacio:

```text
MEETING_ROOM
DESK
PRIVATE_OFFICE
```

Los espacios se desactivan lógicamente mediante el campo `active`, en lugar de eliminarse físicamente. Esto permite conservar el historial de reservas asociadas.

---

### Reservation

Representa una reserva realizada por un usuario sobre un espacio.

Campos principales:

```text
id
user
space
startTime
endTime
totalPrice
status
paymentReference
createdAt
```

Estados de reserva:

```text
PENDING_PAYMENT
CONFIRMED
CANCELLED
```

---

## Flujo de creación de reserva

Cuando un usuario crea una reserva, el sistema ejecuta el siguiente flujo:

1. Obtiene el usuario autenticado.
2. Busca el espacio solicitado.
3. Aplica un bloqueo pesimista sobre el espacio.
4. Valida que el rango de fechas sea correcto.
5. Verifica que no existan reservas solapadas.
6. Calcula el precio total usando Strategy.
7. Guarda la reserva inicialmente como `PENDING_PAYMENT`.
8. Invoca el servicio externo simulado de pagos.
9. Si el pago es aprobado, cambia la reserva a `CONFIRMED`.
10. Publica un evento de reserva confirmada.
11. Procesa la notificación de forma asíncrona.

Si el servicio externo de pagos falla, tarda demasiado o no está disponible, el Circuit Breaker ejecuta el fallback y la reserva permanece en estado `PENDING_PAYMENT`.

---

## Validación de reservas solapadas

Para detectar solapamientos se utiliza la siguiente condición:

```text
existing.startTime < requested.endTime
AND
existing.endTime > requested.startTime
```

Esto permite que una reserva termine exactamente cuando otra inicia, pero evita que dos reservas compartan tiempo sobre el mismo espacio.

Ejemplo permitido:

```text
Reserva existente: 10:00 - 12:00
Nueva reserva:     12:00 - 14:00
```

Ejemplo no permitido:

```text
Reserva existente: 10:00 - 12:00
Nueva reserva:     11:00 - 13:00
```

Además, se utiliza `PESSIMISTIC_WRITE` al cargar el espacio durante la creación de la reserva. Esto evita que dos solicitudes concurrentes validen disponibilidad al mismo tiempo sobre el mismo espacio.

---

## Patrón de diseño utilizado

### Strategy Pattern

Se aplicó el patrón Strategy para el cálculo de tarifas.

El cálculo del precio puede variar según el tipo de espacio. Por esa razón, se separó cada política de precio en una clase distinta:

```text
MeetingRoomPricingStrategy
DeskPricingStrategy
PrivateOfficePricingStrategy
```

Esto evita tener toda la lógica de precios en un bloque grande de `if/else` o `switch` dentro del servicio de reservas.

Si en el futuro se agrega un nuevo tipo de espacio o una nueva regla de precio, se puede crear una nueva estrategia sin modificar el flujo principal de reservas.

---

## Validación externa de pago

El servicio de pago se trata como una dependencia externa potencialmente lenta o inestable.

Para ejecución local se utiliza WireMock como servicio simulado.

Endpoint simulado:

```text
POST /payments/validate
```

Respuesta exitosa simulada:

```json
{
  "approved": true,
  "reference": "PAY-WIREMOCK-001",
  "message": "Pago aprobado por servicio del mock"
}
```

La aplicación consume este servicio mediante `RestClient`.

La llamada está protegida con Resilience4j Circuit Breaker. Si el servicio externo falla, el fallback devuelve una respuesta controlada y la reserva queda en estado:

```text
PENDING_PAYMENT
```

Esto evita que la API falle con error 500 cuando la dependencia externa no responde.

---

## Notificación asíncrona

Cuando una reserva es confirmada, se publica un evento de dominio:

```text
ReservationConfirmedEvent
```

El listener procesa este evento de forma asíncrona usando `@Async`.

También se utiliza `@TransactionalEventListener` con fase `AFTER_COMMIT`, para asegurar que la notificación simulada se ejecute únicamente después de que la transacción de la reserva haya sido confirmada correctamente.

La notificación se simula mediante logs.

---

## Caché de reportes

El endpoint de reporte de ocupación utiliza caché con Spring Cache y Caffeine.

Endpoint:

```text
GET /api/reports/occupancy
```

El reporte calcula el porcentaje de ocupación de cada espacio en un rango de fechas.

La ocupación se calcula tomando en cuenta únicamente reservas en estado:

```text
CONFIRMED
```

Cuando se crea o cancela una reserva, se limpia la caché de reportes usando `@CacheEvict`, ya que los datos de ocupación pueden cambiar.

---

## Seguridad

La API utiliza autenticación JWT.

Endpoints públicos:

```text
POST /api/auth/register
POST /api/auth/login
GET /actuator/health
GET /actuator/info
GET /swagger-ui.html
GET /v3/api-docs
```

Permisos principales:

```text
ADMIN:
- Crear espacios.
- Actualizar espacios.
- Desactivar espacios.
- Ver todas las reservas.
- Cancelar cualquier reserva.
- Consultar reportes.
- Consultar endpoints protegidos de Actuator.

USER:
- Consultar espacios activos.
- Crear reservas.
- Consultar sus propias reservas.
- Cancelar sus propias reservas.
```

En el perfil `dev`, se crea automáticamente un usuario administrador para facilitar las pruebas locales:

```text
email: admin@coworking.com
password: Admin12345!
```

Este usuario solo existe para el entorno de desarrollo.

---

## Variables de configuración

La aplicación utiliza `@ConfigurationProperties` para agrupar configuraciones propias.

Ejemplo:

```yaml
app:
  jwt:
    secret: dev-secret-key-change-me-please-1234567890-1234567890
    expiration: 8h

  payment:
    base-url: ${APP_PAYMENT_BASE_URL:http://localhost:8081}
    timeout: ${APP_PAYMENT_TIMEOUT:2s}
```

En producción, valores sensibles como `JWT_SECRET` no deberían versionarse en el repositorio. Deben inyectarse como variables de entorno.

---

## Ejecución con Docker Compose

### Requisitos

- Docker Desktop
- Java 21
- Maven Wrapper incluido en el proyecto

### Levantar la aplicación completa

Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

Esto levanta los siguientes servicios:

```text
coworking-api
coworking-postgres
coworking-wiremock
```

### Verificar contenedores

```bash
docker ps
```

### Verificar salud de la aplicación

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:

```json
{
  "status": "UP"
}
```

### Detener servicios

```bash
docker compose down
```

---

## Ejecución local sin Docker para la app

También se puede levantar únicamente PostgreSQL y WireMock con Docker, y ejecutar la aplicación desde el IDE o terminal.

Levantar servicios externos:

```bash
docker compose up -d postgres wiremock
```

Ejecutar la aplicación:

```bash
./mvnw spring-boot:run
```

---

## Pruebas

Ejecutar todas las pruebas:

```bash
./mvnw clean test
```

El proyecto incluye:

- Pruebas unitarias con Mockito.
- Pruebas de integración con Spring Boot.
- Testcontainers para PostgreSQL.
- Pruebas de seguridad con Spring Security Test.

Durante el cierre del contexto de pruebas puede aparecer algún warning relacionado con Hikari o PostgreSQL si el contenedor de Testcontainers se está cerrando. Mientras el resultado final sea `BUILD SUCCESS`, las pruebas se consideran exitosas.

---

## Documentación OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

## Ejemplos de uso

Se incluyen dos formas de probar la API:

```text
http/requests.http
postman/coworking-reservation-api.postman_collection.json
```

### Archivo HTTP

El archivo:

```text
http/requests.http
```

contiene ejemplos ejecutables desde IntelliJ IDEA o VS Code con extensiones HTTP Client.

Antes de usarlo, reemplazar:

```text
@adminToken = paste_admin_token_here
@userToken = paste_user_token_here
```

por tokens reales generados desde los endpoints de login o registro.

No se deben subir tokens reales al repositorio.

### Colección Postman

También se incluye una colección Postman en:

```text
postman/coworking-reservation-api.postman_collection.json
```

La colección contiene ejemplos para:

- Health check.
- Login de administrador.
- Registro de usuario.
- Creación de espacios.
- Consulta de espacios.
- Validación de autorización por rol.
- Creación de reservas.
- Consulta de reservas.
- Reporte de ocupación.
- Validación del mock de pago con WireMock.
- Consulta del estado del Circuit Breaker.

La colección utiliza variables:

```text
baseUrl
wiremockUrl
adminToken
userToken
spaceId
```

Los requests de login y registro guardan automáticamente los tokens en las variables `adminToken` y `userToken`.

---

## Flujo manual de prueba

### 1. Login como administrador

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@coworking.com",
    "password": "Admin12345!"
  }'
```

Copiar el valor de `accessToken`.

---

### 2. Registrar usuario normal

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Oliver User",
    "email": "oliver.user@test.com",
    "password": "User12345!"
  }'
```

Copiar el valor de `accessToken`.

---

### 3. Crear espacio como administrador

```bash
curl -X POST http://localhost:8080/api/spaces \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d '{
    "name": "Sala de Reuniones A",
    "type": "MEETING_ROOM",
    "capacity": 8,
    "location": "Segundo piso",
    "hourlyRate": 25.00
  }'
```

---

### 4. Consultar espacios como usuario

```bash
curl -X GET http://localhost:8080/api/spaces \
  -H "Authorization: Bearer <USER_TOKEN>"
```

---

### 5. Crear reserva como usuario

```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "spaceId": 1,
    "startTime": "2026-07-15T09:00:00",
    "endTime": "2026-07-15T11:00:00",
    "paymentMethod": "CARD"
  }'
```

Si WireMock está disponible, la respuesta esperada tendrá:

```text
status: CONFIRMED
paymentReference: PAY-WIREMOCK-001
```

Si el servicio de pago no está disponible, la reserva quedará como:

```text
status: PENDING_PAYMENT
paymentReference: null
```

---

### 6. Consultar reservas propias

```bash
curl -X GET http://localhost:8080/api/reservations/mine \
  -H "Authorization: Bearer <USER_TOKEN>"
```

---

### 7. Consultar todas las reservas como administrador

```bash
curl -X GET http://localhost:8080/api/admin/reservations \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

### 8. Consultar reporte de ocupación

```bash
curl -X GET "http://localhost:8080/api/reports/occupancy?startTime=2026-07-15T08:00:00&endTime=2026-07-15T18:00:00" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

## Endpoints principales

### Autenticación

```text
POST /api/auth/register
POST /api/auth/login
```

### Espacios

```text
POST   /api/spaces
GET    /api/spaces
GET    /api/spaces/{id}
PUT    /api/spaces/{id}
DELETE /api/spaces/{id}
```

### Reservas de usuario

```text
POST  /api/reservations
GET   /api/reservations/mine
PATCH /api/reservations/{id}/cancel
```

### Reservas de administrador

```text
GET   /api/admin/reservations
PATCH /api/admin/reservations/{id}/cancel
```

### Reportes

```text
GET /api/reports/occupancy
```

### Actuator

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
GET /actuator/circuitbreakers
```

---

## Decisiones técnicas y trade-offs

### Arquitectura en capas

Se eligió una arquitectura en capas porque es suficiente para el alcance de la prueba, facilita la lectura del código y permite separar responsabilidades sin agregar demasiada complejidad.

No se implementó arquitectura hexagonal completa porque habría aumentado el número de clases y abstracciones sin aportar valor directo al objetivo de la prueba.

---

### DTOs en lugar de entidades expuestas

Los controladores reciben y devuelven DTOs. Esto evita exponer directamente las entidades JPA y permite mantener separado el contrato de la API del modelo de persistencia.

---

### Mappers manuales

Se usaron mappers manuales en lugar de MapStruct para evitar agregar una dependencia adicional. La lógica de mapeo es pequeña y explícita.

---

### Inyección por constructor

Se utilizó inyección por constructor con `@RequiredArgsConstructor`, en lugar de `@Autowired` sobre campos.

Esto permite que las dependencias sean obligatorias, facilita las pruebas unitarias y permite declarar los atributos como `final`.

---

### Strategy para tarifas

Se utilizó Strategy porque el cálculo de tarifa puede variar según el tipo de espacio. Esto evita centralizar todas las reglas de precio en condicionales dentro del servicio principal de reservas.

---

### Bloqueo pesimista

Se utilizó `PESSIMISTIC_WRITE` sobre el espacio durante la creación de reservas para reducir riesgos de concurrencia al validar disponibilidad.

La consulta de solapamiento por sí sola no garantiza consistencia ante dos solicitudes simultáneas, por eso se agregó el bloqueo dentro de la transacción.

---

### Circuit Breaker

La validación de pago se trata como una integración externa inestable. Por eso se usa Circuit Breaker con fallback.

Cuando el servicio externo no responde correctamente, la reserva queda en `PENDING_PAYMENT` en lugar de fallar con error interno.

---

### Notificación asíncrona

La notificación se simula con logs mediante eventos de Spring y `@Async`. No se agregó un broker de mensajería porque el requisito solo pide simular el envío de correo sin bloquear la respuesta HTTP.

---

### Cache de reportes

El reporte de ocupación se cachea porque puede ser consultado repetidamente con el mismo rango de fechas.

La caché se invalida completamente cuando se crean o cancelan reservas. Es una estrategia conservadora, simple y segura para evitar datos obsoletos.

---

### Tokens JWT simples

Se implementó access token JWT sin refresh token. Los refresh tokens quedaron fuera del alcance para mantener la solución enfocada en los requisitos principales.

---

## Fuera de alcance

Los siguientes puntos se dejaron fuera intencionalmente:

- Frontend.
- Refresh tokens.
- Recuperación de contraseña.
- Verificación de correo electrónico.
- Integración real con proveedor de email.
- Integración real con proveedor de pagos.
- Redis.
- Kafka o RabbitMQ.
- Kubernetes.
- Pipeline CI/CD.
- Auditoría avanzada.
- Migraciones con Flyway o Liquibase.

---

## Mejoras futuras

Con más tiempo se podrían agregar:

- Migraciones con Flyway o Liquibase.
- Más pruebas de concurrencia.
- Refresh tokens.
- Integración real con proveedor de pagos.
- Integración real con proveedor de correo.
- Logs estructurados con correlation IDs.
- Métricas más detalladas para caché y pagos.
- Pipeline CI/CD.
- Separación más estricta por arquitectura hexagonal.
- Mejor manejo de usuarios administradores en producción.

---

## Estado final de la solución

La solución cubre los puntos principales solicitados:

- API REST con Spring Boot.
- PostgreSQL.
- Seguridad JWT.
- Roles `ADMIN` y `USER`.
- Reservas con prevención de solapamiento.
- Transacciones y bloqueo pesimista.
- Validación externa de pago simulada.
- Circuit Breaker con fallback.
- Notificación asíncrona.
- Reporte cacheado.
- Actuator.
- Swagger/OpenAPI.
- Dockerfile y Docker Compose.
- Archivo `.http`.
- Colección Postman.
- Pruebas unitarias e integración.