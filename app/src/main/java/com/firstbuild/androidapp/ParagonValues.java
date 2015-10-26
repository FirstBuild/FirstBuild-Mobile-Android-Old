package com.firstbuild.androidapp;

/**
 * Created by RyanLee on 6/26/15/FW26.
 */
public class ParagonValues {

    // device specific characteristics
    public static final String CHARACTERISTIC_SPECIAL_FEATURES          = "E7CDDD9D-DCAC-4D70-A0E1-D3B6DFEB5E4C";
    public static final String CHARACTERISTIC_PROBE_FIRMWARE_INFO       = "83D33E5C-68EA-4158-8655-1A2AC0313FF6";
    public static final String CHARACTERISTIC_APP_INFO                  = "318DB1F5-67F1-119B-6A41-1EECA0C744CE";
    public static final String CHARACTERISTIC_BATTERY_LEVEL             = "A74C3FB9-6E13-B4B9-CD47-465AAD76FCE7"; //read,notify

    //cooking specific characteristics
    public static final String CHARACTERISTIC_BURNER_STATUS             = "B98F1B81-F098-2CBA-7940-EB8C0FFD7BDC"; //read,notify
    public static final String CHARACTERISTIC_ELAPSED_TIME              = "7FCE08B6-B7B2-168B-C44F-5E576045FD2E";
    public static final String CHARACTERISTIC_COOK_TIME                 = "";

    public static final String CHARACTERISTIC_TARGET_TEMPERATURE        = "";
    public static final String CHARACTERISTIC_CURRENT_TEMPERATURE       = "8F080B1C-7C3B-FBB9-584A-F0AFD57028F0";

    // NEW ADDED SERVICES.
    public static final String CHARACTERISTIC_PARAGON_MASTER_SERVICE = "D6B2767C-C5B6-0088-DC-4A-53-3E-B5-9C-A1-90";
    public static final String CHARACTERISTIC_COOK_CONFIGURATION = "E0BA615A-A869-1C9D-BE45-4E3B83F592D9";
    public static final String CHARACTERISTIC_CURRENT_POWER_LEVEL = "B2019449-F5B4-4198-96-B4-C1-48-AC-94-18-00";
    public static final String CHARACTERISTIC_CURRENT_COOK_STATE = "F80ABE44-C3D5-E99A-6B-46-99-DD-F2-27-F8-2D";
    public static final String CHARACTERISTIC_CURRENT_COOK_STAGE = "BB641183-73D8-4FC4-A7-E1-7D-3D-B6-6F-CA-A7";
    public static final String CHARACTERISTIC_TEMPERATURE_DISPLAY_UNIT = "C1382B17-2DE7-4593-BC-49-3B-1-50-2D-42-C7";
    public static final String CHARACTERISTIC_START_HOLD_TIMER = "4F568285-9D2F-4C3D-86-4E-70-47-A3-B-D4-A8";
    public static final String CHARACTERISTIC_PROBE_CONNECTION_STATE = "6B402ECC-3DDA-8BB4-9E-42-F1-21-D7-E1-CF-69";
    public static final String CHARACTERISTIC_PROBE_FW_INFO = "83D33E5C-68EA-4158-86-55-1A-2A-C0-31-3F-F6";
    public static final String CHARACTERISTIC_LAST_KNOWN_ERROR = "5BCBF6B1-DE80-94B6-0F-4B-99-FB-98-47-07-B6";
    public static final String CHARACTERISTIC_USER_INFO = "007A7511-0D69-4749-AA-E3-85-6C-FF-25-79-12";
    public static final String CHARACTERISTIC_PARAGON_PROBE_SERVICE = "CFA29500-5F33-498E-9F-43-C5-09-60-D2-23-F8";
    public static final String CHARACTERISTIC_ERROR_STATE = "7EE9F201-120D-1CB9-E2-48-9D-48-55-F6-83-35";

    // Target device's name
    public static final String TARGET_DEVICE_NAME = "Paragon Master";

    public static final int MAX_COOK_TIME = 100;
}
