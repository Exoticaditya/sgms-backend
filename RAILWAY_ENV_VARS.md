# Railway Environment Variables

**Required for Production Deployment**

## Critical Variables

### 1. SPRING_PROFILES_ACTIVE
```
SPRING_PROFILES_ACTIVE=prod
```
**Purpose**: Activates production Spring profile  
**Required**: YES  
**Set in**: Railway Dashboard → Variables

---

### 2. DATABASE_URL
```
DATABASE_URL=postgresql://user:password@host:port/database
```
**Purpose**: PostgreSQL connection string  
**Required**: YES (auto-provided by Railway PostgreSQL plugin)  
**Set in**: Automatically set when you add PostgreSQL service

---

### 3. APP_SECURITY_JWT_SECRET
```
APP_SECURITY_JWT_SECRET=your-super-secret-jwt-key-min-256-bits
```
**Purpose**: JWT token signing secret (256+ bits)  
**Required**: YES  
**Set in**: Railway Dashboard → Variables  
**Security**: Use a cryptographically secure random string

**Generate secure secret:**
```bash
# Option 1: OpenSSL
openssl rand -base64 64

# Option 2: Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

---

### 4. JWT_ACCESS_TTL_SECONDS
```
JWT_ACCESS_TTL_SECONDS=86400
```
**Purpose**: JWT token expiration (in seconds)  
**Required**: NO (defaults to 86400 = 24 hours)  
**Set in**: Railway Dashboard → Variables  
**Recommended**: 86400 (24h) for production

---

## Optional Variables

### 5. PORT
```
PORT=8080
```
**Purpose**: HTTP server port  
**Required**: NO (Railway auto-sets this)  
**Set in**: Auto-managed by Railway

---

### 6. CORS_ALLOWED_ORIGINS
```
CORS_ALLOWED_ORIGINS=https://zplusesecurity.com,https://*.netlify.app
```
**Purpose**: Override default CORS origins  
**Required**: NO (defaults configured in application-prod.yml)  
**Set in**: Railway Dashboard → Variables (only if you need custom origins)

---

## Verification Checklist

Before deploying to Railway:

- [ ] SPRING_PROFILES_ACTIVE=prod is set
- [ ] DATABASE_URL is available (via PostgreSQL plugin)
- [ ] APP_SECURITY_JWT_SECRET is set (64+ character random string)
- [ ] JWT_ACCESS_TTL_SECONDS is set (or using default 86400)
- [ ] Railway PostgreSQL service is linked to backend service

---

## How to Set Variables in Railway

1. Open Railway Dashboard
2. Select your backend service
3. Click **"Variables"** tab
4. Click **"New Variable"**
5. Add each variable:
   - Variable: `SPRING_PROFILES_ACTIVE`
   - Value: `prod`
6. Click **"Add"**
7. Repeat for other variables

---

## Production Startup Validation

When backend starts successfully, you should see:

```
✓ Database connection successful
✓ Database: postgresql://...
✓ Validating required database tables...
✓ Table exists: users
✓ Table exists: guards
✓ Table exists: client_accounts
✓ Table exists: sites
✓ JWT secret configured
✓ All startup validations passed
✓ SGMS Backend is ready for production traffic
```

**If startup fails**, check Railway logs:
```bash
railway logs
```

Look for errors related to:
- Missing DATABASE_URL → Add PostgreSQL service
- Missing JWT_SECRET → Set APP_SECURITY_JWT_SECRET variable
- Table doesn't exist → Run database migrations from PHASE2_DEPLOYMENT_GUIDE.md

---

## Security Best Practices

1. **Never commit secrets to Git**
2. **Use Railway's variable encryption** (automatic)
3. **Rotate JWT_SECRET periodically** (every 90 days)
4. **Use different JWT_SECRET for prod vs staging**
5. **Keep DATABASE_URL private** (never expose in logs)

---

**Last Updated**: February 2026
