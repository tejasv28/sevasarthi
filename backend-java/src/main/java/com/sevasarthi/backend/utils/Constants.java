package com.sevasarthi.backend.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {

    
    public static final class Roles {
        public static final String USER = "user";
        public static final String PROVIDER = "provider";
        public static final String ADMIN = "admin";
    }

    
    public static final class BookingStatus {
        public static final String PENDING = "pending";
        public static final String ACCEPTED = "accepted";
        public static final String EN_ROUTE = "en_route";
        public static final String WORKING = "working";
        public static final String COMPLETED = "completed";
        public static final String CANCELLED = "cancelled";
    }

    
    public static final class ToolStatus {
        public static final String AVAILABLE = "available";
        public static final String RENTED = "rented";
        public static final String MAINTENANCE = "maintenance";
    }

    
    public static final class RentalStatus {
        public static final String PENDING = "pending";
        public static final String CONFIRMED = "confirmed";
        public static final String DELIVERED = "delivered";
        public static final String RETURNED = "returned";
        public static final String CANCELLED = "cancelled";
    }

    
    public static final class ComplaintStatus {
        public static final String PENDING = "pending";
        public static final String IN_REVIEW = "in_review";
        public static final String RESOLVED = "resolved";
        public static final String REJECTED = "rejected";
        public static final String ESCALATED = "escalated";
        public static final String REOPENED = "reopened";
    }

    
    public static final class ComplaintTypes {
        public static final String SERVICE_BOOKING = "service_booking";
        public static final String TOOL_RENTAL = "tool_rental";
    }

    
    public static final Map<String, List<String>> COMPLAINT_CATEGORIES = Map.of(
        ComplaintTypes.SERVICE_BOOKING, Arrays.asList(
            "Provider No Show", "Poor Quality Work", "Overcharging",
            "Delayed Service", "Rude Behavior", "Property Damage",
            "Incomplete Work", "Other"
        ),
        ComplaintTypes.TOOL_RENTAL, Arrays.asList(
            "Damaged Tool Received", "Wrong Tool Delivered", "Late Delivery",
            "Tool Malfunction", "Overcharged Deposit", "Missing Parts",
            "Return Not Processed", "Other"
        )
    );

    
    public static final class AdminActions {
        public static final String WARNING_ISSUED = "warning_issued";
        public static final String REFUND_INITIATED = "refund_initiated";
        public static final String FREE_RESERVICE = "free_reservice";
        public static final String TRUST_SCORE_REDUCED = "trust_score_reduced";
        public static final String TEMPORARY_SUSPENSION = "temporary_suspension";
        public static final String PERMANENT_BAN = "permanent_ban";
        public static final String PENALTY_APPLIED = "penalty_applied";
    }

    
    public static final class NotificationTypes {
        public static final String BOOKING = "booking";
        public static final String RENTAL = "rental";
        public static final String COMPLAINT = "complaint";
        public static final String SYSTEM = "system";
        public static final String OFFER = "offer";
        public static final String ALERT = "alert";
    }

    
    public static final List<String> SERVICE_CATEGORIES = Arrays.asList(
        "Home Maintenance", "Professional Cleaning", "Electrical Works",
        "Gardening & Landscaping", "Plumbing", "Pest Control",
        "Painting", "Carpentry", "Appliance Repair", "Personal Care"
    );

    
    public static final List<String> TOOL_CATEGORIES = Arrays.asList(
        "Power Tools", "Hand Tools", "Construction", "Gardening"
    );

    
    public static final class BusinessTypes {
        public static final String INDIVIDUAL = "individual";
        public static final String SHOP = "shop";
        public static final String AGENCY = "agency";
    }

    
    public static final class ProviderStatus {
        public static final String PENDING = "pending";
        public static final String APPROVED = "approved";
        public static final String REJECTED = "rejected";
    }

    
    public static final class PaymentMethods {
        public static final String ONLINE = "online";
        public static final String CASH = "cash_after_service";
    }

    
    public static final class PaymentStatus {
        public static final String PENDING = "pending";
        public static final String PAID = "paid";
        public static final String FAILED = "failed";
    }
}
