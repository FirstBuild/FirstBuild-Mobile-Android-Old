package com.firstbuild.androidapp.addProduct;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.dashboard.DashboardActivity;
import com.firstbuild.commonframework.bleManager.BleManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductSetParagonNameFragment extends Fragment {
    private String TAG = AddProductActivity.class.getSimpleName();
    private EditText paragonNameEditText;
    private AddProductActivity attached = null;

    public AddProductSetParagonNameFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchParagonFragment IN");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = (AddProductActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "DeviceName: " + BleManager.getInstance().getDeviceName());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_set_paragon_name, container, false);

        paragonNameEditText = (EditText) view.findViewById(R.id.edit_paragon_name);
        paragonNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // If edit text looses focus, hide keyboard
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        view.findViewById(R.id.button_add_product_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pressed");

                // Get name from edit text
                String paragonName = paragonNameEditText.getText().toString();

                if(paragonName.isEmpty()){
                    paragonName = "My Paragon";
                }

                // Save device name here
                Log.d(TAG, "Paragon Nickname: " + paragonName);
                attached.setNewProductNickname(paragonName);
                attached.addNewProductToList();

                // Go to dash board
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                intent.putExtra("previous_activity", AddProductActivity.class.getSimpleName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        return view;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
