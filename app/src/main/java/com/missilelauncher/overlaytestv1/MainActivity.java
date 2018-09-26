package com.missilelauncher.overlaytestv1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Collections;

import static java.sql.Types.INTEGER;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    private Button b;
    public SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("SettingsActivity", 0);
        prefEditor = sharedPref.edit();

        setContentView(R.layout.activity_main);

        String[] sizes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
        //int[] sizes = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        Spinner numGroupSpinner = (Spinner) findViewById(R.id.groupSpinner);
        Spinner numAppColsSpinner = (Spinner) findViewById(R.id.appColSpinner);

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this,R.layout.list_text_view, sizes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numGroupSpinner.setAdapter(aa);

        int spinnerPosition;
        spinnerPosition = aa.getPosition(sharedPref.getInt("numGroups",7)+"");
        numGroupSpinner.setSelection(spinnerPosition);
        numGroupSpinner.setOnItemSelectedListener(this);
        numAppColsSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numAppCols",9)+"");
        numAppColsSpinner.setSelection(spinnerPosition);
        numAppColsSpinner.setOnItemSelectedListener(this);

        b = (Button) findViewById(R.id.start);


    }

    @Override
    protected void onStart() {
        super.onStart();

        b = (Button) findViewById(R.id.start);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            b.setVisibility(View.VISIBLE);
        } else {
            b.setVisibility(View.GONE);
            Log.v("ol", "Ready to draw overlays");
            startService(new Intent(MainActivity.this, FloatingWindow.class));
        }

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        position++;
        parent.getItemAtPosition(position-1);
        parent.setOnItemSelectedListener(this);
        if (parent.toString().contains("groupSpinner")){
            prefEditor.putInt("numGroups",position).commit();
            Log.v("prefs","numGroups = " + position + " now.");
        }
        else if(parent.toString().contains("appRowSpinner")){
            prefEditor.putInt("numAppRows",position).commit();
            Log.v("prefs","numAppRows = " + position + " now.");
        }
        else if(parent.toString().contains("appColSpinner")){
            prefEditor.putInt("numAppCols",position).commit();
            Log.v("prefs","numAppCols = " + position + " now.");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
