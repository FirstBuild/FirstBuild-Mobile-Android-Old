package com.firstbuild.androidapp.opal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleListener;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.commonframework.blemanager.BleValues;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hans on 16. 6. 16..
 */
public class OpalMainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1234;

    private static final String TAG = OpalMainActivity.class.getSimpleName();

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opal_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).
                setText(getString(R.string.product_name_opal).toUpperCase());
        setSupportActionBar(toolbar);

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.frame_content, new OpalMainFragment()).
                commit();

        // Check bluetooth adapter. If the adapter is disabled, enable it
        boolean result = BleManager.getInstance().isBluetoothEnabled();

        if (!result) {
            Log.d(TAG, "Bluetooth adapter is disabled. Enable bluetooth adapter.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth adapter is already enabled. Start connect");
        }
    }
}

