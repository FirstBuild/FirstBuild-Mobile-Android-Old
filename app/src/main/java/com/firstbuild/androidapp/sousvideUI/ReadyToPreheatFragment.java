package com.firstbuild.androidapp.sousvideUI;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadyToPreheatFragment extends Fragment {


    public ReadyToPreheatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_ready_to_preheat, container, false);

        view.findViewById(R.id.layout_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_content, new PreheatingFragment()).
                        addToBackStack(null).
                        commit();
            }
        });

        return view;
    }


}
