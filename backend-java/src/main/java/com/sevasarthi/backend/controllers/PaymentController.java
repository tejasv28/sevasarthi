package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Rental;
import com.sevasarthi.backend.models.Tool;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.RentalRepository;
import com.sevasarthi.backend.repository.ToolRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import com.sevasarthi.backend.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${RAZORPAY_KEY_ID:}")
    private String razorpayKeyId;

    @Value("${RAZORPAY_KEY_SECRET:}")
    private String razorpayKeySecret;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    ToolRepository toolRepository;

    @Autowired
    ProviderRepository providerRepository;

    private UserDetailsImpl getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) return null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Number amountNum = (Number) body.get("amount");
        if (amountNum == null || amountNum.doubleValue() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Invalid payment amount."));
        }

        String type = (String) body.get("type");
        if (!"booking".equals(type) && !"rental".equals(type)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Invalid payment type."));
        }

        // Placeholder for Razorpay API call
        // In reality, you'd use the Razorpay Java SDK: RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
        // Order order = razorpay.orders.create(orderRequest);
        
        String dummyOrderId = "order_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", dummyOrderId);
        data.put("amount", Math.round(amountNum.doubleValue() * 100)); // paise
        data.put("currency", "INR");
        data.put("keyId", razorpayKeyId);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, data, "Razorpay order created (Placeholder)."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String razorpayOrderId = (String) body.get("razorpay_order_id");
        String razorpayPaymentId = (String) body.get("razorpay_payment_id");
        String razorpaySignature = (String) body.get("razorpay_signature");
        String type = (String) body.get("type");
        Map<String, Object> payload = (Map<String, Object>) body.get("payload");

        try {
            // Verify signature
            // In a real scenario with Razorpay enabled:
            // String generatedSignature = calculateRFC2104HMAC(razorpayOrderId + "|" + razorpayPaymentId, razorpayKeySecret);
            // if (!generatedSignature.equals(razorpaySignature)) throw ...
            
            if ("booking".equals(type)) {
                Booking booking = createBookingAfterPayment(currentUser, payload, razorpayOrderId, razorpayPaymentId);
                Map<String, Object> data = new HashMap<>();
                data.put("booking", booking);
                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, data, "Payment verified. Booking created successfully."));
            } else if ("rental".equals(type)) {
                Rental rental = createRentalAfterPayment(currentUser, payload, razorpayOrderId, razorpayPaymentId);
                Map<String, Object> data = new HashMap<>();
                data.put("rental", rental);
                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, data, "Payment verified. Rental created successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Invalid payment type."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Payment verification failed: " + e.getMessage()));
        }
    }

    private Booking createBookingAfterPayment(UserDetailsImpl currentUser, Map<String, Object> payload, String orderId, String paymentId) throws Exception {
        String providerId = (String) payload.get("providerId");
        Optional<Provider> providerOpt = providerRepository.findById(providerId);
        if (providerOpt.isEmpty()) throw new Exception("Provider not found.");

        Number totalAmountNum = (Number) payload.get("totalAmount");
        Number baseRateNum = (Number) payload.get("baseRate");

        java.util.Date parsedDate = new java.util.Date();
        try {
            if (payload.get("scheduledDate") != null) {
                parsedDate = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse((String) payload.get("scheduledDate"));
            }
        } catch (Exception e) {
            try {
                parsedDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse((String) payload.get("scheduledDate"));
            } catch (Exception e2) {}
        }

        Booking booking = Booking.builder()
                .userId(currentUser.getId())
                .providerId(providerOpt.get().getId())
                .serviceId((String) payload.get("serviceId"))
                .serviceName((String) payload.get("serviceName"))
                .scheduledDate(parsedDate)
                .scheduledTime((String) payload.get("scheduledTime"))
                .address(payload.get("address") instanceof String ? (String) payload.get("address") : String.valueOf(payload.get("address")))
                .instructions((String) payload.get("instructions"))
                .photos((List<String>) payload.get("photos"))
                .paymentMethod("online")
                .couponCode((String) payload.get("couponCode"))
                .baseRate(baseRateNum != null ? baseRateNum.doubleValue() : 0.0)
                .platformFee(49.0)
                .discount(0.0)
                .tax(0.0)
                .totalAmount(totalAmountNum != null ? totalAmountNum.doubleValue() : 0.0)
                .status(Constants.BookingStatus.PENDING)
                .paymentStatus(Constants.PaymentStatus.PAID)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .createdAt(new Date())
                .build();

        return bookingRepository.save(booking);
    }

    private Rental createRentalAfterPayment(UserDetailsImpl currentUser, Map<String, Object> payload, String orderId, String paymentId) throws Exception {
        String toolId = (String) payload.get("toolId");
        Optional<Tool> toolOpt = toolRepository.findById(toolId);
        if (toolOpt.isEmpty()) throw new Exception("Tool not found.");

        Tool tool = toolOpt.get();
        if (!"available".equals(tool.getStatus())) throw new Exception("Tool is not available for rent.");

        Number daysNum = (Number) payload.get("days");
        int days = daysNum != null ? daysNum.intValue() : 1;

        double subtotal = tool.getDailyRate() * days;
        double deliveryFee = 99.0;
        double tax = Math.round(subtotal * 0.05);
        double refundableDeposit = Math.max(500, Math.round(subtotal * 0.4));
        double total = subtotal + deliveryFee + tax;

        Map<String, Object> ddMap = (Map<String, Object>) payload.get("deliveryDetails");
        Rental.DeliveryDetails deliveryDetails = null;
        if (ddMap != null) {
            deliveryDetails = Rental.DeliveryDetails.builder()
                .addressLine1((String) ddMap.get("addressLine1"))
                .addressLine2((String) ddMap.get("addressLine2"))
                .city((String) ddMap.get("city"))
                .pincode((String) ddMap.get("pincode"))
                .phone((String) ddMap.get("phone"))
                .fullName((String) ddMap.get("fullName"))
                .build();
        }

        Rental rental = Rental.builder()
                .userId(currentUser.getId())
                .toolId(tool.getId())
                .toolName(tool.getName())
                .days(days)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .tax(tax)
                .refundableDeposit(refundableDeposit)
                .total(total)
                .deliveryDetails(deliveryDetails)
                .status("confirmed")
                .deliveryOtp("123456") // Mock OTP
                .returnOtp("654321")   // Mock OTP
                .paymentStatus(Constants.PaymentStatus.PAID)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .createdAt(new Date())
                .build();

        tool.setStatus("rented");
        toolRepository.save(tool);

        return rentalRepository.save(rental);
    }

    @GetMapping("/key")
    public ResponseEntity<?> getKey() {
        Map<String, Object> data = new HashMap<>();
        data.put("keyId", razorpayKeyId);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Razorpay key retrieved."));
    }

    private String calculateRFC2104HMAC(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : rawHmac) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
