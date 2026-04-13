package taskmanager.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utility component for generating, parsing and validating JWT tokens.
 *
 * <p>Tokens are signed with HMAC-SHA256. The signing secret is loaded from
 * {@code app.jwt.secret} — override it in production via the {@code JWT_SECRET}
 * environment variable. The secret must be at least 32 characters long.
 */
@Component
public class JwtUtils {

    /** Secret key used to sign and verify JWT tokens. Override in production. */
    @Value("${app.jwt.secret:change-me-in-production-min-32-chars!!}")
    private String jwtSecret;

    /** Token expiration time in milliseconds. Default: 24 hours. */
    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    /**
     * Builds the HMAC signing key from the configured secret.
     *
     * @return the {@link Key} used for signing and verification
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token for the given username.
     *
     * @param username the subject to embed in the token
     * @return a compact, URL-safe JWT string
     */
    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username (subject) from a valid JWT token.
     *
     * @param token the JWT string to parse
     * @return the username stored as the token subject
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String getUsernameFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token's signature and expiration.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid and not expired; {@code false} otherwise
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
