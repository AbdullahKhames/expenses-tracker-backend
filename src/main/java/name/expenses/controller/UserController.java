package name.expenses.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.auth.ChangeEmailRequest;
import name.expenses.dto.auth.LoginRequest;
import name.expenses.dto.user.UserReqDto;
import name.expenses.dto.user.UserRoleDto;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.security.AuthService;
import name.expenses.service.UserService;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // ---- Auth endpoints ----

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@Valid @RequestBody UserReqDto userReqDto) {
        log.info("Registering user with email: {}", userReqDto.getEmail());
        ResponseDto response = authService.register(userReqDto, false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        ResponseDto response = authService.phoneLogin(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<ResponseDto> refreshToken(HttpServletRequest request,
                                                    @RequestBody AccessToken accessToken) {
        log.info("Refresh token request");
        ResponseDto response = authService.refreshToken(request, accessToken.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resetAccount")
    public ResponseEntity<ResponseDto> resetAccount(@RequestBody LoginRequest loginRequest) {
        log.info("Reset account request for email: {}", loginRequest.getEmail());
        ResponseDto response = authService.resetAccount(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout/{refNo}")
    public ResponseEntity<ResponseDto> logoutWithToken(@PathVariable String refNo) {
        log.info("Admin logout for user with refNo: {}", refNo);
        ResponseDto response = authService.logoutWithRef(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth")
    public ResponseEntity<ResponseDto> testAuth() {
        log.info("Auth test endpoint accessed");
        // ADMIN only test endpoint
        ResponseDto response = ResponseDtoBuilder.createResponse(
                "Auth test endpoint - ADMIN access confirmed",
                true, 800, null);
        return ResponseEntity.ok(response);
    }

    // ---- User management endpoints ----

    @GetMapping("/{email}")
    public ResponseEntity<ResponseDto> getUser(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        ResponseDto response = userService.getUser(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        ResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addRole")
    public ResponseEntity<ResponseDto> addRoleToUser(@RequestBody UserRoleDto userRoleDto) {
        log.info("Adding role {} to user {}", userRoleDto.getRoleName(), userRoleDto.getEmail());
        ResponseDto response = userService.addRoleToUser(userRoleDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> softDeleteUser(@PathVariable String id) {
        log.info("Soft deleting user with refNo: {}", id);
        ResponseDto response = userService.softDeleteUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto> getAllUsers() {
        log.info("Fetching all users");
        ResponseDto response = userService.getAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<ResponseDto> activateUser(@PathVariable Long id) {
        log.info("Activating user with id: {}", id);
        ResponseDto response = userService.activateUser(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ResponseDto> deActivateUser(@PathVariable Long id) {
        log.info("Deactivating user with id: {}", id);
        ResponseDto response = userService.deActivateUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseDto> logout() {
        log.info("Logout current user");
        ResponseDto response = authService.logout();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/validateChangeEmail")
    public ResponseEntity<ResponseDto> validateChangeEmail(@RequestBody ChangeEmailRequest changeEmailRequest) {
        log.info("Validating email change for refNo: {}", changeEmailRequest.getRefNo());
        ResponseDto response = userService.validateChangeEmail(changeEmailRequest.getRefNo(), changeEmailRequest.getToken());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/changeEmail")
    public ResponseEntity<ResponseDto> changeEmail(@RequestBody ChangeEmailRequest changeEmailRequest) {
        log.info("Changing email for refNo: {}", changeEmailRequest.getRefNo());
        ResponseDto response = userService.changeEmail(changeEmailRequest.getToken(), changeEmailRequest.getRefNo(), changeEmailRequest.getNewEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getByRef/{refNo}")
    public ResponseEntity<ResponseDto> getUserByRef(@PathVariable String refNo) {
        log.info("Fetching user by refNo: {}", refNo);
        ResponseDto response = userService.getUserByRef(refNo);
        return ResponseEntity.ok(response);
    }

    // ---- Inner class for refresh token request body ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessToken {
        private String token;
    }
}
