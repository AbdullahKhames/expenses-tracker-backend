# Requirements Document

## Introduction

This document defines the requirements for migrating the existing Jakarta EE (Jersey/GlassFish) expenses tracker application to Spring Boot 3.4.x with Java 21 LTS. The system provides a REST API for managing personal expenses, budgets, accounts, categories, and transactions. It uses PostgreSQL for persistence, Redis for caching and token management, and JWT-based authentication with device binding.

## Glossary

- **System**: The Spring Boot expenses tracker application
- **JWT_Filter**: The JWT authentication filter component that intercepts and validates requests
- **Security_Config**: The Spring Security filter chain configuration
- **Association_Manager**: The centralized service for managing entity relationships
- **CRUD_Service**: The generic service interface providing standard create, read, update, delete, and pagination operations
- **Cache_Manager**: The Redis-based caching component
- **Auth_Service**: The authentication service handling login, logout, and token management
- **Token_Service**: The JWT token generation and validation service
- **Global_Exception_Handler**: The centralized exception handling component
- **Repository**: A Spring Data JPA repository interface for database access
- **ResponseDto**: The unified API response envelope containing message, status, code, data, and date fields
- **RefNo**: A UUID string that uniquely identifies an entity across the system
- **Soft_Delete**: The pattern of marking entities as deleted without physical removal
- **Device_ID**: A client-provided header value binding a user session to a specific device
- **Whitelist**: The set of API endpoints that bypass authentication

## Requirements

### Requirement 1: JWT Authentication

**User Story:** As an API consumer, I want all non-public endpoints to require valid JWT authentication, so that only authorized users can access protected resources.

#### Acceptance Criteria

1. WHEN a request targets a non-whitelisted endpoint without an Authorization header, THEN THE JWT_Filter SHALL return a ResponseDto with code 810 and an appropriate error message
2. WHEN a request contains an Authorization header with an expired JWT token, THEN THE JWT_Filter SHALL return a ResponseDto with code 810 indicating token expiration
3. WHEN a request contains a valid, non-expired, non-revoked JWT token, THEN THE JWT_Filter SHALL set the authenticated user in the SecurityContext and allow the request to proceed
4. WHEN a request targets a whitelisted endpoint, THEN THE JWT_Filter SHALL skip authentication validation and allow the request to proceed
5. WHEN a token has been revoked in Redis, THEN THE JWT_Filter SHALL reject the request with code 810 regardless of token signature validity

### Requirement 2: Device-ID Validation

**User Story:** As a security administrator, I want each authenticated request to be validated against the user's registered device, so that stolen tokens cannot be used from unauthorized devices.

#### Acceptance Criteria

1. WHEN an authenticated request contains a Device-ID header that does not match the user's registered deviceId, THEN THE JWT_Filter SHALL return a ResponseDto with code 810 and message "device id mismatch"
2. WHEN an authenticated request contains a Device-ID header matching the user's registered deviceId, THEN THE JWT_Filter SHALL allow the request to proceed

### Requirement 3: Role-Based Authorization

**User Story:** As a system administrator, I want endpoints to be restricted by user role, so that customers cannot access admin functionality and vice versa.

#### Acceptance Criteria

1. WHEN a user with ROLE_CUSTOMER attempts to access an admin-only endpoint, THEN THE Security_Config SHALL return a ResponseDto with code 810 and message "you are not authorized"
2. WHEN a user with ROLE_ADMIN attempts to access a customer-only endpoint, THEN THE Security_Config SHALL allow the request based on role hierarchy (ROLE_ADMIN > ROLE_CUSTOMER)
3. WHEN a user with the appropriate role accesses a role-restricted endpoint, THEN THE Security_Config SHALL allow the request to proceed

### Requirement 4: User Login

**User Story:** As a user, I want to authenticate with my email and password, so that I can obtain access and refresh tokens for API usage.

#### Acceptance Criteria

1. WHEN a valid login request is submitted with correct email, password, and deviceId, THEN THE Auth_Service SHALL return a ResponseDto with code 801 containing accessToken, refreshToken, and user details
2. WHEN a login request is submitted with an incorrect password, THEN THE Auth_Service SHALL return a ResponseDto with code 810 indicating authentication failure
3. WHEN a login request is submitted with a non-existent email, THEN THE Auth_Service SHALL return a ResponseDto with code 810 indicating authentication failure
4. WHEN a user successfully logs in, THEN THE Auth_Service SHALL revoke all previously issued tokens for that user
5. WHEN a user successfully logs in, THEN THE Auth_Service SHALL update the user's deviceId and set loggedIn flag to true

### Requirement 5: User Logout and Token Revocation

**User Story:** As a user, I want to log out and have all my tokens invalidated, so that my session cannot be reused.

#### Acceptance Criteria

1. WHEN a user logs out, THEN THE Auth_Service SHALL revoke all tokens for that user in Redis
2. WHEN a token has been revoked, THEN THE JWT_Filter SHALL reject any subsequent request using that token

### Requirement 6: JWT Token Generation

**User Story:** As a developer, I want JWT tokens to be generated with proper claims and expiration, so that tokens are secure and time-limited.

#### Acceptance Criteria

1. THE Token_Service SHALL generate access tokens containing the user's email as subject, configured extra claims, and an expiration time based on the configured access-token-expiration value
2. THE Token_Service SHALL generate refresh tokens with a longer expiration time based on the configured refresh-token-expiration value
3. THE Token_Service SHALL sign all tokens using HMAC-SHA256 with the configured secret key (minimum 256-bit)

### Requirement 7: Entity CRUD Operations

**User Story:** As a user, I want to create, read, update, and delete domain entities (accounts, budgets, categories, sub-categories, expenses, transactions), so that I can manage my financial data.

#### Acceptance Criteria

1. WHEN a valid create request is submitted, THEN THE CRUD_Service SHALL persist the entity with an auto-generated UUID refNo and return a ResponseDto with code 801
2. WHEN a get request is submitted with a valid refNo, THEN THE CRUD_Service SHALL return a ResponseDto with code 800 containing the entity DTO
3. WHEN a get request is submitted with a non-existent or soft-deleted refNo, THEN THE CRUD_Service SHALL return a ResponseDto with code 804 and an error message
4. WHEN a valid update request is submitted with an existing refNo, THEN THE CRUD_Service SHALL update the entity fields and return a ResponseDto with code 802
5. WHEN a delete request is submitted with a valid refNo, THEN THE CRUD_Service SHALL set the entity's deleted flag to true (soft-delete) and return a ResponseDto with code 805
6. THE CRUD_Service SHALL exclude all soft-deleted entities from GET and list operations

### Requirement 8: Pagination

**User Story:** As an API consumer, I want to retrieve entities in paginated, sorted lists, so that I can efficiently browse large datasets.

#### Acceptance Criteria

1. WHEN a paginated list request is submitted with page, per_page, sortBy, and sortDirection parameters, THEN THE CRUD_Service SHALL return a ResponseDto with code 800 containing a Page object with content, pageNumber, pageSize, totalElements, totalPages, hasNext, and hasPrevious fields
2. WHEN a paginated list request is submitted, THEN THE CRUD_Service SHALL return content with size less than or equal to the requested per_page value
3. WHEN a paginated list request specifies an invalid sortBy field, THEN THE CRUD_Service SHALL default to sorting by "id"
4. THE CRUD_Service SHALL use 1-based page numbering in the API and convert to 0-based internally for Spring Data
5. THE CRUD_Service SHALL exclude soft-deleted entities from all paginated results

### Requirement 9: Entity Associations

**User Story:** As a user, I want to add and remove relationships between entities (e.g., budgets to accounts, categories to customers), so that I can organize my financial data hierarchically.

#### Acceptance Criteria

1. WHEN an add-association request is submitted with a valid entity refNo and a set of association refNos, THEN THE Association_Manager SHALL add each valid, non-duplicate association and return a ResponseDto with code 802 containing success and error maps
2. WHEN an add-association request includes a refNo that is already associated with the entity, THEN THE Association_Manager SHALL record that refNo in the error map with message "entity already contains this association" and leave the collection unchanged
3. WHEN an add-association request includes a refNo that does not correspond to any existing entity, THEN THE Association_Manager SHALL record that refNo in the error map with message "no entity corresponds to this ref no"
4. WHEN a remove-association request is submitted with valid refNos, THEN THE Association_Manager SHALL remove the specified associations and return a ResponseDto with code 802
5. WHEN the entityRefNo is null and entityType is CUSTOMER, THEN THE Association_Manager SHALL resolve to the current authenticated user's customer entity

### Requirement 10: Transaction Processing

**User Story:** As a user, I want to create transactions that adjust budget balances, so that my spending is tracked against my budgets.

#### Acceptance Criteria

1. WHEN a valid transaction create request is submitted, THEN THE CRUD_Service SHALL persist the transaction with its associated expense and budget amounts, and return a ResponseDto with code 801
2. WHEN a transaction contains budget amounts with AmountType DEBIT, THEN THE CRUD_Service SHALL subtract the amount from the corresponding budget's balance
3. WHEN a transaction contains budget amounts with AmountType CREDIT, THEN THE CRUD_Service SHALL add the amount to the corresponding budget's balance
4. WHEN a transaction is created, THEN THE CRUD_Service SHALL add the transaction to the current customer's transactions collection
5. WHEN a transaction is created, THEN THE Cache_Manager SHALL invalidate the cached transaction data for the affected customer

### Requirement 11: Budget Transfers

**User Story:** As a user, I want to transfer amounts between budgets, so that I can reallocate funds as needed.

#### Acceptance Criteria

1. WHEN a valid budget transfer request is submitted, THEN THE CRUD_Service SHALL persist the transfer with sender and receiver budget amounts, and return a ResponseDto with code 801
2. WHEN a budget transfer is processed, THEN THE CRUD_Service SHALL debit the sender budget and credit each receiver budget by the specified amounts

### Requirement 12: Redis Caching

**User Story:** As a system operator, I want frequently accessed data to be cached in Redis, so that database load is reduced and response times are improved.

#### Acceptance Criteria

1. WHEN an entity is fetched by refNo and exists in cache, THEN THE Cache_Manager SHALL return the cached value without querying the database
2. WHEN an entity is fetched by refNo and does not exist in cache, THEN THE Cache_Manager SHALL query the database, cache the result with the configured TTL, and return the value
3. WHEN an entity is created, updated, or deleted, THEN THE Cache_Manager SHALL invalidate all related cache entries
4. THE Cache_Manager SHALL use configurable TTL values per entity type (accounts: 300s, categories: 600s, budgets: 120s)

### Requirement 13: Unified Response Structure

**User Story:** As an API consumer, I want all API responses to follow a consistent structure, so that I can parse responses uniformly.

#### Acceptance Criteria

1. THE System SHALL wrap all API responses in a ResponseDto containing message, status, code, data, and date fields
2. THE System SHALL set the date field to the server timestamp at response creation time formatted as "yyyy-MM-dd HH:mm:ss"
3. THE System SHALL use code 800 for successful fetch operations, 801 for successful creates, 802 for successful updates, 804 for errors, 805 for successful deletes, and 810 for authentication/authorization errors
4. THE System SHALL exclude null fields from JSON serialization of ResponseDto

### Requirement 14: Error Handling

**User Story:** As an API consumer, I want meaningful error responses when operations fail, so that I can understand and recover from errors.

#### Acceptance Criteria

1. WHEN an entity is not found by refNo, THEN THE Global_Exception_Handler SHALL return a ResponseDto with code 804 containing errorCategory, errorCode, and errorMessage
2. WHEN a request body fails bean validation, THEN THE Global_Exception_Handler SHALL return a ResponseDto with code 804 containing a list of field-level validation errors
3. WHEN an access denied exception occurs, THEN THE Global_Exception_Handler SHALL return a ResponseDto with code 810
4. WHEN an unexpected exception occurs, THEN THE Global_Exception_Handler SHALL return a ResponseDto with code 804 and a generic error message without exposing internal details
5. IF a general failure exception is thrown during association processing, THEN THE Global_Exception_Handler SHALL return a ResponseDto with code 804 containing the failure message

### Requirement 15: Entity Auditing

**User Story:** As a system operator, I want all entities to track creation and modification timestamps, so that I can audit data changes.

#### Acceptance Criteria

1. WHEN an entity is first persisted, THEN THE System SHALL set both createdAt and updatedAt to the current server timestamp
2. WHEN an entity is updated, THEN THE System SHALL update the updatedAt field to the current server timestamp while preserving createdAt
3. THE System SHALL generate a unique UUID refNo for each entity at creation time that remains immutable thereafter

### Requirement 16: Soft Delete

**User Story:** As a system operator, I want entities to be soft-deleted rather than physically removed, so that data integrity and audit trails are preserved.

#### Acceptance Criteria

1. WHEN a delete operation is performed on an entity, THEN THE CRUD_Service SHALL set the entity's deleted flag to true without removing the database record
2. WHILE an entity has deleted flag set to true, THE Repository SHALL exclude that entity from all standard query results (findByRefNo, findAll, paginated queries)

### Requirement 17: Externalized Configuration

**User Story:** As a DevOps engineer, I want all configuration to be externalized via YAML and environment variables, so that the application can be deployed across environments without code changes.

#### Acceptance Criteria

1. THE System SHALL read database connection parameters (url, username, password, pool size) from environment variables with sensible defaults
2. THE System SHALL read Redis connection parameters (host, port, password, timeout) from environment variables with sensible defaults
3. THE System SHALL read JWT configuration (secret key, access token expiration, refresh token expiration) from environment variables
4. THE System SHALL read server configuration (port, context path) from environment variables with defaults (port: 8080, context-path: /expenses-tracker)
5. THE System SHALL define the authentication whitelist in YAML configuration

### Requirement 18: Database Configuration

**User Story:** As a developer, I want the application to use PostgreSQL with HikariCP connection pooling, so that database access is reliable and performant.

#### Acceptance Criteria

1. THE System SHALL connect to PostgreSQL using the configured JDBC URL and credentials
2. THE System SHALL use HikariCP connection pooling with configurable maximum pool size (default: 20) and minimum idle connections (default: 5)
3. THE System SHALL use Hibernate with PostgreSQL dialect and configurable DDL auto mode
4. THE System SHALL disable open-in-view to prevent lazy loading outside of transactions

### Requirement 19: RefNo Uniqueness

**User Story:** As a developer, I want each entity to have a globally unique reference number, so that entities can be reliably identified across the system.

#### Acceptance Criteria

1. THE System SHALL generate a UUID-format refNo for every new entity at creation time
2. THE System SHALL enforce uniqueness of refNo within each entity type via a database unique constraint
3. THE System SHALL make refNo immutable after entity creation (updatable = false)

### Requirement 20: MapStruct DTO Mapping

**User Story:** As a developer, I want compile-time type-safe mapping between entities and DTOs, so that data transformation is reliable and performant.

#### Acceptance Criteria

1. THE System SHALL use MapStruct mappers to convert between request DTOs, response DTOs, and JPA entities
2. WHEN a mapper converts an entity to a response DTO, THEN THE System SHALL include all relevant fields without exposing internal identifiers (database IDs) or sensitive data (passwords)
3. WHEN a mapper converts a request DTO to an entity, THEN THE System SHALL map only the fields provided in the request, leaving system-managed fields (refNo, createdAt, updatedAt, deleted) untouched
