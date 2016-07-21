package com.firstbuild.androidapp.opal;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.firstbuild.androidapp.R;

/**
 * Created by hans on 16. 7. 19..
 */
public class OpalHelpTutorialFragment extends DialogFragment {

    private ViewPager help_view_pager;
    private LinearLayout pager_indicator;
    private ImageButton exit_btn;
    private HelpTutorialViewPagerAdapter adapter;

    private int prevSelectedPos;

    private int[] tutorialImages = {
            R.drawable.img_help_instruction_tap_first,
            R.drawable.img_help_instruction_drag_second,
            R.drawable.img_help_instruction_apply_third,
            R.drawable.img_help_instruction_dots_fourth,
            R.drawable.img_help_instruction_select_fifth
    };

    private int[] tutorialText = {
            R.string.schedule_help_tutorial_body_first,
            R.string.schedule_help_tutorial_body_second,
            R.string.schedule_help_tutorial_body_third,
            R.string.schedule_help_tutorial_body_fourth,
            R.string.schedule_help_tutorial_body_fifth,
            R.string.schedule_help_tutorial_body_sixth
    };

    private ImageView[] dots;

    public OpalHelpTutorialFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static OpalHelpTutorialFragment getInstance() {
        OpalHelpTutorialFragment dialogFragment = new OpalHelpTutorialFragment();

        Bundle args = new Bundle();
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get 90% of screen height
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int height = dm.heightPixels;
        height *= 0.9;

        // Set height of Dialog to 90% of screen height
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = height;

        getDialog().getWindow().setAttributes(params);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_help_tutorial, container);

        help_view_pager = (ViewPager) view.findViewById(R.id.pager_help_tutorial);
        pager_indicator = (LinearLayout) view.findViewById(R.id.viewPagerCountDots);

        adapter = new HelpTutorialViewPagerAdapter(getContext(), tutorialImages, tutorialText, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        help_view_pager.setAdapter(adapter);
        help_view_pager.setCurrentItem(0);
        help_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                // Toggle indicator according to the current selected position
                if(prevSelectedPos != position) {
                    dots[position].setSelected(true);
                    dots[prevSelectedPos].setSelected(false);

                    prevSelectedPos = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        configureIndicator();

        return view;
    }

    private void configureIndicator() {

        int count = adapter.getCount();
        dots = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(15, 0, 15, 0);

        // Configure Indicator view
        for(int i=0; i<count; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.selector_help_tutorial_indicator, getActivity().getTheme()));
            pager_indicator.addView(dots[i], params);
        }

        dots[0].setSelected(true);
        prevSelectedPos = 0;
    }
}
