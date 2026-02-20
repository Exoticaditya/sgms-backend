# ================================================================
# PHASE 2: FLYWAY BASELINE RESET CONFIGURATION
# ================================================================

## APPLICATION.YML VERIFICATION

Ensure your application.yml files have these settings:

### src/main/resources/application.yml
---
spring:
  main:
    banner-mode: off
  jackson:
    time-zone: UTC
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none              # CRITICAL - NEVER change this
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
  flyway:
    enabled: true                 # Enable Flyway
    baseline-on-migrate: false    # Do NOT baseline automatically
    clean-disabled: true          # Prevent accidental data loss
    locations: classpath:db/migration
    validate-on-migrate: true     # Validate checksums

logging:
  level:
    root: INFO
    com.sgms: DEBUG
    org.flywaydb: DEBUG           # Enable Flyway logging
    org.hibernate.SQL: DEBUG      # Log SQL statements
---

### src/main/resources/application-prod.yml
---
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: none              # CRITICAL - NEVER change this
    show-sql: false
  flyway:
    enabled: true
    clean-disabled: true          # NEVER allow clean in production

logging:
  level:
    org.flywaydb: INFO
    org.hibernate.SQL: WARN
---

### src/main/resources/application-local.yml
---
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sgms
    username: ${LOCAL_PGUSER:postgres}
    password: ${LOCAL_PGPASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none              # CRITICAL - NEVER change this
    show-sql: true
  flyway:
    enabled: true
    clean-disabled: false         # Allow clean in local dev only

logging:
  level:
    org.flywaydb: DEBUG
    org.hibernate.SQL: DEBUG
---

## PRE-MIGRATION DATABASE VERIFICATION

### Connect to Railway PostgreSQL (production):
Use Railway dashboard or psql client

### Connect to local PostgreSQL:
```powershell
psql -U postgres -d sgms
```

### Verify database is EMPTY:
```sql
-- Should return NO tables (except maybe flyway_schema_history)
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE';

-- If flyway_schema_history exists, CRITICAL DECISION REQUIRED:
-- Option A: Keep it if you want to baseline
-- Option B: Drop it for clean start (recommended)

-- To start completely fresh:
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Verify NO tables exist:
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
-- Expected: 0 rows
```

## IMPORTANT NOTES:

1. Flyway will create `flyway_schema_history` automatically on first run
2. NEVER manually modify flyway_schema_history
3. Each migration runs ONCE - checksum is recorded
4. If you change a migration file after it runs, Flyway will FAIL validation
5. In production, NEVER use flyway.clean (it deletes all data)

## READY STATE CHECKLIST:

- [ ] application.yml has ddl-auto: none
- [ ] application.yml has flyway.enabled: true
- [ ] Flyway logging level is DEBUG (for initial setup)
- [ ] Database connection verified
- [ ] Database is empty (or flyway_schema_history dropped)
- [ ] Migration directory exists: src/main/resources/db/migration/
- [ ] Migration directory is EMPTY

Once all checkboxes are ticked, proceed to PHASE 3.
