package com.firstbuild.androidapp.addProduct;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

public class AddProductActivity extends ActionBarActivity {
    private String TAG = AddProductActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_product);

        // Set title name
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText("ADD PRODUCT");

        setSupportActionBar(toolbar);

        getFragmentManager().
                beginTransaction().
                replace(R.id.content_frame, new AddProductSelectFragment()).
                addToBackStack(null).
                commit();

//        getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
//            @Override
//            public void onTransitionStart(Transition transition) {
//                Log.d(TAG, "onTransitionStart");
//            }
//
//            @Override
//            public void onTransitionEnd(Transition transition) {
//                Log.d(TAG, "onTransitionEnd");
//            }
//
//            @Override
//            public void onTransitionCancel(Transition transition) {
//                Log.d(TAG, "onTransitionCancel");
//            }
//
//            @Override
//            public void onTransitionPause(Transition transition) {
//                Log.d(TAG, "onTransitionPause");
//            }
//
//            @Override
//            public void onTransitionResume(Transition transition) {
//                Log.d(TAG, "onTransitionResume");
//            }
//        });

    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void enterReveal() {
//        // previously invisible view
//        final View myView = findViewById(R.id.layoutSelectProduct);
//
//        // get the center for the clipping circle
//        int cx = myView.getMeasuredWidth() / 2;
//        int cy = myView.getMeasuredHeight() / 2;
//
//        // get the final radius for the clipping circle
//        int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
//        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
//        myView.setVisibility(View.VISIBLE);
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
////                getWindow().getEnterTransition().removeListener(transitionListener);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        anim.start();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


//    void exitReveal() {
//        // previously visible view
//        final View myView = findViewById(R.id.my_view);
//
//        // get the center for the clipping circle
//        int cx = myView.getMeasuredWidth() / 2;
//        int cy = myView.getMeasuredHeight() / 2;
//
//        // get the initial radius for the clipping circle
//        int initialRadius = myView.getWidth() / 2;
//
//        // create the animation (the final radius is zero)
//        Animator anim =
//                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
//
//        // make the view invisible when the animation is done
//        anim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                myView.setVisibility(View.INVISIBLE);
//            }
//        });
//
//        // start the animation
//        anim.start();
//    }
}
