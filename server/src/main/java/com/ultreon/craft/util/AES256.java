package com.ultreon.craft.util;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class AES256 {

    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final String DEFAULT_SALT = "HelloUltracraft!";

    @NotNull
    public static SecretKeySpec createKey(String secretKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return AES256.createKey(secretKey, AES256.DEFAULT_SALT);
    }

    @NotNull
    public static SecretKeySpec createKey(String secretKey, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        @SuppressWarnings("SpellCheckingInspection")
        var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        var spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), AES256.ITERATION_COUNT, AES256.KEY_LENGTH);

        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static byte @NotNull [] encrypt(byte[] data, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        var iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        var ivSpec = new IvParameterSpec(iv);
        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        var cipherText = cipher.doFinal(data);
        var encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

        return encryptedData;
    }

    public static byte @NotNull [] decrypt(byte @NotNull [] encrypted, SecretKey spec) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        var iv = new byte[16];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        var ivSpec = new IvParameterSpec(iv);

        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, spec, ivSpec);

        var cipherText = new byte[encrypted.length - 16];
        System.arraycopy(encrypted, 16, cipherText, 0, cipherText.length);

        return cipher.doFinal(cipherText);
    }

    public static void encryptFile(File input, File output, SecretKey key) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        try (var inputStream = new FileInputStream(input); var outputStream = new FileOutputStream(output)) {
            AES256.encrypt(inputStream, outputStream, key);
        }
    }
    
    public static void decryptFile(File input, File output, SecretKeySpec spec) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        try (var inputStream = new FileInputStream(input); var outputStream = new FileOutputStream(output)) {
            AES256.decrypt(inputStream, outputStream, spec);
        }
    }

    public static void encrypt(InputStream inputStream, OutputStream outputStream, SecretKey key) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var bytes = inputStream.readAllBytes();
        var encrypted = AES256.encrypt(bytes, key);
        outputStream.write(encrypted);
    }
    
    public static void decrypt(InputStream inputStream, OutputStream outputStream, SecretKeySpec spec) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var bytes = inputStream.readAllBytes();
        var decrypted = AES256.decrypt(bytes, spec);
        outputStream.write(decrypted);
    }
}