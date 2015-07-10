/**
 * @file DashboardAdapter.java
 * @brief simple description
 * @author Hollis Kim(320006828)
 * @date Jul/06/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */
package com.firstbuild.androidapp.dashboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productManager.ProductInfo;

import java.util.ArrayList;

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ProductInfo> listProductInfo = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private ClickListener clickListener;
    private Context context;

    /**
     * Constructor
     *
     * @param context Context get from Dashboard Activity.
     */
    public DashboardAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

    }

    /**
     * Replace DashboardCardInfo list and notify redraw screen.
     *
     * @param listProductInfo
     */
    public void updateData(ArrayList<ProductInfo> listProductInfo) {
        this.listProductInfo = listProductInfo;
        notifyItemRangeChanged(0, listProductInfo.size());
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.adapter_product_card_view, parent, false);
        ViewHolderDashboard viewHolder = new ViewHolderDashboard(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ProductInfo currentProduct = listProductInfo.get(position);
        ViewHolderDashboard holderDashboard = (ViewHolderDashboard)holder;

        if (currentProduct.type == ProductInfo.PRODUCT_TYPE_CILLHUB) {
            holderDashboard.imageLogo.setImageResource(R.drawable.ic_logo_name_chillhub);
            holderDashboard.textDescription.setText(R.string.descript_chilhub);
        }
        else if (currentProduct.type == ProductInfo.PRODUCT_TYPE_PARAGON) {
            holderDashboard.imageLogo.setImageResource(R.drawable.ic_logo_name_paragon);
            holderDashboard.textDescription.setText(R.string.descript_paragon);
        }
        else {
        }

    }

    @Override
    public int getItemCount() {
        return listProductInfo.size();
    }

    /**
     * Set click listener
     *
     * @param clickListener
     */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    /**
     * Get DashboardCardInfo class in the array list by given position.
     *
     * @param position Zero based index of array list.
     * @return Single DashboardCardInfo object.
     */
    public ProductInfo getItem(int position) {
        return listProductInfo.get(position);
    }

    /**
     * Called when user click item. Send selected view and position.
     */
    public interface ClickListener {
        public void itemClicked(View view, int position);
    }


    /**
     * ViewHolder for CardList.
     */
    class ViewHolderDashboard extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imageLogo;
        private final TextView textDescription;

        /**
         * Basic constructor.
         *
         * @param itemView Parent view. Card container layout view.
         */
        public ViewHolderDashboard(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageLogo = (ImageView) itemView.findViewById(R.id.imageLogo);
            textDescription = (TextView) itemView.findViewById(R.id.itemDescription);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }

        }
    }
}
