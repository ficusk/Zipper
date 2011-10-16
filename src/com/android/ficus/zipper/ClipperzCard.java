package com.android.ficus.zipper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ClipperzCard {
    public ClipperzCard(String label, List<ClipperzField> fields) {
        this.label = label;
        this.fields = fields;
    }

    public final String label;
    public final List<ClipperzField> fields;

    public static List<ClipperzCard> from(String jsonString) {
        JSONTokener tokener = new JSONTokener(jsonString);
        try {
            JSONArray root = (JSONArray) tokener.nextValue();
            List<ClipperzCard> cards = new ArrayList<ClipperzCard>();
            for (int i = 0; i < root.length(); i++) {
                JSONObject obj = root.getJSONObject(i);
                String label = obj.getString("label");
                JSONObject rawFields = obj.getJSONObject("currentVersion").getJSONObject("fields");
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
                cards.add(new ClipperzCard(label, fields));
            }
            return cards;
        } catch (JSONException e) {
            return null;
        }
    }

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
}
