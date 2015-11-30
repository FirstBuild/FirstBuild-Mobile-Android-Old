package com.firstbuild.androidapp.addProduct;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productManager.ProductInfo;

public class AddProductSelectFragment extends Fragment {
    private String TAG = AddProductSelectFragment.class.getSimpleName();
    private AddProductActivity attached = null;

    public AddProductSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = (AddProductActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_add_product_paragon, container, false);

        view.findViewById(R.id.relative_layout_add_product_paragon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pressed");

                attached.createNewProduct(ProductInfo.PRODUCT_TYPE_PARAGON);

                getFragmentManager().
                        beginTransaction().
                        replace(R.id.content_frame, new AddProductSearchParagonFragment()).
                        addToBackStack(null).
                        commit();
            }
        });


        return view;
    }


}
