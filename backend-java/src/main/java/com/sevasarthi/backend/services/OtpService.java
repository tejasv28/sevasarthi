package com.sevasarthi.backend.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    
    
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    public static class OtpData {
        public String otp;
        public long expires;
        public int attempts = 0;
        public boolean verified = false;
        public String verifyToken;

        public OtpData(String otp, long expires) {
            this.otp = otp;
            this.expires = expires;
        }
    }

    public String generateOtp(String key) {
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        long expires = System.currentTimeMillis() + (10 * 60 * 1000); 
        otpStore.put(key, new OtpData(otp, expires));
        return otp;
    }

    public OtpData getOtpData(String key) {
        return otpStore.get(key);
    }

    public void removeOtpData(String key) {
        otpStore.remove(key);
    }

    public boolean isValid(String key) {
        OtpData data = otpStore.get(key);
        if (data == null) return false;
        if (System.currentTimeMillis() > data.expires) {
            otpStore.remove(key);
            return false;
        }
        return true;
    }
}
