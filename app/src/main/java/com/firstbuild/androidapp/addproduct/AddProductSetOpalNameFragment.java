package com.firstbuild.androidapp.addproduct;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * Created by hans on 16. 6. 8..
 */
public class AddProductSetOpalNameFragment extends AddProductSetParagonNameFragment{
    private static final String TAG = AddProductSetOpalNameFragment.class.getSimpleName();

    public AddProductSetOpalNameFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSetOpalNameFragment IN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);


        // // TODO: hans 16. 6. 8. replace logo image
        ((ImageView)view.findViewById(R.id.image_name_your_paragon)).setImageResource(R.drawable.ic_opal_logo);
        ((TextView)view.findViewById(R.id.text_name_your_paragon)).setText(R.string.name_your_opal);
        ((TextView)view.findViewById(R.id.text_name_your_paragon_explanation)).setText(R.string.name_your_opal_explanation);
        ((EditText)view.findViewById(R.id.edit_paragon_name)).setHint(R.string.default_product_name_opal);

        // Override click behavior
        view.findViewById(R.id.button_add_product_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pressed");

                // Get name from edit text
                String paragonName = paragonNameEditText.getText().toString();

                if(paragonName.isEmpty()){
                    paragonName = getString(R.string.default_product_name_opal);
                }

                // Save device name here
                Log.d(TAG, "Opal Nickname: " + paragonName);
                attached.setNewProductNickname(paragonName);
                attached.addNewProductToList();

                gotoDashboard();
            }
        });

        return view;
    }
}
