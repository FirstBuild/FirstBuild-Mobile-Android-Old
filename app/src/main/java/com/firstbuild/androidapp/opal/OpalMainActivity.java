package com.firstbuild.androidapp.opal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * Created by hans on 16. 6. 16..
 */
public class OpalMainActivity extends AppCompatActivity {

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
    }
}

