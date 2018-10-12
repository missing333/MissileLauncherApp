package com.missilelauncher.missilelauncher;

/**
 * Created by mmissildine on 9/28/2018.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.missilelauncher.missilelauncher.R;

public class GridItemView extends FrameLayout {

    private ImageView iconView;
    private TextView textView;

    public GridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_grid, this);
        iconView = getRootView().findViewById(R.id.appIcon);
        textView = getRootView().findViewById(R.id.text);
    }

    public void display(String text, Drawable icon, boolean isSelected) {
        LayoutParams gp = new LayoutParams(50,50 );
        gp.gravity = Gravity.CENTER;
        iconView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        iconView.setImageDrawable(icon);

        textView.setText(text);
        display(isSelected);
    }

    public void display(boolean isSelected) {
        textView.setBackgroundResource(isSelected ? R.drawable.primary_light_square: R.drawable.gray_square);

    }
}
