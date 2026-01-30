-- Healthcare Platform Database Initialization Script
-- This script runs when the PostgreSQL container is first created

-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create patients table (if not exists - JPA will handle this, but good for reference)
-- CREATE TABLE IF NOT EXISTS patients (
--     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
--     first_name VARCHAR(100) NOT NULL,
--     last_name VARCHAR(100) NOT NULL,
--     date_of_birth DATE NOT NULL,
--     email VARCHAR(255) UNIQUE,
--     phone VARCHAR(20),
--     address VARCHAR(500),
--     medical_record_number VARCHAR(50) UNIQUE,
--     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
--     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
-- );

-- Create index for common queries
-- CREATE INDEX IF NOT EXISTS idx_patients_email ON patients(email);
-- CREATE INDEX IF NOT EXISTS idx_patients_mrn ON patients(medical_record_number);
-- CREATE INDEX IF NOT EXISTS idx_patients_status ON patients(status);
-- CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(last_name, first_name);

-- Insert sample data for testing (optional)
-- This will be handled by the application seed data

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE healthcare TO healthcare;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'Healthcare Platform database initialized successfully!';
END
$$;
