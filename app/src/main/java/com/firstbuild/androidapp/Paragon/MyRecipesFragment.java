package com.firstbuild.androidapp.paragon;


import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.viewUtil.SwipeMenu;
import com.firstbuild.androidapp.viewUtil.SwipeMenuCreator;
import com.firstbuild.androidapp.viewUtil.SwipeMenuItem;
import com.firstbuild.androidapp.viewUtil.SwipeMenuListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyRecipesFragment extends Fragment {

    private String TAG = MyRecipesFragment.class.getSimpleName();
    private ParagonMainActivity attached = null;
//    private ArrayList<RecipeInfo> listRecipeDataInfo = new ArrayList<>();

    private SwipeMenuListView recipeListView;
    private RecipeListAdapter recipeListAdapter;

    public MyRecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        attached.setTitle("My Recipes");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_recipes, container, false);

        recipeListView = (SwipeMenuListView) view.findViewById(R.id.list_recipes);
        recipeListAdapter = new RecipeListAdapter();
        recipeListView.setAdapter(recipeListAdapter);


        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem item;

                // create "edit" item
                item = new SwipeMenuItem(attached.getApplicationContext());
                item.setBackground(R.color.colorParagonDivider);
                item.setWidth(dp2px(90));
                item.setTitle("Edit");
                item.setTitleSize(18);
                item.setTitleColor(Color.WHITE);
                menu.addMenuItem(item);


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
        recipeListView.setMenuCreator(creator);

        // step 2. listener item click event
        recipeListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 0:
                        // open
                        Log.d(TAG, "onMenuItemClick 0");
                        RecipeManager.getInstance().setCurrentRecipe(position);
                        attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_EDIT_RECIPES);
                        break;
                    case 1:
                        // delete
                        Log.d(TAG, "onMenuItemClick 1");
                        RecipeManager.getInstance().remove(position);
                        recipeListAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });


        // other setting
        recipeListView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        recipeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Toast.makeText(attached.getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        readRecipes();

        return view;
    }


    /**
     * Read recipes from file.
     */
    private void readRecipes() {
        RecipeManager.getInstance().ReadFromFile();


//
//        int size = RecipeManager.getInstance().getSize();
//
//        for(int i = 0; i < size; i++){
//            listRecipeDataInfo.add(RecipeManager.getInstance().get(i));
//        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity) getActivity();
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


    public class RecipeListAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return RecipeManager.getInstance().getSize();
        }

        @Override
        public RecipeInfo getItem(int position) {
            return RecipeManager.getInstance().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(attached.getApplicationContext(), R.layout.adapter_recipe_item, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            RecipeInfo item = getItem(position);
            holder.name.setText(item.getName());
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
