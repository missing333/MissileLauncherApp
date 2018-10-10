package com.missilelauncher.overlaytestv1;

import android.annotation.TargetApi;
import android.app.Notification;
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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
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
    private SharedPreferences settingsPrefs;
    private LinearLayout ll;
    private LinearLayout lhs;
    private RelativeLayout gl;
    private RelativeLayout tl;
    public int statusBarOffset;
    public int screenWidth;
    public int screenHeight;
    private boolean portrait = true;
    private RelativeLayout.LayoutParams relativeParams;
    private WindowManager.LayoutParams gparameters;
    private WindowManager.LayoutParams parameters;
    private WindowManager.LayoutParams lhsparameters;
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
    private ArrayList<AppInfo>[] groupAppList;
    private int appSize = 100;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void updateRHS(){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        SharedPreferences  settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("rhsWidth", 3)/100;
        int activationHeight = (int) Math.round(screenHeight * settingsPrefs.getInt("rhsHeight", 45)/100);
        int transparency = settingsPrefs.getInt("rhsTransparency", 25);
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        boolean on = settingsPrefs.getBoolean("rhs",true );
        if (!on) {
            activationWidth = 0;
            activationHeight = 0;
            transparency = 0;
        }
        parameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = -screenHeight/10;
        parameters.gravity = Gravity.END;
        ll.setBackgroundColor(Color.argb(transparency,0,200,200));
        wm.updateViewLayout(ll,parameters);
    }


    public void updateLHS(){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        SharedPreferences  settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3)/100;
        int activationHeight = (int) Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45)/100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        boolean on = settingsPrefs.getBoolean("lhs",true );
        if (!on) {
            activationWidth = 0;
            activationHeight = 0;
            transparency = 0;
        }
        lhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsparameters.x = 0;
        lhsparameters.y = -screenHeight/10;
        lhsparameters.gravity = Gravity.START;
        lhs.setBackgroundColor(Color.argb(transparency,0,200,200));
        wm.updateViewLayout(lhs,lhsparameters);
    }



    public void stopMe(){
        stopForeground(true);
        stopSelf();
    }



    @Override
    public void onCreate() {
        super.onCreate();

        sharedPref = getSharedPreferences("SettingsActivity", 0);
        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPref.edit();

        numZones = settingsPrefs.getInt("numGroups",7);
        numAppRows = settingsPrefs.getInt("numAppRows", 10);
        numAppCols = settingsPrefs.getInt("numAppCols", 6);
        Log.v("prefs","numGroups = " + numZones);
        Log.v("prefs","numAppRows = " + numAppRows);
        Log.v("prefs","numAppCols = " + numAppCols);
        Configuration config = new Configuration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            portrait = true;
        }else {
            portrait = false;
        }


        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        lhs = new LinearLayout(this);

        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("rhsWidth", 3)/100;
        int activationHeight = (int) Math.round(screenHeight * settingsPrefs.getInt("rhsHeight", 45)/100);
        int transparency = settingsPrefs.getInt("rhsTransparency", 25);
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        parameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        activationWidth = (int) Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3)/100;
        activationHeight = (int) Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45)/100);
        transparency = settingsPrefs.getInt("lhsTransparency", 25);

        lhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsparameters.x = 0;
        lhsparameters.y = -screenHeight/10;
        lhsparameters.gravity = Gravity.START;
        lhs.setBackgroundColor(Color.argb(transparency,0,200,200));

        wm.addView(ll,parameters);
        wm.addView(lhs,lhsparameters);

        t = new TextView(this);
        t.setText("");
        t.setTextColor(Color.WHITE);
        t.setTextSize(30);
        t.setGravity(Gravity.CENTER);
        lastAppTouched = new int[2];

        g = new ImageView[10];
        getDimensions();



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
                        updateLHS();
                        updateRHS();
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
                        setIconSizePos((int) (screenWidth-zoneXSize));
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
                        if (event.getRawX() > screenWidth - zoneXSize) {
                            int group = checkGroup((int) event.getRawX(), (int) event.getRawY());
                            switch (group) {
                                case 1:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName1","Group 1" ));
                                    setContentsPositionGroup(1, 0);
                                    break;
                                case 2:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName2","Group 2" ));
                                    setContentsPositionGroup(2, 0);
                                    break;
                                case 3:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName3","Group 3" ));
                                    setContentsPositionGroup(3, 0);
                                    break;
                                case 4:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName4","Group 4" ));
                                    setContentsPositionGroup(4, 0);
                                    break;
                                case 5:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName5","Group 5" ));
                                    setContentsPositionGroup(5, 0);
                                    break;
                                case 6:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName6","Group 6" ));
                                    setContentsPositionGroup(6, 0);
                                    break;
                                case 7:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName7","Group 7" ));
                                    setContentsPositionGroup(7, 0);
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
                                try {
                                    startActivity(launchApp);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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

        lhs.setOnTouchListener(new View.OnTouchListener(){

            private WindowManager.LayoutParams updatedParameters = parameters;
            int x, y;
            float touchedX, touchedY;
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            public boolean onTouch(View arg0, MotionEvent event){

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        updateLHS();
                        updateRHS();
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
                        setIconSizePos(0);
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
                        if (event.getRawX() < zoneXSize*.8) {
                            int group = checkGroup((int) event.getRawX(), (int) event.getRawY());
                            switch (group) {
                                case 1:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName1","Group 1" ));
                                    setContentsPositionGroup(1, 1);
                                    break;
                                case 2:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName2","Group 2" ));
                                    setContentsPositionGroup(2, 1);
                                    break;
                                case 3:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName3","Group 3" ));
                                    setContentsPositionGroup(3, 1);
                                    break;
                                case 4:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName4","Group 4" ));
                                    setContentsPositionGroup(4, 1);
                                    break;
                                case 5:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName5","Group 5" ));
                                    setContentsPositionGroup(5, 1);
                                    break;
                                case 6:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName6","Group 6" ));
                                    setContentsPositionGroup(6, 1);
                                    break;
                                case 7:
                                    tl.removeAllViews();
                                    t.setText(settingsPrefs.getString("groupName7","Group 7" ));
                                    setContentsPositionGroup(7, 1);
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
                                Intent launchApp = null;
                                launchApp = appPositions[coords[0]][coords[1]].launchIntent;
                                try {

                                    startActivity(launchApp);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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

        statusBarOffset = getStatusBarHeight();
        setScreenSize();

        numZones = settingsPrefs.getInt("numZones", 7);
        numAppCols = settingsPrefs.getInt("numAppCols", 6);
        numAppRows = settingsPrefs.getInt("numAppRows", 10);

        zoneXSize = screenWidth / numZones;
        zoneYSize = screenHeight / numZones;
        Log.d("screen","Screen Width: "+screenWidth+", zoneXSize: "+zoneXSize);
        Log.d("screen","Screen Height: "+screenHeight+", zoneYSize: "+zoneYSize);
        if (numAppRows>numAppCols){
            maxAppSize = (int) (zoneXSize * .7);
        }
        else{
            maxAppSize = (int) (zoneXSize * .5);
        }


        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2 * .03);
        int activationHeight = (int) Math.round(screenHeight * .5);

        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        updateRHS();
        updateLHS();
//        parameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
//        lhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        gparameters = new WindowManager.LayoutParams(screenWidth,screenHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        relativeParams = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        appPositions = new AppInfo[numAppCols+numAppRows+10][numAppCols+numAppRows+10];
        initAppArray();

        t.setX((float) (screenWidth * .2));
        t.setY((float) (screenHeight * .01));

        groupAppList = new ArrayList[10];
        groupAppList[1] = G1SelectedItems.G1SelectedApps;
        groupAppList[2] = G2SelectedItems.G2SelectedApps;
        groupAppList[3] = G3SelectedItems.G3SelectedApps;
        groupAppList[4] = G4SelectedItems.G4SelectedApps;
        groupAppList[5] = G5SelectedItems.G5SelectedApps;
        groupAppList[6] = G6SelectedItems.G6SelectedApps;
        groupAppList[7] = G7SelectedItems.G7SelectedApps;
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


    private void setIconSizePos(int marginLeft){
        RelativeLayout.LayoutParams groupIconParams = new RelativeLayout.LayoutParams(maxAppSize,maxAppSize);
        int yOffset = 0;
        numZones = settingsPrefs.getInt("numGroups",7);
        if (portrait)
        {yOffset = statusBarOffset;}

        for (int i=0;i<numZones;i++){
            g[i] = new ImageView(this);
            g[i].setImageResource(R.drawable.ic_star_black_50dp);
            groupIconParams.setMarginStart(marginLeft);
            g[i].setLayoutParams(groupIconParams);
            g[i].setY((float) (  zoneYSize * i )+yOffset);
        }
    }

    private void setContentsPositionGroup(int group, int ltr){

        numAppCols = settingsPrefs.getInt("numAppCols", 6);
        numAppRows = settingsPrefs.getInt("numAppRows", 10);
        int index = 0;
        int rowOffset = 0;
        int colOffset = 0;
        if (group == 1)
            {rowOffset = 1;}
        else if (group == 7)
            {rowOffset = -1;}
        if (numAppRows<numAppCols)
            {colOffset = 1;}
        try {
            int numAppsInGroup = groupAppList[group].size();;
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (group * zoneYSize/(screenHeight/(numAppRows+2)))-1;
            int row = nearestRow+rowOffset;

            for(AppInfo item:groupAppList[group]){
                item.setLaunchCount(sharedPref.getInt(item.label.toString(),0 ));
            }

            Collections.sort(groupAppList[group], AppInfo.appLaunchCount);


            while (row < numAppRows+1){
                if (ltr == 0){
                    for (int col = numAppCols-colOffset; col > 0 ; col--){

                        //populate apps from right to left
                        if (index < numAppsInGroup){
                            AppInfo a = groupAppList[group].get(index);
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

                }
                else{
                    for (int col = 1+colOffset; col < numAppCols+1 ; col++){

                        //populate apps from left to right
                        if (index < numAppsInGroup){
                            AppInfo a = groupAppList[group].get(index);
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
                }
                if (nearestRow + rowsNeeded > numAppRows){
                    row--;
                }
                else{
                    row++;
                }
            }
        } catch (Exception e) {
            Log.e("error", "Exception thrown when trying to display apps.");
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
        for (int row = 0; row < numAppCols+numAppRows+4; row ++){
            for (int col = 0; col < numAppCols+numAppRows+4; col++){
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
            if ((y < (zoneYSize * groupNum + getStatusBarHeight()))){
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
        int appXSize = (screenWidth/(numAppCols+2));
        int appYSize = (screenHeight/(numAppRows+2));
        x = Math.round((rawX+(appXSize/3))/appXSize);
        y = Math.round((rawY-(appYSize/3))/appYSize);

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

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            portrait = false;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            portrait = true;
        }
        int temp = numAppRows;
        numAppRows = numAppCols;
        numAppCols = temp;
        settingsPrefs.edit().putInt("numAppRows",numAppRows).commit();
        settingsPrefs.edit().putInt("numAppCols",numAppCols).commit();
        Log.v("orientation","Rows: " +numAppRows + ", Cols: "+numAppCols);
        wm.removeView(ll);
        wm.removeView(lhs);
        getDimensions();
        wm.addView(ll,parameters);
        wm.addView(lhs, lhsparameters);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("Start")) {
            Log.i("start", "Received Start Foreground Intent ");
            // your start service code
        }
        else if (intent.getAction().equals( "Stop" )) {
            Log.i("stop", "Received Stop Foreground Intent");
            //your end servce code
            stopForeground(true);
            stopSelf();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(this, "333")
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("MissileLauncher")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("MissileLauncher")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        wm.removeView(ll);
        wm.removeView(lhs);
    }
}


