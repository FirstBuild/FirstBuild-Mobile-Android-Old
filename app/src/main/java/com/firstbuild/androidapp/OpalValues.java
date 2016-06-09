package com.firstbuild.androidapp;

/**
 * Created by hans on 16. 5. 31..
 */
public class OpalValues {

    // BLE Service UUID
    public static final String OPAL_BLE_SERVICE_UUID = "BE87748C-BFBE-9E90-CC40-2994C563673E";
    public static final String OPAL_OP_STATE_UUID = "5155E431-2F7D-B587-2F43-0DCA51277A09";
    public static final String OPAL_OP_MODE_UUID = "D04D8D1A-DD02-C985-CD40-044B30429979";
    public static final String OPAL_LIGHT_UUID = "2EC03565-ADAF-8399-2D4A-39EA008F9837";
    public static final String OPAL_CLEAN_CYCLE_UUID = "AF9A0DAF-81DC-F6B3-D747-000677BDE4EF";
    public static final String OPAL_TIME_SYNC_UUID = "FC07C2A6-39D4-E2AF-F447-F1FB84079EED";
    public static final String OPAL_SET_SCHEDULE_UUID = "470ED63A-5A5C-8498-8544-5ECB73E81A9E";
    public static final String OPAL_ENABLE_DISABLE_SCHEDULE_UUID = "889B4AAC-431A-DC95-2547-9210B36351B4";
    public static final String OPAL_IMG_TYPE_UUID = "B4A4437B-5236-9999-DB41-5920C770A35E";
    public static final String OPAL_VERSION_CHAR_UUID = "CC0EA478-9EBB-218E-144F-8766B6A588CF";
    public static final String OPAL_UPDATE_PROGRESS_UUID = "46849A1A-9E75-7D92-5644-FA36FB6DFF14";
    public static final String OPAL_ERROR_CHAR_UUID = "B6074798-FB99-4B0F-B694-80DEB1F6CB5B";
    public static final String OPAL_TEMPERATURE_CHAR_UUID = "6B0A45F1-BF83-8DB7-4748-CEB5305020BD";
    public static final String OPAL_FILTER_INSTALL_CHAR_UUID = "2593AA95-EBF2-6B99-8C46-2CE9DDF2FD9B";
    public static final String OPAL_PUMP_CYCLE_CHAR_UUID = "53579184-0E10-429D-0E49-9C62F88DF76A";


    // OTA Upgrade Service UUID
    public static final String OPAL_OTA_UPGRADE_SERVICE_UUID = "7BF2A1CD-6AF4-48B6-A7FA-D08D7A8736E9";
    public static final String OPAL_CONTROL_COMMAND_CHAR_UUID = "FF8F637F-4AE2-4F48-A0E5-0762B14AB34F";
    public static final String OPAL_IMAGE_DATA_CHAR_UUID = "E5142470-74EC-497D-B6C3-6030E52A2878";
    public static final String OPAL_OTA_APP_INFO_CHAR_UUID = "CE44C7A0-EC1E-416A-9B11-F167F5B18D31";

    // OP State Char , Permission > Read only , Property > Read/Notify
    public static final byte OPAL_STATE_IDLE = 0x00;
    public static final byte OPAL_STATE_ICE_MAKING = 0x01;
    public static final byte OPAL_STATE_ADD_WATER = 0x02;
    public static final byte OPAL_STATE_ICE_FULL = 0x03;
    public static final byte OPAL_STATE_CLEANING = 0x04;

    // OP Mode Char , Permission > Read/Write , Property > Read/Write/Notify
    // Clean mode cannot set by app
    public static final byte OPAL_MODE_OFF = 0x00;
    public static final byte OPAL_MODE_ICE_MAKING = 0x01;
    public static final byte OPAL_MODE_CLEAN = 0x02;

    // Light Char , Permission > Read/Write , Property > Read/Write/Notify
    // Available during Opal is on
    public static final byte OPAL_DAY_TIME_LIGHT = 0x00;
    public static final byte OPAL_NIGHT_TIME_LIGHT = 0x01;

    //Clean cycle Char, Permission > Read only , Property > Read/Notify
    public static final byte OPAL_CYCLE_ONE = 0x00;
    public static final byte OPAL_CYCLE_TWO = 0x01;
    public static final byte OPAL_CYCLE_THREE = 0x02;
    public static final byte OPAL_CYCLE_FOUR = 0x03;
    public static final byte OPAL_CYCLE_FIVE = 0x04;

    // Enable/Disable schedule , Permission > Read/Write , Property > Read/Write
    public static final byte OPAL_DISABLE_SCHEDULE = 0x00;
    public static final byte OPAL_ENABLE_SCHEDULE = 0x01;

    // Image Type char , Permission > Read/Write , Property > Read/Write
    public static final byte OPAL_BLE_IMAGE_TYPE = 0x00;
    public static final byte OPAL_OPAL_IMAGE_TYPE = 0x01;


    // BLE return code
    public static final String OPAL_BLE_RET_SUCCESS = "0x00";
    public static final String OPAL_BLE_RET_OUT_OF_RANGE = "0x80";
    public static final String OPAL_BLE_RET_WRITE_NOT_ALLOWED = "0x90";
    public static final String OPAL_BLE_RET_ILLEGAL_OTA_STATE_TRANSITION = "0xE0";

    // Target device Opal's name
    public static final String TARGET_DEVICE_NAME = "OPAL Bluetooth";
}
