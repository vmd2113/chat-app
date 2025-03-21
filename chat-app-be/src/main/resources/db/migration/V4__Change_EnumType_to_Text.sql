-- File: src/main/resources/db/migration/V4__Change_EnumType_to_Text.sql

-- 1. Sao lưu định nghĩa của tất cả view
CREATE TABLE view_definitions AS
SELECT viewname, definition
FROM pg_views
WHERE schemaname = 'public';

-- 2. Xóa tất cả view
DO $$
    DECLARE
        view_rec RECORD;
    BEGIN
        FOR view_rec IN SELECT viewname FROM pg_views WHERE schemaname = 'public'
            LOOP
                EXECUTE 'DROP VIEW IF EXISTS ' || view_rec.viewname || ' CASCADE';
            END LOOP;
    END $$;

-- 3. Xóa các ràng buộc và giá trị mặc định trước khi thay đổi kiểu dữ liệu
ALTER TABLE users ALTER COLUMN status DROP DEFAULT;
ALTER TABLE friendships ALTER COLUMN status DROP DEFAULT;
ALTER TABLE conversations ALTER COLUMN type DROP DEFAULT;
ALTER TABLE conversation_participants ALTER COLUMN role DROP DEFAULT;
ALTER TABLE messages ALTER COLUMN type DROP DEFAULT;

-- 4. Thực hiện thay đổi kiểu dữ liệu
ALTER TABLE users
    ALTER COLUMN status TYPE TEXT
        USING status::TEXT;

ALTER TABLE friendships
    ALTER COLUMN status TYPE TEXT
        USING status::TEXT;

ALTER TABLE conversations
    ALTER COLUMN type TYPE TEXT
        USING type::TEXT;

ALTER TABLE conversation_participants
    ALTER COLUMN role TYPE TEXT
        USING role::TEXT;

ALTER TABLE messages
    ALTER COLUMN type TYPE TEXT
        USING type::TEXT;

-- 5. Thiết lập lại giá trị mặc định (nếu cần)
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'OFFLINE';
ALTER TABLE friendships ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE conversations ALTER COLUMN type SET DEFAULT 'INDIVIDUAL';
ALTER TABLE conversation_participants ALTER COLUMN role SET DEFAULT 'MEMBER';
ALTER TABLE messages ALTER COLUMN type SET DEFAULT 'TEXT';

-- 6. Tạo lại các view
-- Tạo lại view v_user_friends
CREATE OR REPLACE VIEW v_user_friends AS
SELECT
    f.requester_id AS user_id,
    f.addressee_id AS friend_id,
    f.status,
    f.created_at
FROM
    friendships f
WHERE
    f.status = 'ACCEPTED'
UNION
SELECT
    f.addressee_id AS user_id,
    f.requester_id AS friend_id,
    f.status,
    f.created_at
FROM
    friendships f
WHERE
    f.status = 'ACCEPTED';

-- Tạo lại view v_unread_messages_count
CREATE OR REPLACE VIEW v_unread_messages_count AS
SELECT
    cp.user_id,
    m.conversation_id,
    COUNT(m.id) AS unread_count
FROM
    messages m
        JOIN
    conversation_participants cp ON m.conversation_id = cp.conversation_id
        LEFT JOIN
    message_reads mr ON m.id = mr.message_id AND mr.user_id = cp.user_id
WHERE
    m.sender_id != cp.user_id AND
    mr.id IS NULL AND
    m.deleted = FALSE AND
    cp.left_at IS NULL
GROUP BY
    cp.user_id, m.conversation_id;

-- Tạo lại view v_conversations_with_last_message
CREATE OR REPLACE VIEW v_conversations_with_last_message AS
SELECT
    c.id AS conversation_id,
    c.type,
    c.name,
    c.avatar,
    c.is_encrypted,
    lm.message_id AS last_message_id,
    lm.sender_id AS last_sender_id,
    lm.sent_at AS last_message_time,
    lm.type AS last_message_type
FROM
    conversations c
        LEFT JOIN (
        SELECT
            m.conversation_id,
            m.id AS message_id,
            m.sender_id,
            m.sent_at,
            m.type,
            ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.sent_at DESC) AS rn
        FROM
            messages m
        WHERE
            m.deleted = FALSE
    ) lm ON c.id = lm.conversation_id AND lm.rn = 1;

-- Tạo lại view v_messages
CREATE OR REPLACE VIEW v_messages AS
SELECT
    m.id,
    m.conversation_id,
    m.sender_id,
    m.parent_id,
    m.type,
    m.sent_at,
    m.edited_at,
    m.deleted,
    mc.content,
    mc.encrypted,
    mc.metadata
FROM
    messages m
        LEFT JOIN
    messages_content mc ON m.id = mc.message_id;

-- 7. Bây giờ mới có thể xóa các kiểu enum
DROP TYPE IF EXISTS user_status CASCADE;
DROP TYPE IF EXISTS friendship_status CASCADE;
DROP TYPE IF EXISTS conversation_type CASCADE;
DROP TYPE IF EXISTS participant_role CASCADE;
DROP TYPE IF EXISTS message_type CASCADE;

-- 8. Xóa bảng tạm lưu định nghĩa view
DROP TABLE IF EXISTS view_definitions;