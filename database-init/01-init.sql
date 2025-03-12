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