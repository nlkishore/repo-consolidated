package com.uob.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uob.dto.UserDTO;
import com.uob.service.TurbineSecurityService;

/**
 * REST Controller for Authentication operations
 * Handles login/logout and session management
 */
@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private static final Log log = LogFactory.getLog(AuthRestController.class);

    @Autowired
    private TurbineSecurityService turbineSecurityService;

    /**
     * Login request DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Login response DTO
     */
    public static class LoginResponse {
        private boolean success;
        private String message;
        private UserDTO user;

        public LoginResponse(boolean success, String message, UserDTO user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public UserDTO getUser() {
            return user;
        }

        public void setUser(UserDTO user) {
            this.user = user;
        }
    }

    /**
     * Login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, "Username is required", null));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, "Password is required", null));
            }

            // Get SecurityService
            SecurityService securityService = (SecurityService) 
                TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);

            // Check if user exists
            User user;
            try {
                user = (User) securityService.getUser(username);
            } catch (UnknownEntityException e) {
                log.warn("Login failed: User not found - " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Invalid username or password", null));
            }

            // Authenticate user (check password)
            // getAuthenticatedUser returns User if credentials are valid, throws exception otherwise
            User authenticatedUser;
            try {
                authenticatedUser = securityService.getAuthenticatedUser(username, password);
                if (authenticatedUser == null) {
                    log.warn("Login failed: Authentication returned null for user - " + username);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid username or password", null));
                }
                // Use the authenticated user
                user = authenticatedUser;
            } catch (Exception e) {
                log.warn("Login failed: Invalid credentials for user - " + username, e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Invalid username or password", null));
            }

            // Check if anonymous user
            User anonymousUser = securityService.getAnonymousUser();
            if (user.getName().equals(anonymousUser.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Anonymous user cannot login", null));
            }

            // Create session and set user
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("username", username);

            // Create RunData-like session attributes for Turbine compatibility
            session.setAttribute("turbine.user", user);

            // Convert user to DTO
            UserDTO userDTO = convertToDTO(user);

            log.info("User logged in successfully: " + username);

            return ResponseEntity.ok(new LoginResponse(true, "Login successful", userDTO));

        } catch (Exception e) {
            log.error("Error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "Login failed: " + e.getMessage(), null));
        }
    }

    /**
     * Check if user is authenticated
     * GET /api/auth/check
     */
    @GetMapping("/check")
    public ResponseEntity<LoginResponse> checkAuth(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            
            if (session == null) {
                return ResponseEntity.ok(new LoginResponse(false, "Not authenticated", null));
            }

            User user = (User) session.getAttribute("user");
            if (user == null || !user.hasLoggedIn()) {
                return ResponseEntity.ok(new LoginResponse(false, "Not authenticated", null));
            }

            UserDTO userDTO = convertToDTO(user);
            return ResponseEntity.ok(new LoginResponse(true, "Authenticated", userDTO));

        } catch (Exception e) {
            log.error("Error checking authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "Error checking authentication", null));
        }
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                String username = (String) session.getAttribute("username");
                session.invalidate();
                log.info("User logged out: " + (username != null ? username : "unknown"));
            }

            return ResponseEntity.ok(new LoginResponse(true, "Logout successful", null));

        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "Logout failed", null));
        }
    }

    /**
     * Convert User entity to DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        
        // Handle ID conversion (getId() returns Object)
        Object id = user.getId();
        if (id instanceof Integer) {
            dto.setUserId((Integer) id);
        } else if (id != null) {
            dto.setUserId(Integer.valueOf(id.toString()));
        }
        
        dto.setLoginName(user.getName());
        
        // Cast to TurbineUser (GtpUser) to access additional properties
        if (user instanceof com.uob.om.GtpUser) {
            com.uob.om.GtpUser gtpUser = (com.uob.om.GtpUser) user;
            dto.setFirstName(gtpUser.getFirstName());
            dto.setLastName(gtpUser.getLastName());
            dto.setEmail(gtpUser.getEmail());
            
            // getConfirmed() returns String, convert to Boolean
            String confirmed = gtpUser.getConfirmed();
            dto.setConfirmed("Y".equalsIgnoreCase(confirmed) || "true".equalsIgnoreCase(confirmed));
            
            dto.setLastLogin(gtpUser.getLastLogin());
            dto.setCreated(gtpUser.getCreateDate());
            dto.setModified(gtpUser.getModifiedDate());
        }
        
        return dto;
    }
}
