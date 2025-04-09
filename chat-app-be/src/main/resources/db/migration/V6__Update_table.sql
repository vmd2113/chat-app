-- V6__Add_updated_at_columns.sql

-- Add updated_at column to verification_tokens table
ALTER TABLE verification_tokens
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at column to reset_tokens table
ALTER TABLE reset_tokens
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at column to user_status_logs table
ALTER TABLE user_status_logs
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Create trigger function for updating updated_at automatically (if not exists already)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for each table to automatically update the updated_at column
DROP TRIGGER IF EXISTS update_verification_tokens_updated_at ON verification_tokens;
CREATE TRIGGER update_verification_tokens_updated_at
    BEFORE UPDATE ON verification_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_reset_tokens_updated_at ON reset_tokens;
CREATE TRIGGER update_reset_tokens_updated_at
    BEFORE UPDATE ON reset_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_user_status_logs_updated_at ON user_status_logs;
CREATE TRIGGER update_user_status_logs_updated_at
    BEFORE UPDATE ON user_status_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();