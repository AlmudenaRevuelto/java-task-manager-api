package taskmanager.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet filter that intercepts every request and validates the JWT Bearer token
 * present in the {@code Authorization} header.
 *
 * <p>Processing order:
 * <ol>
 *   <li>Extract the token from {@code Authorization: Bearer <token>}.</li>
 *   <li>Validate the token signature and expiration via {@link JwtUtils}.</li>
 *   <li>Load the corresponding {@link UserDetails} from the database.</li>
 *   <li>Set the authenticated principal in the {@link SecurityContextHolder} so
 *       that downstream filters and controllers see an authenticated request.</li>
 * </ol>
 *
 * <p>Requests without a valid Bearer token are passed through unchanged; Spring
 * Security's access-control rules will then reject them if the endpoint requires
 * authentication.
 *
 * <p>Registered in {@link taskmanager.config.SecurityConfig} to run before
 * {@code UsernamePasswordAuthenticationFilter}.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * Creates a new {@code JwtAuthFilter}.
     *
     * @param jwtUtils           utility for validating and parsing JWT tokens
     * @param userDetailsService service for loading user details by username
     */
    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Reads the {@code Authorization} header, validates the Bearer token if present,
     * and populates the {@link SecurityContextHolder} with the authenticated principal.
     *
     * <p>If no valid token is found the request is forwarded unchanged.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                username = jwtUtils.getUsernameFromJwt(token);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}