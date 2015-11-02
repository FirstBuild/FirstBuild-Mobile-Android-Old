package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyRecipesFragment extends Fragment implements RecipesAdapter.ClickListener{

    private String TAG = MyRecipesFragment.class.getSimpleName();
    private ParagonMainActivity attached = null;
    private RecipesAdapter recipesAdapter;
    private RecyclerView listRecipes;

    public MyRecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        attached.setTitle("My Recipes");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_recipes, container, false);

        listRecipes = (RecyclerView) view.findViewById(R.id.list_recipes);

        recipesAdapter = new RecipesAdapter(getActivity());
        recipesAdapter.setClickListener(this);

        listRecipes.setAdapter(recipesAdapter);
        listRecipes.setLayoutManager(new LinearLayoutManager(getActivity()));

        readRecipes();

        return view;
    }


    /**
     * Read recipes from file.
     */
    private void readRecipes() {
        RecipeManager.getInstance().ReadFromFile();

        int size = RecipeManager.getInstance().getSize();

        for(int i = 0; i < size; i++){
            recipesAdapter.addItem(RecipeManager.getInstance().get(i));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity) getActivity();
    }

    @Override
    public void itemClicked(View view, int position) {
        Log.d(TAG, "recipe clicked");

        RecipeManager.getInstance().setCurrentRecipe(position);
        attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_EDIT_RECIPES);
    }
}
