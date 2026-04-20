package taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import taskmanager.dto.auth.JwtAuthResponse;
import taskmanager.dto.auth.RegisterRequest;
import taskmanager.dto.auth.LoginRequest;
import taskmanager.model.User;
import taskmanager.model.Role;
import taskmanager.repository.UserRepository;
import taskmanager.security.JwtUtils;

/**
 * REST controller that exposes authentication endpoints.
 * All endpoints are served under the /auth base path and are publicly accessible
 * (no credentials required).
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "User registration and login")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructs the controller with its required dependencies.
     *
     * @param userRepository       repository for persisting and looking up users
     * @param passwordEncoder      BCrypt encoder used to hash passwords before storage
     * @param jwtUtils             utility for generating and validating JWT tokens
     * @param authenticationManager Spring Security manager used to validate credentials on login
     */
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user with a BCrypt-encoded password and returns a JWT token.
     *
     * <p>The {@code role} field in the request is optional; if omitted, the user
     * is assigned {@link Role#USER} by default.
     *
     * @param request the registration payload containing username, password, and optional role
     * @return 201 with a {@link JwtAuthResponse} containing the token, or 400 if the username is already taken
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account. The role field is optional and defaults to USER. The password is stored BCrypt-encoded. Returns a JWT token on success. Returns 400 if the username already exists."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = JwtAuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Username already exists")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())
        );
        // Use the requested role, defaulting to USER when none is specified
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        userRepository.save(user);

        String token = jwtUtils.generateJwtToken(user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtAuthResponse(token, user.getUsername(), user.getRole()));
    }

    /**
     * Authenticates a user and returns a signed JWT token.
     *
     * <p>Returns 401 automatically if the credentials are invalid (handled by Spring Security).
     *
     * @param request the login payload containing username and password
     * @return 200 with a {@link JwtAuthResponse} containing the signed token and the user's role
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Validates user credentials and returns a signed JWT token. Use the token in the Authorization header as: Bearer <token>."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = JwtAuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtils.generateJwtToken(auth.getName());
        Role role = userRepository.findByUsername(auth.getName())
                .map(User::getRole)
                .orElse(Role.USER);
        return ResponseEntity.ok(new JwtAuthResponse(token, auth.getName(), role));
    }
}