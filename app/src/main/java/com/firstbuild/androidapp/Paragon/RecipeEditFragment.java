package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeDataInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeEditFragment extends Fragment implements StageAdapter.ClickListener {


    private EditText editIngredients;
    private EditText editDirections;
    private RecyclerView listStages;
    private RadioGroup groupDetail;

    private StageAdapter stageAdapter;
    private EditText editName;
    private ParagonMainActivity attached = null;


    public RecipeEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe_edit, container, false);

        editName = (EditText) view.findViewById(R.id.edit_name);

        editIngredients = (EditText) view.findViewById(R.id.edit_ingredients);
        editDirections = (EditText) view.findViewById(R.id.edit_directions);
        listStages = (RecyclerView) view.findViewById(R.id.list_stages);
        groupDetail = (RadioGroup) view.findViewById(R.id.group_recipe_detail);

        groupDetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_ingredients:
                        editIngredients.setVisibility(View.VISIBLE);
                        editDirections.setVisibility(View.GONE);
                        listStages.setVisibility(View.GONE);
                        break;

                    case R.id.radio_directions:
                        editIngredients.setVisibility(View.GONE);
                        editDirections.setVisibility(View.VISIBLE);
                        listStages.setVisibility(View.GONE);
                        break;

                    case R.id.radio_settings:
                        editIngredients.setVisibility(View.GONE);
                        editDirections.setVisibility(View.GONE);
                        listStages.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        groupDetail.check(R.id.radio_ingredients);


        stageAdapter = new StageAdapter(getActivity());
        stageAdapter.setClickListener(this);

        listStages.setAdapter(stageAdapter);
        listStages.setLayoutManager(new LinearLayoutManager(getActivity()));


        getCurrentRecipe();


        return view;
    }

    private void getCurrentRecipe() {
        RecipeDataInfo recipe = RecipeManager.getInstance().getCurrentRecipe();

        editName.setText(recipe.getName());
        editIngredients.setText(recipe.getIngredients());
        editDirections.setText(recipe.getDirections());

        int size = recipe.numStage();

        for (int i = 0; i < size; i++) {
            stageAdapter.addItem(recipe.getStage(i));
        }

        StageInfo stageAdd = new StageInfo(0, 0, 0, false, "");
        stageAdd.setType(StageInfo.TYPE_ADD_ITEM);
        stageAdapter.addItem(stageAdd);
    }


    @Override
    public void itemClicked(View view, int position) {
        if(position == stageAdapter.getItemCount()-1){
            // Case for Add stage button.

        }
        else{
            // Case for edit selected stage.

            RecipeManager.getInstance().setCurrentStage(position);
        }

        attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_EDIT_STAGE);

    }
}
