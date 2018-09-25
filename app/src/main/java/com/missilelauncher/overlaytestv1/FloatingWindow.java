package com.missilelauncher.overlaytestv1;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mmissildine on 9/20/2018.
 */

public class FloatingWindow extends Service {


    private WindowManager wm;
    private LinearLayout ll;
    private RelativeLayout gl;
    private TableLayout tl;
    public int statusBarOffset;
    public int screenWidth;
    public int screenHeight;
    private WindowManager.LayoutParams tparameters;
    private RelativeLayout.LayoutParams relativeParams;
    private RelativeLayout.LayoutParams tableParams;
    private WindowManager.LayoutParams gparameters;
    private WindowManager.LayoutParams parameters;
    public int numZones = 7;
    public int numAppRows = 12;
    public int numAppCols = 8;
    public int zoneXSize;
    public int zoneYSize;
    private TextView t;
    private ImageView g1;
    private ImageView g2;
    private ImageView g3;
    private ImageView g4;
    private ImageView g5;
    private ImageView g6;
    private ImageView g7;
    private ImageView g8;
    private ImageView g9;
    private ImageView g0;
    private ImageView app1, app2, app3;
    private int lastGroup;
    private int[] lastAppTouched;
    public AppInfo appArray[][];


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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



        tl = new TableLayout(this);
        tl.setBackgroundColor(Color.argb(55,0,0,0));

        ll.setOnTouchListener(new View.OnTouchListener(){

            private WindowManager.LayoutParams updatedParameters = parameters;
            int x, y;
            float touchedX, touchedY;
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            public boolean onTouch(View arg0, MotionEvent event){

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParameters.x;
                        y = updatedParameters.y;
                        touchedX = event.getRawX();
                        touchedY = event.getRawY();
                        lastGroup = -99;
                        lastAppTouched[0] = -99;
                        lastAppTouched[1] = -99;
                        Log.v("touch","Touch detected.");
                        gl.removeAllViews();
                        getDimensions();
                        wm.addView(gl,gparameters);
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
                        if ( event.getRawX() > screenWidth * .8) {
                            int group = checkGroup((int) event.getRawX(), (int) event.getRawY());
                            switch (group) {
                                case 1:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    break;
                                case 2:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    break;
                                case 3:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    break;
                                case 4:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    setContentsPositionG4();
                                    /*tl.addView(app1,tableParams);
                                    tableParams.addRule(RelativeLayout.RIGHT_OF,app1.getId());
                                    tl.setLayoutParams(tableParams);
                                    tl.addView(app2,tableParams);
                                    tableParams.addRule(RelativeLayout.RIGHT_OF,app2.getId());
                                    tl.setLayoutParams(tableParams);
                                    tl.addView(app3,tableParams);
                                    Log.v("app","app1 x: "+app1.getX()+", y: "+app1.getY());
                                    Log.v("app","app2 x: "+app2.getX()+", y: "+app2.getY());
                                    Log.v("app","app3 x: "+app3.getX()+", y: "+app3.getY());*/
                                    break;
                                case 5:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    break;
                                case 6:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    break;
                                case 7:
                                    tl.removeAllViews();
                                    t.setText("Group "+ group);
                                    break;
                            }
                        }
                        else{
                            int[] coords = checkWhichAppSelected((int) event.getRawX(), (int) event.getRawY());
                            t.setText("App " + coords[0] + ", " + coords[1]);
                        }


                        gl.setLayoutParams(relativeParams);
                        tl.setLayoutParams(relativeParams);
                        gl.removeView(tl);
                        gl.addView(tl,relativeParams);

                        //Log.v("touch","Move detected.");
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.v("touch","Touch no longer detected.");
                        try{
                            wm.removeView(gl);
                        }
                        catch (Exception e){
                            Log.v("touch",e.getMessage());
                        }
                        //wait(3000);
                        //Toast.makeText(FloatingWindow.this,"View will return shortly.",Toast.LENGTH_LONG).show();

                    default:
                        break;
                }
                return false;
            }

        });
    }

    public void getDimensions(){

        SharedPreferences sharedPref = getSharedPreferences("SettingsActivity", 0);
        //on/off toggle example Boolean notifPref = sharedPref.getBoolean("notifications_new_message", true);

        numZones = sharedPref.getInt("numGroups",7);
        numAppRows = sharedPref.getInt("numAppRows", 10);
        numAppCols = sharedPref.getInt("numAppCols", 8);
        Log.v("prefs","numGroups = " + numZones);
        Log.v("prefs","numAppRows = " + numAppRows);
        Log.v("prefs","numAppCols = " + numAppCols);

        statusBarOffset = getStatusBarHeight();
        setScreenSize(FloatingWindow.this);

        zoneXSize = (int) (screenWidth / numZones);
        zoneYSize = (int) (screenHeight / numZones);

        int activationWidth = (int) Math.round((screenWidth + screenHeight) / 2 * .04);
        int activationHeight = (int) Math.round(screenHeight * .45);
        parameters = new WindowManager.LayoutParams(activationWidth,activationHeight,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = -screenHeight/10;
        parameters.gravity = Gravity.END;
        gparameters = new WindowManager.LayoutParams(screenWidth,screenHeight,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        relativeParams = new RelativeLayout.LayoutParams((int) (screenWidth/1.5), screenHeight/2);
        relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tparameters = new WindowManager.LayoutParams(screenWidth/3, screenHeight/3,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        setIconSizePos();
        appArray = new AppInfo[numAppCols+numAppRows+1][numAppCols+numAppRows+1];
        initAppArray();

        t.setX((float) (screenWidth * .40));
        t.setY((float) (screenHeight * .15));
    }

    private void swapRowsCols(int r, int c){
            numAppRows = c;
            numAppCols = r;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int vibTime = 100;// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibTime, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(vibTime);
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
        int maxWidth = (int) (zoneXSize*.8);
        g1 = new ImageView(this);
        g1.setImageResource(R.mipmap.g1);
        g1.setX(xpos);
        g1.setY(ypos + zoneYSize * 0);
        Log.v("icons","g1 x pos: " + g1.getX());
        Log.v("icons","g1 y pos: " + g1.getY());
        g1.setMaxHeight(maxWidth);
        g1.setMaxWidth(maxWidth);
        g1.setId(1);

        g2 = new ImageView(this);
        g2.setImageResource(R.mipmap.g1);
        g2.setX(xpos);
        g2.setY(ypos + zoneYSize * 1);
        Log.v("icons","g2 x pos: " + g2.getX());
        Log.v("icons","g2 y pos: " + g2.getY());
        g2.setMaxHeight(maxWidth);
        g2.setMaxWidth(maxWidth);
        g2.setId(2);

        g3 = new ImageView(this);
        g3.setImageResource(R.mipmap.g1);
        g3.setX(xpos);
        g3.setY(ypos + zoneYSize * 2);
        Log.v("icons","g3 x pos: " + g3.getX());
        Log.v("icons","g3 y pos: " + g3.getY());
        g3.setMaxHeight(maxWidth);
        g3.setMaxWidth(maxWidth);
        g3.setId(3);

        g4 = new ImageView(this);
        g4.setImageResource(R.mipmap.g1);
        g4.setX(xpos);
        g4.setY(ypos + zoneYSize * 3);
        Log.v("icons","g4 x pos: " + g4.getX());
        Log.v("icons","g4 y pos: " + g4.getY());
        g4.setMaxHeight(maxWidth);
        g4.setMaxWidth(maxWidth);
        g4.setId(4);

        g5 = new ImageView(this);
        g5.setImageResource(R.mipmap.g1);
        g5.setX(xpos);
        g5.setY(ypos + zoneYSize * 4);
        Log.v("icons","g5 x pos: " + g5.getX());
        Log.v("icons","g5 y pos: " + g5.getY());
        g5.setMaxHeight(maxWidth);
        g5.setMaxWidth(maxWidth);
        g5.setId(5);

        g6 = new ImageView(this);
        g6.setImageResource(R.mipmap.g1);
        g6.setX(xpos);
        g6.setY(ypos + zoneYSize * 5);
        Log.v("icons","g6 x pos: " + g6.getX());
        Log.v("icons","g6 y pos: " + g6.getY());
        g6.setMaxHeight(maxWidth);
        g6.setMaxWidth(maxWidth);
        g6.setId(6);

        g7 = new ImageView(this);
        g7.setImageResource(R.mipmap.g1);
        g7.setX(xpos);
        g7.setY(ypos + zoneYSize * 6);
        Log.v("icons","g7 x pos: " + g7.getX());
        Log.v("icons","g7 y pos: " + g7.getY());
        g7.setMaxHeight(maxWidth);
        g7.setMaxWidth(maxWidth);
        g7.setId(7);
    }

    private void setContentsPositionG4(){
        tableParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tableParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        int maxWidth = (int) (zoneXSize*.4);

        /*app1 = new ImageView(this);
        app1.setImageDrawable(AppInfo.getActivityIcon(this,"com.android.chrome","com.google.android.apps.chrome.Main"));
        app1.setMaxHeight(maxWidth);
        app1.setMaxWidth(maxWidth);
        app1.setId(41);

        app2 = new ImageView(this);
        app2.setImageResource(R.mipmap.ic_launcher);
        app2.setMaxHeight(maxWidth);
        app2.setMaxWidth(maxWidth);
        app2.setId(42);

        app3 = new ImageView(this);
        app3.setImageResource(R.mipmap.ic_launcher_round);
        app3.setMaxHeight(maxWidth);
        app3.setMaxWidth(maxWidth);
        app3.setId(43);*/

        GridLayout gridContainer = new GridLayout(this);
        gridContainer.setColumnCount(numAppCols);
        gridContainer.setUseDefaultMargins(true);
        gridContainer.setPadding(0, 0, 0, 0);
        tl.addView(gridContainer);
        for(int i = 0; i < 12; i++)
        {
            ImageView img = new ImageView(this);
            img.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_notifications_black_24dp));
            gridContainer.addView(img, Math.max(0, gridContainer.getChildCount()));
        }

    }

    public void setScreenSize(Context context) {
        int x, y, orientation = context.getResources().getConfiguration().orientation;
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }

        screenWidth = x;
        screenHeight = y - statusBarOffset;
    }


    private void initAppArray(){
        for (int row = 0; row < numAppCols+numAppRows+1; row ++)
            for (int col = 0; col < numAppCols+numAppRows+1; col++)
                {
                    appArray[row][col] = new AppInfo();
                    appArray[row][col].setX(col * screenWidth/numAppCols);
                    appArray[row][col].setY(row * screenHeight/numAppRows);
                    appArray[row][col].setIcon(AppInfo.getActivityIcon(this,"com.android.chrome","com.google.android.apps.chrome.Main"));

                }

    }

    int checkGroup (int x, int y){
        for (int groupNum = 1; groupNum <= numZones ; groupNum++){
            if ((y < (zoneYSize * groupNum))){
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

    private int[] checkWhichAppSelected(int rawX, int rawY){
        int x, y;
        int touchIndexX = (rawX/(screenWidth/numAppCols));
        int touchIndexY = (rawY/(screenHeight/numAppRows));

        AppInfo prevPos = appArray[touchIndexX][touchIndexY];
        AppInfo nextPos = appArray[touchIndexX+1][touchIndexY+1];

        if(rawX - prevPos.x > nextPos.x - rawX){
            x = touchIndexX;
        }
        else{
            x = touchIndexX+1;
        }
        if(x != lastAppTouched[0]){
            lastAppTouched[0] = x;
            vibrate();
        }

        if(rawY - prevPos.y > nextPos.y - rawY){
            y = touchIndexY;
        }
        else{
            y = touchIndexY+1;
        }
        if(y != lastAppTouched[1]){
            lastAppTouched[1] = y;
            vibrate();
        }
        int[] r = {x,y};
        return r;
    }
}
