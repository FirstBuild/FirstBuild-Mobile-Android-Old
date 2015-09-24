package com.firstbuild.androidapp.paragon;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.sousvideUI.CookingFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadyToCookFragment extends Fragment {


    public ReadyToCookFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_ready_to_cook, container, false);

        ((TextView)view.findViewById(R.id.textCurrentTemp)).setText(((ParagonMainActivity)getActivity()).getCurrentTemp()+"");

        view.findViewById(R.id.btn_start_timer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_content, new CookingFragment()).
                        addToBackStack(null).
                        commit();
            }
        });



        return view;
    }


}
