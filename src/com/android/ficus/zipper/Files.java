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

/**
 * Utilities for reading and writing to files on disk.
 */
public class Files {

    private static final String JSON_DATA_FILE = "clipperz.json";

    /**
     * Returns a File referencing the JSON data file of cards stored on disk.
     * @param context Context to use for finding the app's files directory
     */
    public static File getJsonDataFile(Context context) {
        return new File(context.getFilesDir(), JSON_DATA_FILE);
    }

    /**
     * Reads the entire contents of the given file and returns as a String.
     * @param file The file to read
     * @return The contents of the file as a string, or null if it can't be read
     */
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

    /**
     * Writes the given String out to the given File.
     * @param file The file to write to
     * @param data The string to write
     */
    public static void write(File file, String data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data.getBytes());
        fos.close();
    }
}
