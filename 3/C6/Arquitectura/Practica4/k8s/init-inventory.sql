-- Esquema de base de datos para el Servicio de Inventario (PostgreSQL)
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock INT NOT NULL DEFAULT 0
);

-- Datos iniciales de prueba
INSERT INTO products (id, name, stock) VALUES (1, 'Portátil Gaming', 10) ON CONFLICT (id) DO NOTHING;
INSERT INTO products (id, name, stock) VALUES (2, 'Monitor 4K', 5) ON CONFLICT (id) DO NOTHING;
