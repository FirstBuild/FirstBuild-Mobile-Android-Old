package com.firstbuild.androidapp.addproduct;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.ProductInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductConnectionErrorFragment extends Fragment {
    private String TAG = AddProductActivity.class.getSimpleName();

    private final static String KEY_CURRENT_PRODUCT_TYPE = "key_current_product_type";

    public AddProductConnectionErrorFragment() {
        // Required empty public constructor
        Log.d(TAG, "AddProductSearchParagonFragment IN");
    }

    /**
     * Create instances of the fragment.
     */
    public static AddProductConnectionErrorFragment newInstance(int type) {

        AddProductConnectionErrorFragment fragment = new AddProductConnectionErrorFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_CURRENT_PRODUCT_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product_connection_error, container, false);

        ((TextView)view.findViewById(R.id.connection_error_description)).setText(getCurrentProductErrorString());

        view.findViewById(R.id.button_try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pressed");

                Fragment previousFragment = getPreviousSearchFragment();

                if(previousFragment != null) {
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getFragmentManager().
                            beginTransaction().
                            replace(R.id.content_frame, previousFragment).
                            addToBackStack(null).
                            commit();
                }
                else {
                    Log.d(TAG, "Previous Fragment is not supported at the moment, Check return value of getPreviousSearchFragment() ");
                }

            }
        });

        // Catch back button
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Log.i(TAG, "Back button pressed");
                    getFragmentManager().
                            beginTransaction().
                            replace(R.id.content_frame, new AddProductSelectFragment()).
                            addToBackStack(null).
                            commit();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        return view;
    }

    private String getCurrentProductErrorString() {
        int currentProductType = getArguments().getInt(KEY_CURRENT_PRODUCT_TYPE);
        String ret = "";

        if(currentProductType == ProductInfo.PRODUCT_TYPE_PARAGON) {

            ret = getString(R.string.connection_error_explanation);

        }else if(currentProductType == ProductInfo.PRODUCT_TYPE_OPAL) {

            ret = getString(R.string.connection_error_explanation_opal);

        } else {
            // Do nothing
        }

        return ret;
    }

    private Fragment getPreviousSearchFragment() {

        int currentProductType = getArguments().getInt(KEY_CURRENT_PRODUCT_TYPE);
        Fragment previousSearchFragment = null;

        if(currentProductType == ProductInfo.PRODUCT_TYPE_PARAGON) {

            previousSearchFragment = new AddProductSearchParagonFragment();

        }else if(currentProductType == ProductInfo.PRODUCT_TYPE_OPAL) {

            previousSearchFragment = new AddProductSearchOpalFragment();

        } else {
            // Do nothing
        }

        return previousSearchFragment;
    }
}
