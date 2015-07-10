package com.firstbuild.androidapp.AddProduct;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductParagonFragment extends Fragment {


    public AddProductParagonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank_fragmen_add_product_paragon, container, false);

//        view.findViewById(R.id.imgSpinner).startAnimation(makeAnimation());

        return view;
    }


    private RotateAnimation makeAnimation() {

        RotateAnimation animation = new RotateAnimation(0, 360);
        animation.setDuration(250);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }
        });

        return animation;
    }

}
