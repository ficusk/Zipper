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

import java.util.List;

/**
 * One card, holding some number of key/value fields.
 */
public class Card {
    /** The display name of this card. */
    public final String label;
    /** An ordered list of key/value fields for this card. */
    public final List<Field> fields;

    /**
     * Create a new card.
     * @param label Display name of this card
     * @param fields List of fields
     */
    public Card(String label, List<Field> fields) {
        this.label = label;
        this.fields = fields;
    }

    /**
     * One name/value field in a card.
     */
    public static class Field {
        public Field(String name, String value, boolean hidden) {
            this.name = name;
            this.value = value;
            this.hidden = hidden;
        }

        public final String name;
        public final String value;
        public final boolean hidden;
    }


    /**
     * Get a list of cards from the provided data string, attempting to auto-detect
     * the correct format. Return null if the string can't be parsed into a supported
     * format.
     */
    public static List<Card> from(String data) {
        List<Card> clipperz = ClipperzFormat.from(data);
        if (clipperz != null) {
            return clipperz;
        }

        List<Card> lastpass = LastpassFormat.from(data);
        if (lastpass != null) {
            return lastpass;
        }

        return null;
    }
}
