package com.firstbuild.viewutil;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.firstbuild.androidapp.opal.OpalMainActivity;

/**
 * Created by hans on 16. 7. 6..
 */
public class OTAConfirmDialogFragment extends DialogFragment{

    public interface OTAUpdateStartDelegate {
        void onOTAStart();
    }

    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_CONTENTS = "CONTENTS";
    private static final String KEY_POSITIVE_BTN = "POSITIVE_BTN";
    private static final String KEY_NEGATIVE_BTN = "NEGATIVE_BTN";

    private OTAUpdateStartDelegate delegate;

    public OTAConfirmDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static OTAConfirmDialogFragment getInstance(String title, String contents, String positiveBtn, String negativeBtn) {
        OTAConfirmDialogFragment dialogFragment = new OTAConfirmDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_CONTENTS, contents);
        args.putString(KEY_POSITIVE_BTN, positiveBtn);
        args.putString(KEY_NEGATIVE_BTN, negativeBtn);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OTAUpdateStartDelegate) {
            delegate = (OTAUpdateStartDelegate) activity;
        }
        else {
            throw new ClassCastException(activity + " must implements "
                    + OTAUpdateStartDelegate.class.getSimpleName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString(KEY_TITLE);
        String positiveBtn = getArguments().getString(KEY_POSITIVE_BTN);
        String negativeBtn = getArguments().getString(KEY_NEGATIVE_BTN);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        if(title != null) {
            alertDialogBuilder.setTitle(title);
        }

        alertDialogBuilder.setMessage(getArguments().getString(KEY_CONTENTS));

        if(positiveBtn != null) {
            alertDialogBuilder.setPositiveButton(getArguments().getString(KEY_POSITIVE_BTN),  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch(getTag()) {
                        case OpalMainActivity.TAG_OTA_UPDATE_NOT_AVAILABLE_DIALOG:
                        case OpalMainActivity.TAG_OTA_FAILURE_DIALOG:
                            // As Positive btn indicates "Ok" for update not available message, just dimiss dialog
                            break;

                        case OpalMainActivity.TAG_OPAL_OTA_UPDATE_CONFIRM_DIALOG:
                        case OpalMainActivity.TAG_BLE_OTA_UPDATE_CONFIRM_DIALOG:
                            if(delegate != null) {
                                delegate.onOTAStart();
                            }
                            break;

                        default:
                    }
                }
            });
        }

        if(negativeBtn != null) {
            alertDialogBuilder.setNeutralButton(getArguments().getString(KEY_NEGATIVE_BTN), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        return alertDialogBuilder.create();
    }

}
