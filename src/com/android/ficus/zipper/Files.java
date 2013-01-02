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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Cipher;
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
     * Generates an AES {@link SecretKeySpec} for the given cleartext password.
     */
    private static SecretKeySpec generateAesKey(String secret)
            throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha.digest(secret.getBytes());
        byte[] key = new byte[16];
        System.arraycopy(hash, 0, key, 0, 16);
        return new SecretKeySpec(key, "AES");
    }

    /**
     * AES-encrypts a string.
     * @param string The string to encrypt
     * @param password The salted cleartext password to use for generating a key
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
     * @param password The salted cleartext password to use for generating a key
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

    private static final int FILE_VERSION = 1;

    /*
     * The on-disk file uses a Java Data{Input,Output}Stream in the following format:
     * int version       | version of this file format
     * UTF salt          | salt for the password
     * int data length   | number of bytes to follow
     * byte[]...         | AES encrypted data with the key SHA1[0:15](salt + password)
     */

    public static void writeEncrypted(File file, String data, String password) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeInt(FILE_VERSION);
            String salt = UUID.randomUUID().toString();
            dos.writeUTF(salt);
            byte[] encrypted = aesEncryptString(data, salt + password);
            dos.writeInt(encrypted.length);
            dos.write(encrypted);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    public static String readEncrypted(File file, String password) {
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            int version = dis.readInt();
            if (version != FILE_VERSION) {
                // XXX close stream
                return null;
            }
            String salt = dis.readUTF();
            int dataLen = dis.readInt();
            byte[] encrypted = new byte[dataLen];
            dis.readFully(encrypted);
            return aesDecryptString(encrypted, salt + password);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
        } catch (GeneralSecurityException e) {
            // Meh. Better than dealing with Java's 9000 different crypto exceptions
            // that will never happen because everyone has AES.
        }
        return null;
    }
}
