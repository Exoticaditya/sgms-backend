SET search_path TO public;

-- Add status column to guards table
ALTER TABLE guards ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add status column to site_posts table  
ALTER TABLE site_posts ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add status column to guard_assignments table
ALTER TABLE guard_assignments ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add description and created_at columns to shift_types table
ALTER TABLE shift_types ADD COLUMN description TEXT;
ALTER TABLE shift_types ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
