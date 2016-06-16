package com.firstbuild.androidapp.addproduct;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleListener;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.commonframework.blemanager.BleValues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hans on 16. 6. 3..
 */
public abstract class AddProductSearchFragment extends Fragment {

    protected ImageView spinningImage;

    private final int SPINNING_REPEAT_COUNT = 30;
    private String TAG = AddProductActivity.class.getSimpleName();
    private RotateAnimation spinningAnimation;
    private String deviceAddress = null;
    private AddProductActivity attached = null;
    private BleListener bleListener = new BleListener() {
        @Override
        public void onScanDevices(HashMap<String, BluetoothDevice> bluetoothDevices) {
            super.onScanDevices(bluetoothDevices);

            Log.d(TAG, "onScanDevices IN");

            Log.d(TAG, "bluetoothDevices size: " + bluetoothDevices.size());
            for (Map.Entry<String, BluetoothDevice> entry : bluetoothDevices.entrySet()) {

                // Retrieves address and name
                BluetoothDevice device = entry.getValue();
                deviceAddress = device.getAddress();
                String name = device.getName();
                int bondState = device.getBondState();

                Log.d(TAG, "------------------------------------");
                Log.d(TAG, "Device address: " + deviceAddress);
                Log.d(TAG, "Device name: " + name);
                Log.d(TAG, "Device Bond state: " + device.getBondState());

                if (name.contains(getCurrentProductDefaultName())) {
                    Log.d(TAG, "device found: " + device.getName());
                    boolean isFound = false;

                    if (bondState == device.BOND_NONE) {
                        Log.d(TAG, "device not bonded: " + bondState);
                        // Pair to device
                        BleManager.getInstance().pairDevice(device);
                    }
                    else if (bondState == device.BOND_BONDED || bondState == device.BOND_BONDING) {
                        Log.d(TAG, "device bonded: " + bondState);

                        if (ProductManager.getInstance().getProductByAddress(deviceAddress) == null) {
                            isFound = true;
                        }
                    }
                    else {
                        Log.d(TAG, "device bonded or bonding state: " + bondState);

                        // This part must be replace with proper warning page
                        goToErrorScreen();
                    }


                    if (isFound) {
                        // Stop ble device scanning
                        BleManager.getInstance().stopScan();

                        // case for Paragon Master already paired but not in dashboard.
                        attached.setNewProductAddress(deviceAddress);

                        // Transit to success UI
                        transitionToPairSuccessScreen();

                        break;
                    }
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
            }
            else {
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
        }

        @Override
        public void onCharacteristicRead(String address, String uuid, byte[] value) {
            super.onCharacteristicRead(address, uuid, value);

            Log.d(TAG, "[onCharacteristicRead] address: " + address + ", uuid: " + uuid);

        }
    };

    public AddProductSearchFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchParagonFragment IN");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult - requestCode: " + requestCode);
        Log.d(TAG, "onActivityResult - resultCode: " + resultCode);

        if (requestCode == BleValues.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Success
                Log.d(TAG, "Bluetooth adapter is enabled. Start scanning.");
                spinningAnimation.setRepeatCount(SPINNING_REPEAT_COUNT);
                BleManager.getInstance().startScan();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Bluetooth adapter is still disabled");
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.content_frame, new AddProductSelectFragment()).
                        addToBackStack(null).
                        commit();
            }
            else {
                // Else
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AddProductActivity) {
            attached = (AddProductActivity) context;
        }
        else {
            throw new ClassCastException(context + " must be an instance of "
                    + AddProductActivity.class.getSimpleName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BleValues.REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start scanning.");
            spinningAnimation.setRepeatCount(SPINNING_REPEAT_COUNT);
            BleManager.getInstance().startScan();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = null;
        BleManager.getInstance().removeListener(bleListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_searching, container, false);

        // Set and start spinning image
        spinningImage = (ImageView) view.findViewById(R.id.imgSpinner);
        spinningImage.setAnimation(makeAnimation());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ble manager
        BleManager.getInstance().initBleManager(this.getActivity());

        // Add ble event listener
        BleManager.getInstance().addListener(bleListener);
    }

    protected RotateAnimation makeAnimation() {

        spinningAnimation = new RotateAnimation(0.f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        spinningAnimation.setDuration(1000);
        spinningAnimation.setFillAfter(true);
        spinningAnimation.setRepeatCount(Animation.INFINITE);

        spinningAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
//                Log.d(TAG, "onAnimationStart IN");
            }

            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd IN");

                BleManager.getInstance().stopScan();

                // Cannot found paragon - go to error screen
                goToErrorScreen();
            }

            public void onAnimationRepeat(Animation animation) {
//                Log.d(TAG, "onAnimationRepeat IN");
            }
        });

        return spinningAnimation;
    }

    private void goToErrorScreen() {

        getFragmentManager().
                beginTransaction().
                replace(R.id.content_frame, AddProductConnectionErrorFragment.newInstance(getCurrentApplianceType())).
                addToBackStack(null).
                commit();
    }

    private String getCurrentProductDefaultName() {
        String ret = "";

        if (getCurrentApplianceType() == ProductInfo.PRODUCT_TYPE_OPAL) {
            ret = OpalValues.TARGET_DEVICE_NAME;

        } else if (getCurrentApplianceType() == ProductInfo.PRODUCT_TYPE_PARAGON) {
            ret = ParagonValues.TARGET_DEVICE_NAME;
        }

        return ret;
    }

    abstract protected int getCurrentApplianceType();
    abstract protected void transitionToPairSuccessScreen();
}
