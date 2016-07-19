package com.firstbuild.androidapp.opal;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by hans on 16. 7. 19..
 */
public class OpalHelpTutorialFragment extends DialogFragment {

    public OpalHelpTutorialFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static OpalHelpTutorialFragment getInstance(String title, String contents, String positiveBtn, String negativeBtn) {
        OpalHelpTutorialFragment dialogFragment = new OpalHelpTutorialFragment();

        Bundle args = new Bundle();
        dialogFragment.setArguments(args);

        return dialogFragment;
    }
}
