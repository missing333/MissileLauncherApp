package com.missilelauncher.missilelauncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class GroupIconPicker extends AppCompatActivity {
    private SharedPreferences settingsPrefs;

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
                int intValue = mIntent.getIntExtra("Group", 1);
                settingsPrefs.edit().putLong("iconID"+intValue, ia.getItemId(position)).commit();
                finish();
            }
        });
    }
}
