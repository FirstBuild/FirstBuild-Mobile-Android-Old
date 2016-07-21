package com.firstbuild.androidapp.opal;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * Created by hans on 16. 7. 20..
 */
public class HelpTutorialViewPagerAdapter extends PagerAdapter{

    private Context context;
    private int[] imageResources;
    private int[] textResources;
    private View.OnClickListener listener;

    public HelpTutorialViewPagerAdapter(Context c, int[] imageRes, int[] textRes, View.OnClickListener l) {
        context = c;
        imageResources = imageRes;
        textResources = textRes;
        listener = l;
    }


    @Override
    public int getCount() {
        return textResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.help_tutorial_viewpager_item, container, false);

        ImageView bodyImage = (ImageView)itemView.findViewById(R.id.help_tutorial_image);
        TextView bodyText = (TextView)itemView.findViewById(R.id.help_tutorial_body);
        TextView titleText = (TextView)itemView.findViewById(R.id.help_tutorial_title);

        // modify layout for the last page
        if(position == getCount() -1 ) {
            bodyImage.setVisibility(View.GONE);

            // show exit button and set the click listener
            View exit = itemView.findViewById(R.id.help_tutorial_exit_btn);
            exit.setVisibility(View.VISIBLE);
            exit.setOnClickListener(listener);

            // change text and style
            titleText.setText(R.string.got_it);
            titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleText.getTextSize() * 2);
            titleText.setTranslationY(context.getResources().getDimensionPixelSize(R.dimen.help_tutorial_title_text_size));
        }else {
            bodyImage.setImageResource(imageResources[position]);
        }

        bodyText.setText(textResources[position]);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }
}
