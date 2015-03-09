package com.firstbuild.androidapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ryanlee on 3/9/15.
 */
public class DeviceListAdapter extends BaseAdapter{
    private final String TAG = "DeviceListAdapter";
    private LayoutInflater layoutInflater = null;
    private List<BluetoothDevice> mData;

    public DeviceListAdapter(Context context) {
        Log.d(TAG, "IN DeviceListAdapter");
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        Log.d(TAG, "IN setData");
        mData = data;
    }

    public int getCount() {
        Log.d(TAG, "IN getCount");
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        Log.d(TAG, "IN getItem");

        return null;
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "IN getItemId");

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "IN getView");

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_ble_device, null);
        }

        BluetoothDevice device	= mData.get(position);
        TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
        TextView deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
        TextView deviceBond = (TextView) convertView.findViewById(R.id.bond_state);

        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());
        deviceBond.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");



        return convertView;
    }
}
