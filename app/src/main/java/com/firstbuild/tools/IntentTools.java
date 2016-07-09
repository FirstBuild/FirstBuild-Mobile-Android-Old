package com.firstbuild.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

import com.firstbuild.androidapp.dashboard.DashboardActivity;

/**
 * Created by hans on 16. 7. 4..
 */
public class IntentTools {

    public static final String OPAL_INTRODUCTION_HOME_URL = "http://www.nuggetice.com";
    public static final String OPAL_APP_SOURCE_CODE_URL = "https://github.com/FirstBuild/FirstBuild-Mobile-Android";
    public static final String OPAL_APP_LEARN_MORE_URL = "https://cocreate.firstbuild.com/JBerg/opal-nugget-ice-maker/activity/";

    public static final String OPAL_CONTACT_AN_EXPERT_EMAIL_ADDRESS = "support@firstbuild.com";
    public static final String OPAL_FEEDBACK_EMAIL_ADDRESS = OPAL_CONTACT_AN_EXPERT_EMAIL_ADDRESS;

    public static final String OPAL_CONTACT_WARRANTY_EMAIL_ADDRESS = "warranty@firstbuild.com";
    public static final String OPAL_CONTACT_AN_EXPERT_EAMIL_SUBJECT = "Opal Expert Contact";
    public static final String OPAL_CONTACT_WARRANTY_EMAIL_SUBJECT = "Opal Warranty Contact";
    public static final String OPAL_FEEDBACK_EMAIL_SUBJECT = "Opal Android App Feedback";

    public static void goToDashboard(Context c, String previousSreen) {

        Intent intent = new Intent(c, DashboardActivity.class);
        intent.putExtra("previous_activity", previousSreen);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
    }

    public static void openBrowser(Context c, String urlToOpen) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(intent);
    }


    public static void composeEmail(Context c, String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        }
    }
}
