package com.firstbuild.androidapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;



/**
 * Created by ryanlee on 3/9/15.
 */
public class DeviceListAdapter extends BaseAdapter{
    private final String TAG = "DeviceListAdapter";
    private LayoutInflater layoutInflater = null;
    private ArrayList<BluetoothDevice> mData = new ArrayList<BluetoothDevice>();

    public DeviceListAdapter(Context context) {
        Log.d(TAG, "IN DeviceListAdapter");
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<BluetoothDevice> data){
        mData  = data;
    }

    public int getCount() {
        Log.d(TAG, "IN getCount");
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        Log.d(TAG, "IN getItem");
        return mData.get(position);
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

            // Hold text views not to load resource every time.
            TextViewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            TextViewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            TextViewHolder.deviceBond = (TextView) convertView.findViewById(R.id.bond_state);
        }

        BluetoothDevice device = (BluetoothDevice) getItem(position);

        TextViewHolder.deviceName.setText(device.getName());
        TextViewHolder.deviceAddress.setText(device.getAddress());
        TextViewHolder.deviceBond.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Paired" : "Not Pair");

        return convertView;
    }

    // Text View hold class
    static class TextViewHolder {
        static TextView deviceName;
        static TextView deviceAddress;
        static TextView deviceBond;
    }
}
