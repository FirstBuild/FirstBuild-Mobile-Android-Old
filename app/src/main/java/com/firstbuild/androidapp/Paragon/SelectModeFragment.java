package com.firstbuild.androidapp.Paragon;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.dashboard.DashboardAdapter;

import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;
import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.animators.OvershootInLeftAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectModeFragment extends Fragment  implements SelectModeAdapter.ClickListener {


    private String TAG = SelectModeFragment.class.getSimpleName();
    private RecyclerView listMode;
    private SelectModeAdapter selectModeAdapter;
    private SelectModeSteps selectModeSteps;

    private enum SelectModeSteps {
        STEP_COOKING_METHOD,
        STEP_MATERIAL,
        STEP_HOW_TO_COOK,
    }


    public SelectModeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_mode, container, false);

        view.findViewById(R.id.btn_my_recipes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick my recipes");

            }
        });

        view.findViewById(R.id.btn_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick custom");

            }
        });

        listMode = (RecyclerView) view.findViewById(R.id.list_mode);

        selectModeAdapter = new SelectModeAdapter(getActivity());
        selectModeAdapter.setClickListener(this);

        SlideInUpAnimator animator = new SlideInUpAnimator();
        animator.setRemoveDuration(500);
        animator.setAddDuration(200);
        listMode.setItemAnimator(animator);
        listMode.setAdapter(selectModeAdapter);
        listMode.setLayoutManager(new LinearLayoutManager(getActivity()));

        selectModeSteps = SelectModeSteps.STEP_COOKING_METHOD;
        removeAllList();
        fillList(R.array.paragon_modes);

        return view;
    }


    private void removeAllList(){
        int size = selectModeAdapter.getItemCount();

        for (int i = 0; i < size; i++) {
            selectModeAdapter.removeItem(0);
        }
    }


    private void fillList(int resourceId){
        String[] arryString = getResources().getStringArray(resourceId);

        for (int i = 0; i < arryString.length; i++) {
            selectModeAdapter.addItem(arryString[i]);
        }
    }


    @Override
    public void itemClicked(View view, int position) {
        Log.d(TAG, "itemclicked "+position);

        switch(selectModeSteps){
            case STEP_COOKING_METHOD:
                if(position == 2){
                    selectModeSteps = SelectModeSteps.STEP_MATERIAL;
                    removeAllList();
                    fillList(R.array.paragon_modes_sousvide);
                }
                else{
                    //do nothing
                }
                break;

            case STEP_MATERIAL:
                if(position == 0){
                    selectModeSteps = SelectModeSteps.STEP_HOW_TO_COOK;
                    removeAllList();
                    fillList(R.array.paragon_modes_beef);
                }
                else{
                    //do nothing
                }
                break;

            case STEP_HOW_TO_COOK:
                if(position == 0){
                    ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_READY_PREHEAT);
                }
                else{
                    //do nothing
                }
                break;
        }

    }
}
