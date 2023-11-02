package com.ultreon.craft.util;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
    public static byte @NotNull [] hashMD5(byte @NotNull [] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean verifyMD5(byte @NotNull [] input, byte @NotNull [] hash) {
        byte[] generatedHash = Hashing.hashMD5(input);

        if (generatedHash.length != hash.length) {
            return false;
        }

        for (int i = 0; i < generatedHash.length; i++) {
            if (generatedHash[i] != hash[i]) {
                return false;
            }
        }

        return true;
    }

}
