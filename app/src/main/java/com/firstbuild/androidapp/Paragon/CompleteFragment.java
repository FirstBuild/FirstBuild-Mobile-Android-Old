package com.firstbuild.androidapp.paragon;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * Created by Hollis on 10/28/15.
 */
public class CompleteFragment extends Fragment {

    public CompleteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_get_ready, container, false);

        View btnDone = view.findViewById(R.id.btn_done);
        btnDone.setVisibility(View.VISIBLE);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
            }
        });

        ((TextView)view.findViewById(R.id.text_explanation)).setText(Html.fromHtml("Press <b>STOP</b> on your Paragon"));

        ((ParagonMainActivity)getActivity()).setTitle("Complete");

        return view;
    }



}
