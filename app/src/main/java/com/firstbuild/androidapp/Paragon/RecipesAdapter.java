package com.firstbuild.androidapp.paragon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

import java.util.ArrayList;

/**
 * Created by Hollis on 10/28/15.
 */
public class RecipesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<RecipeDataInfo> listRecipeDataInfo = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;
    private ClickListener  clickListener;

    public RecipesAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.adapter_recipe_item, parent, false);
        ViewHolderRecipe viewHolder = new ViewHolderRecipe(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String name = listRecipeDataInfo.get(position).getName();
        String image = listRecipeDataInfo.get(position).getImageFileName();

        ViewHolderRecipe holderRecipe = (ViewHolderRecipe) holder;

        holderRecipe.name.setText(name);
    }

    @Override
    public int getItemCount() {
        return listRecipeDataInfo.size();
    }

    public void addItem(RecipeDataInfo item){
        listRecipeDataInfo.add(item);
        notifyItemInserted(listRecipeDataInfo.size());
    }

    public void removeItem(int position){
        listRecipeDataInfo.remove(position);
        notifyItemRemoved(position);

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }


    /**
     * ViewHolder for RecipeDataInfo.
     */
    class ViewHolderRecipe extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView name;

        /**
         * Basic constructor.
         *
         * @param itemView Parent view. Card container layout view.
         */
        public ViewHolderRecipe(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(R.id.text_name);
            image = (ImageView) itemView.findViewById(R.id.image_thumbnail);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }

        }
    }

}
