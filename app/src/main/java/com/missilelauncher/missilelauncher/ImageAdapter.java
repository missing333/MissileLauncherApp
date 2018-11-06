package com.missilelauncher.missilelauncher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return mThumbIds[position];
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(185, 185));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(18, 18, 18, 18);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    public Integer[] mThumbIds = {
            //media
            R.drawable.ticket, R.drawable.reel,
            R.drawable.play, R.drawable.play_circle_filled,
            R.drawable.play_circle_outline, R.drawable.ic_music_video_black_24dp,
            R.drawable.ic_movie_creation_black_24dp,R.drawable.ic_image_black_24dp,
            R.drawable.ic_camera_alt_black_24dp,R.drawable.ic_audiotrack_black_24dp,

            //social
            R.drawable.ic_group_black_24dp, R.drawable.ic_group_work_black_24dp,
            R.drawable.ic_share_black_24dp, R.drawable.ic_language_black_24dp,
            R.drawable.ic_phone_black_24dp, R.drawable.ic_chat_black_24dp,
            R.drawable.ic_email_black_40dp, R.drawable.ic_forum_black_24dp,

            //games
            R.drawable.ic_games_black_24dp,R.drawable.ic_videogame_asset_black_24dp,
            R.drawable.ic_casino_black_24dp, R.drawable.ic_extension_black_24dp,

            //reading
            R.drawable.ic_book_black_24dp, R.drawable.ic_local_library_black_24dp,
            R.drawable.ic_import_contacts_black_24dp,

            //productivity
            R.drawable.ic_insert_chart_black_24dp,R.drawable.ic_insert_invitation_black_24dp,
            R.drawable.ic_keyboard_black_24dp, R.drawable.ic_location_city_black_24dp,

            //personalization
            R.drawable.ic_build_black_24dp,R.drawable.ic_tune_black_24dp,
            R.drawable.ic_phonelink_setup_black_24dp, R.drawable.ic_settings_black_24dp,

            //lifestyle
            R.drawable.ic_beach_access_black_24dp,R.drawable.ic_brightness_3_black_24dp,
            R.drawable.ic_brightness_low_black_24dp, R.drawable.ic_explore_black_24dp,
             R.drawable.ic_home_black_24dp,

            //other
            R.drawable.ic_spa_black_24dp,
            R.drawable.ic_favorite_black_24dp, R.drawable.ic_filter_vintage_black_40dp,
            R.drawable.ic_star_black_50dp, R.drawable.ic_star_border_black_24dp,
            R.drawable.ic_add_circle_outline_black_40dp, R.drawable.ic_add_circle_black_40,
            R.drawable.ic_radio_button_unchecked_black_24dp, R.drawable.ic_adjust_black_24dp,
            R.drawable.ic_lightbulb_outline_black_24dp, R.drawable.ic_flare_black_40dp,
            R.drawable.missile_launcher_icon_nobg

    };
}
