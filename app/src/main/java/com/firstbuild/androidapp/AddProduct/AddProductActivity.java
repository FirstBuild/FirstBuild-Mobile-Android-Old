package com.firstbuild.androidapp.addproduct;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.ProductInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;

public class AddProductActivity extends AppCompatActivity {
    private String TAG = AddProductActivity.class.getSimpleName();
    private ProductInfo newProduct = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_product);

        // Set title name
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(R.string.add_product_screen_title);

        setSupportActionBar(toolbar);

        getFragmentManager().
                beginTransaction().
                replace(R.id.content_frame, new AddProductSelectFragment()).
                addToBackStack(null).
                commit();


//        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        registerReceiver(mPairReceiver, intent);
//


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



    public void setNewProductAddress(String deviceAddress) {
        if(newProduct != null){
            newProduct.address = deviceAddress;
        }
    }

    public void createNewProduct(int productTypeParagon) {

        newProduct = new ProductInfo(productTypeParagon, "", "");
    }

    public void setNewProductNickname(String nickName) {
        if(newProduct != null){
            newProduct.nickname = nickName;
        }
    }

    public void addNewProductToList() {
        if(newProduct != null){
            ProductManager.getInstance().add(newProduct);
        }
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
