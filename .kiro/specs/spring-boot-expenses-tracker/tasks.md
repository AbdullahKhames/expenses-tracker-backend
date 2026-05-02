# Implementation Plan: Spring Boot Expenses Tracker Migration

## Overview

Migrate the existing Jakarta EE (Jersey/GlassFish) expenses tracker to Spring Boot 3.4.x with Java 21. The implementation replaces the WAR/GlassFish deployment with an executable Spring Boot JAR, swaps manual JPA/DataSource configuration for Spring Data JPA auto-configuration, replaces Jersey filters with Spring Security + JWT, and introduces Redis for caching and token management. All existing REST API contracts are preserved.

## Tasks

- [x] 1. Project setup and Spring Boot bootstrap
  - [x] 1.1 Replace pom.xml with Spring Boot parent and dependencies
    - Set `spring-boot-starter-parent` 3.4.x as parent POM
    - Change packaging from `war` to `jar`
    - Add Spring Boot starters: web, data-jpa, security, data-redis, validation, cache, actuator
    - Add PostgreSQL driver, jjwt 0.12.x, MapStruct 1.6.x, Lombok 1.18.x, springdoc-openapi 2.7.x
    - Add test dependencies: spring-boot-starter-test, spring-security-test, testcontainers (postgresql, junit-jupiter), jqwik
    - Configure maven-compiler-plugin with Lombok + MapStruct annotation processors
    - Remove all Jersey, GlassFish, Jakarta EE, manual Hibernate, commons-dbcp2, jbcrypt, and Gson dependencies
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 18.1, 18.2, 18.3_

  - [x] 1.2 Create Spring Boot main application class and application.yml
    - Create `ExpensesTrackerApplication.java` with `@SpringBootApplication` and `@EnableJpaAuditing`
    - Create `src/main/resources/application.yml` with all externalized configuration from the design document
    - Configure datasource (PostgreSQL, HikariCP pool), JPA (Hibernate, PostgreSQL dialect, ddl-auto, open-in-view: false), Redis, Jackson, server port/context-path, JWT settings, whitelist, and cache TTLs
    - All values must use `${ENV_VAR:default}` pattern for externalization
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 18.1, 18.2, 18.3, 18.4_

  - [x] 1.3 Remove legacy Jakarta EE configuration classes
    - Delete `DataSourceConfig.java`, `EntityManagerConfig.java`, `MyPersistenceUnit.java`, `ObjectMapperConfig.java`
    - Delete `BasicSecurityFilter.java`, `CorsFilter.java`, and all validator classes under `config/filters/validators/`
    - Delete `AroundAdvice.java`, `RepoAdvice.java`
    - Delete `MyResource.java`
    - _Requirements: 17.1_

- [x] 2. Base infrastructure: models, enums, utilities, and response structure
  - [x] 2.1 Create BaseEntity mapped superclass
    - Create `BaseEntity` as `@MappedSuperclass` with `@EntityListeners(AuditingEntityListener.class)`
    - Add fields: `refNo` (UUID, unique, updatable=false), `createdAt` (@CreatedDate), `updatedAt` (@LastModifiedDate), `deleted` (boolean, default false)
    - Use Lombok `@Getter @Setter`
    - _Requirements: 15.1, 15.2, 15.3, 19.1, 19.2, 19.3_

  - [x] 2.2 Create ResponseDto, Page model, and response builder utility
    - Create `ResponseDto` with fields: message, status, code, data, date (formatted "yyyy-MM-dd HH:mm:ss")
    - Use `@JsonInclude(JsonInclude.Include.NON_NULL)` to exclude null fields
    - Create custom `Page<T>` model with: content, pageNumber, pageSize, totalElements, totalPages, hasNext, hasPrevious
    - Create `ResponseDtoBuilder` utility with static methods: `getCreateResponse`, `getFetchResponse`, `getFetchAllResponse`, `getUpdateResponse`, `getDeleteResponse`, `getErrorResponse`
    - Define response codes: 800 (fetch), 801 (create), 802 (update), 804 (error), 805 (delete), 810 (auth error)
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [x] 2.3 Create enums and shared types
    - Create `SortDirection` enum with ASC, DESC and a `fromString()` factory method
    - Create `BudgetType` enum (ENTERTAINMENT, SAVINGS, BILLS, ALLOWANCE, MOM, MISC, DONATION, EXTERNAL, DEFAULT)
    - Create `AmountType` enum (DEBIT, CREDIT)
    - Create `EntityType` enum for association routing (ACCOUNT, BUDGET, CATEGORY, SUB_CATEGORY, EXPENSE, TRANSACTION, CUSTOMER, BUDGET_TRANSFER)
    - Create `CrudService<REQ, UPD, ENTITY>` generic interface with: create, get, getEntity, update, delete, getAllEntities, getEntities
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1_

- [x] 3. JPA entities and Spring Data repositories
  - [x] 3.1 Create User and Role entities
    - Create `User` entity extending `BaseEntity`, implementing `UserDetails`
    - Fields: id, fullName, email (unique), age, password (@JsonIgnore), verified, loggedIn, deviceId (unique), customer (OneToOne), tokens (OneToMany), roles (ManyToMany with join table `user_roles`)
    - Implement `UserDetails` methods: `getAuthorities()` returns roles, `getUsername()` returns email
    - Create `Role` entity with id, name fields
    - Create `Token` entity with id, token, tokenType, revoked, expired, user (ManyToOne)
    - _Requirements: 4.1, 4.5, 6.1_

  - [x] 3.2 Create Customer entity
    - Create `Customer` entity extending `BaseEntity`
    - Fields: id, user (OneToOne), accounts (ManyToMany with join table `customer_accounts`), budgets (OneToMany), categories (ManyToMany with join table `customer_categories`), subCategories (ManyToMany with join table `customer_sub_categories`), expenses (OneToMany), transactions (OneToMany), budgetTransfers (OneToMany)
    - Use appropriate cascade types and fetch = LAZY on all collections
    - _Requirements: 9.1, 9.4, 9.5_

  - [x] 3.3 Create Account, Budget, and BudgetAmount entities
    - Create `Account` entity extending `BaseEntity` with: id, name, details, budgets (OneToMany), customers (ManyToMany mappedBy)
    - Create `Budget` entity extending `BaseEntity` with: id, name, details, amount, budgetType (enum), defaultReceiver, defaultSender, customer (ManyToOne), account (ManyToOne)
    - Create `BudgetAmount` entity extending `BaseEntity` with: id, budget (ManyToOne), amount, trans, transaction (ManyToOne), amountType (enum)
    - _Requirements: 7.1, 10.1, 10.2, 10.3_

  - [x] 3.4 Create Category, SubCategory, and Expense entities
    - Create `Category` entity extending `BaseEntity` with: id, name, details, customers (ManyToMany mappedBy)
    - Create `SubCategory` entity extending `BaseEntity` with: id, name, details, category (ManyToOne), customers (ManyToMany mappedBy)
    - Create `Expense` entity extending `BaseEntity` with: id, name, details, amount, customer (ManyToOne)
    - _Requirements: 7.1_

  - [x] 3.5 Create Transaction and BudgetTransfer entities
    - Create `Transaction` entity extending `BaseEntity` with: id, name, details, amount, budgetAmounts (OneToMany with orphanRemoval), customer (ManyToOne), expense (OneToOne with cascade)
    - Create `BudgetTransfer` entity extending `BaseEntity` with: id, name, details, amount, lending, customer (ManyToOne), senderBudgetAmount (OneToOne), receiverBudgetAmounts (OneToMany with orphanRemoval)
    - _Requirements: 10.1, 11.1_

  - [x] 3.6 Create Spring Data JPA repositories for all entities
    - Create `UserRepository` with: `findByEmail`, `findByRefNoAndDeletedFalse`, `findAllByDeletedFalse(Pageable)`
    - Create `CustomerRepository` with: `findByRefNoAndDeletedFalse`, `findByUserEmail`
    - Create `AccountRepository` with: `findByRefNoAndDeletedFalse`, `findByNameContainingIgnoreCaseAndDeletedFalse`, `findAllByDeletedFalse(Pageable)`, `findAllByRefNoIn`
    - Create repositories for Budget, BudgetAmount, Category, SubCategory, Expense, Transaction, BudgetTransfer, Token, Role — each with `findByRefNoAndDeletedFalse` and `findAllByDeletedFalse(Pageable)` where applicable
    - All repositories extend `JpaRepository<Entity, Long>`
    - _Requirements: 7.2, 7.3, 7.6, 8.5, 16.2_

- [x] 4. Checkpoint - Verify entities and repositories compile
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. DTOs and MapStruct mappers
  - [x] 5.1 Create request and response DTOs for all entities
    - For each entity (Account, Budget, BudgetTransfer, Category, SubCategory, Expense, Transaction, Customer, User): create `XxxReqDto`, `XxxUpdateDto`, and `XxxRespDto`
    - Request DTOs include Jakarta Bean Validation annotations (@NotNull, @NotBlank, @Size, etc.)
    - Response DTOs exclude database IDs (Long id) and sensitive fields (password)
    - Create `LoginRequest` DTO with email, password, deviceId
    - Create `AuthResponse` DTO with accessToken, refreshToken, user details
    - Create `AssociationResponse` DTO with success and error maps
    - _Requirements: 13.1, 14.2, 20.1, 20.2, 20.3_

  - [x] 5.2 Create MapStruct mappers for all entities
    - Create mapper interfaces annotated with `@Mapper(componentModel = "spring")`
    - For each entity: `reqDtoToEntity`, `entityToRespDto`, `updateDtoToEntity` (with `@MappingTarget`)
    - Add `entityPageToRespDtoPage` helper method for pagination mapping (converts Spring `Page<Entity>` to custom `Page<RespDto>`)
    - Ensure mappers ignore system-managed fields (refNo, createdAt, updatedAt, deleted) on request-to-entity mapping
    - Ensure mappers exclude id and password fields on entity-to-response mapping
    - _Requirements: 20.1, 20.2, 20.3_

  - [ ]* 5.3 Write property tests for DTO mapping
    - **Property 21: DTO mapping does not expose sensitive data**
    - **Property 22: Request DTO mapping preserves system fields**
    - **Validates: Requirements 20.2, 20.3**

- [x] 6. Service layer implementation
  - [x] 6.1 Implement AccountService with CRUD and pagination
    - Create `AccountService` interface extending `CrudService<AccountReqDto, AccountUpdateDto, Account>`
    - Create `AccountServiceImpl` with `@Service`, `@Transactional`, `@RequiredArgsConstructor`
    - Implement create (code 801), get by refNo (code 800), update (code 802), soft-delete (code 805), getAllEntities with pagination (code 800)
    - Add `getAccountByName` and `getAccountBudgets` methods
    - Add `addAssociation` and `removeAssociation` for budget associations
    - Use `@Cacheable` on get, `@CacheEvict` on create/update/delete
    - Convert 1-based page number to 0-based for Spring Data; default invalid sortBy to "id"
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1, 8.2, 8.3, 8.4, 8.5, 12.1, 12.2, 12.3, 16.1_

  - [x] 6.2 Implement BudgetService, CategoryService, SubCategoryService, and ExpenseService
    - Follow the same CRUD + pagination pattern as AccountService for each entity
    - BudgetService: include budget amount adjustment logic for transactions
    - CategoryService: include sub-category association management
    - Each service uses its respective repository, mapper, and cache annotations
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1, 12.1, 12.2, 12.3_

  - [x] 6.3 Implement TransactionService with budget balance adjustments
    - Create TransactionService implementing CrudService
    - On create: persist transaction with cascaded expense and budget amounts
    - For each BudgetAmount: DEBIT → subtract from budget.amount; CREDIT → add to budget.amount
    - Add transaction to current customer's transactions collection
    - Invalidate related caches on mutation
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [x] 6.4 Implement BudgetTransferService
    - Create BudgetTransferService implementing CrudService
    - On create: persist transfer with sender and receiver budget amounts
    - Debit sender budget, credit each receiver budget
    - Add transfer to current customer's budgetTransfers collection
    - _Requirements: 11.1, 11.2_

  - [x] 6.5 Implement CustomerService and UserService
    - CustomerService: CRUD operations for customer entity, resolve current authenticated user's customer
    - UserService: implement `UserDetailsService` for Spring Security, `loadUserByUsername(email)`
    - UserService: registration, profile retrieval, user management operations
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 9.5_

  - [ ]* 6.6 Write property tests for service layer
    - **Property 9: Entity CRUD round-trip** — create then fetch returns equivalent data
    - **Property 10: Soft-delete exclusion** — deleted entities excluded from queries
    - **Property 11: Pagination page size invariant** — content.size() <= per_page
    - **Property 12: Invalid sort field defaults to id**
    - **Property 15: Transaction budget balance adjustment** — DEBIT subtracts, CREDIT adds
    - **Property 16: Budget transfer conservation** — sender debit equals sum of receiver credits
    - **Property 17: RefNo uniqueness and immutability** — N creations produce N distinct refNos
    - **Property 18: Entity timestamp consistency** — createdAt == updatedAt on create; updatedAt grows on update
    - **Validates: Requirements 7.1, 7.2, 7.5, 7.6, 8.2, 8.3, 10.2, 10.3, 11.2, 15.1, 15.2, 15.3, 16.1, 16.2, 19.1, 19.2, 19.3**

- [ ] 7. Association management
  - [x] 7.1 Implement AssociationManager service
    - Create `AssociationManager` with `@Service`, `@Transactional`
    - Implement `addAssociation(entityRefNo, entityType, associationRefNos, associationType)` routing to correct service
    - Implement `removeAssociation` with same routing pattern
    - Implement `addDtoAssociation` for DTO-based association creation
    - Build success/error maps per refNo: "was added successfully", "entity already contains this association", "no entity corresponds to this ref no"
    - When entityRefNo is null and entityType is CUSTOMER, resolve to current authenticated user's customer
    - Create `CollectionAdder` and `CollectionRemover` interfaces for per-entity-type association logic
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ]* 7.2 Write property tests for association management
    - **Property 13: Association addition idempotency** — adding existing association goes to error map, collection size unchanged
    - **Property 14: Non-existent association refNo goes to error map**
    - **Validates: Requirements 9.2, 9.3**

- [x] 8. Checkpoint - Verify service layer and associations compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. REST controllers
  - [x] 9.1 Implement AccountController and BudgetController
    - Create `AccountController` with `@RestController`, `@RequestMapping("/api/accounts")`
    - Endpoints: POST (create), GET `/refNo/{refNo}` (get), GET `/name/{name}` (by name), GET `/{refNo}/budgets`, PUT `/{refNo}` (update), DELETE `/{refNo}` (delete), GET (paginated list with page, per_page, sortBy, sortDirection params), PUT `/addAssociation/{accountRefNo}/{budgetRefNo}`, PUT `/removeAssociation/{accountRefNo}/{budgetRefNo}`
    - Create `BudgetController` with equivalent CRUD + pagination endpoints at `/api/budgets`
    - Use `@Valid` on request body parameters for bean validation
    - All methods return `ResponseEntity<ResponseDto>`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.4, 13.1_

  - [x] 9.2 Implement CategoryController, SubCategoryController, and ExpenseController
    - Create controllers at `/api/categories`, `/api/sub-categories`, `/api/expenses`
    - Each with standard CRUD + pagination endpoints following the same pattern
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 13.1_

  - [x] 9.3 Implement TransactionController and BudgetTransferController
    - Create `TransactionController` at `/api/transactions` with CRUD + pagination
    - Create `BudgetTransferController` at `/api/budget-transfers` with CRUD + pagination
    - _Requirements: 10.1, 11.1, 13.1_

  - [x] 9.4 Implement UserController, CustomerController, and AssociationController
    - Create `UserController` at `/api/users` with: POST `/register`, POST `/login`, POST `/logout`, POST `/refreshToken`, PUT `/resetAccount`, PUT `/changeEmail`, PUT `/validateChangeEmail`, PUT `/activate/{token}`
    - Create `CustomerController` at `/api/customers` with CRUD + pagination
    - Create `AssociationController` at `/api/associations` with: PUT `/addAssociation`, PUT `/removeAssociation`, PUT `/addDtoAssociation`
    - _Requirements: 4.1, 4.2, 4.3, 5.1, 9.1, 9.4, 13.1_

- [ ] 10. Security: Spring Security configuration, JWT filter, and auth service
  - [x] 10.1 Implement JwtService for token generation and validation
    - Create `JwtService` with methods: `generateToken(User, extraClaims, expirationMs)`, `generateAccessToken(User)`, `generateRefreshToken(User)`, `extractUsername(token)`, `extractClaim(token, resolver)`, `isTokenValid(token, userDetails)`, `isTokenExpired(token)`
    - Sign tokens with HMAC-SHA256 using the configured secret key from `app.security.jwt.secret-key`
    - Set subject to user email, include extra claims, set expiration from config
    - _Requirements: 6.1, 6.2, 6.3_

  - [ ]* 10.2 Write property test for JWT token round-trip
    - **Property 8: JWT token round-trip** — generate then extract yields original email, claims, and correct expiration
    - **Validates: Requirements 6.1, 6.2**

  - [x] 10.3 Implement AuthService for login, logout, and token management
    - Create `AuthService` with `phoneLogin(LoginRequest)`: validate credentials with BCrypt, revoke previous tokens, generate new access + refresh tokens, update user deviceId and loggedIn flag, persist tokens, return AuthResponse
    - Implement `logout(User)`: revoke all tokens for user in database and Redis
    - Implement `refreshToken(request)`: validate refresh token, generate new access token
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2_

  - [x] 10.4 Implement JwtAuthenticationFilter
    - Create `JwtAuthenticationFilter` extending `OncePerRequestFilter`
    - In `doFilterInternal`: extract Bearer token, validate JWT, check Redis revocation, load UserDetails, validate Device-ID header against user's deviceId, set SecurityContext authentication
    - In `shouldNotFilter`: skip whitelisted endpoints from YAML config
    - On failure: return ResponseDto with code 810
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2_

  - [x] 10.5 Implement SecurityConfig
    - Create `SecurityConfig` with `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`
    - Define `SecurityFilterChain` bean: disable CSRF, configure CORS, set session management to STATELESS
    - Add JWT filter before `UsernamePasswordAuthenticationFilter`
    - Configure endpoint authorization: whitelist public endpoints, restrict admin/customer endpoints by role
    - Define `PasswordEncoder` bean (BCrypt with strength 12)
    - Define `AuthenticationManager` bean
    - _Requirements: 1.1, 1.3, 1.4, 3.1, 3.2, 3.3_

  - [ ]* 10.6 Write property tests for security
    - **Property 1: Valid token grants authentication**
    - **Property 2: Revoked token denies access**
    - **Property 3: Whitelisted endpoints bypass authentication**
    - **Property 4: Device-ID mismatch denies access**
    - **Property 5: Role-based access control**
    - **Property 6: Login revokes previous tokens**
    - **Property 7: Login updates user state**
    - **Validates: Requirements 1.3, 1.4, 1.5, 2.1, 2.2, 3.1, 3.2, 3.3, 4.4, 4.5, 5.1, 5.2**

- [ ] 11. Redis caching configuration
  - [x] 11.1 Implement RedisConfig with cache manager and template
    - Create `RedisConfig` with `@Configuration`, `@EnableCaching`
    - Define `RedisCacheManager` bean with per-entity TTLs from `app.cache.entity-ttls` config (accounts: 300s, categories: 600s, budgets: 120s, default: 300s)
    - Define `RedisTemplate<String, Object>` bean with JSON serialization (Jackson2JsonRedisSerializer)
    - Configure `RedisConnectionFactory` using Spring Boot auto-configuration from `spring.data.redis.*` properties
    - _Requirements: 12.1, 12.2, 12.3, 12.4_

  - [x] 11.2 Add cache annotations to service layer
    - Verify all service implementations use `@Cacheable` on read operations and `@CacheEvict(allEntries = true)` on write operations
    - Ensure token revocation checks use Redis directly via `RedisTemplate`
    - _Requirements: 12.1, 12.2, 12.3_

- [ ] 12. Error handling: GlobalExceptionHandler and custom exceptions
  - [x] 12.1 Create custom exception classes
    - Create `ObjectNotFoundException` (extends RuntimeException) for entity-not-found scenarios
    - Create `GeneralFailureException` for general operation failures
    - Create `AuthenticationException` for auth failures
    - Create `ApiValidationException` for validation errors
    - Create `ErrorCategory` enum (DATABASE_Error, BusinessError, ValidationError)
    - Create `ResponseError` model with errorCategory, errorCode, errorMessage
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

  - [x] 12.2 Implement GlobalExceptionHandler with @RestControllerAdvice
    - Handle `ObjectNotFoundException` → ResponseDto code 804 with ResponseError
    - Handle `MethodArgumentNotValidException` → ResponseDto code 804 with field-level validation errors list
    - Handle `AccessDeniedException` → ResponseDto code 810
    - Handle `GeneralFailureException` → ResponseDto code 804 with failure message
    - Handle `Exception` (catch-all) → ResponseDto code 804 with generic message, no internal details exposed
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

  - [ ]* 12.3 Write property tests for error handling
    - **Property 19: ResponseDto null field exclusion** — null fields not in JSON output
    - **Property 20: Error responses do not expose internals** — no stack traces or class names in generic error responses
    - **Validates: Requirements 13.4, 14.4**

- [x] 13. Checkpoint - Full compilation and integration verification
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Testing setup and integration tests
  - [x] 14.1 Configure test infrastructure
    - Create `src/test/resources/application-test.yml` with Testcontainers-compatible configuration
    - Create a base `AbstractIntegrationTest` class with `@SpringBootTest`, `@Testcontainers`, PostgreSQL and Redis container setup
    - Configure jqwik in `pom.xml` test dependencies and verify property test runner works
    - _Requirements: 18.1, 18.2_

  - [ ]* 14.2 Write integration tests for authentication flow
    - Test full login flow: POST /api/users/login → receive tokens → use token on protected endpoint
    - Test expired token rejection
    - Test revoked token rejection (logout then reuse)
    - Test Device-ID mismatch rejection
    - Test whitelisted endpoint access without token
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 4.1, 4.2, 4.3_

  - [ ]* 14.3 Write integration tests for CRUD and pagination
    - Test create → get → update → delete lifecycle for Account entity
    - Test pagination with various page sizes and sort directions
    - Test soft-delete exclusion from queries
    - Test association add/remove operations
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1, 8.2, 9.1, 9.2, 9.3_

  - [ ]* 14.4 Write integration tests for transactions and budget transfers
    - Test transaction creation with budget balance adjustments
    - Test budget transfer with sender debit and receiver credits
    - _Requirements: 10.1, 10.2, 10.3, 11.1, 11.2_

- [x] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after major milestones
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests validate specific examples and edge cases using JUnit 5 + Mockito
- Integration tests use Testcontainers for PostgreSQL and Redis
- The existing Jakarta EE source code in `src/main/java/name/expenses/` serves as reference for business logic but will be replaced entirely
