package com.firstbuild.androidapp.paragon.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

import java.util.ArrayList;

public class SelectModeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> listMode = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private ClickListener  clickListener;
    private Context        context;

    public SelectModeAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.adapter_selectmode_item, parent, false);
        ViewHolderMode viewHolder = new ViewHolderMode(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String modeName = listMode.get(position);
        ViewHolderMode holderDashboard = (ViewHolderMode) holder;

        holderDashboard.modeName.setText(modeName);
    }


    @Override
    public int getItemCount() {

        return listMode.size();
    }

    public void addItem(String item){
        listMode.add(item);
        notifyItemInserted(listMode.size());
    }

    public void removeItem(int position){
        listMode.remove(position);
        notifyItemRemoved(position);

    }

//
//    public void updateData(String[] listMode) {
//        this.listMode = listMode;
//        notifyItemRangeChanged(0, listMode.length);
//    }
//

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }


    /**
     * ViewHolder for CardList.
     */
    class ViewHolderMode extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView modeName;

        /**
         * Basic constructor.
         *
         * @param itemView Parent view. Card container layout view.
         */
        public ViewHolderMode(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            modeName = (TextView) itemView.findViewById(R.id.item_name);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }

        }
    }
}
