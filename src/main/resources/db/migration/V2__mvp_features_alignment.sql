-- ============================================================================
-- V2__mvp_features_alignment.sql - DESPACHA FÁCIL (MVP v1.0)
-- PostgreSQL 16 Multi-Tenant: Alineación Funcional con Especificación de Producto
-- ============================================================================

-- 1. IDENTIDAD DE MARCA Y BILLETERAS DIGITALES (Módulo 2: Configuración de Marca y Tarifarios)
ALTER TABLE tenants
    ADD COLUMN logo_url VARCHAR(500),
    ADD COLUMN primary_color VARCHAR(10) DEFAULT '#FFA800',
    ADD COLUMN banner_url VARCHAR(500),
    ADD COLUMN yape_qr_url VARCHAR(500),
    ADD COLUMN yape_phone VARCHAR(30),
    ADD COLUMN plin_phone VARCHAR(30),
    ADD COLUMN free_shipping_threshold DECIMAL(10, 2) CHECK (free_shipping_threshold >= 0);

-- 2. TARIFARIO BASE CONFIGURABLE POR ZONAS/MODALIDADES (Módulo 2)
CREATE TABLE shipping_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (cost >= 0),
    estimated_delivery VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shipping_rates_tenant ON shipping_rates(tenant_id);

-- 3. MERCHANDISING, GALERÍA DE PRENDAS Y OFERTAS (Módulo 3: Catálogo y Merchandising)
ALTER TABLE products
    ADD COLUMN badge VARCHAR(50),
    ADD COLUMN has_free_shipping BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0;

ALTER TABLE product_variants
    ADD COLUMN compare_at_price DECIMAL(10, 2) CHECK (compare_at_price >= 0);

CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_products_tenant_sort ON products(tenant_id, sort_order);

-- 4. RESERVA ATÓMICA DE 20 MINUTOS, AUDITORÍA DE PAGO Y CANAL (Módulos 4, 6 y 7)
ALTER TABLE orders
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN paid_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN channel VARCHAR(30) NOT NULL DEFAULT 'WEB_CATALOG';

-- Índice parcial para optimizar el job/cron de expiración automática de reservas
CREATE INDEX idx_orders_pending_expires ON orders(expires_at) WHERE status = 'PENDING_PAYMENT';
CREATE INDEX idx_orders_tenant_channel ON orders(tenant_id, channel);

-- 5. CHECKLIST EN ALMACÉN / MODO EMPAQUE (Módulo 8: Picking/Packing)
ALTER TABLE order_items
    ADD COLUMN is_packed BOOLEAN NOT NULL DEFAULT FALSE;

-- 6. KARDEX INMUTABLE: COSTO UNITARIO DE COMPRA (Módulo 4: Kardex)
ALTER TABLE inventory_movements
    ADD COLUMN unit_cost DECIMAL(10, 2) CHECK (unit_cost >= 0);

-- 7. COURIERS PERÚ: AGENCIAS (SHALOM/MARVISUR) Y CLAVE DE RECOJO (Módulos 9 y 10)
ALTER TABLE dispatches
    ADD COLUMN dispatch_type VARCHAR(50) NOT NULL DEFAULT 'COURIER',
    ADD COLUMN agency_name VARCHAR(150),
    ADD COLUMN pickup_code VARCHAR(50),
    ADD COLUMN packed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_dispatches_type ON dispatches(tenant_id, dispatch_type);
