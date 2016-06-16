package com.firstbuild.androidapp.addproduct;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.ProductInfo;

/**
 * Created by hans on 16. 6. 3..
 */
public class AddProductSearchOpalFragment extends AddProductSearchFragment {

    private String TAG = AddProductSearchOpalFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  super.onCreateView(inflater, container, savedInstanceState);

        ((TextView)view.findViewById(R.id.search_description)).setText(R.string.searching_your_opal);
        ((ImageView)view.findViewById(R.id.imgParagonLogo)).setImageResource(R.drawable.img_opal_logo_gray);

        return view;
    }

    @Override
    protected int getCurrentApplianceType() {
        return ProductInfo.PRODUCT_TYPE_OPAL;
    }

    @Override
    protected void transitionToPairSuccessScreen() {

        if(getFragmentManager() != null) {
            getFragmentManager().
                    beginTransaction().
                    replace(R.id.content_frame, new AddProductFoundOpalFragment()).
                    addToBackStack(null).
                    commit();
        }
        else {
            Log.d(TAG, "getFragmentManager() is null , so skip transitioning to next screen");
        }

    }
}