# QPH ProductHub Full Stack

Aplicación Full Stack para la gestión de productos, usuarios y ventas, desarrollada con **Spring Boot**, **Angular** y **PostgreSQL**.

El proyecto incorpora autenticación mediante JWT, autorización basada en roles y autenticación de dos factores (2FA) mediante correo electrónico y aplicaciones TOTP como Microsoft Authenticator.

Además, permite registrar ventas validando y descontando el stock de manera transaccional para evitar ventas por encima del inventario disponible.

---

## Tecnologías

### Backend

- Java 21
- Spring Boot 3.3.4
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT
- BCrypt
- Spring Mail
- TOTP
- ZXing
- Maven

### Frontend

- Angular 21
- Angular CLI 21
- TypeScript 5.9
- PrimeNG 21
- PrimeIcons
- RxJS
- Node.js 22
- npm 10

---

## Arquitectura general

```text
┌──────────────────────────────┐
│           Angular            │
│        Frontend SPA          │
└──────────────┬───────────────┘
               │
               │ HTTP / REST
               │ JWT Bearer
               ▼
┌──────────────────────────────┐
│        Spring Boot API       │
│                              │
│  Controllers                 │
│       │                      │
│       ▼                      │
│  Services                    │
│       │                      │
│       ▼                      │
│  Repositories                │
└──────────────┬───────────────┘
               │
               │ JPA / Hibernate
               ▼
┌──────────────────────────────┐
│          PostgreSQL          │
└──────────────────────────────┘
```

---

## Estructura del repositorio

```text
qph-producthub-fullstack/
│
├── backend-qph/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── frontend-qph/
│   ├── src/
│   ├── angular.json
│   ├── package.json
│   └── package-lock.json
│
├── .gitignore
└── README.md
```

---

# Funcionalidades

## Autenticación

La aplicación utiliza **Spring Security** y **JWT**.

Flujo principal:

```text
Usuario
   │
   │ username + password
   ▼
Spring Security
   │
   ▼
AuthenticationManager
   │
   ▼
UserDetailsService
   │
   ▼
PostgreSQL
   │
   ▼
JWT
```

El token se envía posteriormente en las peticiones protegidas mediante:

```http
Authorization: Bearer <token>
```

---

## Roles

Actualmente existen dos roles:

```text
ADMIN
USER
```

### ADMIN

Puede:

- Consultar productos.
- Crear productos.
- Editar productos.
- Eliminar productos.
- Gestionar usuarios.
- Crear usuarios.
- Editar usuarios.
- Eliminar usuarios.
- Habilitar o deshabilitar autenticación de dos factores.
- Registrar ventas.
- Consultar todas las ventas.

### USER

Puede:

- Consultar productos.
- Registrar ventas.
- Consultar sus propias ventas.

Las restricciones se validan en el backend mediante Spring Security.

---

# Autenticación de dos factores

El sistema soporta dos métodos de segundo factor:

```text
EMAIL_AUTH
TOTP_AUTH
```

Cuando un usuario tiene 2FA habilitado:

```text
username + password
        │
        ▼
Credenciales correctas
        │
        ▼
¿2FA habilitado?
        │
        ├── NO ──────► JWT
        │
        └── SÍ
             │
             ▼
        Challenge 2FA
             │
        ┌────┴─────┐
        │          │
        ▼          ▼
    Email OTP     TOTP
        │          │
        └────┬─────┘
             ▼
            JWT
```

---

## Email OTP

Cuando el usuario inicia sesión con 2FA habilitado, el backend genera un código temporal de seis dígitos.

Características principales:

- Tiempo de expiración.
- Número máximo de intentos.
- Código no almacenado en texto plano.
- Protección mediante HMAC.
- Validación mediante un challenge temporal.

---

## TOTP

El sistema genera un secreto individual para cada usuario.

El usuario puede registrar su cuenta mediante un código QR compatible con aplicaciones como:

- Microsoft Authenticator
- Google Authenticator
- Authy
- Otras aplicaciones compatibles con TOTP

Los códigos TOTP cambian aproximadamente cada 30 segundos.

---

# Gestión de productos

Endpoints principales:

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

Los productos contienen:

```text
id
nombre
precio
stock
```

---

# Gestión de usuarios

Endpoints principales:

```text
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

La contraseña se almacena utilizando BCrypt.

La API no expone en sus respuestas:

```text
password
totpSecret
```

---

# Gestión de ventas

Las ventas se registran mediante:

```text
POST /api/sales
```

Ejemplo de request:

```json
{
  "productId": 1,
  "quantity": 3
}
```

El frontend no envía:

- Precio.
- Total.
- Username.

Estos valores se obtienen y calculan en el backend.

---

## Flujo de venta

```text
POST /api/sales
       │
       ▼
Usuario obtenido desde JWT
       │
       ▼
Buscar producto
       │
       ▼
Validar cantidad
       │
       ▼
Validar stock
       │
       ▼
Descontar stock
       │
       ▼
Calcular total
       │
       ▼
Guardar venta
       │
       ▼
COMMIT
```

Toda la operación se ejecuta dentro de una transacción.

---

## Validación de stock

La validación principal se realiza en PostgreSQL mediante una actualización condicional:

```sql
UPDATE products
SET stock = stock - :quantity
WHERE id = :productId
AND stock >= :quantity;
```

Esto evita sobreventa cuando existen solicitudes concurrentes.

Ejemplo:

```text
Stock disponible = 1

Usuario A intenta comprar 1
Usuario B intenta comprar 1
```

Solo una de las dos operaciones podrá descontar el stock correctamente.

---

## Stock insuficiente

Cuando no existe suficiente inventario, la API devuelve:

```http
409 Conflict
```

Ejemplo:

```json
{
  "error": "Stock insuficiente para el producto Laptop. Disponible: 2, solicitado: 5"
}
```

En este caso:

- No se registra la venta.
- No se modifica el stock.
- La transacción se revierte.

---

## Endpoints de ventas

```text
POST /api/sales
GET  /api/sales/me
GET  /api/sales/{id}
GET  /api/sales
```

Permisos:

```text
Endpoint               ADMIN       USER
------------------------------------------------
POST /api/sales          Sí          Sí
GET  /api/sales/me       Sí          Sí
GET  /api/sales/{id}     Sí        Propias
GET  /api/sales          Sí          No
```

---

# Base de datos

El proyecto utiliza PostgreSQL.

Crear la base de datos:

```sql
CREATE DATABASE products_db;
```

---

## Tabla de productos

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio NUMERIC(38,2) NOT NULL,
    stock INTEGER NOT NULL
);
```

---

## Tabla de usuarios

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    totp_secret VARCHAR(128)
);
```

---

## Tabla de challenges 2FA

```sql
CREATE TABLE two_factor_challenges (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email_otp_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_2fa_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
```

---

## Tabla de ventas

```sql
CREATE TABLE sales (
    id UUID PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(38,2) NOT NULL,
    total NUMERIC(38,2) NOT NULL,
    username VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT sales_quantity_check
        CHECK (quantity > 0),

    CONSTRAINT fk_sale_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);
```

---

# Configuración del backend

Ingresar al directorio:

```bash
cd backend-qph
```

## Requisitos

```text
Java 21
PostgreSQL
```

Verificar Java:

```bash
java -version
javac -version
```

---

## Variables de entorno

Se recomienda no almacenar secretos directamente en Git.

Ejemplo de configuración:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/products_db
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:admin}

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000

app.2fa.issuer=QPH
app.2fa.expiration-seconds=300
app.2fa.max-attempts=3
app.2fa.otp-pepper=${OTP_PEPPER}

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.mail.from=${MAIL_USERNAME}
```

Ejemplo en PowerShell:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="admin"

$env:JWT_SECRET="your-jwt-secret"

$env:OTP_PEPPER="your-otp-pepper"

$env:MAIL_USERNAME="correo@gmail.com"
$env:MAIL_PASSWORD="gmail-app-password"
```

---

## Compilar backend

### Windows

```powershell
.\mvnw.cmd clean package
```

### Linux / macOS

```bash
./mvnw clean package
```

---

## Ejecutar backend

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

Backend disponible en:

```text
http://localhost:8080
```

---

# Configuración del frontend

Ingresar al directorio:

```bash
cd frontend-qph
```

## Requisitos

Proyecto probado con:

```text
Node.js 22
npm 10
Angular 21
TypeScript 5.9
```

---

## Instalar dependencias

Se recomienda:

```bash
npm ci
```

Este comando utiliza `package-lock.json` para instalar las versiones registradas en el proyecto.

---

## Ejecutar frontend

```bash
npm start
```

Frontend disponible en:

```text
http://localhost:4200
```

---

## Compilar frontend

```bash
npm run build
```

El resultado se genera en:

```text
dist/frontend
```

---

# Usuario administrador inicial

Durante el arranque del backend, si no existe un administrador, el proyecto crea un usuario inicial para desarrollo:

```text
Username: admin
Password: admin123
```

Estas credenciales son únicamente para desarrollo y pruebas.

No deben utilizarse en producción.

---

# Manejo de errores

La aplicación utiliza un manejador global de excepciones para devolver respuestas HTTP consistentes.

Ejemplos:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

Ejemplo:

```json
{
  "error": "El username ya existe"
}
```

---

# Seguridad

El proyecto implementa:

- JWT.
- BCrypt.
- Autorización basada en roles.
- Segundo factor de autenticación.
- OTP con expiración.
- TOTP.
- Validación de stock en backend.
- Descuento atómico de inventario.
- Manejo global de excepciones.
- Protección de endpoints mediante Spring Security.
- Interceptor JWT en Angular.

Para un entorno productivo se deberían considerar adicionalmente:

- HTTPS obligatorio.
- Refresh Tokens.
- Rotación de claves JWT.
- Claves JWT asimétricas.
- Rate limiting.
- Protección de secretos TOTP.
- Gestión centralizada de secretos.
- Auditoría.
- Observabilidad.
- Flyway o Liquibase.
- Docker.
- CI/CD.

---

# Ejecución rápida

## 1. Crear base de datos

```sql
CREATE DATABASE products_db;
```

Crear las tablas indicadas anteriormente.

## 2. Configurar variables de entorno

Ejemplo en PowerShell:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="admin"

$env:JWT_SECRET="your-jwt-secret"
$env:OTP_PEPPER="your-otp-pepper"

$env:MAIL_USERNAME="correo@gmail.com"
$env:MAIL_PASSWORD="gmail-app-password"
```

## 3. Ejecutar backend

```powershell
cd backend-qph

.\mvnw.cmd clean package

.\mvnw.cmd spring-boot:run
```

## 4. Ejecutar frontend

En otra terminal:

```powershell
cd frontend-qph

npm ci

npm start
```

## 5. Abrir aplicación

```text
http://localhost:4200
```

---

# Compilación final antes de publicar

## Backend

```powershell
cd backend-qph

.\mvnw.cmd clean package
```

## Frontend

```powershell
cd frontend-qph

npm ci

npm run build
```

---

# Archivos que no deben subirse al repositorio

No subir:

```text
node_modules/
dist/
.angular/
out-tsc/
target/
.env
archivos con secretos
```

Sí subir:

```text
package.json
package-lock.json
pom.xml
mvnw
mvnw.cmd
.mvn/
src/
README.md
.gitignore
```

---

# Autor

Proyecto Full Stack desarrollado como ejercicio técnico utilizando **Java**, **Spring Boot**, **Angular**, **PostgreSQL**, **JWT**, autenticación multifactor y consistencia transaccional.
