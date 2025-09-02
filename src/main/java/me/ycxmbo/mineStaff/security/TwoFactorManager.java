package me.ycxmbo.mineStaff.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class TwoFactorManager {
    private static final SecureRandom RNG = new SecureRandom();

    public static String generateSecret() {
        byte[] buf = new byte[20];
        RNG.nextBytes(buf);
        return base32Encode(buf);
    }

    public static boolean verify(String base32Secret, String code, int window) {
        try {
            long ts = System.currentTimeMillis() / 1000L / 30L;
            for (int i = -window; i <= window; i++) {
                String gen = totp(base32Secret, ts + i);
                if (gen.equals(code)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static String totp(String base32Secret, long timeStep) throws Exception {
        byte[] key = base32Decode(base32Secret);
        byte[] msg = new byte[8];
        long v = timeStep;
        for (int i = 7; i >= 0; i--) { msg[i] = (byte) (v & 0xFF); v >>= 8; }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(msg);
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        int otp = binary % 1000000;
        return String.format(java.util.Locale.US, "%06d", otp);
    }

    // RFC 4648 Base32 (no padding)
    private static final String ALPH = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public static String base32Encode(byte[] data) {
        StringBuilder out = new StringBuilder();
        int buffer = 0; int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 31;
                bitsLeft -= 5;
                out.append(ALPH.charAt(index));
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 31;
            out.append(ALPH.charAt(index));
        }
        return out.toString();
    }

    public static byte[] base32Decode(String s) {
        int buffer = 0; int bitsLeft = 0; java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (char c : s.toUpperCase().toCharArray()) {
            int val = ALPH.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out.write((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return out.toByteArray();
    }
}

