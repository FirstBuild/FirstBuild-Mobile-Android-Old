package com.firstbuild.androidapp;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.List;

/**
 * Created by ryanlee on 3/9/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AttributeListAdapter extends BaseExpandableListAdapter {
    private List<BluetoothGattService> services;

    public AttributeListAdapter(List<BluetoothGattService> services){
        this.services = services;
    }

    @Override
    public int getGroupCount() {
        return services.size();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return services.get(groupPosition).getCharacteristics().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return services.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return services.get(groupPosition).getCharacteristics().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupName = services.get(groupPosition).toString();
        View v = convertView;
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
