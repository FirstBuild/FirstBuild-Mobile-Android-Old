package com.firstbuild.androidapp.addproduct;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.tools.MainQueue;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductFoundParagonFragment extends Fragment {
    private String TAG = AddProductFoundParagonFragment.class.getSimpleName();
    protected Runnable runnable;


    public AddProductFoundParagonFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchParagonFragment IN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_found_paragon, container, false);



        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        Log.d(TAG, "Launch 5 sec timer");
        MainQueue.postDelayed(runnable, 5000);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        MainQueue.postDelayed(runnable, 1000);
    }



    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        MainQueue.removeCallbacks(runnable);

        super.onPause();
    }
}
