package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.datamodel.BuiltInRecipeInfo;
import com.firstbuild.androidapp.paragon.datamodel.BuiltInRecipeSettingsInfo;
import com.firstbuild.androidapp.paragon.helper.SelectModeAdapter;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectModeFragment extends Fragment implements SelectModeAdapter.ClickListener {


    private String TAG = SelectModeFragment.class.getSimpleName();
    private RecyclerView listMode;
    private SelectModeAdapter selectModeAdapter;
    private SelectModeSteps selectModeSteps;
    private View layoutButtons;
    private ParagonMainActivity attached;
    private BuiltInRecipeInfo builtInRecipes = null;

    public SelectModeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_select_mode, container, false);

        //TODO: block recipeManaber until multi-stage enabled.
//        view.findViewById(R.id.btn_my_recipes).setVisibility(View.GONE);
//        view.findViewById(R.id.btn_my_recipes).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onclick my recipes");
//                RecipeManager.getInstance().ReadFromFile();
//                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_MY_RECIPES);
//
//            }
//        });

        view.findViewById(R.id.btn_quick_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick Quick Start");

                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_QUICK_START);

            }
        });

        layoutButtons = view.findViewById(R.id.layout_buttons);

        listMode = (RecyclerView) view.findViewById(R.id.list_mode);

        selectModeAdapter = new SelectModeAdapter(getActivity());
        selectModeAdapter.setClickListener(this);

        SlideInUpAnimator animator = new SlideInUpAnimator();
        animator.setRemoveDuration(100);
        animator.setAddDuration(200);
        listMode.setItemAnimator(animator);
        listMode.setAdapter(selectModeAdapter);
        listMode.setLayoutManager(new LinearLayoutManager(getActivity()));

        builtInRecipes = attached.builtInRecipes;

        selectModeSteps = SelectModeSteps.STEP_COOKING_METHOD;
        removeAllList();
        fillList();

        ((ParagonMainActivity) getActivity()).setTitle("Paragon");

        return view;
    }

    private void removeAllList() {
        int size = selectModeAdapter.getItemCount();

        for (int i = 0; i < size; i++) {
            selectModeAdapter.removeItem(0);
        }
    }

    private void fillList() {
        ArrayList<BuiltInRecipeInfo> recipeFoods = builtInRecipes.child;

        for (BuiltInRecipeInfo recipeInfo : recipeFoods) {
            selectModeAdapter.addItem(recipeInfo.name);
        }

    }

    @Override
    public void itemClicked(View view, int position) {
        Log.d(TAG, "itemclicked " + position);

        int size = builtInRecipes.child.size();

        if (position < 0 || position >= size) {
            return;
        }

        builtInRecipes = builtInRecipes.child.get(position);

        if(builtInRecipes.type == BuiltInRecipeInfo.TYPE_FOOD){
            removeAllList();
            fillList();
        }
        else{
            attached.selectedBuiltInRecipe = (BuiltInRecipeSettingsInfo)builtInRecipes;
            SetTitle("Settings");

            attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_SETTINGS);
        }

    }

    /**
     * Set title text on header.
     *
     * @param text string to be title.
     */
    private void SetTitle(String text) {
        ((ParagonMainActivity) getActivity()).setTitle(text);
    }

    public void onBackPressed() {
        if(builtInRecipes.parent != null){
            builtInRecipes = builtInRecipes.parent;
            removeAllList();
            fillList();
        }
        else{
            attached.finishParagonMain();
        }

    }

    private enum SelectModeSteps {
        STEP_COOKING_METHOD,
        STEP_MATERIAL,
        STEP_HOW_TO_COOK,
    }
}
