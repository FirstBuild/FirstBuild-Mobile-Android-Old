/**
 * @file BleValues.java
 * @brief This class has constant values used in other bluetooth modules
 * @author Ryan Lee (strike77@gmail.com)
 * @date Jun/29/2015
 */

package com.firstbuild.commonframework.bleManager;

public class BleValues {

    // Scan related variables
    public static final int STOP_SCAN = 0;
    public static final int START_SCAN = 1;

    // Bluetooth connection state related variables
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    // Set request enable bluetooth true
    public static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    public static final long SCAN_PERIOD = 10000;
}
