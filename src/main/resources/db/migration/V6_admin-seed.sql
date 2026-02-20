SET search_path TO public;

INSERT INTO users (email,password_hash,full_name,status)
VALUES (
'admin@sgms.com',
'$2a$10$8K1p/a0dL3.W5vSTL0qLc.zX5BYM0f5F7E5JN1O0R8zX5BYM0f5F7',
'System Administrator',
'ACTIVE'
)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id
FROM users u,roles r
WHERE u.email='admin@sgms.com' AND r.name='ADMIN'
ON CONFLICT DO NOTHING;
