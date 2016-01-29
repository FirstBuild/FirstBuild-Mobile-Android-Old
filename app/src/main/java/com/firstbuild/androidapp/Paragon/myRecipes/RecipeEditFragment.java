package com.firstbuild.androidapp.paragon.myRecipes;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.ParagonMainActivity;
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
public class RecipeEditFragment extends Fragment {

    private String TAG = RecipeEditFragment.class.getSimpleName();

    private EditText editIngredients;
    private EditText editDirections;
    private SwipeMenuListView stageListView;
    private RadioGroup groupDetail;

    private StageListAdapter stageListAdapter;
    private EditText editName;
    private ParagonMainActivity attached = null;
    private View layoutStages;
    private ImageView imageTitle;
    private String imageFileName;


    public RecipeEditFragment() {
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
        View view = inflater.inflate(R.layout.fragment_recipe_edit, container, false);

        editName = (EditText) view.findViewById(R.id.edit_name);

        editIngredients = (EditText) view.findViewById(R.id.edit_ingredients);
        editDirections = (EditText) view.findViewById(R.id.edit_directions);
        groupDetail = (RadioGroup) view.findViewById(R.id.group_recipe_detail);
        layoutStages = view.findViewById(R.id.layout_stages);
        imageTitle = (ImageView) view.findViewById(R.id.image_title);

        view.findViewById(R.id.layout_title_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attached.dispatchTakePictureIntent();
            }
        });

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

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem item;

                // create "delete" item
                item = new SwipeMenuItem(attached.getApplicationContext());
                item.setBackground(R.color.colorParagonHighlight);
                item.setWidth(dp2px(90));
                item.setTitle("Delete");
                item.setTitleSize(18);
                item.setTitleColor(Color.WHITE);
                menu.addMenuItem(item);
            }
        };
        // set creator
        stageListView.setMenuCreator(creator);

        // step 2. listener item click event
        stageListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 1:
                        // delete
                        Log.d(TAG, "onMenuItemClick 1");
                        RecipeManager.getInstance().getCurrentRecipe().deleteStage(position);
                        stageListAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });


        // other setting
        stageListView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        stageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Toast.makeText(attached.getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        stageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecipeManager.getInstance().setCurrentStage(position);
                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_EDIT_STAGE);
            }
        });

        view.findViewById(R.id.fab_add_stage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_EDIT_STAGE);
            }
        });

        view.findViewById(R.id.btn_cook).setVisibility(View.GONE);
        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RecipeInfo recipe = RecipeManager.getInstance().getCurrentRecipe();

                recipe.setName(editName.getText().toString());
                recipe.setIngredients(editIngredients.getText().toString());
                recipe.setDirections(editDirections.getText().toString());
                recipe.setImageFileName(imageFileName);

                RecipeManager.getInstance().restoreCurrentRecipe();
                attached.getFragmentManager().popBackStack();
            }
        });


        getCurrentRecipe();


        return view;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                attached.getResources().getDisplayMetrics());
    }

    private void getCurrentRecipe() {
        RecipeInfo recipe = RecipeManager.getInstance().getCurrentRecipe();

        editName.setText(recipe.getName());
        editIngredients.setText(recipe.getIngredients());
        editDirections.setText(recipe.getDirections());
    }


    public void setRecipeImage(Bitmap imageBitmap, String currentPhotoPath) {
        imageTitle.setImageBitmap(imageBitmap);

        imageFileName = currentPhotoPath;

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

            if (stage.getType() == StageInfo.TYPE_ADD_ITEM) {
                convertView = View.inflate(attached.getApplicationContext(), R.layout.adapter_stage_add, null);
            }
            else {
                convertView = View.inflate(attached.getApplicationContext(), R.layout.adapter_stage_item, null);
            }

            new ViewHolder(convertView);

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
