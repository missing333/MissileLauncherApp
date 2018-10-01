package com.missilelauncher.overlaytestv1;

/**
 * Created by mmissildine on 9/28/2018.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class GridItemView extends FrameLayout {

    private ImageView iconView;
    private TextView textView;

    public GridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_grid, this);
        iconView = (ImageView) getRootView().findViewById(R.id.appIcon);
        textView = (TextView) getRootView().findViewById(R.id.text);
    }

    public void display(String text, Drawable icon, boolean isSelected) {
        iconView.setImageDrawable(icon);
        textView.setText(text);
        display(isSelected);
    }

    public void display(boolean isSelected) {
        textView.setBackgroundResource(isSelected ? R.drawable.primary_light_square: R.drawable.gray_square);
    }
}
