package com.ultreon.craft.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;

import java.security.*;

public class Hashing {
    public static byte @NotNull [] md5(byte @NotNull [] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean md5Verify(byte @NotNull [] input, byte @NotNull [] hash) {
        byte[] generatedHash = Hashing.md5(input);

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

    public static byte @NotNull [] sha1(byte @NotNull [] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean sha1Verify(byte @NotNull [] input, byte @NotNull [] hash) {
        byte[] generatedHash = Hashing.sha1(input);

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

    public static byte @NotNull [] sha256(byte @NotNull [] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean sha256Verify(byte @NotNull [] input, byte @NotNull [] hash) {
        byte[] generatedHash = Hashing.sha256(input);

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

    public static byte @NotNull [] sha512(byte @NotNull [] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @CanIgnoreReturnValue
    public static boolean sha512Verify(byte @NotNull [] input, byte @NotNull [] hash) {
        byte[] generatedHash = Hashing.sha512(input);

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
