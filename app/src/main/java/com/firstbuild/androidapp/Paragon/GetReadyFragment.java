package com.firstbuild.androidapp.Paragon;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetReadyFragment extends Fragment {


    public GetReadyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_get_ready, container, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_CIRCLE);
            }
        });

        return view;
    }


}
