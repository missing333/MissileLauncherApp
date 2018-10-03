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

/**
 * Created by mmissildine on 9/20/2018.
 */

public class FloatingWindow extends Service{


    private WindowManager wm;
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
    private ImageView g1;
    private ImageView g2;
    private ImageView g3;
    private ImageView g4;
    private ImageView g5;
    private ImageView g6;
    private ImageView g7;
    private int lastGroup;
    private int[] lastAppTouched;
    public AppInfo appPositions[][];
    private int[] coords;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        t = new TextView(this);
        t.setText("");
        t.setTextColor(Color.WHITE);
        t.setTextSize(30);
        lastAppTouched = new int[2];

        getDimensions();


        ll = new LinearLayout(this);
        ll.setBackgroundColor(Color.argb(66,0,200,200));
        wm.addView(ll,parameters);

        /////////////////////////////done with activation area/////////////////////////

        gl = new RelativeLayout(this.getApplicationContext());

        gl.setBackgroundColor(Color.argb(155,0,0,0));
        gl.setLayoutParams(relativeParams);

        tl = new RelativeLayout(this);
        tl.setBackgroundColor(Color.argb(25,0,0,0));

        coords = new int[]{-99, -99};

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
                        gl.addView(g1);
                        gl.addView(g2);
                        gl.addView(g3);
                        gl.addView(g4);
                        gl.addView(g5);
                        gl.addView(g6);
                        gl.addView(g7);
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
                        if (appPositions[coords[0]][coords[1]].launchIntent != null && event.getRawX() < screenWidth * .85){
                            Intent launchApp = appPositions[coords[0]][coords[1]].launchIntent;
                            startActivity(launchApp);
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

        SharedPreferences sharedPref = getSharedPreferences("SettingsActivity", 0);

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

        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2 * .04);
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

        t.setX((float) (screenWidth * .30));
        t.setY((float) (screenHeight * .05));
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
        int xpos = (int) (screenWidth - zoneXSize*1.3);
        int ypos = -statusBarOffset;
        int maxSize = (int) (zoneXSize*.8);

        g1 = new ImageView(this);
        g1.setImageResource(R.mipmap.g1);
        g1.setX(xpos);
        g1.setY(ypos + zoneYSize * 0);
        Log.v("icons","g1 x pos: " + g1.getX());
        Log.v("icons","g1 y pos: " + g1.getY());
        g1.setMaxHeight(maxSize);
        g1.setMaxWidth(maxSize);
        //g1.setId(1);

        g2 = new ImageView(this);
        g2.setImageResource(R.mipmap.g1);
        g2.setX(xpos);
        g2.setY(ypos + zoneYSize * 1);
        Log.v("icons","g2 x pos: " + g2.getX());
        Log.v("icons","g2 y pos: " + g2.getY());
        g2.setMaxHeight(maxSize);
        g2.setMaxWidth(maxSize);
        //g2.setId(2);

        g3 = new ImageView(this);
        g3.setImageResource(R.mipmap.g1);
        g3.setX(xpos);
        g3.setY(ypos + zoneYSize * 2);
        Log.v("icons","g3 x pos: " + g3.getX());
        Log.v("icons","g3 y pos: " + g3.getY());
        g3.setMaxHeight(maxSize);
        g3.setMaxWidth(maxSize);
        //g3.setId(3);

        g4 = new ImageView(this);
        g4.setImageResource(R.mipmap.g1);
        g4.setX(xpos);
        g4.setY(ypos + zoneYSize * 3);
        Log.v("icons","g4 x pos: " + g4.getX());
        Log.v("icons","g4 y pos: " + g4.getY());
        g4.setMaxHeight(maxSize);
        g4.setMaxWidth(maxSize);
        //g4.setId(4);

        g5 = new ImageView(this);
        g5.setImageResource(R.mipmap.g1);
        g5.setX(xpos);
        g5.setY(ypos + zoneYSize * 4);
        Log.v("icons","g5 x pos: " + g5.getX());
        Log.v("icons","g5 y pos: " + g5.getY());
        g5.setMaxHeight(maxSize);
        g5.setMaxWidth(maxSize);
        //g5.setId(5);

        g6 = new ImageView(this);
        g6.setImageResource(R.mipmap.g1);
        g6.setX(xpos);
        g6.setY(ypos + zoneYSize * 5);
        Log.v("icons","g6 x pos: " + g6.getX());
        Log.v("icons","g6 y pos: " + g6.getY());
        g6.setMaxHeight(maxSize);
        g6.setMaxWidth(maxSize);
        //g6.setId(6);

        g7 = new ImageView(this);
        g7.setImageResource(R.mipmap.g1);
        g7.setX(xpos);
        g7.setY(ypos + zoneYSize * 6);
        Log.v("icons","g7 x pos: " + g7.getX());
        Log.v("icons","g7 y pos: " + g7.getY());
        g7.setMaxHeight(maxSize);
        g7.setMaxWidth(maxSize);
        //g7.setId(7);
    }

    private void setContentsPositionG1(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G1SelectedItems.G1SelectedApps.size()){
                        AppInfo a = G1SelectedItems.G1SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setContentsPositionG2(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G2SelectedItems.G2SelectedApps.size()){
                        AppInfo a = G2SelectedItems.G2SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setContentsPositionG3(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G3SelectedItems.G3SelectedApps.size()){
                        AppInfo a = G3SelectedItems.G3SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void setContentsPositionG4(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G4SelectedItems.G4SelectedApps.size()){
                        AppInfo a = G4SelectedItems.G4SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setContentsPositionG5(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G5SelectedItems.G5SelectedApps.size()){
                        AppInfo a = G5SelectedItems.G5SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setContentsPositionG6(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G6SelectedItems.G6SelectedApps.size()){
                        AppInfo a = G6SelectedItems.G6SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setContentsPositionG7(){

        int index = 0;

        for (int row = 1; row < numAppRows+1; row ++){
            for (int col = numAppCols; col > 0 ; col--)
            {

                try {
                    if (index < G7SelectedItems.G7SelectedApps.size()){
                        AppInfo a = G7SelectedItems.G7SelectedApps.get(index);
                        ImageButton appIcon = new ImageButton(this);
                        appIcon.setBackground(a.icon);
                        appIcon.setMaxHeight(40);
                        appIcon.setMaxWidth(40);
                        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        appIcon.setX(col * screenWidth/(numAppCols+2));
                        appIcon.setY(row * screenHeight/(numAppRows+2));
                        appPositions[col][row].setLabel(a.label);
                        appPositions[col][row].setLaunchIntent(a.launchIntent);
                        tl.addView(appIcon);
                    }
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

            for (int col = 0; col < numAppCols+numAppRows+1; col++)
                {
                    appPositions[row][col] = new AppInfo();
                    appPositions[row][col].label = "";
                    appPositions[row][col].setX(col * screenWidth/(numAppCols+2));
                    appPositions[row][col].setY(row * screenHeight/(numAppRows+2));
                    appPositions[row][col].setLaunchIntent(null);
                    //appPositions[row][col].setIcon(AppInfo.getActivityIcon(this,"com.android.chrome","com.google.android.apps.chrome.Main"));

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
        wm.updateViewLayout(ll,parameters);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
}


