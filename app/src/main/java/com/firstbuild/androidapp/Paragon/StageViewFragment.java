package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class StageViewFragment extends Fragment {

    private String TAG = StageEditFragment.class.getSimpleName();

    private TextView textTime;
    private TextView textTemp;
    private TextView textSpeed;
    private TextView textAutoTransition;
    private EditText editDirection;

    private ParagonMainActivity attached = null;


    public StageViewFragment() {
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


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stage_edit, container, false);

        textTime = (TextView) view.findViewById(R.id.text_time);
        textTemp = (TextView) view.findViewById(R.id.text_temp);
        textSpeed = (TextView) view.findViewById(R.id.text_speed);
        editDirection = (EditText) view.findViewById(R.id.edit_directions);
        textAutoTransition = (TextView) view.findViewById(R.id.text_auto_transition);

        textTime.setBackground(getResources().getDrawable(R.drawable.img_text_background));
        textTemp.setBackground(getResources().getDrawable(R.drawable.img_text_background));
        textSpeed.setBackground(getResources().getDrawable(R.drawable.img_text_background));
        editDirection.setBackground(getResources().getDrawable(R.drawable.img_text_background));

        view.findViewById(R.id.layout_picker_time).setVisibility(View.GONE);
        view.findViewById(R.id.layout_picker_temp).setVisibility(View.GONE);
        view.findViewById(R.id.layout_picker_speed).setVisibility(View.GONE);
        view.findViewById(R.id.switch_auto_transition).setVisibility(View.GONE);
        textAutoTransition.setVisibility(View.VISIBLE);

        view.findViewById(R.id.btn_add_stage).setVisibility(View.GONE);
        view.findViewById(R.id.btn_save).setVisibility(View.GONE);

        int index = RecipeManager.getInstance().getCurrentStageIndex();
        attached.setTitle("Stage " + (index+1));

        updateUi();

        return view;
    }


    /**
     * Show values of currrent StageInfo data.
     */
    private void updateUi() {
        StageInfo stageInfo = RecipeManager.getInstance().getCurrentStage();

        if(stageInfo.isAutoTransition()){
            textAutoTransition.setText("On");
        }
        else{
            textAutoTransition.setText("Off");
        }
        editDirection.setText(stageInfo.getDirection());
        textTime.setText((stageInfo.getTime() / 60) + "H:" + (stageInfo.getTime() / 60) + "M");
        textTemp.setText(stageInfo.getTemp() + "℉");
        textSpeed.setText(stageInfo.getSpeed() + "");
    }
}
