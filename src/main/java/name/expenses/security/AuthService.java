package name.expenses.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.auth.AuthResponse;
import name.expenses.dto.auth.LoginRequest;
import name.expenses.dto.user.UserReqDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.UserMapper;
import name.expenses.model.Customer;
import name.expenses.model.Role;
import name.expenses.model.Token;
import name.expenses.model.User;
import name.expenses.model.enums.TokenType;
import name.expenses.repository.CustomerRepository;
import name.expenses.repository.RoleRepository;
import name.expenses.repository.TokenRepository;
import name.expenses.repository.UserRepository;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public ResponseDto register(UserReqDto reqDto, boolean isAdmin) {
        // Check if email already exists
        if (userRepository.findByEmail(reqDto.getEmail()).isPresent()) {
            return ResponseDtoBuilder.getErrorResponse(810, "Email already exists");
        }

        // Create User entity from DTO
        User user = userMapper.reqDtoToEntity(reqDto);
        user.setPassword(passwordEncoder.encode(reqDto.getPassword()));
        user.setDeviceId(reqDto.getDeviceId());

        // Assign role
        String roleName = isAdmin ? "ROLE_ADMIN" : "ROLE_CUSTOMER";
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
        user.getRoles().add(role);

        // Save user
        user = userRepository.save(user);

        // Create Customer entity linked to user
        Customer customer = new Customer();
        customer.setUser(user);
        customerRepository.save(customer);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save tokens
        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        // Build auth response
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.entityToRespDto(user))
                .build();

        log.info("User registered successfully with email: {}", user.getEmail());
        return ResponseDtoBuilder.getCreateResponse("User", user.getRefNo(), authResponse);
    }

    public ResponseDto phoneLogin(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("User not found with email: " + loginRequest.getEmail()));

        // Verify password with BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseDtoBuilder.createResponse("Invalid credentials", false, 810, null);
        }

        // Check user is verified and not deleted
        if (!user.isVerified()) {
            return ResponseDtoBuilder.createResponse("User is not verified", false, 810, null);
        }
        if (user.isDeleted()) {
            return ResponseDtoBuilder.createResponse("User account is deleted", false, 810, null);
        }

        // Revoke all existing tokens for user
        revokeAllUserTokens(user);

        // Update user's deviceId and loggedIn status
        user.setDeviceId(loginRequest.getDeviceId());
        user.setLoggedIn(true);
        userRepository.save(user);

        // Generate new access + refresh tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save tokens
        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        // Build auth response
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.entityToRespDto(user))
                .build();

        log.info("User logged in successfully: {}", user.getEmail());
        return ResponseDtoBuilder.getCreateResponse("Auth", user.getRefNo(), authResponse);
    }

    public ResponseDto refreshToken(HttpServletRequest request, String accessToken) {
        // Extract refresh token from Authorization header
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseDtoBuilder.createResponse("Missing refresh token", false, 810, null);
        }

        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if (username == null) {
            return ResponseDtoBuilder.createResponse("Invalid refresh token", false, 810, null);
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            return ResponseDtoBuilder.createResponse("Invalid or expired refresh token", false, 810, null);
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        saveUserToken(user, newAccessToken);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(userMapper.entityToRespDto(user))
                .build();

        log.info("Token refreshed for user: {}", username);
        return ResponseDtoBuilder.getCreateResponse("Auth", user.getRefNo(), authResponse);
    }

    public ResponseDto logout() {
        // Get current user from SecurityContext
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            return ResponseDtoBuilder.createResponse("Not authenticated", false, 810, null);
        }

        // Revoke all tokens for user
        revokeAllUserTokens(user);

        // Set loggedIn=false
        user.setLoggedIn(false);
        userRepository.save(user);

        log.info("User logged out: {}", user.getEmail());
        return ResponseDtoBuilder.createResponse("Logged out successfully", true, 800, null);
    }

    public ResponseDto logoutWithRef(String refNo) {
        // Find user by refNo
        User user = userRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with refNo: " + refNo));

        // Revoke all tokens
        revokeAllUserTokens(user);

        // Set loggedIn=false
        user.setLoggedIn(false);
        userRepository.save(user);

        log.info("Admin logged out user with refNo: {}", refNo);
        return ResponseDtoBuilder.createResponse("User logged out successfully", true, 800, null);
    }

    public ResponseDto resetAccount(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("User not found with email: " + loginRequest.getEmail()));

        // Hash new password
        user.setPassword(passwordEncoder.encode(loginRequest.getPassword()));

        // Revoke all tokens
        revokeAllUserTokens(user);

        // Save user
        userRepository.save(user);

        log.info("Account reset for user: {}", loginRequest.getEmail());
        return ResponseDtoBuilder.createResponse("Account reset successfully", true, 802, null);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
