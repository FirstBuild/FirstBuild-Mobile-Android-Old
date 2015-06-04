package com.firstbuild.androidapp.cookingMethod;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.SplashActivity;
import com.firstbuild.androidapp.sousvideUI.SousvideActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class Step2Fragment extends Fragment {


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
                Intent intent = new Intent(getActivity(), SousvideActivity.class);
                startActivity(intent);

            }
        });

        return view;
    }


}
