package com.firstbuild.androidapp.paragon;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.commonframework.bleManager.BleManager;

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

        view.findViewById(R.id.btn_done).setVisibility(View.GONE);

        ((TextView)view.findViewById(R.id.text_explanation)).setText(Html.fromHtml("Press <b>START</b> on your Paragon"));

//        //TODO: remove below listener after test.
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_COOK_STATUS);
//            }
//        });

        ((ParagonMainActivity)getActivity()).setTitle("Get Ready");


        return view;
    }


}
