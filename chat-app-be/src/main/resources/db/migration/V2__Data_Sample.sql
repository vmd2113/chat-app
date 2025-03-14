
-- Insert người dùng mẫu
INSERT INTO users (email, password, full_name, status) VALUES
                                                           ('user1@example.com', crypt('password123', gen_salt('bf')), 'Nguyễn Văn A', 'OFFLINE'),
                                                           ('user2@example.com', crypt('password123', gen_salt('bf')), 'Trần Thị B', 'OFFLINE'),
                                                           ('user3@example.com', crypt('password123', gen_salt('bf')), 'Lê Văn C', 'OFFLINE');

--

-- Tạo mối quan hệ bạn bè
INSERT INTO friendships (requester_id, addressee_id, status) VALUES
                                                                 (1, 2, 'ACCEPTED'),  -- User 1 và User 2 là bạn bè
                                                                 (1, 3, 'PENDING'),   -- User 1 gửi yêu cầu kết bạn cho User 3
                                                                 (3, 2, 'ACCEPTED');  -- User 3 và User 2 là bạn bè

-- Tạo nhóm bạn bè
INSERT INTO friend_groups (user_id, name) VALUES
                                              (1, 'Bạn thân'),
                                              (1, 'Đồng nghiệp'),
                                              (2, 'Gia đình');

-- Thêm thành viên vào nhóm bạn bè
INSERT INTO friend_group_members (friend_group_id, friend_id) VALUES
                                                                  (1, 2),  -- User 2 thuộc nhóm "Bạn thân" của User 1
                                                                  (2, 3);  -- User 3 thuộc nhóm "Đồng nghiệp" của User 1

-- Tạo cuộc trò chuyện cá nhân
INSERT INTO conversations (type, is_encrypted) VALUES
                                                   ('INDIVIDUAL', TRUE),   -- Cuộc trò chuyện giữa User 1 và User 2
                                                   ('GROUP', TRUE);        -- Cuộc trò chuyện nhóm

-- Thêm thành viên vào cuộc trò chuyện
INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES
                                                                           (1, 1, 'MEMBER'),
                                                                           (1, 2, 'MEMBER'),
                                                                           (2, 1, 'ADMIN'),
                                                                           (2, 2, 'MEMBER'),
                                                                           (2, 3, 'MEMBER');

-- Cập nhật thông tin cho cuộc trò chuyện nhóm
UPDATE conversations
SET name = 'Nhóm chat thử nghiệm', avatar = 'group_avatar.png'
WHERE id = 2;




-- Insert người dùng mẫu với mật khẩu mã hóa
INSERT INTO users (email, password, full_name, status, avatar, email_verified) VALUES
                                                                                   ('user4@example.com', crypt('password123', gen_salt('bf')), 'Phạm Thị D', 'OFFLINE', 'avatar4.png', TRUE),
                                                                                   ('user5@example.com', crypt('password123', gen_salt('bf')), 'Hoàng Văn E', 'OFFLINE', 'avatar5.png', FALSE);

-- Thêm OAuth cho user1
INSERT INTO user_oauth (user_id, provider, provider_user_id) VALUES
                                                                 (1, 'GOOGLE', 'google_user_id_123'),
                                                                 (3, 'GITHUB', 'github_user_id_456');

-- Tạo mối quan hệ bạn bè
INSERT INTO friendships (requester_id, addressee_id, status, created_at) VALUES
                                                                             (1, 5, 'PENDING', NOW() - INTERVAL '5 days'),    -- User 1 gửi yêu cầu kết bạn cho User 5
                                                                             (5, 3, 'PENDING', NOW() - INTERVAL '3 days'),    -- User 4 gửi yêu cầu kết bạn cho User 3
                                                                             (2, 5, 'BLOCKED', NOW() - INTERVAL '7 days');    -- User 2 chặn User 5

-- Tạo nhóm bạn bè
INSERT INTO friend_groups (user_id, name) VALUES
                                              (1, 'Bạn thân'),
                                              (1, 'Đồng nghiệp'),
                                              (2, 'Gia đình'),
                                              (3, 'Trường học');

-- Thêm thành viên vào nhóm bạn bè
INSERT INTO friend_group_members (friend_group_id, friend_id) VALUES
                                                                  (1, 3),  -- User 3 thuộc nhóm "Bạn thân" của User 1
                                                                  (2, 1),  -- User 4 thuộc nhóm "Đồng nghiệp" của User 1
                                                                  (3, 1),  -- User 1 thuộc nhóm "Gia đình" của User 2
                                                                  (5, 2);  -- User 2 thuộc nhóm "Trường học" của User 3

-- Tạo cuộc trò chuyện cá nhân
INSERT INTO conversations (type, is_encrypted) VALUES
                                                   ('INDIVIDUAL', TRUE),   -- Cuộc trò chuyện giữa User 1 và User 2
                                                   ('INDIVIDUAL', TRUE),   -- Cuộc trò chuyện giữa User 1 và User 3
                                                   ('INDIVIDUAL', TRUE),   -- Cuộc trò chuyện giữa User 2 và User 3
                                                   ('GROUP', TRUE);        -- Cuộc trò chuyện nhóm

-- Thêm thành viên vào cuộc trò chuyện
INSERT INTO conversation_participants (conversation_id, user_id, role) VALUES
                                                                           (3, 2, 'MEMBER'),
                                                                           (3, 3, 'MEMBER'),
                                                                           (4, 1, 'ADMIN'),
                                                                           (4, 2, 'MEMBER'),
                                                                           (4, 3, 'MEMBER');

-- Cập nhật thông tin cho cuộc trò chuyện nhóm
UPDATE conversations
SET name = 'Nhóm chat thử nghiệm', avatar = 'group_avatar.png'
WHERE id = 4;

-- Tạo tin nhắn trong cuộc trò chuyện 1 (giữa User 1 và User 2)
-- Sử dụng hàm add_message
SELECT add_message(1, 1, NULL, 'Xin chào, bạn khỏe không?', 'TEXT', FALSE, NULL);
SELECT add_message(1, 2, NULL, 'Chào bạn, mình khỏe. Còn bạn?', 'TEXT', FALSE, NULL);
SELECT add_message(1, 1, NULL, 'Mình cũng ổn, cảm ơn bạn', 'TEXT', FALSE, NULL);

-- Tạo tin nhắn trong cuộc trò chuyện 2 (giữa User 1 và User 3)
SELECT add_message(2, 1, NULL, 'Chào bạn, đã lâu không gặp', 'TEXT', FALSE, NULL);
SELECT add_message(2, 3, NULL, 'Ừ nhỉ, dạo này bạn thế nào?', 'TEXT', FALSE, NULL);

-- Tạo tin nhắn trong cuộc trò chuyện nhóm
SELECT add_message(4, 1, NULL, 'Xin chào tất cả mọi người!', 'TEXT', FALSE, NULL);
SELECT add_message(4, 2, NULL, 'Chào cả nhóm!', 'TEXT', FALSE, NULL);
SELECT add_message(4, 3, NULL, 'Rất vui được gặp mọi người ở đây', 'TEXT', FALSE, NULL);
SELECT add_message(4, 1, NULL, 'Hôm nay chúng ta sẽ bàn về dự án mới', 'TEXT', FALSE, NULL);

-- Tạo phản ứng cho tin nhắn
INSERT INTO message_reactions (message_id, user_id, reaction) VALUES
                                                                  (1, 2, '👍'),  -- User 2 thích tin nhắn 1
                                                                  (4, 3, '❤️'),  -- User 3 thích tin nhắn 4
                                                                  (7, 2, '😄'),  -- User 2 cười tin nhắn 7
                                                                  (7, 3, '👍');  -- User 3 thích tin nhắn 7

-- Đánh dấu tin nhắn đã đọc
INSERT INTO message_reads (message_id, user_id, read_at) VALUES
                                                             (1, 2, NOW() - INTERVAL '2 days'),
                                                             (2, 1, NOW() - INTERVAL '2 days'),
                                                             (4, 3, NOW() - INTERVAL '1 day'),
                                                             (7, 1, NOW() - INTERVAL '12 hours'),
                                                             (7, 3, NOW() - INTERVAL '12 hours');

-- Tạo thông báo
INSERT INTO notifications (user_id, type, reference_id, content, read) VALUES
                                                                           (3, 'MESSAGE', 4, 'Bạn có tin nhắn mới từ Nguyễn Văn A', FALSE),
                                                                           (2, 'FRIEND_REQUEST', 6, 'Phạm Thị D muốn kết bạn với bạn', FALSE),
                                                                           (5, 'FRIEND_REQUEST', 5, 'Nguyễn Văn A muốn kết bạn với bạn', TRUE);

-- Tạo cài đặt người dùng
UPDATE user_settings
SET notification_enabled = TRUE, notification_sound = TRUE, show_status = TRUE, language = 'vi', theme = 'light'
WHERE user_id = 1;

UPDATE user_settings
SET notification_enabled = TRUE, notification_sound = FALSE, show_status = FALSE, language = 'en', theme = 'dark'
WHERE user_id = 2;

