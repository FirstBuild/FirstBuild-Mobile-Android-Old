package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.productManager.ProductInfo;
import com.firstbuild.androidapp.productManager.ProductManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetReadyFragment extends Fragment {


    private ParagonMainActivity attached;
    private String TAG = GetReadyFragment.class.getSimpleName();
    private MaterialDialog dialogTryAgain = null;

    public GetReadyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = (ParagonMainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_get_ready, container, false);

        view.findViewById(R.id.btn_done).setVisibility(View.GONE);

        ((TextView) view.findViewById(R.id.text_explanation)).setText(Html.fromHtml("Press <b>START</b> on your Paragon"));

        ((ParagonMainActivity) getActivity()).setTitle("Get Ready");

        dialogTryAgain = new MaterialDialog.Builder(attached)
                .content("Induction Cookware not detected\nPlease check your Cookware is turn on.")
                .positiveText(null)
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // TODO: Block RecipeManager until multi-stage activated.
//                        RecipeManager.getInstance().sendCurrentStages();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }
                })
                .cancelable(false).build();

        ProductInfo product = ProductManager.getInstance().getCurrent();

//        if (product.getErdCurrentCookMode() != ParagonValues.CURRENT_COOK_MODE_RAPID &&
//                product.getErdCurrentCookMode() != ParagonValues.CURRENT_COOK_MODE_GENTLE) {
//            dialogTryAgain.show();
//        }
//        else {
//            //do nothing.
//        }


        return view;
    }

    public void onCookModeChanged(){
        byte cookMode = ProductManager.getInstance().getCurrent().getErdCurrentCookMode();

        if(cookMode == ParagonValues.CURRENT_COOK_MODE_RAPID ||
                cookMode == ParagonValues.CURRENT_COOK_MODE_GENTLE){
            dialogTryAgain.setActionButton(DialogAction.POSITIVE, "Try again");
        }
        else{
            dialogTryAgain.setActionButton(DialogAction.POSITIVE, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        dialogTryAgain.dismiss();
    }
}
