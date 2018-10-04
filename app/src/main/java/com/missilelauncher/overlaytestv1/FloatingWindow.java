package com.missilelauncher.overlaytestv1;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Created by mmissildine on 9/20/2018.
 */

public class FloatingWindow extends Service{


    private WindowManager wm;
    private SharedPreferences sharedPref;
    private LinearLayout ll;
    private RelativeLayout gl;
    private RelativeLayout tl;
    public int statusBarOffset;
    public int screenWidth;
    public int screenHeight;
    private RelativeLayout.LayoutParams relativeParams;
    private WindowManager.LayoutParams gparameters;
    private WindowManager.LayoutParams parameters;
    public int numZones;
    public int numAppRows;
    public int numAppCols;
    public int zoneXSize;
    public int zoneYSize;
    public int maxAppSize;
    private TextView t;
    private ImageView[] g;
    private int lastGroup;
    private int[] lastAppTouched;
    public AppInfo appPositions[][];
    private int[] coords;
    private int appSize = 100;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setTransparency(int t){
        ll.setBackgroundColor(Color.argb(getSharedPreferences("SettingsActivity", 0).getInt("transparency",55 ),0,200,200));
        wm.updateViewLayout(ll,parameters);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        sharedPref = getSharedPreferences("SettingsActivity", 0);
        final SharedPreferences.Editor editor = sharedPref.edit();

        t = new TextView(this);
        t.setText("");
        t.setTextColor(Color.WHITE);
        t.setTextSize(30);
        t.setGravity(Gravity.CENTER);
        lastAppTouched = new int[2];

        g = new ImageView[10];
        getDimensions();


        ll = new LinearLayout(this);
        ll.setBackgroundColor(Color.argb(getSharedPreferences("SettingsActivity", 0).getInt("transparency",55 ),0,200,200));
        wm.addView(ll,parameters);

        /////////////////////////////done with activation area/////////////////////////

        gl = new RelativeLayout(this.getApplicationContext());

        gl.setBackgroundColor(Color.argb(155,0,0,0));
        gl.setLayoutParams(relativeParams);

        tl = new RelativeLayout(this);
        tl.setBackgroundColor(Color.argb(25,0,0,0));

        coords = new int[]{-1, -1};

        ll.setOnTouchListener(new View.OnTouchListener(){

            private WindowManager.LayoutParams updatedParameters = parameters;
            int x, y;
            float touchedX, touchedY;
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            public boolean onTouch(View arg0, MotionEvent event){

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParameters.x;
                        y = updatedParameters.y;
                        touchedX = event.getRawX();
                        touchedY = event.getRawY();
                        lastGroup = -99;
                        lastAppTouched[0] = -99;
                        lastAppTouched[1] = -99;
                        Log.v("touch", "Touch detected.");
                        gl.removeAllViews();
                        getDimensions();
                        wm.addView(gl, gparameters);
                        gl.addView(t);
                        for (int i=0;i<numZones;i++){
                            gl.addView(g[i]);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));

                        //////user touching Groups or Apps?
                        if (event.getRawX() > screenWidth * .85) {
                            int group = checkGroup((int) event.getRawX(), (int) event.getRawY());
                            switch (group) {
                                case 1:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG1();
                                    break;
                                case 2:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG2();
                                    break;
                                case 3:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG3();
                                    break;
                                case 4:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG4();
                                    break;
                                case 5:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG5();
                                    break;
                                case 6:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG6();
                                    break;
                                case 7:
                                    tl.removeAllViews();
                                    t.setText("Group " + group);
                                    setContentsPositionG7();
                                    break;
                            }
                        } else {
                            //This is where I choose the app by position.
                            coords = checkWhichAppSelected((int) event.getRawX(), (int) event.getRawY());
                            t.setText(appPositions[coords[0]][coords[1]].label);

                        }

                        tl.setLayoutParams(relativeParams);
                        gl.removeView(tl);
                        gl.addView(tl, relativeParams);
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.v("touch", "Touch no longer detected.");
                        //Toast.makeText(FloatingWindow.this, "App " + coords[0] + ", " + coords[1] + " selected", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(FloatingWindow.this, "" + appPositions[coords[0]][coords[1]].label, Toast.LENGTH_SHORT).show();
                        if (coords[0] != -1 || coords[1] !=-1){
                            AppInfo a = appPositions[coords[0]][coords[1]];
                            if (a.launchIntent != null && event.getRawX() < screenWidth * .85){
                                editor.putInt(a.label.toString(), sharedPref.getInt(a.label.toString(), 0)+1);
                                editor.commit();
                                Log.v("launchCount", "LaunchCount for "+a.label+" is: " +sharedPref.getInt(a.label.toString(), 0));
                                Intent launchApp = appPositions[coords[0]][coords[1]].launchIntent;
                                startActivity(launchApp);
                            }
                        }


                        try {
                            wm.removeView(gl);
                        } catch (Exception e) {
                            Log.v("touch", e.getMessage());
                        }

                    default:
                        break;
                }
                return false;
            }

        });
    }

    public void getDimensions(){

        numZones = sharedPref.getInt("numGroups",7);
        numAppRows = sharedPref.getInt("numAppRows", 10);
        numAppCols = sharedPref.getInt("numAppCols", 8);
        Log.v("prefs","numGroups = " + numZones);
        Log.v("prefs","numAppRows = " + numAppRows);
        Log.v("prefs","numAppCols = " + numAppCols);

        statusBarOffset = getStatusBarHeight();
        setScreenSize();

        zoneXSize = screenWidth / numZones;
        zoneYSize = screenHeight / numZones;
        maxAppSize = (int) (zoneXSize * .5);

        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2 * .03);
        int activationHeight = (int) Math.round(screenHeight * .45);

        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        parameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = -screenHeight/10;
        parameters.gravity = Gravity.END;
        gparameters = new WindowManager.LayoutParams(screenWidth,screenHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        relativeParams = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        setIconSizePos();
        appPositions = new AppInfo[numAppCols+numAppRows+1][numAppCols+numAppRows+1];
        initAppArray();

        t.setX((float) (screenWidth * .2));
        t.setY((float) (screenHeight * .01));
    }

    private void swapRowsCols(int r, int c){
            numAppRows = c;
            numAppCols = r;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int vibTime = 100;// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (v != null) {
                v.vibrate(VibrationEffect.createOneShot(vibTime, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }else{
            //deprecated in API 26
            if (v != null) {
                v.vibrate(vibTime);
            }
        }
    }



    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    private void setIconSizePos(){
        int ypos = -statusBarOffset;
        RelativeLayout.LayoutParams groupIconParams = new RelativeLayout.LayoutParams(400,400 );

        for (int i=0;i<numZones;i++){
            g[i] = new ImageView(this);
            g[i].setImageResource(R.drawable.ic_star_black_50dp);
            groupIconParams.setMarginStart(screenWidth-zoneXSize);
            g[i].setLayoutParams(groupIconParams);
            g[i].setY((float) (ypos + zoneYSize * i ));
        }
    }

    private void setContentsPositionG1(){

        int index = 0;
        int thisGroup = 1;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G1SelectedItems.G1SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {

                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContentsPositionG2(){

        int index = 0;
        int thisGroup = 2;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G2SelectedItems.G2SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow-1;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {
                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContentsPositionG3(){

        int index = 0;
        int thisGroup = 3;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G3SelectedItems.G3SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow-1;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {
                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setContentsPositionG4(){

        int index = 0;
        int thisGroup = 4;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G4SelectedItems.G4SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow-1;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {
                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContentsPositionG5(){

        int index = 0;
        int thisGroup = 5;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G5SelectedItems.G5SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow-1;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {
                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContentsPositionG6(){

        int index = 0;
        int thisGroup = 6;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G6SelectedItems.G6SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow-1;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {
                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContentsPositionG7(){

        int index = 0;
        int thisGroup = 7;                                                      //change this
        try {
            ArrayList<AppInfo> groupAppList = G7SelectedItems.G7SelectedApps;   //change this
            int numAppsInGroup = groupAppList.size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (thisGroup * zoneYSize/(screenHeight/(numAppRows+2)));
            int row = nearestRow-2;

            for(AppInfo item:groupAppList){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList, AppInfo.appLaunchCount);

            while (row < numAppRows+1){
                for (int col = numAppCols; col > 0 ; col--)
                {
                    if (index < numAppsInGroup){
                        AppInfo a = groupAppList.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        configImageButton(appIcon);
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appIcon.setBackground(a.icon);
                        tl.addView(appIcon);
                    }
                    else{
                        appPositions[col][row].setLabel("");
                        appPositions[col][row].setLaunchIntent(null);
                    }

                    index++;
                    if (index >= numAppsInGroup){
                        row = numAppRows+11;
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configImageButton(ImageButton b){
        WindowManager.LayoutParams appIconParams = new WindowManager.LayoutParams();
        appIconParams.width = appSize;
        appIconParams.height = appSize;
        b.setLayoutParams(appIconParams);
        b.setMaxHeight(appSize);
        b.setMaxWidth(appSize);
        b.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }


    public void setScreenSize() {
        int x, y;
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenSize);
            x = screenSize.x;
            y = screenSize.y;
        } else {
            display.getSize(screenSize);
            x = screenSize.x;
            y = screenSize.y;
        }

        screenWidth = x;
        screenHeight = y - statusBarOffset;
    }


    private void initAppArray(){
        for (int row = 0; row < numAppCols+numAppRows+1; row ++){
            for (int col = 0; col < numAppCols+numAppRows+1; col++){
                appPositions[row][col] = new AppInfo();
                appPositions[row][col].label = "";
                appPositions[row][col].setX(col * screenWidth/(numAppCols+2));
                appPositions[row][col].setY(row * screenHeight/(numAppRows+2));
                appPositions[row][col].setLaunchIntent(null);
            }
        }
    }

    int checkGroup (int x, int y){
        for (int groupNum = 1; groupNum <= numZones ; groupNum++){
            if ((y < (zoneYSize * groupNum))){
                Log.v("group","pointer coords are : " + x +", " +y);
                Log.v("group","lastGroup is: " + lastGroup);
                Log.v("group","Group found is: " + groupNum);
                if(groupNum != lastGroup){
                    lastGroup = groupNum;
                    vibrate();
                    initAppArray();
                }
                return groupNum;
            }
        }
        return -99; //cant find any group
    }

    @NonNull
    private int[] checkWhichAppSelected(int rawX, int rawY){
        int x, y;
        int touchIndexX = (rawX/(screenWidth/(numAppCols+2)));
        int touchIndexY = (rawY/(screenHeight/(numAppRows+2)));

        x = Math.round(touchIndexX);
        y = Math.round(touchIndexY);

        if(x != lastAppTouched[0]){
            lastAppTouched[0] = x;
            if (appPositions[x][y].launchIntent != null)
                vibrate();
        }
        if(y != lastAppTouched[1]){
            lastAppTouched[1] = y;
            if (appPositions[x][y].launchIntent != null)
                vibrate();
        }
        return new int[]{x,y};
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        swapRowsCols(numAppRows,numAppCols);
        getDimensions();
        setIconSizePos();
        wm.updateViewLayout(ll,parameters);
/*
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }*/
    }
}


