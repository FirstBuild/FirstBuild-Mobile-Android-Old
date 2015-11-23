package com.firstbuild.androidapp.paragon;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.commonframework.bleManager.BleManager;

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

    private static final int SIZE_CHUNK = 20;
    private static final int SIZE_CHECKSUM = 160;

    private Context context;
    private int versionMajor = 0;
    private int versionMinor = 0;
    private int versionBuild = 0;
    private int currentStep;
    private byte[] imageChunk = null;
    private int transferCount = 0;
    private int transferOffset = 0;


    public static OtaManager getInstance() {
        return ourInstance;
    }

    private OtaManager() {
    }

    public int getTransferCount() {
        return transferCount;
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
            inputStream = assetManager.open("image/paragon_master.ota");

            // Image file contained its version number in for four byte from 5th data.
            inputStream.skip(4);
            inputStream.read(versionChunk);
            inputStream.close();
        }
        catch (Exception e) {
            Log.d(TAG, "readImageFile :" + e);
        }

        versionMajor = versionChunk[0];
        versionMinor = versionChunk[1];
        versionBuild = (short) versionChunk[2];


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

        if (this.versionMajor != versionMajor ||
                this.versionMinor != versionMinor ||
                this.versionBuild != versionBuild) {

            isNeedUpdate = true;
        }
        else {
            isNeedUpdate = false;
        }

        return isNeedUpdate;
    }


    public void startProcess(){
        currentStep = OTA_STEP_COMPARE_VERSION;

        ByteBuffer valueBuffer = ByteBuffer.allocate(1);

        valueBuffer.put((byte) 1);
        BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_OTA_COMMAND, valueBuffer.array());
        Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND Send:" + valueBuffer.toString());
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
            inputStream = assetManager.open("image/paragon_master.ota");

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

            transferCount = (int)(imageChunk.length / (float)SIZE_CHUNK);
            transferOffset = 0;

        }
        catch (Exception e) {
            Log.d(TAG, "readImageFile :" + e);
        }

        if(transferCount > 0) {
            ByteBuffer valueBuffer = ByteBuffer.allocate(3);

            valueBuffer.put((byte) 2);
            valueBuffer.put(1, (byte) (imageSize & 0xff));
            valueBuffer.put(2, (byte) ((imageSize >> 8) & 0xff));
            BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_OTA_COMMAND, valueBuffer.array());
            Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND Send OTA_STEP_SEND_IMAGE_DATA:" + valueBuffer.toString());
        }
        else{
            Log.d(TAG, "OTA image file read failed");
            ((ParagonMainActivity) context).failedOta();
        }

    }


    /**
     * Send image data to Paragon Master 20 byte every transfer.
     */
    private void transferData() {

        Log.d(TAG, "transferData : transferOffset :"+transferOffset + ", transferCount :"+transferCount);

        if (transferOffset < transferCount) {
            currentStep = OTA_STEP_SEND_IMAGE_DATA;
            ByteBuffer valueBuffer = ByteBuffer.allocate(SIZE_CHUNK);

            valueBuffer.put(imageChunk, transferOffset * SIZE_CHUNK, SIZE_CHUNK);
            BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_OTA_DATA, valueBuffer.array());
            Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_DATA Send OTA_STEP_SEND_IMAGE_DATA:" + transferOffset + ", " + valueBuffer.toString());

            transferOffset++;
        }
        else {
            currentStep = OTA_STEP_SEND_IMAGE_DONE;

            ByteBuffer valueBuffer = ByteBuffer.allocate(1);

            valueBuffer.put((byte) 3);
            BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_OTA_COMMAND, valueBuffer.array());
            Log.d(TAG, "ParagonValues.CHARACTERISTIC_OTA_COMMAND Send OTA_STEP_SEND_IMAGE_DONE:" + valueBuffer.toString());
        }

    }

    private void successfulDone() {
        Log.d(TAG, "successfulDone");

        ((ParagonMainActivity) context).succeedOta();

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

            }
        }
        else {
            Log.d(TAG, "getResponse : get error response :" + response + ", step :" + currentStep);
        }

    }

    public void responseWriteData() {
        Log.d(TAG, "responseWriteData");
        transferData();
    }
}
