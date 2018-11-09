package com.missilelauncher.missilelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import static com.missilelauncher.missilelauncher.SharedListPreferencesHelper.PREFS_NAME;

public class GroupIconPicker extends AppCompatActivity {
    private SharedPreferences settingsPrefs;
    public static final String PREFS_NAME = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_pick);

        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ImageAdapter ia = new ImageAdapter(GroupIconPicker.this);

                //Toast.makeText(GroupIconPicker.this, "Grid Position: " + position + ", itemID: "+ia.getItemId(position),Toast.LENGTH_SHORT).show();

                Intent mIntent = getIntent();
                int group = mIntent.getIntExtra("Group", 1);
                settingsPrefs.edit().putLong("iconID"+group, ia.getItemId(position)).commit();
                SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("iconID"+group, ia.getItemId(position));
                editor.apply();
                finish();
            }
        });
    }
}
