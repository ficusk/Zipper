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

import com.android.ficus.zipper.ClipperzCard.ClipperzField;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * An ExpandableListAdapter that exposes each {@link ClipperzCard} as a group,
 * with each underlying field as a child of the group.
 */
public class ZipperAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    private final List<ClipperzCard> mCards;

    public ZipperAdapter(Context context, List<ClipperzCard> cards) {
        mContext = context;
        mCards = cards;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mCards.get(groupPosition).fields.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 1000 + childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.field_view, null);
        }

        ClipperzField field = mCards.get(groupPosition).fields.get(childPosition);
        TextView fieldName = (TextView) convertView.findViewById(R.id.field_name);
        fieldName.setText(field.name);

        // TODO: Add an option for showing/hiding passwords?
        TextView fieldValue = (TextView) convertView.findViewById(R.id.field_value);
        fieldValue.setText(field.hidden ? mContext.getString(R.string.hidden): field.value);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mCards.get(groupPosition).fields.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCards.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mCards.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.card_view, null);
        }
        TextView label = (TextView) convertView.findViewById(R.id.label);
        label.setText(mCards.get(groupPosition).label);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
