-- YangMVC Spring Boot Starter Demo - 示例数据

-- 注意：如果使用H2数据库，这些数据会在应用启动时自动加载
-- 如果使用MySQL，需要手动执行此脚本

-- 更多示例数据
INSERT INTO users (name, email, age) VALUES
    ('孙悟空', 'wukong@flame.mountain', 500),
    ('猪八戒', 'bajie@heaven.gov', 300),
    ('沙和尚', 'shaseng@river.bank', 400),
    ('唐僧', 'tangseng@temple.org', 40);
