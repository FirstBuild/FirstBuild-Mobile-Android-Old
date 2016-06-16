package com.firstbuild.androidapp.addproduct;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.tools.MainQueue;

/**
 * Created by hans on 16. 6. 8..
 */
public class AddProductFoundOpalFragment extends Fragment {

    private String TAG = AddProductFoundOpalFragment.class.getSimpleName();
    private Runnable runnable;

    public AddProductFoundOpalFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductFoundOpalFragment IN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_found_paragon, container, false);

        ((ImageView)view.findViewById(R.id.image_got_it)).setImageResource(R.drawable.img_opal_got_it);
        ((TextView)view.findViewById(R.id.add_product_pairing_success_description)).setText(R.string.we_found_your_device);

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
                        replace(R.id.content_frame, new AddProductSetOpalNameFragment()).
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
