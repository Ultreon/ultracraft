package com.ultreon.craft.util;

public class Seeder {
    public static long hash(byte[] data) {
        if (data == null) return 0;

        long result = 1;
        for (byte element : data) {
            result = 31L * result + element;
        }

        return result;
    }

    public static long hash(char[] data) {
        if (data == null) return 0;

        long result = 1;
        for (char element : data) {
            result = 31L * result + element;
        }

        return result;
    }


    // intrinsic performs no bounds checks
    static char getChar(byte[] val, int index) {
        index <<= 1;
        return (char) (((val[index++] & 0xff) << 8) | ((val[index] & 0xff)));
    }
}
