/*
 * Copyright (C) 2013 Ficus Kirkpatrick
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

import com.android.ficus.zipper.Card.Field;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LastpassFormat {
    private static final Set<String> HIDDEN_FIELDS = new HashSet<String>();
    private static final Set<String> IGNORE_VALUES = new HashSet<String>();
    static {
        HIDDEN_FIELDS.add("password");

        IGNORE_VALUES.add("");
        IGNORE_VALUES.add("0");
        IGNORE_VALUES.add("http://");
    }

    /**
     * Parse a list of cards from a CSV file as exported by LastPass
     * @return A list of cards or null if the string cannot be parsed
     */
    public static List<Card> from(String data) {
        String[] lines = TextUtils.split(data, "[\r\n]+");
        // Need at least a header row and one data row.
        if (lines.length < 2) {
            return null;
        }
        // TODO: Doesn't support escaping / commas in field names.
        String[] names = TextUtils.split(lines[0], ",");
        List<Card> cards = new ArrayList<Card>();
        for (int i = 1; i < lines.length; i++) {
            List<Field> fields = new ArrayList<Field>();
            CsvTokenizer csv = new CsvTokenizer(lines[i]);
            String cardName = "???";
            int j = 0;
            String value;
            while ((value = csv.next()) != null) {
                // All fields must be named. If we overrun the field names
                // row, assume the file is bad and bail out.
                if (j > names.length) {
                    return null;
                }
                String name = names[j++];
                if (name.equals("name")) {
                    cardName = value;
                    continue;
                }
                if (IGNORE_VALUES.contains(value)) {
                    continue;
                }
                fields.add(new Field(name, value, HIDDEN_FIELDS.contains(name)));
            }
            cards.add(new Card(cardName, fields));
        }

        return cards;
    }

    // Not my best work but seems to do OK for my needs here.
    private static class CsvTokenizer {
        private final String line;
        private final int len;
        private int start;

        public CsvTokenizer(String line) {
            this.line = line;
            this.len = line.length();
            this.start = 0;
        }

        public String next() {
            if (start >= len) {
                return null;
            }

            // Catch the simple empty case.
            char first = line.charAt(start);
            if (first == ',') {
                start++;
                return "";
            }

            char lookingFor = (first == '"') ? '"' : ',';
            if (lookingFor == '"') start++; // skip

            int end = start;
            while (end < len) {
                char c = line.charAt(end);
                if (c == lookingFor) {
                    break;
                }
                end++;
            }
            String token = line.substring(start, end);
            start = end + 1;
            if (lookingFor == '"') {
                // Next one should be a comma, if not, it's malformed.
                if (line.charAt(start) != ',') {
                    return null;
                }
                start++;
            }

            return token;
        }
    }
}
