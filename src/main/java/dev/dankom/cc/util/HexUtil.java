package dev.dankom.cc.util;

import java.util.Base64;

public class HexUtil {
    public static String hexFromBytes(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] hexToBytes(String s) {
        return Base64.getDecoder().decode(s);
    }
}
