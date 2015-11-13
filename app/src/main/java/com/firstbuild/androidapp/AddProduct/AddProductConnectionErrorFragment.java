package com.firstbuild.androidapp.addProduct;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductConnectionErrorFragment extends Fragment {
    private String TAG = AddProductActivity.class.getSimpleName();

    public AddProductConnectionErrorFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchParagonFragment IN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_connection_error, container, false);

        view.findViewById(R.id.button_try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pressed");
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.content_frame, new AddProductSearchParagonFragment()).
                        addToBackStack(null).
                        commit();
            }
        });

        // Catch back button
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Log.i(TAG, "Back button pressed");
                    getFragmentManager().
                            beginTransaction().
                            replace(R.id.content_frame, new AddProductSelectFragment()).
                            addToBackStack(null).
                            commit();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        return view;
    }
}
