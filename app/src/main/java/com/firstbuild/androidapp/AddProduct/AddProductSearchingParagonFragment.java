package com.firstbuild.androidapp.addProduct;


import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.commonframework.bleManager.BleListener;
import com.firstbuild.commonframework.bleManager.BleManager;
import com.firstbuild.commonframework.bleManager.BleValues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductSearchingParagonFragment extends Fragment {
    private String TAG = AddProductActivity.class.getSimpleName();

    public AddProductSearchingParagonFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchingParagonFragment IN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_searching_paragon, container, false);

//        view.findViewById(R.id.imgSpinner).startAnimation(makeAnimation());

        // Initialize ble manager
        BleManager.getInstance().initBleManager(this.getActivity());

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);

        // Scan device here
        Log.d(TAG, "Scan BLE device.");
        BleManager.getInstance().startScan();

        return view;
    }


    private RotateAnimation makeAnimation() {

        RotateAnimation animation = new RotateAnimation(0, 360);
        animation.setDuration(250);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }
        });

        return animation;
    }

    private BleListener bleListener = new BleListener() {
        @Override
        public void onScanDevices(HashMap<String, BluetoothDevice> bluetoothDevices) {
            super.onScanDevices(bluetoothDevices);

            Log.d(TAG, "onScanDevices IN");

            Log.d(TAG, "bluetoothDevices size: " + bluetoothDevices.size());
            for (Map.Entry<String, BluetoothDevice> entry : bluetoothDevices.entrySet()) {

                // Retrieves address and name
                BluetoothDevice device = entry.getValue();
                String address = device.getAddress();
                String name = device.getName();

                Log.d(TAG, "------------------------------------");
                Log.d(TAG, "Device address: " + address);
                Log.d(TAG, "Device name: " + name);

                if (ParagonValues.TARGET_DEVICE_NAME.equals(name)) {
                    Log.d(TAG, "device found: " + device.getName());

                    // Connect to device
                    BleManager.getInstance().connect(address);

                    // Stop ble device scanning
                    BleManager.getInstance().stopScan();

                    break;
                }
            }
            Log.d(TAG, "====================================");
        }

        @Override
        public void onScanStateChanged(int status) {
            super.onScanStateChanged(status);

            Log.d(TAG, "[onScanStateChanged] status: " + status);

            if (status == BleValues.START_SCAN) {
                Log.d(TAG, "Scanning BLE devices");
            } else {
                Log.d(TAG, "Stop scanning BLE devices");
            }
        }

        @Override
        public void onConnectionStateChanged(final String address, final int status) {
            super.onConnectionStateChanged(address, status);

            Log.d(TAG, "[onConnectionStateChanged] address: " + address + ", status: " + status);
        }

        @Override
        public void onServicesDiscovered(String address, List<BluetoothGattService> bleGattServices) {
            super.onServicesDiscovered(address, bleGattServices);

            Log.d(TAG, "[onServicesDiscovered] address: " + address);

            // Transit to success UI
            getFragmentManager().
                    beginTransaction().
                    replace(R.id.content_frame, new AddProductFoundParagonFragment()).
                    addToBackStack(null).
                    commit();
        }
    };
}
