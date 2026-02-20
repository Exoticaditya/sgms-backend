# SGMS Backend Refactoring - Quick Start Guide

**🎯 Goal:** Transform backend into standardized ERP architecture  
**⏱️ Time:** 15 hours across 4 days  
**🛡️ Safety:** NO breaking changes to APIs or database

---

## 📋 PRE-EXECUTION CHECKLIST

```bash
# 1. Create refactor branch
git checkout -b refactor/backend-restructure

# 2. Verify current state compiles
cd backend
mvn clean compile

# 3. Document current endpoints
curl http://localhost:8080/api/guards
curl http://localhost:8080/api/clients
curl http://localhost:8080/api/sites
# Save responses for comparison

# 4. Backup current codebase
git commit -am "Pre-refactor backup - working state"
```

---

## 🚀 EXECUTION SEQUENCE

### DAY 1: Common Infrastructure (3 hours)

#### **TASK 1.1: Create Mapper Framework** (30 min)

```bash
# Create directories
mkdir -p src/main/java/com/sgms/common/mapper

# Create base mapper interface
```

**File: `common/mapper/EntityMapper.java`**
```java
package com.sgms.common.mapper;

import java.util.List;
import java.util.stream.Collectors;

public interface EntityMapper<E, D> {
    D toResponse(E entity);
    
    default List<D> toResponseList(List<E> entities) {
        return entities.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
```

**Test:** `mvn compile` → Should succeed

---

#### **TASK 1.2: Create Custom Exceptions** (45 min)

```bash
mkdir -p src/main/java/com/sgms/common/exception
```

**File: `common/exception/ResourceNotFoundException.java`**
```java
package com.sgms.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() { return resourceName; }
    public Object getResourceId() { return resourceId; }
}
```

**File: `common/exception/DuplicateResourceException.java`**
```java
package com.sgms.common.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

**File: `common/exception/InvalidRequestException.java`**
```java
package com.sgms.common.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
```

**Test:** `mvn compile` → Should succeed

---

#### **TASK 1.3: Move Exception Handler** (30 min)

```bash
# Move ErrorResponse and GlobalExceptionHandler
mv src/main/java/com/sgms/exception/ErrorResponse.java src/main/java/com/sgms/common/exception/
mv src/main/java/com/sgms/exception/GlobalExceptionHandler.java src/main/java/com/sgms/common/exception/

# Update package declarations
# In ErrorResponse.java: Change package to com.sgms.common.exception
# In GlobalExceptionHandler.java: Change package to com.sgms.common.exception
```

**File: `common/exception/GlobalExceptionHandler.java`** (add handlers)
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFound(
    ResourceNotFoundException ex, 
    WebRequest request
) {
    ErrorResponse error = new ErrorResponse(
        "NOT_FOUND",
        ex.getMessage(),
        404,
        request.getDescription(false).replace("uri=", "")
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(DuplicateResourceException.class)
public ResponseEntity<ErrorResponse> handleDuplicateResource(
    DuplicateResourceException ex, 
    WebRequest request
) {
    ErrorResponse error = new ErrorResponse(
        "CONFLICT",
        ex.getMessage(),
        409,
        request.getDescription(false).replace("uri=", "")
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler(InvalidRequestException.class)
public ResponseEntity<ErrorResponse> handleInvalidRequest(
    InvalidRequestException ex, 
    WebRequest request
) {
    ErrorResponse error = new ErrorResponse(
        "BAD_REQUEST",
        ex.getMessage(),
        400,
        request.getDescription(false).replace("uri=", "")
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

**Update imports** in all controllers that import ErrorResponse or GlobalExceptionHandler

**Test:** `mvn clean compile` → Should succeed

---

#### **TASK 1.4: Create Response Package** (15 min)

```bash
mkdir -p src/main/java/com/sgms/common/response
mv src/main/java/com/sgms/common/ApiResponse.java src/main/java/com/sgms/common/response/
```

**Update package:** Change to `package com.sgms.common.response;`

**Update all imports:** Find/replace `com.sgms.common.ApiResponse` → `com.sgms.common.response.ApiResponse`

**Test:** `mvn clean compile` → Should succeed

---

#### **TASK 1.5: Move Security Classes** (1 hour)

```bash
mkdir -p src/main/java/com/sgms/common/security
mv src/main/java/com/sgms/security/* src/main/java/com/sgms/common/security/
```

**Update package declarations** in all moved files:
- SecurityConfig.java
- JwtService.java
- JwtProperties.java
- JwtAuthenticationFilter.java
- CustomUserDetailsService.java
- UserPrincipal.java
- RestAuthenticationEntryPoint.java

**Update imports** across entire codebase (search for `com.sgms.security.`)

**Test:** `mvn clean compile` → Fix any import errors

**Delete old directory:**
```bash
rm -rf src/main/java/com/sgms/security
```

---

#### **TASK 1.6: Move Config Classes** (30 min)

```bash
mkdir -p src/main/java/com/sgms/common/config
mv src/main/java/com/sgms/config/* src/main/java/com/sgms/common/config/
```

**Update package declarations** in all config files

**Update imports** if any

**Test:** `mvn clean compile`

**Delete old directory:**
```bash
rm -rf src/main/java/com/sgms/config
```

---

### DAY 2: User & Guard Modules (4 hours)

#### **TASK 2.1: Create User Module Service** (2 hours)

```bash
mkdir -p src/main/java/com/sgms/user/dto
mkdir -p src/main/java/com/sgms/user/service
mkdir -p src/main/java/com/sgms/user/mapper
```

**File: `user/dto/CreateUserRequest.java`**
```java
package com.sgms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class CreateUserRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8)
    private String password;
    
    @NotBlank
    private String fullName;
    
    private String phone;
    
    private Set<String> roles;
    
    // Getters and setters
}
```

**File: `user/dto/UpdateUserRequest.java`** (similar structure, optional password)

**File: `user/service/UserService.java`** (interface)
```java
package com.sgms.user.service;

import com.sgms.user.dto.*;
import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}
```

**File: `user/service/UserServiceImpl.java`** (implementation)

**File: `user/mapper/UserMapper.java`**
```java
package com.sgms.user.mapper;

import com.sgms.common.mapper.EntityMapper;
import com.sgms.user.UserEntity;
import com.sgms.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements EntityMapper<UserEntity, UserResponse> {
    @Override
    public UserResponse toResponse(UserEntity entity) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setRoles(entity.getRoles().stream()
            .map(RoleEntity::getName)
            .toList());
        return response;
    }
}
```

**File: `user/UserController.java`**
```java
package com.sgms.user;

import com.sgms.common.response.ApiResponse;
import com.sgms.user.dto.*;
import com.sgms.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null, "User deleted successfully");
    }
}
```

**Test:**
```bash
mvn clean compile
curl http://localhost:8080/api/users
```

---

#### **TASK 2.2: Refactor Guard Module** (2 hours)

```bash
mkdir -p src/main/java/com/sgms/guard/entity
mkdir -p src/main/java/com/sgms/guard/repository
mkdir -p src/main/java/com/sgms/guard/service
mkdir -p src/main/java/com/sgms/guard/mapper

# Move files
mv src/main/java/com/sgms/guard/GuardEntity.java src/main/java/com/sgms/guard/entity/
mv src/main/java/com/sgms/guard/GuardRepository.java src/main/java/com/sgms/guard/repository/
```

**Update package declarations** in moved files

**File: `guard/service/GuardService.java`** (create interface)
```java
package com.sgms.guard.service;

import com.sgms.guard.dto.*;
import com.sgms.common.security.UserPrincipal;
import java.util.List;

public interface GuardService {
    GuardResponse createGuard(CreateGuardRequest request);
    List<GuardResponse> getAllGuards(UserPrincipal principal);
    GuardResponse getGuardById(Long id, UserPrincipal principal);
    GuardResponse updateGuard(Long id, CreateGuardRequest request, UserPrincipal principal);
    void deleteGuard(Long id);
}
```

**Rename current GuardService.java:**
```bash
mv src/main/java/com/sgms/guard/GuardService.java src/main/java/com/sgms/guard/service/GuardServiceImpl.java
```

**Update GuardServiceImpl:**
- Change class name to `GuardServiceImpl`
- Add `implements GuardService`
- Move to `service` subdirectory
- Update package to `com.sgms.guard.service`
- Add `@Override` to all methods

**File: `guard/mapper/GuardMapper.java`**
```java
package com.sgms.guard.mapper;

import com.sgms.common.mapper.EntityMapper;
import com.sgms.guard.entity.GuardEntity;
import com.sgms.guard.dto.GuardResponse;
import org.springframework.stereotype.Component;

@Component
public class GuardMapper implements EntityMapper<GuardEntity, GuardResponse> {
    @Override
    public GuardResponse toResponse(GuardEntity entity) {
        GuardResponse response = new GuardResponse();
        response.setId(entity.getId());
        response.setEmployeeCode(entity.getEmployeeCode());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setPhone(entity.getPhone());
        response.setStatus(entity.getStatus());
        response.setHireDate(entity.getHireDate());
        response.setBaseSalary(entity.getBaseSalary());
        response.setPerDayRate(entity.getPerDayRate());
        response.setOvertimeRate(entity.getOvertimeRate());
        
        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
            response.setEmail(entity.getUser().getEmail());
        }
        
        if (entity.getSupervisor() != null) {
            response.setSupervisorId(entity.getSupervisor().getId());
            response.setSupervisorName(entity.getSupervisor().getFullName());
        }
        
        return response;
    }
}
```

**Update GuardServiceImpl:**
- Inject `GuardMapper`
- Replace `mapToResponse()` calls with `guardMapper.toResponse()`
- Delete private `mapToResponse()` method

**Update GuardController:**
- Change import to `com.sgms.guard.service.GuardService`
- No other changes needed (uses interface)

**Test:**
```bash
mvn clean compile
curl http://localhost:8080/api/guards
```

---

### DAY 3: Client & Site Modules (5 hours)

#### **TASK 3.1: Refactor Client Module** (1.5 hours)

```bash
mkdir -p src/main/java/com/sgms/client/entity
mkdir -p src/main/java/com/sgms/client/repository
mkdir -p src/main/java/com/sgms/client/service
mkdir -p src/main/java/com/sgms/client/mapper

# Move files
mv src/main/java/com/sgms/client/ClientAccountEntity.java src/main/java/com/sgms/client/entity/
mv src/main/java/com/sgms/client/ClientAccountRepository.java src/main/java/com/sgms/client/repository/
```

**Follow same pattern as Guard module:**
1. Create `ClientService` interface
2. Rename `ClientAccountService` to `ClientServiceImpl`
3. Create `ClientMapper`
4. Rename `ClientAccountController` to `ClientController`
5. Update all imports

**Test:**
```bash
mvn clean compile
curl http://localhost:8080/api/clients
```

---

#### **TASK 3.2: Refactor Site Module** (3.5 hours)

Site module is complex (4 sub-domains). Apply same pattern to each:

1. **Sites**
   - Create `site/entity/`, move `SiteEntity`
   - Create `site/service/SiteService` interface
   - Rename `SiteService` to `SiteServiceImpl`
   - Create `SiteMapper`

2. **Site Posts**
   - Move `SitePostEntity` to `entity/`
   - Create `SitePostService` interface
   - Rename to `SitePostServiceImpl`
   - Create `SitePostMapper`

3. **Site Access**
   - Move `ClientSiteAccessEntity` to `entity/`
   - Create `ClientSiteAccessService` interface
   - Rename to `ClientSiteAccessServiceImpl`
   - Create `ClientSiteAccessMapper`

4. **Supervisor Sites**
   - Move `SupervisorSiteMappingEntity` to `entity/`
   - Create `SupervisorSiteService` interface
   - Rename to `SupervisorSiteServiceImpl`
   - Create `SupervisorSiteMapper`

**Test each sub-domain:**
```bash
curl http://localhost:8080/api/sites
curl http://localhost:8080/api/sites/posts
curl http://localhost:8080/api/sites/access
curl http://localhost:8080/api/sites/supervisors
```

---

### DAY 4: Assignment Module & Cleanup (3 hours)

#### **TASK 4.1: Refactor Assignment Module** (1.5 hours)

```bash
mkdir -p src/main/java/com/sgms/assignment/entity
mkdir -p src/main/java/com/sgms/assignment/repository
mkdir -p src/main/java/com/sgms/assignment/service
mkdir -p src/main/java/com/sgms/assignment/mapper

# Move entities
mv src/main/java/com/sgms/assignment/GuardAssignmentEntity.java src/main/java/com/sgms/assignment/entity/
mv src/main/java/com/sgms/assignment/ShiftTypeEntity.java src/main/java/com/sgms/assignment/entity/

# Move repositories
mv src/main/java/com/sgms/assignment/GuardAssignmentRepository.java src/main/java/com/sgms/assignment/repository/
mv src/main/java/com/sgms/assignment/ShiftTypeRepository.java src/main/java/com/sgms/assignment/repository/
```

**Create interfaces:**
- `GuardAssignmentService`
- `ShiftTypeService`

**Create mappers:**
- `GuardAssignmentMapper`
- `ShiftTypeMapper`

**Rename implementations:**
- `GuardAssignmentServiceImpl`
- `ShiftTypeServiceImpl`

**Test:**
```bash
curl http://localhost:8080/api/assignments
curl http://localhost:8080/api/assignments/shift-types
```

---

#### **TASK 4.2: Replace ResponseStatusException** (1 hour)

**Search and replace across all services:**

```java
// OLD
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Guard not found");

// NEW
throw new ResourceNotFoundException("Guard", id);
```

```java
// OLD
throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

// NEW
throw new DuplicateResourceException("Email already exists");
```

```java
// OLD
throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");

// NEW
throw new InvalidRequestException("Invalid request");
```

**Test all endpoints:**
```bash
# Test 404
curl http://localhost:8080/api/guards/99999

# Test 409 (duplicate)
# Create client with same name twice

# Test 400 (validation)
# Send invalid request body
```

---

#### **TASK 4.3: Cleanup** (30 min)

```bash
# Delete empty directories
rm -rf src/main/java/com/sgms/exception
rm -rf src/main/java/com/sgms/security
rm -rf src/main/java/com/sgms/config

# Verify no orphaned files
find src/main/java/com/sgms -type f -name "*.java" | grep -v "/target/"
```

---

## ✅ FINAL VALIDATION

```bash
# 1. Clean build
mvn clean install

# 2. Test all endpoints (use PHASE_A_TESTING_GUIDE.md)
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"email":"admin@example.com","password":"password"}'

curl http://localhost:8080/api/guards -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/clients -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/sites -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/assignments -H "Authorization: Bearer <token>"

# 3. Check application startup
mvn spring-boot:run

# 4. Verify no errors in logs
```

---

## 📂 FINAL DIRECTORY STRUCTURE

```
com.sgms
 ├── common/
 │    ├── config/
 │    ├── exception/
 │    ├── mapper/
 │    ├── response/
 │    ├── security/
 │    └── util/
 ├── auth/
 │    ├── dto/
 │    ├── AuthController.java
 │    └── AuthService.java
 ├── user/
 │    ├── dto/
 │    ├── entity/
 │    ├── repository/
 │    ├── service/
 │    ├── mapper/
 │    └── UserController.java
 ├── client/
 │    ├── dto/
 │    ├── entity/
 │    ├── repository/
 │    ├── service/
 │    ├── mapper/
 │    └── ClientController.java
 ├── site/
 │    ├── dto/
 │    ├── entity/
 │    ├── repository/
 │    ├── service/
 │    ├── mapper/
 │    └── [Controllers]
 ├── guard/
 │    ├── dto/
 │    ├── entity/
 │    ├── repository/
 │    ├── service/
 │    ├── mapper/
 │    └── GuardController.java
 ├── assignment/
 │    ├── dto/
 │    ├── entity/
 │    ├── repository/
 │    ├── service/
 │    ├── mapper/
 │    └── GuardAssignmentController.java
 ├── seed/
 └── SgmsBackendApplication.java
```

---

## 🎯 COMMIT STRATEGY

```bash
# After each major task
git add .
git commit -m "Refactor: <task-name> - <brief description>"

# Examples:
git commit -m "Refactor: Create common mapper framework and custom exceptions"
git commit -m "Refactor: Move security and config to common package"
git commit -m "Refactor: Add User module service layer"
git commit -m "Refactor: Convert Guard module to interface-based services"
git commit -m "Refactor: Convert Client module to interface-based services"
git commit -m "Refactor: Reorganize Site module with service interfaces"
git commit -m "Refactor: Reorganize Assignment module with mappers"
git commit -m "Refactor: Replace ResponseStatusException with custom exceptions"
```

---

## 🆘 TROUBLESHOOTING

### Build Fails After Moving Files
```bash
# Check for incorrect package declarations
grep -r "package com.sgms.security" src/main/java/com/sgms/common/security/

# Fix import statements
# Use IDE "Optimize Imports" or search/replace
```

### Circular Dependency Errors
- Remove `@Autowired` if found
- Ensure constructor injection
- Check mapper dependencies

### Application Won't Start
```bash
# Check for missing @Component/@Service annotations
# Verify Spring can scan all packages
# Check application.yml configuration intact
```

---

**Status:** ✅ READY TO EXECUTE  
**Next:** Start with Day 1, Task 1.1

