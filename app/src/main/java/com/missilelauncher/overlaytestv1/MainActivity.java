package com.missilelauncher.overlaytestv1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button b, c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = (Button) findViewById(R.id.start);

        setContentView(R.layout.activity_main);
        c = findViewById(R.id.config);

        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(myIntent);
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

        if (b.getVisibility() == View.GONE){
            c.performClick();
        }
    }
}
