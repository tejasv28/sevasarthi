package com.sevasarthi.backend.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.dto.JwtResponse;
import com.sevasarthi.backend.dto.LoginRequest;
import com.sevasarthi.backend.dto.RegisterRequest;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.UserRepository;
import com.sevasarthi.backend.security.JwtUtils;
import com.sevasarthi.backend.security.UserDetailsImpl;
import com.sevasarthi.backend.services.EmailService;
import com.sevasarthi.backend.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @Autowired
    OtpService otpService;

    @Value("${google.client.id:YOUR_GOOGLE_CLIENT_ID}")
    private String googleClientId;

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {

        String email = signUpRequest.getEmail() != null ? signUpRequest.getEmail().toLowerCase() : null;
        String phone = signUpRequest.getPhone() != null ? signUpRequest.getPhone().replaceAll("[\\s\\-+()]", "") : null;

        if (phone != null && phone.startsWith("91") && phone.length() > 10) {
            phone = phone.substring(2);
        }

        if (email != null && userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, null, "An account with this email already exists."));
        }

        if (phone != null && !phone.isEmpty() && userRepository.existsByPhone(phone)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, null, "An account with this mobile number already exists."));
        }

        String role = signUpRequest.getRole() != null ? signUpRequest.getRole() : "user";
        String signupMethod = signUpRequest.getSignupMethod();
        String otpToken = signUpRequest.getOtpToken();

        // Verify OTP Token for non-providers
        if (!"provider".equals(role) && signupMethod != null) {
            String key = "email".equals(signupMethod) ? email : phone;
            OtpService.OtpData stored = otpService.getOtpData(key);
            if (stored == null || !stored.verified) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "Please verify your " + signupMethod + " first."));
            }
            if (!otpToken.equals(stored.verifyToken)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "Invalid verification. Please verify again."));
            }
            otpService.removeOtpData(key);
        }

        String dashboard = "/user/dashboard";
        if ("provider".equals(role)) dashboard = "/provider/dashboard";
        if ("admin".equals(role)) dashboard = "/admin/dashboard";

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(email)
                .password(encoder.encode(signUpRequest.getPassword()))
                .phone(phone != null ? phone : "")
                .role(role)
                .dashboard(dashboard)
                .address(signUpRequest.getAddress())
                .isActive(true)
                .createdAt(new Date())
                .build();

        user = userRepository.save(user);

        if ("provider".equals(role)) {
            String finalBusinessName = signUpRequest.getBusinessName();
            if ((finalBusinessName == null || finalBusinessName.trim().isEmpty()) && 
                (signUpRequest.getBusinessType() == null || "individual".equals(signUpRequest.getBusinessType()))) {
                String cat = signUpRequest.getPrimaryCategory() != null ? signUpRequest.getPrimaryCategory() : "Service";
                finalBusinessName = signUpRequest.getName() + " " + cat + " Services";
            }

            Provider provider = Provider.builder()
                    .userId(user.getId())
                    .ownerName(signUpRequest.getName())
                    .phone(user.getPhone())
                    .businessType(signUpRequest.getBusinessType() != null ? signUpRequest.getBusinessType() : "individual")
                    .businessName(finalBusinessName)
                    .category(signUpRequest.getPrimaryCategory() != null ? signUpRequest.getPrimaryCategory() : "Home Maintenance")
                    .primaryCategory(signUpRequest.getPrimaryCategory() != null ? signUpRequest.getPrimaryCategory() : "Home Maintenance")
                    .title(signUpRequest.getTitle() != null ? signUpRequest.getTitle() : "Service Professional")
                    .bio(signUpRequest.getBio() != null ? signUpRequest.getBio() : "")
                    .skills((List<String>) signUpRequest.getSkills())
                    .experience(signUpRequest.getExperience() != null ? signUpRequest.getExperience() : "1 yr")
                    .city(signUpRequest.getCity() != null ? signUpRequest.getCity() : "")
                    .fullAddress(signUpRequest.getFullAddress() != null ? signUpRequest.getFullAddress() : "")
                    .pincode(signUpRequest.getPincode() != null ? signUpRequest.getPincode() : "")
                    // Need to map documents properly
                    .verificationStatus("pending")
                    .createdAt(new Date())
                    .build();

            providerRepository.save(provider);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail() != null ? user.getEmail() : user.getPhone(), signUpRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        String refreshToken = jwt; // Using JWT as a placeholder refresh token for simplicity
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", jwt)
                .httpOnly(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        String providerStatus = "provider".equals(role) ? "pending" : null;

        JwtResponse jwtResponse = JwtResponse.builder()
                .user(user)
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .providerStatus(providerStatus)
                .build();

        String message = "provider".equals(role) ? "Application submitted! Your account is pending review." : "Registration successful.";

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new ApiResponse<>(201, jwtResponse, message));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        String loginId = loginRequest.getEmail();
        if (loginId == null || loginId.trim().isEmpty()) loginId = loginRequest.getPhone();
        if (loginId == null || loginId.trim().isEmpty()) loginId = loginRequest.getIdentifier();

        if (loginId == null || loginId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Email or mobile number is required."));
        }

        loginId = loginId.trim();
        boolean isPhone = loginId.matches("^[6-9]\\d{9}$");
        String cleanedPhone = loginId.replaceAll("[\\s\\-+()]", "");
        if (cleanedPhone.startsWith("91") && cleanedPhone.length() > 10) {
            cleanedPhone = cleanedPhone.substring(2);
        }

        Optional<User> userOpt = isPhone ? userRepository.findByPhone(cleanedPhone) : userRepository.findByEmail(loginId.toLowerCase());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, null, "Invalid credentials. No account found."));
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Account has been deactivated. Contact support."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(isPhone ? cleanedPhone : loginId.toLowerCase(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            String refreshToken = jwt;
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            String providerStatus = null;
            if ("provider".equals(user.getRole())) {
                Optional<Provider> providerOpt = providerRepository.findByUserId(user.getId());
                providerStatus = providerOpt.isPresent() ? providerOpt.get().getVerificationStatus() : "pending";
            }

            ResponseCookie jwtCookie = ResponseCookie.from("accessToken", jwt)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            JwtResponse jwtResponse = JwtResponse.builder()
                    .user(user)
                    .accessToken(jwt)
                    .refreshToken(refreshToken)
                    .providerStatus(providerStatus)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new ApiResponse<>(200, jwtResponse, "Login successful."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, null, "Invalid credentials."));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");
        String role = body.get("role");

        if (credential == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Google credential is required."));
        }

        if ("provider".equals(role)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Service providers must register with email and password."));
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(401, null, "Invalid Google token."));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;

            if (userOpt.isPresent()) {
                user = userOpt.get();
                if (!user.isActive()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(403, null, "Account has been deactivated."));
                }
                if ("provider".equals(user.getRole())) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Service providers must log in with email and password."));
                }
            } else {
                user = User.builder()
                        .name(name)
                        .email(email)
                        .avatar(picture)
                        .role("user")
                        .dashboard("/user/dashboard")
                        .isVerified(true)
                        .isActive(true)
                        .createdAt(new Date())
                        .build();
                user = userRepository.save(user);
            }

            // In Spring, we should create an authentication token manually for OAuth users since they don't have a password in our DB
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwt;
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            ResponseCookie jwtCookie = ResponseCookie.from("accessToken", jwt)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            JwtResponse jwtResponse = JwtResponse.builder()
                    .user(user)
                    .accessToken(jwt)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new ApiResponse<>(200, jwtResponse, "Google authentication successful."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(401, null, "Google authentication failed."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findById(userDetails.getId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRefreshToken("");
                userRepository.save(user);
            }
        }

        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new ApiResponse<>(200, null, "Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(401, null, "Unauthorized"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "User not found."));
        }

        User user = userOpt.get();
        Provider providerProfile = null;
        String providerStatus = null;

        if ("provider".equals(user.getRole())) {
            Optional<Provider> providerOpt = providerRepository.findByUserId(user.getId());
            if (providerOpt.isPresent()) {
                providerProfile = providerOpt.get();
                providerStatus = providerProfile.getVerificationStatus();
            }
        }

        Map<String, Object> responseData = new HashMap<>();
        user.setPassword(null);
        user.setRefreshToken(null);
        responseData.put("user", user);
        responseData.put("providerProfile", providerProfile);
        responseData.put("providerStatus", providerStatus);

        return ResponseEntity.ok(new ApiResponse<>(200, responseData, "Profile retrieved."));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication authentication) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        User user = userOpt.get();

        if (!encoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(401, null, "Current password is incorrect."));
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, null, "Password changed successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) email = "";
        
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase());
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "If an account with this email exists, an OTP has been sent."));
        }

        User user = userOpt.get();
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        String hashedOtp = hashString(otp);

        user.setResetPasswordOtp(hashedOtp);
        user.setResetPasswordOtpExpires(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        user.setOtpAttempts(0);
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp, "Password Reset OTP - Seva Sarthi");

        Map<String, Object> data = new HashMap<>();
        data.put("mailConfigured", true);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "OTP sent to your email address."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Email and OTP are required."));
        }

        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid request."));

        User user = userOpt.get();

        if (user.getResetPasswordOtpExpires() == null || user.getResetPasswordOtpExpires().before(new Date())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "OTP has expired. Please request a new one."));
        }

        if (user.getOtpAttempts() >= 5) {
            user.setResetPasswordOtp(null);
            user.setResetPasswordOtpExpires(null);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(429, null, "Too many attempts. Please request a new OTP."));
        }

        String hashedOtp = hashString(otp);
        if (!hashedOtp.equals(user.getResetPasswordOtp())) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userRepository.save(user);
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid OTP. Please try again."));
        }

        String resetToken = generateRandomToken();
        String hashedResetToken = hashString(resetToken);

        user.setResetPasswordOtp(hashedResetToken);
        user.setResetPasswordOtpExpires(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        user.setOtpAttempts(0);
        userRepository.save(user);

        Map<String, String> data = new HashMap<>();
        data.put("resetToken", resetToken);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "OTP verified successfully."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String resetToken = body.get("resetToken");
        String newPassword = body.get("newPassword");

        if (email == null || resetToken == null || newPassword == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Email, reset token, and new password are required."));
        }

        if (newPassword.length() < 6 || newPassword.length() > 15) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Password must be between 6 and 15 characters."));
        }

        String hashedToken = hashString(resetToken);
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid request."));
        }

        User user = userOpt.get();

        if (user.getResetPasswordOtpExpires() == null || user.getResetPasswordOtpExpires().before(new Date()) || !hashedToken.equals(user.getResetPasswordOtp())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid or expired reset token."));
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetPasswordOtp(null);
        user.setResetPasswordOtpExpires(null);
        user.setOtpAttempts(0);
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, null, "Password reset successfully. You can now log in."));
    }

    @PostMapping("/provider/send-otp")
    public ResponseEntity<?> sendProviderOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Email is required."));

        String otp = otpService.generateOtp(email.toLowerCase());
        emailService.sendOtpEmail(email, otp, "Provider Registration OTP - Seva Sarthi");

        Map<String, Object> data = new HashMap<>();
        data.put("mailConfigured", true);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "OTP sent to your email."));
    }

    @PostMapping("/provider/verify-otp")
    public ResponseEntity<?> verifyProviderOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");

        if (email == null || otp == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Email and OTP are required."));

        email = email.toLowerCase();
        OtpService.OtpData stored = otpService.getOtpData(email);

        if (stored == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "No OTP found. Please request a new one."));
        if (System.currentTimeMillis() > stored.expires) {
            otpService.removeOtpData(email);
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "OTP expired."));
        }
        if (!stored.otp.equals(otp)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid OTP."));
        }

        otpService.removeOtpData(email);
        Map<String, Boolean> data = new HashMap<>();
        data.put("verified", true);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "OTP verified."));
    }

    @PostMapping("/user/send-otp")
    public ResponseEntity<?> sendUserOtp(@RequestBody Map<String, String> body) {
        String type = body.get("type");
        String email = body.get("email");
        String phone = body.get("phone");

        String key;
        if ("email".equals(type)) {
            if (email == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Email is required."));
            key = email.toLowerCase();
            if (userRepository.existsByEmail(key)) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, null, "An account with this email already exists."));
        } else if ("phone".equals(type)) {
            if (phone == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Phone is required."));
            key = phone.replaceAll("[\\s\\-+()]", "");
            if (key.startsWith("91") && key.length() > 10) key = key.substring(2);
            if (userRepository.existsByPhone(key)) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, null, "An account with this phone number already exists."));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid type."));
        }

        String otp = otpService.generateOtp(key);
        
        if ("email".equals(type)) {
            emailService.sendOtpEmail(email, otp, "Verify Your Email - Seva Sarthi");
        } else {
            // Mock SMS sending here
            System.out.println("Mock sending SMS OTP " + otp + " to " + phone);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sent", true);
        data.put("method", type);
        data.put("mailConfigured", true);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "OTP sent successfully."));
    }

    @PostMapping("/user/verify-otp")
    public ResponseEntity<?> verifyUserOtp(@RequestBody Map<String, String> body) {
        String type = body.get("type");
        String email = body.get("email");
        String phone = body.get("phone");
        String otp = body.get("otp");

        String key;
        if ("email".equals(type)) {
            key = email != null ? email.toLowerCase() : null;
        } else if ("phone".equals(type)) {
            key = phone != null ? phone.replaceAll("[\\s\\-+()]", "") : null;
            if (key != null && key.startsWith("91") && key.length() > 10) key = key.substring(2);
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid type."));
        }

        if (key == null || otp == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Verification details are required."));

        OtpService.OtpData stored = otpService.getOtpData(key);
        if (stored == null) return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "No OTP found. Please request a new one."));
        
        if (System.currentTimeMillis() > stored.expires) {
            otpService.removeOtpData(key);
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "OTP expired."));
        }

        if (stored.attempts >= 5) {
            otpService.removeOtpData(key);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(429, null, "Too many attempts. Please request a new OTP."));
        }

        if (!stored.otp.equals(otp)) {
            stored.attempts++;
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, null, "Invalid OTP. " + (5 - stored.attempts) + " attempts remaining."));
        }

        String verifyToken = generateRandomToken();
        stored.verified = true;
        stored.verifyToken = verifyToken;
        stored.expires = System.currentTimeMillis() + 15 * 60 * 1000; // Extend 15 mins for registration

        Map<String, Object> data = new HashMap<>();
        data.put("verified", true);
        data.put("otpToken", verifyToken);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Verification successful!"));
    }
}
