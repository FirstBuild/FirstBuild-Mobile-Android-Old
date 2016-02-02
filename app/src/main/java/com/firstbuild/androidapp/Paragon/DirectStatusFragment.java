package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productManager.ProductManager;
import com.firstbuild.viewUtil.gridCircleView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DirectStatusFragment extends Fragment {

    private String TAG = "SousvideStatusFragment";
    private gridCircleView circle;
    private ImageView[] progressDots = new ImageView[4];
    private View layoutStatus;
    private ImageView imgStatus;
    private TextView textTempCurrent;
    private TextView textTempTarget;
    private TextView textStatusName;
    private TextView textLabelCurrent;
    private TextView textExplanation;
    private ParagonMainActivity attached = null;

    public DirectStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView IN");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_circle, container, false);

        view.findViewById(R.id.layout_navi).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.status_divider).setVisibility(View.INVISIBLE);

        circle = (gridCircleView) view.findViewById(R.id.circle);
        circle.setBarValue(1);
        circle.setGridValue(1);
        circle.setDashValue(0);

        textStatusName = (TextView) view.findViewById(R.id.text_status_name);
        textStatusName.setText("Burner On");
        textLabelCurrent = (TextView) view.findViewById(R.id.text_label_current);
        textLabelCurrent.setText("Power");

        progressDots[0] = (ImageView) view.findViewById(R.id.progress_dot_1);
        progressDots[1] = (ImageView) view.findViewById(R.id.progress_dot_2);
        progressDots[2] = (ImageView) view.findViewById(R.id.progress_dot_3);
        progressDots[3] = (ImageView) view.findViewById(R.id.progress_dot_4);

        textTempCurrent = (TextView) view.findViewById(R.id.text_temp_current);
        textTempCurrent.setText("10");
        textTempTarget = (TextView) view.findViewById(R.id.text_temp_target);
        textTempTarget.setVisibility(View.INVISIBLE);
        textExplanation = (TextView) view.findViewById(R.id.text_explanation);

        textExplanation.setVisibility(View.GONE);

        layoutStatus = view.findViewById(R.id.layout_status);
        imgStatus = (ImageView) view.findViewById(R.id.img_status);

        layoutStatus.setVisibility(View.VISIBLE);
        imgStatus.setVisibility(View.GONE);

        view.findViewById(R.id.btn_continue).setVisibility(View.GONE);
        view.findViewById(R.id.btn_complete).setVisibility(View.GONE);

        updateUiPowerLevel();

        return view;
    }

    public void updateCookState() {
        byte state = ProductManager.getInstance().getCurrent().getErdCookState();
        Log.d(TAG, "updateCookState IN " + state);

        if(state == ParagonValues.COOK_STATE_OFF) {
            attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
        }
        else{
            // do nothing.
        }
    }


    public void updateUiPowerLevel() {
        byte powerLevel = ProductManager.getInstance().getCurrent().getErdPowerLevel();

        textTempCurrent.setText(powerLevel + "");
    }

}
