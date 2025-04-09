-- Create verification tokens table
CREATE TABLE verification_tokens (
                                     id BIGSERIAL PRIMARY KEY,
                                     token VARCHAR(255) UNIQUE NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,


                                     CONSTRAINT fk_verification_tokens_user_id
                                         FOREIGN KEY (user_id)
                                             REFERENCES users (id)
                                             ON DELETE CASCADE
);

-- Create reset tokens table
CREATE TABLE reset_tokens (
                              id BIGSERIAL PRIMARY KEY,
                              token VARCHAR(255) UNIQUE NOT NULL,
                              user_id BIGINT NOT NULL,
                              expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,


                              CONSTRAINT fk_reset_tokens_user_id
                                  FOREIGN KEY (user_id)
                                      REFERENCES users (id)
                                      ON DELETE CASCADE
);

-- Create user status logs table
CREATE TABLE user_status_logs (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  status TEXT NOT NULL,
                                  timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,


                                  CONSTRAINT fk_user_status_logs_user_id
                                      FOREIGN KEY (user_id)
                                          REFERENCES users (id)
                                          ON DELETE CASCADE
);

-- Create indices
CREATE INDEX idx_verification_tokens_token ON verification_tokens (token);
CREATE INDEX idx_reset_tokens_token ON reset_tokens (token);
CREATE INDEX idx_user_status_logs_user_id ON user_status_logs (user_id);
CREATE INDEX idx_user_status_logs_timestamp ON user_status_logs (timestamp);