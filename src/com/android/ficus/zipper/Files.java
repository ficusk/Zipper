package com.android.ficus.zipper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Files {
    public static String read(File file) {
        if (!file.exists()) {
            return null;
        }
        String result = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
            result = new String(baos.toByteArray());
            baos.close();
            fis.close();
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    public static void write(File file, String data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data.getBytes());
        fos.close();
    }
}
