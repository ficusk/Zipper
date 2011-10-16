package com.android.ficus.zipper;

import com.android.ficus.zipper.ClipperzCard.ClipperzField;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

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
        String text = field.name + ": ";
        text += (field.hidden ? "<HIDDEN>" : field.value);
        TextView fieldView = (TextView) convertView.findViewById(R.id.field_text);
        fieldView.setText(text);
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
