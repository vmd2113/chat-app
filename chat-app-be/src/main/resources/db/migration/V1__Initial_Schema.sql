-- Tạo database
-- CREATE DATABASE chatapp_db;

-- Kết nối đến database
\c chatapp_db

-- Kích hoạt extension UUID để sử dụng kiểu UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- Kích hoạt extension pgcrypto cho mã hóa
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Kích hoạt extension citext cho tìm kiếm không phân biệt hoa thường
CREATE EXTENSION IF NOT EXISTS "citext";

-- Enum cho trạng thái người dùng
CREATE TYPE user_status AS ENUM ('ONLINE', 'OFFLINE', 'AWAY');

-- Enum cho trạng thái yêu cầu kết bạn
CREATE TYPE friendship_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED');

-- Enum cho loại cuộc trò chuyện
CREATE TYPE conversation_type AS ENUM ('INDIVIDUAL', 'GROUP');

-- Enum cho vai trò thành viên trong cuộc trò chuyện
CREATE TYPE participant_role AS ENUM ('MEMBER', 'ADMIN');

-- Enum cho loại tin nhắn
CREATE TYPE message_type AS ENUM ('TEXT', 'FILE', 'SYSTEM', 'VOICE', 'VIDEO');



CREATE TABLE users
(
    id             BIGSERIAL PRIMARY KEY,
    email          TEXT UNIQUE NOT NULL,
    password       TEXT        NOT NULL,
    full_name      TEXT        NOT NULL,
    avatar         TEXT,
    status         user_status              DEFAULT 'OFFLINE',
    last_active    TIMESTAMP WITH TIME ZONE,
    public_key     TEXT,                                   -- Khóa công khai RSA cho E2EE
    email_verified BOOLEAN                  DEFAULT FALSE, -- Trạng thái xác minh email
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Trigger để tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Index cho email để tìm kiếm nhanh
CREATE INDEX idx_users_email ON users (email);

-- user oauth
CREATE TABLE user_oauth
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    provider         TEXT   NOT NULL, -- 'GOOGLE', 'GITHUB', etc.
    provider_user_id TEXT   NOT NULL, -- ID từ provider
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_oauth_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_user_oauth_provider_id
        UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_user_oauth_user_id ON user_oauth (user_id);


-- friendship
CREATE TABLE friendships
(
    id           BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    addressee_id BIGINT NOT NULL,
    status       friendship_status        DEFAULT 'PENDING',
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_friendships_requester_id
        FOREIGN KEY (requester_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_friendships_addressee_id
        FOREIGN KEY (addressee_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_friendship_users
        UNIQUE (requester_id, addressee_id),
    CONSTRAINT check_not_self_friend
        CHECK (requester_id != addressee_id)
);

CREATE TRIGGER update_friendships_updated_at
    BEFORE UPDATE
    ON friendships
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_friendships_requester_id ON friendships (requester_id);
CREATE INDEX idx_friendships_addressee_id ON friendships (addressee_id);


-- friend group
CREATE TABLE friend_groups
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    name       TEXT   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_friend_groups_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TRIGGER update_friend_groups_updated_at
    BEFORE UPDATE
    ON friend_groups
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_friend_groups_user_id ON friend_groups (user_id);


-- friend group member
CREATE TABLE friend_group_members
(
    id              BIGSERIAL PRIMARY KEY,
    friend_group_id BIGINT NOT NULL,
    friend_id       BIGINT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_friend_group_members_group_id
        FOREIGN KEY (friend_group_id)
            REFERENCES friend_groups (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_friend_group_members_friend_id
        FOREIGN KEY (friend_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_friend_group_member
        UNIQUE (friend_group_id, friend_id)
);

CREATE INDEX idx_friend_group_members_group_id ON friend_group_members (friend_group_id);
CREATE INDEX idx_friend_group_members_friend_id ON friend_group_members (friend_id);

-- conversation
CREATE TABLE conversations
(
    id           BIGSERIAL PRIMARY KEY,
    type         conversation_type NOT NULL,
    name         TEXT,                                  -- Tên cho group conversation
    avatar       TEXT,                                  -- Avatar cho group conversation
    is_encrypted BOOLEAN                  DEFAULT TRUE, -- Có mã hóa E2E hay không
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_conversations_updated_at
    BEFORE UPDATE
    ON conversations
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- conversation participants
CREATE TABLE conversation_participants
(
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    role            participant_role         DEFAULT 'MEMBER',
    muted           BOOLEAN                  DEFAULT FALSE, -- Có tắt thông báo không
    joined_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    left_at         TIMESTAMP WITH TIME ZONE,               -- Null nếu vẫn tham gia

    CONSTRAINT fk_conversation_participants_conversation_id
        FOREIGN KEY (conversation_id)
            REFERENCES conversations (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_conversation_participants_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_conversation_participant
        UNIQUE (conversation_id, user_id)
);

CREATE INDEX idx_conversation_participants_conversation_id ON conversation_participants (conversation_id);
CREATE INDEX idx_conversation_participants_user_id ON conversation_participants (user_id);


-- message

CREATE TABLE messages
(
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id       BIGINT NOT NULL,
    parent_id       BIGINT,                                  -- ID của tin nhắn cha (cho threading)
    type            message_type             DEFAULT 'TEXT', -- Loại tin nhắn
    sent_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    edited_at       TIMESTAMP WITH TIME ZONE,                -- Thời gian chỉnh sửa
    deleted         BOOLEAN                  DEFAULT FALSE,  -- Soft delete

    CONSTRAINT fk_messages_conversation_id
        FOREIGN KEY (conversation_id)
            REFERENCES conversations (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender_id
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_messages_parent_id
        FOREIGN KEY (parent_id)
            REFERENCES messages (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX idx_messages_sender_id ON messages (sender_id);
CREATE INDEX idx_messages_parent_id ON messages (parent_id);
CREATE INDEX idx_messages_sent_at ON messages (sent_at);


-- Tạm thời tạo bảng backup để không mất dữ liệu
CREATE TABLE messages_backup AS
SELECT *
FROM messages;

-- Thay đổi cấu trúc bảng messages
-- ALTER TABLE messages DROP COLUMN content;
-- ALTER TABLE messages DROP COLUMN encrypted;

-- Xóa cột content và encrypted từ bảng messages
-- Chỉ thực hiện sau khi đã di chuyển dữ liệu và cập nhật ứng dụng


-- Tạo bảng messages_content với partitioning theo thời gian
CREATE TABLE messages_content
(
    message_id BIGINT                   NOT NULL,
    content    TEXT,
    encrypted  BOOLEAN DEFAULT FALSE,
    metadata   JSONB, -- Trường mới để lưu thông tin bổ sung
    sent_at    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_messages_content_message_id
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE,

    PRIMARY KEY (message_id, sent_at)
) PARTITION BY RANGE (sent_at);

-- Tạo partition cho tháng hiện tại và tháng tiếp theo
CREATE TABLE messages_content_y2025_m03 PARTITION OF messages_content
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');

CREATE TABLE messages_content_y2025_m04 PARTITION OF messages_content
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');

-- Tạo các index cho các partition
CREATE INDEX idx_messages_content_y2025_m03_message_id
    ON messages_content_y2025_m03 (message_id);

CREATE INDEX idx_messages_content_y2025_m04_message_id
    ON messages_content_y2025_m04 (message_id);


-- message reactions
CREATE TABLE message_reactions
(
    id         BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    reaction   TEXT   NOT NULL, -- Emoji hoặc mã phản ứng
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_reactions_message_id
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_message_reactions_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_message_reaction_user
        UNIQUE (message_id, user_id, reaction)
);

CREATE INDEX idx_message_reactions_message_id ON message_reactions (message_id);
CREATE INDEX idx_message_reactions_user_id ON message_reactions (user_id);


-- message read

CREATE TABLE message_reads
(
    id         BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    read_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_reads_message_id
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_message_reads_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_message_read_user
        UNIQUE (message_id, user_id)
);

CREATE INDEX idx_message_reads_message_id ON message_reads (message_id);

-- files
CREATE TABLE files
(
    id            BIGSERIAL PRIMARY KEY,
    message_id    BIGINT NOT NULL,
    name          TEXT   NOT NULL,
    original_name TEXT   NOT NULL,                            -- Tên gốc của file
    type          TEXT   NOT NULL,                            -- MIME type
    size          BIGINT NOT NULL,                            -- Kích thước (bytes)
    url           TEXT   NOT NULL,                            -- URL để truy cập file
    encrypted     BOOLEAN                  DEFAULT FALSE,     -- File có mã hóa không
    scan_status   TEXT                     DEFAULT 'PENDING', -- PENDING, CLEAN, INFECTED
    uploaded_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_files_message_id
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_files_message_id ON files (message_id);
CREATE INDEX idx_files_type ON files (type);
CREATE INDEX idx_message_reads_user_id ON message_reads (user_id);

-- notifications
CREATE TABLE notifications
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    type         TEXT   NOT NULL,                        -- 'MESSAGE', 'FRIEND_REQUEST', etc.
    reference_id BIGINT,                                 -- ID của đối tượng liên quan
    content      TEXT   NOT NULL,                        -- Nội dung thông báo
    read         BOOLEAN                  DEFAULT FALSE, -- Đã đọc chưa
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_read ON notifications (read);


-- user settings
CREATE TABLE user_settings
(
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL,
    notification_enabled BOOLEAN                  DEFAULT TRUE,    -- Bật/tắt thông báo
    notification_sound   BOOLEAN                  DEFAULT TRUE,    -- Bật/tắt âm thanh
    show_status          BOOLEAN                  DEFAULT TRUE,    -- Hiển thị trạng thái online
    language             TEXT                     DEFAULT 'vi',    -- Ngôn ngữ
    theme                TEXT                     DEFAULT 'light', -- Giao diện
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_settings_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_user_settings_user_id
        UNIQUE (user_id)
);

CREATE TRIGGER update_user_settings_updated_at
    BEFORE UPDATE
    ON user_settings
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- encryption key
CREATE TABLE encryption_keys
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    conversation_id BIGINT,                   -- NULL for personal keys
    key_type        TEXT   NOT NULL,          -- 'RSA_PUBLIC', 'AES', etc.
    key_value       TEXT   NOT NULL,          -- Giá trị khóa (có thể mã hóa)
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP WITH TIME ZONE, -- Thời gian hết hạn của khóa

    CONSTRAINT fk_encryption_keys_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_encryption_keys_conversation_id
        FOREIGN KEY (conversation_id)
            REFERENCES conversations (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_encryption_keys_user_id ON encryption_keys (user_id);
CREATE INDEX idx_encryption_keys_conversation_id ON encryption_keys (conversation_id);


-- not if use redis (option)
CREATE TABLE refresh_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT                   NOT NULL,
    token       TEXT                     NOT NULL,
    device_info TEXT, -- Thông tin thiết bị
    ip_address  TEXT, -- Địa chỉ IP
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN                  DEFAULT FALSE,

    CONSTRAINT fk_refresh_tokens_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);

-- user privacy block
CREATE TABLE user_privacy_blocks
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_privacy_blocks_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user_privacy_blocks_blocked_user_id
        FOREIGN KEY (blocked_user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_user_privacy_block
        UNIQUE (user_id, blocked_user_id),
    CONSTRAINT check_not_self_block
        CHECK (user_id != blocked_user_id)
);

CREATE INDEX idx_user_privacy_blocks_user_id ON user_privacy_blocks (user_id);
CREATE INDEX idx_user_privacy_blocks_blocked_user_id ON user_privacy_blocks (blocked_user_id);


-- Index cho người dùng dựa trên tên
CREATE INDEX idx_users_full_name_gin ON users USING gin (to_tsvector('english', full_name));

-- Index cho thời gian hoạt động cuối cùng
CREATE INDEX idx_users_last_active ON users (last_active);

-- Index cho các cuộc trò chuyện được cập nhật gần đây
CREATE INDEX idx_conversations_updated_at ON conversations (updated_at DESC);

-- Index kết hợp cho file sharing
CREATE INDEX idx_files_type_size ON files (type, size);


-- Chuẩn bị cho partitioning
CREATE TABLE messages_partitioned
(
    id              BIGSERIAL NOT NULL,
    conversation_id BIGINT    NOT NULL,
    sender_id       BIGINT    NOT NULL,
    parent_id       BIGINT,
    content         TEXT,
    encrypted       BOOLEAN                  DEFAULT FALSE,
    type            message_type             DEFAULT 'TEXT',
    sent_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    edited_at       TIMESTAMP WITH TIME ZONE,
    deleted         BOOLEAN                  DEFAULT FALSE,

    CONSTRAINT fk_messages_partitioned_conversation_id
        FOREIGN KEY (conversation_id)
            REFERENCES conversations (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_messages_partitioned_sender_id
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_messages_partitioned_parent_id
        FOREIGN KEY (parent_id)
            REFERENCES messages_partitioned (id)
            ON DELETE SET NULL
) PARTITION BY RANGE (sent_at);

-- Tạo các partition theo tháng
CREATE TABLE messages_y2025_m01 PARTITION OF messages_partitioned
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE messages_y2025_m02 PARTITION OF messages_partitioned
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- Tạo index cho các partition
CREATE INDEX idx_messages_y2025_m01_conversation_id ON messages_y2025_m01 (conversation_id);
CREATE INDEX idx_messages_y2025_m01_sender_id ON messages_y2025_m01 (sender_id);
CREATE INDEX idx_messages_y2025_m02_conversation_id ON messages_y2025_m02 (conversation_id);
CREATE INDEX idx_messages_y2025_m02_sender_id ON messages_y2025_m02 (sender_id);





--- TRIGGER

-- Trigger để tự động tạo user settings khi người dùng đăng ký
CREATE OR REPLACE FUNCTION create_user_settings()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_settings (user_id)
    VALUES (NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_user_insert
    AFTER INSERT ON users
    FOR EACH ROW
EXECUTE FUNCTION create_user_settings();

-- Trigger để cập nhật thời gian hoạt động cuối cùng khi người dùng gửi tin nhắn
CREATE OR REPLACE FUNCTION update_user_last_active()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE users
    SET last_active = CURRENT_TIMESTAMP
    WHERE id = NEW.sender_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_message_insert
    AFTER INSERT ON messages
    FOR EACH ROW
EXECUTE FUNCTION update_user_last_active();

-- Trigger để cập nhật thời gian cập nhật của cuộc trò chuyện khi có tin nhắn mới
CREATE OR REPLACE FUNCTION update_conversation_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE conversations
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_message_insert_update_conversation
    AFTER INSERT ON messages
    FOR EACH ROW
EXECUTE FUNCTION update_conversation_updated_at();



-- FUNCTION


-- Function lấy ID của cuộc trò chuyện giữa 2 người dùng (1-1)
CREATE OR REPLACE FUNCTION get_individual_conversation_id(user1_id BIGINT, user2_id BIGINT)
    RETURNS BIGINT AS $$
DECLARE
    conv_id BIGINT;
BEGIN
    SELECT c.id INTO conv_id
    FROM conversations c
             JOIN conversation_participants cp1 ON c.id = cp1.conversation_id
             JOIN conversation_participants cp2 ON c.id = cp2.conversation_id
    WHERE c.type = 'INDIVIDUAL'
      AND cp1.user_id = user1_id
      AND cp2.user_id = user2_id
      AND cp1.left_at IS NULL
      AND cp2.left_at IS NULL;

    RETURN conv_id;
END;
$$ LANGUAGE plpgsql;

-- Procedure tạo cuộc trò chuyện 1-1 mới giữa 2 người dùng
CREATE OR REPLACE PROCEDURE create_individual_conversation(
    user1_id BIGINT,
    user2_id BIGINT,
    OUT conversation_id BIGINT
)
    LANGUAGE plpgsql
AS $$
BEGIN
    -- Kiểm tra xem đã có cuộc trò chuyện chưa
    conversation_id := get_individual_conversation_id(user1_id, user2_id);

    -- Nếu chưa có, tạo mới
    IF conversation_id IS NULL THEN
        -- Tạo cuộc trò chuyện
        INSERT INTO conversations (type, is_encrypted)
        VALUES ('INDIVIDUAL', TRUE)
        RETURNING id INTO conversation_id;

        -- Thêm thành viên
        INSERT INTO conversation_participants (conversation_id, user_id, role)
        VALUES (conversation_id, user1_id, 'MEMBER');

        INSERT INTO conversation_participants (conversation_id, user_id, role)
        VALUES (conversation_id, user2_id, 'MEMBER');
    END IF;
END;
$$;

-- Function kiểm tra hai người dùng có phải bạn bè không
CREATE OR REPLACE FUNCTION are_users_friends(user1_id BIGINT, user2_id BIGINT)
    RETURNS BOOLEAN AS $$
DECLARE
    friend_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM friendships
        WHERE ((requester_id = user1_id AND addressee_id = user2_id) OR
               (requester_id = user2_id AND addressee_id = user1_id))
          AND status = 'ACCEPTED'
    ) INTO friend_exists;

    RETURN friend_exists;
END;
$$ LANGUAGE plpgsql;

-- Procedure để đánh dấu tin nhắn đã đọc
CREATE OR REPLACE PROCEDURE mark_messages_as_read(
    p_user_id BIGINT,
    p_conversation_id BIGINT
)
    LANGUAGE plpgsql
AS $$
BEGIN
    -- Thêm bản ghi đã đọc cho tất cả tin nhắn chưa đọc trong cuộc trò chuyện
    INSERT INTO message_reads (message_id, user_id)
    SELECT m.id, p_user_id
    FROM messages m
             LEFT JOIN message_reads mr ON m.id = mr.message_id AND mr.user_id = p_user_id
    WHERE m.conversation_id = p_conversation_id
      AND m.sender_id != p_user_id
      AND mr.id IS NULL
      AND m.deleted = FALSE
    ON CONFLICT (message_id, user_id) DO NOTHING;
END;
$$;



-- function tự động tạo partition

CREATE OR REPLACE FUNCTION create_message_content_partition_for_month()
    RETURNS void AS $$
DECLARE
    next_month_start DATE;
    next_month_end DATE;
    partition_name TEXT;
    year_str TEXT;
    month_str TEXT;
BEGIN
    -- Tính toán tháng tiếp theo
    next_month_start := date_trunc('month', CURRENT_DATE + INTERVAL '1 month');
    next_month_end := date_trunc('month', next_month_start + INTERVAL '1 month');

    -- Format tên partition: messages_content_y2025_m05
    year_str := to_char(next_month_start, 'YYYY');
    month_str := to_char(next_month_start, 'MM');
    partition_name := 'messages_content_y' || year_str || '_m' || month_str;

    -- Kiểm tra nếu partition đã tồn tại
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c
                          JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = partition_name AND n.nspname = 'public'
    ) THEN
        -- Tạo partition mới
        EXECUTE format(
                'CREATE TABLE %I PARTITION OF messages_content
                 FOR VALUES FROM (%L) TO (%L)',
                partition_name, next_month_start, next_month_end
                );

        -- Tạo index cho partition mới
        EXECUTE format(
                'CREATE INDEX idx_%I_message_id ON %I(message_id)',
                partition_name, partition_name
                );

        RAISE NOTICE 'Created new partition % for date range % to %',
            partition_name, next_month_start, next_month_end;
    ELSE
        RAISE NOTICE 'Partition % already exists', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;


-- tự động đồng bộ send at
CREATE OR REPLACE FUNCTION sync_message_content_sent_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.sent_at := (SELECT sent_at FROM messages WHERE id = NEW.message_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_message_content_sent_at
    BEFORE INSERT ON messages_content
    FOR EACH ROW
EXECUTE FUNCTION sync_message_content_sent_at();


-- function helper

-- Function để thêm tin nhắn mới
CREATE OR REPLACE FUNCTION add_message(
    p_conversation_id BIGINT,
    p_sender_id BIGINT,
    p_parent_id BIGINT,
    p_content TEXT,
    p_type message_type DEFAULT 'TEXT',
    p_encrypted BOOLEAN DEFAULT FALSE,
    p_metadata JSONB DEFAULT NULL
)
    RETURNS BIGINT AS $$
DECLARE
    new_message_id BIGINT;
BEGIN
    -- Thêm vào bảng messages
    INSERT INTO messages (
        conversation_id,
        sender_id,
        parent_id,
        type
    ) VALUES (
                 p_conversation_id,
                 p_sender_id,
                 p_parent_id,
                 p_type
             ) RETURNING id INTO new_message_id;

    -- Thêm vào bảng messages_content
    INSERT INTO messages_content (
        message_id,
        content,
        encrypted,
        metadata,
        sent_at
    ) VALUES (
                 new_message_id,
                 p_content,
                 p_encrypted,
                 p_metadata,
                 (SELECT sent_at FROM messages WHERE id = new_message_id)
             );

    -- Cập nhật last_activity của conversation
    UPDATE conversations
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = p_conversation_id;

    RETURN new_message_id;
END;
$$ LANGUAGE plpgsql;

-- Function để lấy tin nhắn theo cuộc trò chuyện với phân trang
CREATE OR REPLACE FUNCTION get_conversation_messages(
    p_conversation_id BIGINT,
    p_limit INTEGER DEFAULT 50,
    p_offset INTEGER DEFAULT 0
)
    RETURNS TABLE (
                      id BIGINT,
                      sender_id BIGINT,
                      parent_id BIGINT,
                      content TEXT,
                      type message_type,
                      encrypted BOOLEAN,
                      sent_at TIMESTAMP WITH TIME ZONE,
                      edited_at TIMESTAMP WITH TIME ZONE,
                      deleted BOOLEAN,
                      metadata JSONB
                  ) AS $$
BEGIN
    RETURN QUERY
        SELECT
            m.id,
            m.sender_id,
            m.parent_id,
            mc.content,
            m.type,
            mc.encrypted,
            m.sent_at,
            m.edited_at,
            m.deleted,
            mc.metadata
        FROM
            messages m
                LEFT JOIN
            messages_content mc ON m.id = mc.message_id
        WHERE
            m.conversation_id = p_conversation_id
          AND m.deleted = FALSE
        ORDER BY
            m.sent_at DESC
        LIMIT p_limit
            OFFSET p_offset;
END;
$$ LANGUAGE plpgsql;