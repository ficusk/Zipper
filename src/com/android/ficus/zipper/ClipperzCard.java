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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * One Clipperz card.
 */
public class ClipperzCard {
    /** The display name of this card. */
    public final String label;
    /** An ordered list of key/value fields for this card. */
    public final List<ClipperzField> fields;

    /**
     * Create a new Clipperz card.
     * @param label Display name of this card
     * @param fields List of fields
     */
    public ClipperzCard(String label, List<ClipperzField> fields) {
        this.label = label;
        this.fields = fields;
    }

    /**
     * One name/value field in a Clipperz card.
     */
    public static class ClipperzField {
        public ClipperzField(String name, String value, boolean hidden) {
            this.name = name;
            this.value = value;
            this.hidden = hidden;
        }

        public final String name;
        public final String value;
        public final boolean hidden;
    }

    /**
     * Parse a list of Clipperz cards from a JSON-formatted string in the format
     * exported by Clipperz JSON Export mode.
     * @return A list of cards or null if the string cannot be parsed
     */
    public static List<ClipperzCard> from(String jsonString) {
        JSONTokener tokener = new JSONTokener(jsonString);
        try {
            List<ClipperzCard> cards = new ArrayList<ClipperzCard>();

            // Each card is a subobject in a root-level array.
            JSONArray root = (JSONArray) tokener.nextValue();
            for (int i = 0; i < root.length(); i++) {
                JSONObject obj = root.getJSONObject(i);
                String label = obj.getString("label");
                JSONObject rawFields = obj.getJSONObject("currentVersion").getJSONObject("fields");

                // Walk the fields in the JSON object and convert to ClipperzFields.
                List<ClipperzField> fields = new ArrayList<ClipperzField>();
                @SuppressWarnings("unchecked") Iterator<String> fieldIterator = rawFields.keys();
                while (fieldIterator.hasNext()) {
                    String fieldKey = fieldIterator.next();
                    JSONObject fieldObj = rawFields.getJSONObject(fieldKey);
                    ClipperzField field = new ClipperzField(
                            fieldObj.getString("label"),
                            fieldObj.getString("value"),
                            fieldObj.getBoolean("hidden"));
                    fields.add(field);
                }

                // Create a card and add it to the list.
                cards.add(new ClipperzCard(label, fields));
            }
            return cards;
        } catch (JSONException e) {
            return null;
        } catch (ClassCastException e) {
            // For valid JSON of the wrong type, e.g. an object when we are expecting
            // an array.
            return null;
        }
    }
}
