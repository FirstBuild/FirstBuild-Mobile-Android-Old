package com.firstbuild.androidapp.Paragon;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Step1Fragment extends Fragment {


    private String TAG = Step1Fragment.class.getSimpleName();

    public Step1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cooking_method_step1, container, false);

        view.findViewById(R.id.step1_sousvide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ParagonMainActivity)getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_METHOD_2);
            }
        });

        view.findViewById(R.id.btn_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Custom button clicked");
            }
        });


        return view;
    }


}
