package com.ultreon.craft.util;

public class Numbers {
    public static Integer toIntOrNull(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double toDoubleOrNull(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long toLongOrNull(String number) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Short toShortOrNull(String number) {
        try {
            return Short.parseShort(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Float toFloatOrNull(String number) {
        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static Byte toByteOrNull(String number) {
        try {
            return Byte.parseByte(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static Boolean toBooleanOrNull(String bool) {
        try {
            return Boolean.parseBoolean(bool);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}