package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;
import com.firstbuild.androidapp.viewUtil.SwipeMenu;
import com.firstbuild.androidapp.viewUtil.SwipeMenuCreator;
import com.firstbuild.androidapp.viewUtil.SwipeMenuItem;
import com.firstbuild.androidapp.viewUtil.SwipeMenuListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeViewFragment extends Fragment {


    private String TAG = RecipeViewFragment.class.getSimpleName();

    private EditText editIngredients;
    private EditText editDirections;
    private SwipeMenuListView stageListView;
    private RadioGroup groupDetail;

    private StageListAdapter stageListAdapter;
    private EditText editName;
    private ParagonMainActivity attached = null;
    private View layoutStages;

    public RecipeViewFragment() {
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
        attached.setTitle("My Recipes");

        attached.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe_edit, container, false);

        editName = (EditText) view.findViewById(R.id.edit_name);
        editIngredients = (EditText) view.findViewById(R.id.edit_ingredients);
        editDirections = (EditText) view.findViewById(R.id.edit_directions);
        groupDetail = (RadioGroup) view.findViewById(R.id.group_recipe_detail);
        layoutStages = view.findViewById(R.id.layout_stages);
        view.findViewById(R.id.fab_add_stage).setVisibility(View.GONE);

        editName.setKeyListener(null);
        editName.setBackgroundColor(0xFFFFFFFF);
        editName.setTextColor(getResources().getColor(R.color.colorParagonAccent));

        editIngredients.setKeyListener(null);
        editIngredients.setBackgroundColor(0xFFFFFFFF);
        editIngredients.setTextColor(0xFF000000);

        editDirections.setKeyListener(null);
        editDirections.setBackgroundColor(0xFFFFFFFF);
        editDirections.setTextColor(0xFF000000);

        groupDetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_ingredients:
                        editIngredients.setVisibility(View.VISIBLE);
                        editDirections.setVisibility(View.GONE);
                        layoutStages.setVisibility(View.GONE);
                        break;

                    case R.id.radio_directions:
                        editIngredients.setVisibility(View.GONE);
                        editDirections.setVisibility(View.VISIBLE);
                        layoutStages.setVisibility(View.GONE);
                        break;

                    case R.id.radio_settings:
                        editIngredients.setVisibility(View.GONE);
                        editDirections.setVisibility(View.GONE);
                        layoutStages.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        groupDetail.check(R.id.radio_ingredients);

        stageListAdapter = new StageListAdapter();

        stageListView = (SwipeMenuListView) view.findViewById(R.id.list_stages);
        stageListView.setAdapter(stageListAdapter);


        stageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecipeManager.getInstance().setCurrentStage(position);
                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_VIEW_STAGE);
            }
        });


        view.findViewById(R.id.btn_save).setVisibility(View.GONE);
        view.findViewById(R.id.btn_cook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeManager.getInstance().sendCurrentStages();
                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_GETREADY);

            }
        });


        getCurrentRecipe();

        return view;
    }

    private void getCurrentRecipe() {
        RecipeInfo recipe = RecipeManager.getInstance().getCurrentRecipe();

        editName.setText(recipe.getName());
        editIngredients.setText(recipe.getIngredients());
        editDirections.setText(recipe.getDirections());

    }


    public class StageListAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return RecipeManager.getInstance().getCurrentRecipe().numStage();
        }

        @Override
        public StageInfo getItem(int position) {
            return RecipeManager.getInstance().getCurrentRecipe().getStage(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            StageInfo stage = getItem(position);

//            if (convertView == null) {

                if (stage.getType() == StageInfo.TYPE_ADD_ITEM) {
                    convertView = View.inflate(attached.getApplicationContext(), R.layout.adapter_stage_add, null);
                }
                else {
                    convertView = View.inflate(attached.getApplicationContext(), R.layout.adapter_stage_item, null);
                }

                new ViewHolder(convertView);
//            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (stage.getType() == StageInfo.TYPE_ADD_ITEM) {
                // do nothing
            }
            else {
                holder.name.setText("Stage " + (position + 1));
            }



            return convertView;
        }


        class ViewHolder {
            TextView name;

            public ViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.text_name);
                view.setTag(this);
            }
        }

    }

}
