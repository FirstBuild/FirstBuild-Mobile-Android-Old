package com.firstbuild.androidapp.paragon.trash;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.ParagonMainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class Step2Fragment extends Fragment {

    private String TAG = Step2Fragment.class.getSimpleName();

    public Step2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cooking_method_step2, container, false);

        view.findViewById(R.id.step2_beef).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ParagonMainActivity)getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_BEEF);
//                Intent intent = new Intent(getActivity(), SousvideActivity.class);
//                startActivity(intent);

            }
        });

        view.findViewById(R.id.btn_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Custom button clicked2");
            }
        });


        return view;
    }


}
