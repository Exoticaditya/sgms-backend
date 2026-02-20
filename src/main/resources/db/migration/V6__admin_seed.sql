SET search_path TO public;

INSERT INTO users (email,password_hash,full_name,status)
VALUES (
'admin@sgms.com',
crypt('Admin@123', gen_salt('bf')),
'System Administrator',
'ACTIVE'
)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id
FROM users u,roles r
WHERE u.email='admin@sgms.com' AND r.name='ADMIN'
ON CONFLICT DO NOTHING;
