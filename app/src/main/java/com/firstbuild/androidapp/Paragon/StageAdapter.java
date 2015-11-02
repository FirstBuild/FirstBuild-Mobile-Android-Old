package com.firstbuild.androidapp.paragon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;

import java.util.ArrayList;

/**
 * Created by Hollis on 11/2/15.
 */
public class StageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<StageInfo> listStageInfo = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;
    private ClickListener  clickListener;

    public StageAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        if(viewType == StageInfo.TYPE_ADD_ITEM){
            view = layoutInflater.inflate(R.layout.adapter_stage_add, parent, false);
        }
        else{
            view = layoutInflater.inflate(R.layout.adapter_stage_item, parent, false);
        }

        ViewHolderStage viewHolder = new ViewHolderStage(view, viewType);

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return listStageInfo.get(position).getType();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolderStage holderStage = (ViewHolderStage) holder;

        if (holderStage.title != null) {
            holderStage.title.setText("Stage " + (position + 1));
        }
        else {
            // do nothing.
        }
    }

    @Override
    public int getItemCount() {
        return listStageInfo.size();
    }

    public void addItem(StageInfo item){
        listStageInfo.add(item);
        notifyItemInserted(listStageInfo.size());
    }

    public void removeItem(int position){
        listStageInfo.remove(position);
        notifyItemRemoved(position);

    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }


    /**
     * ViewHolder for Stage
     */
    class ViewHolderStage extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title = null;

        /**
         * Basic constructor.
         *
         * @param itemView Parent view. Card container layout view.
         */
        public ViewHolderStage(View itemView, int viewType) {
            super(itemView);
            itemView.setOnClickListener(this);

            if(viewType == StageInfo.TYPE_NORMAL){
                title = (TextView) itemView.findViewById(R.id.text_name);
            }
            else{
                // do nothing.
            }
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }

        }
    }
}
