-- Tạo bảng roles
CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng permissions
CREATE TABLE permissions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng user_roles
CREATE TABLE user_roles
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_roles_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role_id
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_user_role
        UNIQUE (user_id, role_id)
);

-- Tạo bảng role_permissions
CREATE TABLE role_permissions
(
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_role_permissions_role_id
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission_id
        FOREIGN KEY (permission_id)
            REFERENCES permissions (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_role_permission
        UNIQUE (role_id, permission_id)
);

-- Thêm trigger cập nhật trường updated_at
CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE
    ON roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_permissions_updated_at
    BEFORE UPDATE
    ON permissions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Thêm dữ liệu ban đầu
INSERT INTO roles (name, description)
VALUES ('ROLE_USER', 'Regular user with standard permissions'),
       ('ROLE_ADMIN', 'Administrator with full system access'),
       ('ROLE_MODERATOR', 'Content moderator');

-- Thêm các quyền cơ bản
INSERT INTO permissions (name, description)
VALUES ('MANAGE_USERS', 'Create, update and delete users'),
       ('VIEW_ALL_USERS', 'View all user information'),
       ('MANAGE_CHATS', 'Moderate chat messages and conversations'),
       ('VIEW_REPORTS', 'View user reports and statistics'),
       ('MANAGE_SETTINGS', 'Manage system settings');

-- Gán quyền cho các vai trò
-- Admin có tất cả các quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), id
FROM permissions;

-- Moderator có một số quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'ROLE_MODERATOR'),
       id
FROM permissions
WHERE name IN ('MANAGE_CHATS', 'VIEW_REPORTS');

-- Thiết lập tất cả người dùng là ROLE_USER ban đầu
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE r.name = 'ROLE_USER';