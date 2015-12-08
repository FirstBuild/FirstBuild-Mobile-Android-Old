package com.firstbuild.androidapp.addProduct;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductFoundParagonFragment extends Fragment {
    private String TAG = AddProductActivity.class.getSimpleName();
    private Handler handler;
    private Runnable runnable;


    public AddProductFoundParagonFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchParagonFragment IN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_found_paragon, container, false);

        runnable = new Runnable() {
            @Override
            public void run() {
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.content_frame, new AddProductSetParagonNameFragment()).
                        addToBackStack(null).
                        commit();
            }
        };

        Log.d(TAG, "Launch 3 sec timer");
        handler = new Handler();
        handler.postDelayed(runnable, 3000);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(handler == null) {
            Log.d(TAG, "Launch 1 sec timer");
            handler = new Handler();
            handler.postDelayed(runnable, 1000);
        }
    }



    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        // Cancel the runnable
        handler.removeCallbacks(runnable);
        handler = null;

        super.onPause();
    }
}
