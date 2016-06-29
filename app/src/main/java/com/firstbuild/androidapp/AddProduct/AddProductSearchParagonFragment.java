package com.firstbuild.androidapp.addproduct;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.ProductInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductSearchParagonFragment extends AddProductSearchFragment {

    private String TAG = AddProductSearchParagonFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getCurrentApplianceType() {
        return ProductInfo.PRODUCT_TYPE_PARAGON;
    }

    @Override
    protected void transitionToPairSuccessScreen() {

        if(getFragmentManager() != null) {
            getFragmentManager().
                    beginTransaction().
                    replace(R.id.content_frame, new AddProductFoundParagonFragment()).
                    addToBackStack(null).
                    commit();
        }
        else {
            Log.d(TAG, "getFragmentManager() is null , so skip transitioning to next screen");
        }
    }
}
