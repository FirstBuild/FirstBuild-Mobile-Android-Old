package com.firstbuild.androidapp.paragon;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productManager.ProductManager;

/**
 * Created by Hollis on 10/28/15.
 */
public class CompleteFragment extends Fragment {

    private ParagonMainActivity attached = null;

    public CompleteFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = (ParagonMainActivity) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_get_ready, container, false);

        View btnDone = view.findViewById(R.id.btn_done);
        btnDone.setVisibility(View.GONE);

        ((TextView)view.findViewById(R.id.text_explanation)).setText(Html.fromHtml("Press <b>STOP</b> on your Paragon"));

        ((ParagonMainActivity)getActivity()).setTitle("Complete");

        return view;
    }


    public void onCookModeChanged(){
        byte cookMode = ProductManager.getInstance().getCurrent().getErdCurrentCookMode();

        if(cookMode == ParagonValues.CURRENT_COOK_MODE_OFF ||
                cookMode == ParagonValues.CURRENT_COOK_MODE_DIRECT){
            attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
        }
        else{
            //do nothing.
        }
    }


}
