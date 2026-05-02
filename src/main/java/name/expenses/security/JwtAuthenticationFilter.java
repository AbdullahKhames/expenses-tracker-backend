package name.expenses.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import name.expenses.model.Token;
import name.expenses.model.User;
import name.expenses.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.security")
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Value("${app.security.device-id-header}")
    private String deviceIdHeader;

    @Setter
    private List<WhitelistEntry> whitelist;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        // If no Bearer token, continue filter chain (Spring Security will handle 401)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("Failed to extract username from JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Check token is valid (signature + expiration)
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Check token not revoked in database
                boolean isTokenRevoked = tokenRepository.findByToken(jwt)
                        .map(Token::isRevoked)
                        .orElse(true);

                if (!isTokenRevoked) {
                    // Validate Device-ID header matches user's deviceId
                    String requestDeviceId = request.getHeader(deviceIdHeader);
                    boolean deviceIdValid = true;

                    if (userDetails instanceof User user) {
                        if (user.getDeviceId() != null && requestDeviceId != null
                                && !user.getDeviceId().equals(requestDeviceId)) {
                            deviceIdValid = false;
                            log.warn("Device-ID mismatch for user: {}", username);
                        }
                    }

                    if (deviceIdValid) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (whitelist == null) {
            return false;
        }

        return whitelist.stream().anyMatch(entry -> {
            boolean pathMatches = pathMatcher.match(entry.getPath(), path);
            boolean methodMatches = "*".equals(entry.getMethod())
                    || entry.getMethod().equalsIgnoreCase(method);
            return pathMatches && methodMatches;
        });
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WhitelistEntry {
        private String path;
        private String method;
    }
}
