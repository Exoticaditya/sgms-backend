# ================================================================
# PHASE 1: PROJECT SANITIZATION COMMANDS
# Execute these in VS Code PowerShell terminal
# ================================================================

# Navigate to backend directory
cd C:\Zpluse-Security\backend

# 1. DELETE Maven build artifacts
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue

# 2. DELETE old migration files
Remove-Item -Force src\main\resources\db\migration\V0__client_and_sites.sql -ErrorAction SilentlyContinue
Remove-Item -Force src\main\resources\db\migration\V1__initial_schema.sql -ErrorAction SilentlyContinue
Remove-Item -Force src\main\resources\db\migration\V2__phase3_site_posts.sql -ErrorAction SilentlyContinue
Remove-Item -Force src\main\resources\db\migration\V3__phase_a_guard_deployment.sql -ErrorAction SilentlyContinue
Remove-Item -Force src\main\resources\db\migration\V4__phase_b_attendance_tracking.sql -ErrorAction SilentlyContinue

# 3. DELETE seed and schema files (if exist)
Remove-Item -Force src\main\resources\schema.sql -ErrorAction SilentlyContinue
Remove-Item -Force src\main\resources\data.sql -ErrorAction SilentlyContinue
Remove-Item -Force src\main\resources\import.sql -ErrorAction SilentlyContinue
Remove-Item -Force SEED_DATA_COMPLETE.sql -ErrorAction SilentlyContinue

# 4. DELETE diagnostic files
Remove-Item -Force DB_INTROSPECTION_QUERIES.sql -ErrorAction SilentlyContinue
Remove-Item -Force PHASE1_SCHEMA_ANALYSIS_REPORT.md -ErrorAction SilentlyContinue
Remove-Item -Force PHASE2_BACKEND_AUDIT_REPORT.md -ErrorAction SilentlyContinue
Remove-Item -Force SYSTEM_ARCHITECT_REPORT.md -ErrorAction SilentlyContinue
Remove-Item -Force QUICK_START.md -ErrorAction SilentlyContinue
Remove-Item -Force CLEANUP_CHECKLIST.txt -ErrorAction SilentlyContinue

# 5. Verify migration directory is empty
Get-ChildItem src\main\resources\db\migration -ErrorAction SilentlyContinue

# Expected output: Empty or directory doesn't exist
# If migrations exist, something went wrong - STOP and investigate

Write-Host "✓ Project sanitization complete" -ForegroundColor Green
Write-Host "Migration directory should be empty or non-existent" -ForegroundColor Yellow
