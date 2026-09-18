-- ============================================================
-- QPH ProductHub
-- Base de datos PostgreSQL
-- Archivo: product_db.sql
-- ============================================================
-- IMPORTANTE:
-- 1. Ejecute primero la sección "CREACIÓN DE BASE DE DATOS"
--    conectado a una base existente, por ejemplo: postgres.
-- 2. Luego conéctese a products_db y ejecute desde la sección
--    "TABLA PRODUCTS" en adelante.
--
-- Este script NO se ejecuta automáticamente al iniciar Spring Boot.
-- ============================================================


-- ============================================================
-- 1. CREACIÓN DE BASE DE DATOS
-- ============================================================

CREATE DATABASE products_db;


-- ============================================================
-- A PARTIR DE AQUÍ, CONECTARSE A: products_db
-- ============================================================


-- ============================================================
-- 2. TABLA PRODUCTS
-- ============================================================

CREATE TABLE IF NOT EXISTS public.products (
                                               id      BIGSERIAL      PRIMARY KEY,
                                               nombre  VARCHAR(255)   NOT NULL,
    precio  NUMERIC(38, 2) NOT NULL,
    stock   INTEGER        NOT NULL,

    CONSTRAINT products_precio_check
    CHECK (precio >= 0),

    CONSTRAINT products_stock_check
    CHECK (stock >= 0)
    );


-- ============================================================
-- 3. TABLA USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS public.users (
                                            id                  BIGSERIAL    PRIMARY KEY,
                                            username            VARCHAR(100) NOT NULL,
    email               VARCHAR(255),
    password            VARCHAR(255) NOT NULL,
    role                VARCHAR(30)  NOT NULL,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    two_factor_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    totp_secret         VARCHAR(128),

    CONSTRAINT uk_users_username
    UNIQUE (username),

    CONSTRAINT users_role_check
    CHECK (role IN ('ADMIN', 'USER'))
    );


CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email
    ON public.users (email)
    WHERE email IS NOT NULL;


-- ============================================================
-- 4. TABLA TWO_FACTOR_CHALLENGES
-- ============================================================

CREATE TABLE IF NOT EXISTS public.two_factor_challenges (
                                                            id              UUID                     PRIMARY KEY,
                                                            user_id         BIGINT                   NOT NULL,
                                                            email_otp_hash  VARCHAR(128)             NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
                                  attempts        INTEGER                  NOT NULL DEFAULT 0,
                                  used            BOOLEAN                  NOT NULL DEFAULT FALSE,

                                  CONSTRAINT fk_2fa_user
                                  FOREIGN KEY (user_id)
    REFERENCES public.users(id)
                              ON DELETE CASCADE,

    CONSTRAINT two_factor_attempts_check
    CHECK (attempts >= 0)
    );


CREATE INDEX IF NOT EXISTS idx_2fa_user
    ON public.two_factor_challenges (user_id);

CREATE INDEX IF NOT EXISTS idx_2fa_expires_at
    ON public.two_factor_challenges (expires_at);


-- ============================================================
-- 5. TABLA SALES
-- ============================================================

CREATE TABLE IF NOT EXISTS public.sales (
                                            id            UUID                     PRIMARY KEY,
                                            product_id    BIGINT                   NOT NULL,
                                            product_name  VARCHAR(255)             NOT NULL,
    quantity      INTEGER                  NOT NULL,
    unit_price    NUMERIC(38, 2)           NOT NULL,
    total         NUMERIC(38, 2)           NOT NULL,
    username      VARCHAR(100)             NOT NULL,
    status        VARCHAR(30)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,

                                CONSTRAINT fk_sale_product
                                FOREIGN KEY (product_id)
    REFERENCES public.products(id),

    CONSTRAINT sales_quantity_check
    CHECK (quantity > 0),

    CONSTRAINT sales_unit_price_check
    CHECK (unit_price >= 0),

    CONSTRAINT sales_total_check
    CHECK (total >= 0),

    CONSTRAINT sales_status_check
    CHECK (status IN ('COMPLETED', 'CANCELLED'))
    );


CREATE INDEX IF NOT EXISTS idx_sales_username
    ON public.sales (username);

CREATE INDEX IF NOT EXISTS idx_sales_created_at
    ON public.sales (created_at);

CREATE INDEX IF NOT EXISTS idx_sales_product_id
    ON public.sales (product_id);


-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
