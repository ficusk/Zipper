/*
 * Copyright (C) 2011 Ficus Kirkpatrick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ficus.zipper;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utilities for reading and writing to files on disk.
 */
public class Files {

    private static final String JSON_DATA_FILE = "cards.dat";

    /**
     * Returns a File referencing the JSON data file of cards stored on disk.
     * @param context Context to use for finding the app's files directory
     */
    public static File getJsonDataFile(Context context) {
        return new File(context.getFilesDir(), JSON_DATA_FILE);
    }

    /**
     * Reads the entire contents of the given file and returns as a byte[].
     * @param file The file to read
     * @return The contents of the file as a byte[], or null if it can't be read
     */
    private static byte[] readBytes(File file) {
        if (!file.exists()) {
            return null;
        }
        byte[] result = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
            result = baos.toByteArray();
            baos.close();
            fis.close();
        } catch (IOException e) {
            return null;
        }
        return result;
    }

//    /**
//     * Reads the entire contents of the given file and returns as a String.
//     * @param file The file to read
//     * @return The contents of the file as a String, or null if it can't be read
//     */
//    private static String read(File file) {
//        byte[] data = readBytes(file);
//        return data == null ? null : new String(data);
//    }
//
//    /**
//     * Writes the given String out to the given File.
//     */
//    private static void write(File file, String data) throws IOException {
//        write(file, data.getBytes());
//    }

    /**
     * Writes the given byte array out to the given File.
     */
    private static void write(File file, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    /**
     * Generates an AES {@link SecretKeySpec} for the given cleartext password.
     */
    private static SecretKeySpec generateAesKey(String password) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom random = new SecureRandom(password.getBytes());
        keyGenerator.init(random);
        SecretKey key = keyGenerator.generateKey();
        return new SecretKeySpec(key.getEncoded(), "AES");
    }

    /**
     * AES-encrypts a string.
     * @param string The string to encrypt
     * @param password The cleartext password to use for generating a key
     * @return The encrypted data as a byte[]
     */
    private static byte[] aesEncryptString(String string, String password)
            throws GeneralSecurityException {
        SecretKeySpec keySpec = generateAesKey(password);
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return aesCipher.doFinal(string.getBytes());
    }

    /**
     * AES-decrypts a string.
     * @param data The data to decrypt
     * @param password The cleartext password to use for generating a key
     * @return The decrypted data as a String
     */
    private static String aesDecryptString(byte[] data, String password)
            throws GeneralSecurityException {
        SecretKeySpec keySpec = generateAesKey(password);
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = aesCipher.doFinal(data);
        return new String(decrypted);
    }

    public static void writeEncrypted(File file, String data, String password) throws IOException {
        try {
            byte[] encrypted = aesEncryptString(data, password);
            write(file, encrypted);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readEncrypted(File file, String password) {
        try {
            byte[] encrypted = readBytes(file);
            return aesDecryptString(encrypted, password);
        } catch (GeneralSecurityException e) {
            // Meh. Better than dealing with Java's 9000 different crypto exceptions
            // that will never happen because everyone has AES.
            return null;
        }
    }
}
