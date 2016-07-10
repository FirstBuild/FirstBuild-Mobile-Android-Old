package com.firstbuild.androidapp.paragon;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import com.firstbuild.androidapp.FirstBuildApplication;
import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.opal.OpalMainActivity;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.tools.MathTools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Hollis on 11/17/15.
 */
public class OtaManager {

    private static OtaManager ourInstance = new OtaManager();

    private String TAG = OtaManager.class.getSimpleName();

    private static final int OTA_STEP_NONE = 0;
    private static final int OTA_STEP_COMPARE_VERSION = 1;
    private static final int OTA_STEP_SEND_IMAGE_SIZE = 2;
    private static final int OTA_STEP_SEND_IMAGE_DATA = 3;
    private static final int OTA_STEP_SEND_IMAGE_DONE = 4;
    private static final int OTA_STEP_REBOOT = 5;


    private static final int OTA_BLE_STEP_NONE = OTA_STEP_REBOOT + 1;
    private static final int OTA_BLE_PREPARE_DOWNLOAD = OTA_BLE_STEP_NONE + 1;
    private static final int OTA_BLE_STEP_LOAD_IMAGE_AND_SEND_IMAGE_SIZE = OTA_BLE_PREPARE_DOWNLOAD + 1;
    private static final int OTA_BLE_STEP_SEND_IMAGE_DATA = OTA_BLE_STEP_LOAD_IMAGE_AND_SEND_IMAGE_SIZE + 1;
    private static final int OTA_BLE_STEP_SEND_IMAGE_DONE = OTA_BLE_STEP_SEND_IMAGE_DATA + 1;
    private static final int OTA_BLE_STEP_REBOOT = OTA_BLE_STEP_SEND_IMAGE_DONE + 1;

    private static final int OTA_OPAL_STEP_NONE = OTA_BLE_STEP_REBOOT + 1;
    private static final int OTA_OPAL_PREPARE_DOWNLOAD = OTA_OPAL_STEP_NONE + 1;
    private static final int OTA_OPAL_STEP_LOAD_IMAGE_AND_SEND_IMAGE_HEADER = OTA_OPAL_PREPARE_DOWNLOAD + 1;
    private static final int OTA_OPAL_STEP_SEND_IMAGE_DATA = OTA_OPAL_STEP_LOAD_IMAGE_AND_SEND_IMAGE_HEADER + 1;
    private static final int OTA_OPAL_STEP_SEND_IMAGE_DONE = OTA_OPAL_STEP_SEND_IMAGE_DATA + 1;
    private static final int OTA_OPAL_STEP_REBOOT = OTA_OPAL_STEP_SEND_IMAGE_DONE + 1;

    private static final int SIZE_CHUNK = 20;
    private static final int SIZE_CHECKSUM = 160;

    private Context context;
    private int versionMajor = 0;
    private int versionMinor = 0;
    private int versionBuild = 0;
    private int currentStep;
    private byte[] imageChunk = null;
    private int transferTotalCount = 0;
    private int transferOffset = 0;

    private BluetoothDevice currentDevice;
    private int otaImageSize; // in byte

    OpalMainActivity.OTAResultDelegate delegate;

    public static OtaManager getInstance() {
        return ourInstance;
    }

    private OtaManager() {
    }

    public int getTransferCount() {
        return transferTotalCount;
    }

    public int getTransferOffset() {
        return transferOffset;
    }

    /**
     * Read image file from asset and get majoro version and minor version.
     */
    public void readImageFile(Context context) {
        // Read image file file form asset and get current version.
        this.context = context;
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        byte[] versionChunk = new byte[4];

        try {

            String[] fileNames =assetManager.list("image");
            inputStream = assetManager.open("image/"+fileNames[0]);

            // Image file contained its version number in for four byte from 5th data.
            inputStream.skip(4);
            inputStream.read(versionChunk);
            inputStream.close();

            versionMajor = versionChunk[0];
            versionMinor = versionChunk[1];
            versionBuild = (short) versionChunk[2];

        }
        catch (Exception e) {
            Log.d(TAG, "Not found image file, Skip OTA");

            versionMajor = 0;
            versionMinor = 0;
            versionBuild = 0;
        }



        Log.d(TAG, "readImageFile version is :" + versionMajor + ", " + versionMinor);
    }

    /**
     * Compare the version number between get from Paragon Master and image file.
     *
     * @param versionMajor Major version number get from Paragon.
     * @param versionMinor Minor version number bet from Paragon.
     * @return If need to ge update then return true, other than false.
     */
    public boolean compareVersion(int versionMajor, int versionMinor, int versionBuild) {

        Log.d(TAG, "compareVersion : Paragon version :" + versionMajor + "." + versionMinor + "." + versionBuild);
        Log.d(TAG, "compareVersion : Image version   :" + this.versionMajor + "." + this.versionMinor + "." + this.versionBuild);

        boolean isNeedUpdate;

        if (this.versionMajor > versionMajor ||
            this.versionMinor > versionMinor ||
            this.versionBuild > versionBuild) {
            isNeedUpdate = true;
        }
        else {
            isNeedUpdate = false;
        }

        return isNeedUpdate;
    }


    public void startProcess(){
        currentStep = OTA_STEP_COMPARE_VERSION;

        ByteBuffer valueBuffer = ByteBuffer.allocate(3);

        valueBuffer.put((byte) 1);
//        BleManager.getInstance().writeCharacteristics(ParagonValues.CHARACTERISTIC_OTA_COMMAND, valueBuffer.array());
        Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND Send:" + valueBuffer.toString());
    }

    public void startBleOtaProcess(BluetoothDevice device, OpalMainActivity.OTAResultDelegate resultDelegate){

        resetOtaValues();

        currentStep = OTA_BLE_PREPARE_DOWNLOAD;
        currentDevice = device;
        delegate = resultDelegate;

        ByteBuffer valueBuffer = ByteBuffer.allocate(1);

        Log.d(TAG, "startBleOtaProcess : Sending image type : " + OpalValues.OPAL_BLE_IMAGE_TYPE);
        valueBuffer.put(OpalValues.OPAL_BLE_IMAGE_TYPE);
        BleManager.getInstance().writeCharacteristics(device, OpalValues.OPAL_IMG_TYPE_UUID, valueBuffer.array());
        valueBuffer.clear();

        Log.d(TAG, "startBleOtaProcess : Sending Prepare Download Control Command : " + OpalValues.OPAL_CONTROL_COMMAND_PREPARE_DOWNLOAD);
        valueBuffer.put(OpalValues.OPAL_CONTROL_COMMAND_PREPARE_DOWNLOAD);
        BleManager.getInstance().writeCharacteristics(device, OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID, valueBuffer.array());

        // Should wait for the notification value 0x00 for OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID
        Log.d(TAG, "startBleOtaProcess : Should wait for the notification value 0x00 for OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID");
    }

    public void startOpalOtaProcess(BluetoothDevice device, OpalMainActivity.OTAResultDelegate resultDelegate){

        resetOtaValues();

        currentStep = OTA_OPAL_PREPARE_DOWNLOAD;
        currentDevice = device;
        delegate = resultDelegate;

        ByteBuffer valueBuffer = ByteBuffer.allocate(1);

        Log.d(TAG, "startOpalOtaProcess : Sending image type : " + OpalValues.OPAL_OPAL_IMAGE_TYPE);
        valueBuffer.put(OpalValues.OPAL_OPAL_IMAGE_TYPE);
        BleManager.getInstance().writeCharacteristics(device, OpalValues.OPAL_IMG_TYPE_UUID, valueBuffer.array());
        valueBuffer.clear();

        Log.d(TAG, "startOpalOtaProcess : Sending Prepare Download Control Command : " + OpalValues.OPAL_CONTROL_COMMAND_PREPARE_DOWNLOAD);
        valueBuffer.put(OpalValues.OPAL_CONTROL_COMMAND_PREPARE_DOWNLOAD);
        BleManager.getInstance().writeCharacteristics(device, OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID, valueBuffer.array());

        // Should wait for the notification value 0x00 for OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID
        Log.d(TAG, "startOpalOtaProcess : Should wait for the notification value 0x00 for OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID");
    }

    /**
     * Prepare to update. Show the popup, modify image file with pad if need.
     */
    public void prepareImageFile() {

        currentStep = OTA_STEP_SEND_IMAGE_DATA;

        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        int imageSize = 0;

        try {
            // Open image file
            String[] fileNames = assetManager.list("image");
            inputStream = assetManager.open("image/"+fileNames[0]);

            // get file image file size and get the size to be padded.
            imageSize = inputStream.available();
            int padSize = imageSize % SIZE_CHUNK;

            // If the image size is not divide by 20byte then pad the remained byte.
            // The image size is contained data and checksum chunk. The checksum chunk is be the last
            // 160byte of the image file.
            if (padSize > 0) {
                padSize = SIZE_CHUNK - padSize;
            }
            else {
                //do nothing.
            }

            // Allocate image buffer.

            byte[] dataChunk = new byte[imageSize - SIZE_CHECKSUM + padSize];
            byte[] checksumChunk = new byte[SIZE_CHECKSUM];

            imageChunk = new byte[dataChunk.length + checksumChunk.length];

            // Read data chunk except last 160 byte of checksum chunk.
            inputStream.read(dataChunk, 0, imageSize - SIZE_CHECKSUM);
            inputStream.read(checksumChunk, 0, SIZE_CHECKSUM);
            inputStream.close();

            System.arraycopy(dataChunk, 0, imageChunk, 0, dataChunk.length);
            System.arraycopy(checksumChunk, 0, imageChunk, imageChunk.length-checksumChunk.length, checksumChunk.length);

            transferTotalCount = (int)(imageChunk.length / (float)SIZE_CHUNK);
            transferOffset = 0;

        }
        catch (Exception e) {
            Log.d(TAG, "readImageFile :" + e);
        }

        if(transferTotalCount > 0) {
            ByteBuffer valueBuffer = ByteBuffer.allocate(3);

            valueBuffer.put((byte) 2);
            valueBuffer.put(1, (byte) (imageSize & 0xff));
            valueBuffer.put(2, (byte) ((imageSize >> 8) & 0xff));
//            BleManager.getInstance().writeCharacteristics(ParagonValues.CHARACTERISTIC_OTA_COMMAND, valueBuffer.array());
            Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND Send OTA_STEP_SEND_IMAGE_DATA:" + valueBuffer.toString());
        }
        else{
            Log.d(TAG, "OTA image file read failed");
            ((ParagonMainActivity) context).failedOta();
        }

    }


    private void sendBleImageSize() {

        currentStep = OTA_BLE_STEP_LOAD_IMAGE_AND_SEND_IMAGE_SIZE;
        Log.d(TAG, "sendBleImageSize : image name  : " + OpalValues.LATEST_OPAL_BLE_FIRMWARE_NAME);

        // load ble image into Memory
        boolean loadSuccess = loadImageToMemory(OpalValues.LATEST_OPAL_BLE_FIRMWARE_NAME);

        if( loadSuccess == false) {

            Log.d(TAG, "sendBleImageSize : load failed !");

            onBLEUpdateFailed();
        }
        else {

            ByteBuffer valueBuffer = ByteBuffer.allocate(3);

            // As we put image size into 2byte bucket =>
            // max image size should be 2 to the 16th power = 65,536
            // => 64KByte

            valueBuffer.put((byte) 0x02);
            valueBuffer.put(1, (byte) (otaImageSize & 0xff));
            valueBuffer.put(2, (byte) ((otaImageSize >> 8) & 0xff));

            Log.d(TAG, "sendBleImageSize : OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID : " +  " Image size to send :  " + MathTools.byteArrayToHex(valueBuffer.array()));

            BleManager.getInstance().writeCharacteristics(currentDevice, OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID, valueBuffer.array());
        }
    }

    private void sendOpalImageHeader() {

        currentStep = OTA_OPAL_STEP_LOAD_IMAGE_AND_SEND_IMAGE_HEADER;
        Log.d(TAG, "sendOpalImageHeader : image name  : " + OpalValues.LATEST_OPAL_FIRMWARE_NAME);

        // load ble image into Memory
        boolean loadSuccess = loadImageToMemory(OpalValues.LATEST_OPAL_FIRMWARE_NAME);

        if( loadSuccess == false) {

            Log.d(TAG, "sendOpalImageHeader : load failed !");

            onBLEUpdateFailed();
        }
        else {

            ByteBuffer valueBuffer = ByteBuffer.allocate(20);

            // Image type: 1 byte (should be 0x01 : Opal image type),
            valueBuffer.put((byte) 0x01);
            // Opal firmware version:4 bytes,
            byte[] version = MathTools.hexToByteArray(OpalValues.LATEST_OPAL_FIRMWARE_VERSION);
            valueBuffer.put(version[0]);
            valueBuffer.put(version[1]);
            valueBuffer.put(version[2]);
            valueBuffer.put(version[3]);
            // file size: 4 bytes + random value 11 bytes
            // As we put image size into 2byte bucket =>
            // max image size should be 2 to the 16th power = 65,536
            // => 64KByte

            String hex = String.format("%08x", otaImageSize);
            byte[] imageSize = MathTools.hexToByteArray(hex);

            valueBuffer.put(imageSize[0]);
            valueBuffer.put(imageSize[1]);
            valueBuffer.put(imageSize[2]);
            valueBuffer.put(imageSize[3]);

            Log.d(TAG, "sendOpalImageHeader : OpalValues.OPAL_IMAGE_DATA_CHAR_UUID : " +  " Image header to send :  " + MathTools.byteArrayToHex(valueBuffer.array()));

            BleManager.getInstance().writeCharacteristics(currentDevice, OpalValues.OPAL_IMAGE_DATA_CHAR_UUID, valueBuffer.array());
        }
    }

    private void sendOpalImageData() {

        if (transferOffset < transferTotalCount) {
            currentStep = OTA_OPAL_STEP_SEND_IMAGE_DATA;
            ByteBuffer valueBuffer = ByteBuffer.allocate(SIZE_CHUNK);

            valueBuffer.put(imageChunk, transferOffset * SIZE_CHUNK, SIZE_CHUNK);
            byte[] data = valueBuffer.array();

            BleManager.getInstance().writeCharacteristics(currentDevice, OpalValues.OPAL_IMAGE_DATA_CHAR_UUID, data);
            Log.d(TAG, "sendOpalImageData() : Sending 20 bytes image chunk : OpalValues.OPAL_IMAGE_DATA_CHAR_UUID :" + transferOffset + ", " + MathTools.byteArrayToHex(data));

            transferOffset++;
            if(delegate != null) {
                delegate.onBLEOTAProgressChanged(transferOffset);
            }
        }
        else {
            currentStep = OTA_OPAL_STEP_SEND_IMAGE_DONE;

            ByteBuffer valueBuffer = ByteBuffer.allocate(1);

            valueBuffer.put((byte) 0x03);
            BleManager.getInstance().writeCharacteristics(currentDevice, OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID, valueBuffer.array());
            Log.d(TAG, "sendOpalImageData() : Sending verification command(0x03) for completion of sending binary !");
        }
    }

    private void sendBleImageData() {

        if (transferOffset < transferTotalCount) {
            currentStep = OTA_BLE_STEP_SEND_IMAGE_DATA;
            ByteBuffer valueBuffer = ByteBuffer.allocate(SIZE_CHUNK);

            valueBuffer.put(imageChunk, transferOffset * SIZE_CHUNK, SIZE_CHUNK);
            byte[] data = valueBuffer.array();

            BleManager.getInstance().writeCharacteristics(currentDevice, OpalValues.OPAL_IMAGE_DATA_CHAR_UUID, data);
            Log.d(TAG, "sendBleImageData() : Sending 20 bytes image chunk : OpalValues.OPAL_IMAGE_DATA_CHAR_UUID :" + transferOffset + ", " + MathTools.byteArrayToHex(data));

            transferOffset++;
            if(delegate != null) {
                delegate.onBLEOTAProgressChanged(transferOffset);
            }
        }
        else {
            currentStep = OTA_BLE_STEP_SEND_IMAGE_DONE;

            ByteBuffer valueBuffer = ByteBuffer.allocate(1);

            valueBuffer.put((byte) 0x03);
            BleManager.getInstance().writeCharacteristics(currentDevice, OpalValues.OPAL_OTA_CONTROL_COMMAND_CHAR_UUID, valueBuffer.array());
            Log.d(TAG, "sendBleImageData() : Sending verification command(0x03) for completion of sending binary !");
        }
    }

    private boolean loadImageToMemory(String imageName) {

        Log.d(TAG, "loadImageToMemory() : In");

        boolean loadSuccess = false;

        AssetManager assetManager = FirstBuildApplication.getInstance().getContext().getAssets();

        try {
            AssetFileDescriptor fd = assetManager.openFd("image/" + imageName);
            Long imageSize = fd.getLength();

            otaImageSize = imageSize.intValue();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(otaImageSize == 0) {
            loadSuccess = false;
        }
        else {
            try {
                // Open BLE image file
                InputStream inputStream = assetManager.open("image/"+ imageName);
                int padSize = otaImageSize % SIZE_CHUNK;

                // If the image size is not divide by 20byte then pad the remained byte.
                // The image size is contained data and checksum chunk. The checksum chunk is be the last
                // 160byte of the image file.
                if (padSize > 0) {
                    padSize = SIZE_CHUNK - padSize;
                }
                else {
                    //do nothing.
                }

                // Allocate image buffer.

                byte[] dataChunk = new byte[otaImageSize - SIZE_CHECKSUM + padSize];
                byte[] checksumChunk = new byte[SIZE_CHECKSUM];

                imageChunk = new byte[dataChunk.length + checksumChunk.length];

                // Read data chunk except last 160 byte of checksum chunk.
                inputStream.read(dataChunk, 0, otaImageSize - SIZE_CHECKSUM);
                inputStream.read(checksumChunk, 0, SIZE_CHECKSUM);
                inputStream.close();

                System.arraycopy(dataChunk, 0, imageChunk, 0, dataChunk.length);
                System.arraycopy(checksumChunk, 0, imageChunk, imageChunk.length-checksumChunk.length, checksumChunk.length);

                transferTotalCount = (int)(imageChunk.length / (float)SIZE_CHUNK);
                transferOffset = 0;

            }
            catch (Exception e) {
                Log.d(TAG, "loadImageToMemory :" + e);
                loadSuccess = false;
            }

            if(transferTotalCount > transferOffset) {
                loadSuccess = true;

                // Let the UI to know the max progress value
                if(delegate != null) {
                    delegate.onBLEOTAProgressMax(transferTotalCount);
                }
            }
        }

        return loadSuccess;
    }

    /**
     * Send image data to Paragon Master 20 byte every transfer.
     */
    private void transferData() {

        Log.d(TAG, "transferData : transferOffset :"+transferOffset + ", transferCount :"+transferTotalCount);

        if (transferOffset < transferTotalCount) {
            currentStep = OTA_STEP_SEND_IMAGE_DATA;
            ByteBuffer valueBuffer = ByteBuffer.allocate(SIZE_CHUNK);

            valueBuffer.put(imageChunk, transferOffset * SIZE_CHUNK, SIZE_CHUNK);
//            BleManager.getInstance().writeCharacteristics(ParagonValues.CHARACTERISTIC_OTA_DATA, valueBuffer.array());
            Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_DATA Send OTA_STEP_SEND_IMAGE_DATA:" + transferOffset + ", " + valueBuffer.toString());

            transferOffset++;
        }
        else {
            currentStep = OTA_STEP_SEND_IMAGE_DONE;

            ByteBuffer valueBuffer = ByteBuffer.allocate(3);

            valueBuffer.put((byte) 3);
//            BleManager.getInstance().writeCharacteristics(ParagonValues.CHARACTERISTIC_OTA_COMMAND, valueBuffer.array());
            Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND Send OTA_STEP_SEND_IMAGE_DONE:" + valueBuffer.toString());
        }

    }

    private void successfulDone() {
        Log.d(TAG, "successfulDone");

        ((ParagonMainActivity) context).succeedOta();

    }

    private void onBLEUpdateSuccess() {
        Log.d(TAG, "onBLEUpdateSuccess");
        if(delegate != null) {
            delegate.onBLEOTASuccessful();
        }

        resetOtaValues();
    }

    private void onBLEUpdateFailed() {
        Log.d(TAG, "onBLEUpdateFailed");
        if(delegate != null) {
            delegate.onBLEOTAFailed();
        }

        resetOtaValues();
    }

    private void resetOtaValues() {
        context = null;
        versionMajor = 0;
        versionMinor = 0;
        versionBuild = 0;
        currentStep = OTA_STEP_NONE;
        imageChunk = null;
        transferTotalCount = 0;
        transferOffset = 0;

        currentDevice = null;
        otaImageSize = 0;

        delegate = null;
    }

    public void getResponse(byte response) {

        if (response == 0) {
            switch (currentStep) {

                case OTA_STEP_NONE:
                    //do nothging
                    break;

                case OTA_STEP_COMPARE_VERSION:
                    prepareImageFile();
                    break;

                case OTA_STEP_SEND_IMAGE_SIZE:
                    transferData();
                    break;

                case OTA_STEP_SEND_IMAGE_DATA:
                    transferData();
                    break;

                case OTA_STEP_SEND_IMAGE_DONE:
                    successfulDone();
                    break;

                case OTA_STEP_REBOOT:
                    break;

                case OTA_BLE_PREPARE_DOWNLOAD:
                    sendBleImageSize();
                    break;

                case OTA_BLE_STEP_LOAD_IMAGE_AND_SEND_IMAGE_SIZE:
                    sendBleImageData();
                    break;

                case OTA_BLE_STEP_SEND_IMAGE_DATA:
                    sendBleImageData();
                    break;

                case OTA_BLE_STEP_SEND_IMAGE_DONE:
                    onBLEUpdateSuccess();
                    break;

                case OTA_BLE_STEP_REBOOT:
                    break;

                case OTA_OPAL_PREPARE_DOWNLOAD:
                    sendOpalImageHeader();
                    break;

                case OTA_OPAL_STEP_LOAD_IMAGE_AND_SEND_IMAGE_HEADER:
                    sendOpalImageData();
                    break;

                case OTA_OPAL_STEP_SEND_IMAGE_DATA:
                    sendOpalImageData();
                    break;

                case OTA_OPAL_STEP_SEND_IMAGE_DONE:
                    onBLEUpdateSuccess();
                    break;

                case OTA_OPAL_STEP_REBOOT:
                    break;
            }
        }
        else {
            Log.d(TAG, "getResponse : get error response :" + response + ", step :" + currentStep);

            if(currentStep >= OTA_BLE_STEP_NONE && currentStep <= OTA_BLE_STEP_REBOOT) {
                onBLEUpdateFailed();
            }
        }
    }

    public void responseWriteData() {
        Log.d(TAG, "responseWriteData");
        transferData();
    }
}
