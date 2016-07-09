package com.firstbuild.viewutil;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by hans on 16. 7. 6..
 */
public class ProgressDialogFragment extends DialogFragment{

    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_CONTENTS = "CONTENTS";
    private static final String KEY_POSITIVE_BTN = "POSITIVE_BTN";
    private static final String KEY_NEGATIVE_BTN = "NEGATIVE_BTN";

    private ProgressDialog progressDialog;

    public ProgressDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static ProgressDialogFragment getInstance(String title, String contents, String positiveBtn, String negativeBtn) {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_CONTENTS, contents);
        args.putString(KEY_POSITIVE_BTN, positiveBtn);
        args.putString(KEY_NEGATIVE_BTN, negativeBtn);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString(KEY_TITLE);
        String positiveBtn = getArguments().getString(KEY_POSITIVE_BTN);
        String negativeBtn = getArguments().getString(KEY_NEGATIVE_BTN);

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);

        setCancelable(false);

        if(title != null) {
            progressDialog.setTitle(title);
        }

        progressDialog.setMessage(getArguments().getString(KEY_CONTENTS));

        if(positiveBtn != null) {
            progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getArguments().getString(KEY_POSITIVE_BTN),  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        if(negativeBtn != null) {
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getArguments().getString(KEY_NEGATIVE_BTN), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        return progressDialog;
    }

    public void setMax(int max) {
        progressDialog.setMax(max);
    }

    public void setProgress(int progress) {
        progressDialog.setProgress(progress);
    }
}
