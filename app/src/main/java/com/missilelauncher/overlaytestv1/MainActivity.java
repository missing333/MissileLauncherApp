package com.missilelauncher.overlaytestv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    private Button b;
    public SharedPreferences.Editor prefEditor;
    private View B1, B2, B3, B4, B5, B6, B7;

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
        Spinner numAppRowsSpinner = (Spinner) findViewById(R.id.appRowSpinner);

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this,R.layout.list_text_view, sizes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numGroupSpinner.setAdapter(aa);

        int spinnerPosition;
        numGroupSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numGroups",7)+"");
        numGroupSpinner.setSelection(spinnerPosition);
        numGroupSpinner.setOnItemSelectedListener(this);
        numAppColsSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numAppCols",9)+"");
        numAppColsSpinner.setSelection(spinnerPosition);
        numAppColsSpinner.setOnItemSelectedListener(this);
        numAppRowsSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numAppRows",11)+"");
        numAppRowsSpinner.setSelection(spinnerPosition);
        numAppRowsSpinner.setOnItemSelectedListener(this);


        ///////////////////start groups section//////////////////////////////////////


        B1 = findViewById(R.id.G1);
        //set listener for Button event
        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G1SelectedItems.class);
                startActivity(intent);
            }
        });

        B2 = findViewById(R.id.G2);
        //set listener for Button event
        B2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G2SelectedItems.class);
                startActivity(intent);
            }
        });

        B2 = findViewById(R.id.G2);
        //set listener for Button event
        B2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G2SelectedItems.class);
                startActivity(intent);
            }
        });

        B3 = findViewById(R.id.G3);
        //set listener for Button event
        B3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G3SelectedItems.class);
                startActivity(intent);
            }
        });

        B4 = findViewById(R.id.G4);
        //set listener for Button event
        B4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G4SelectedItems.class);
                startActivity(intent);
            }
        });

        B5 = findViewById(R.id.G5);
        //set listener for Button event
        B5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G5SelectedItems.class);
                startActivity(intent);
            }
        });

        B6 = findViewById(R.id.G6);
        //set listener for Button event
        B6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G6SelectedItems.class);
                startActivity(intent);
            }
        });

        B7 = findViewById(R.id.G7);
        //set listener for Button event
        B7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G7SelectedItems.class);
                startActivity(intent);
            }
        });


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
        parent.getItemAtPosition(position);
        position++;
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
