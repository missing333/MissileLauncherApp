package com.missilelauncher.missilelauncher;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
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
import java.util.List;

import static com.missilelauncher.missilelauncher.GroupIconPicker.PREFS_NAME;
import static java.lang.Math.sqrt;

/**
 * Created by mmissildine on 9/20/2018.
 */

public class FloatingWindow extends Service{

    public FloatingWindow(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public FloatingWindow() {
    }

    private WindowManager wm;
    private SharedPreferences settingsPrefs;
    private LinearLayout rhs;
    private LinearLayout lhs;
    private RelativeLayout gl;
    private RelativeLayout tl;
    public int statusBarOffset;
    public int screenWidth;
    public int screenHeight;
    private boolean portrait = true;
    private RelativeLayout.LayoutParams relativeParams;
    private WindowManager.LayoutParams gparameters;
    private WindowManager.LayoutParams rhsparameters;
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
    public AppInfo[][] appPositions;
    private int[] coords;
    private ArrayList<AppInfo>[] groupAppList;
    int offset;
    int viewAdded;
    private boolean leftSideNavigationBar, rightSideNavigationBar;
    final int distFingerTraveledTolerance = 50;
    int disappearDelay = 2000;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        init();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settingsPrefs.edit();

        numZones = Integer.parseInt(settingsPrefs.getString("numZones","3"));
        numAppCols = Integer.parseInt(settingsPrefs.getString("numAppCols", "6"));
        numAppRows = Integer.parseInt(settingsPrefs.getString("numAppRows", "10"));
        editor.putString("landscapeNumAppRows", numAppCols+"");
        editor.putString("landscapeNumAppCols", numAppRows+"");
        Log.v("prefs","numGroups = " + numZones);
        Log.v("prefs","numAppRows = " + numAppRows);
        Log.v("prefs","numAppCols = " + numAppCols);

        leftSideNavigationBar = false;
        rightSideNavigationBar = false;


        //set up activation zone
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        rhs = new LinearLayout(this);
        lhs = new LinearLayout(this);

        int activationWidth = Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3)/100;
        int activationHeight = Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45)/100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int ypos = settingsPrefs.getInt("lhsYPos", 50)-50;
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        rhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsparameters.x = 0;
        lhsparameters.y = -screenHeight*ypos/100;
        lhsparameters.gravity = Gravity.START;
        lhs.setBackgroundColor(Color.argb(transparency,0,200,200));

        wm.addView(rhs, rhsparameters);
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

        //gl = group layout.  displays the configured Groups first, before displaying apps within that group.
        gl = new RelativeLayout(this.getApplicationContext());

        gl.setBackgroundColor(Color.argb(155,0,0,0));
        gl.setLayoutParams(relativeParams);

        tl = new RelativeLayout(this);
        tl.setBackgroundColor(Color.argb(25,0,0,0));

        coords = new int[]{-1, -1};
        viewAdded = 0;



        rhs.setOnTouchListener(new View.OnTouchListener(){

            private WindowManager.LayoutParams updatedParameters = rhsparameters;
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

                        gl.removeAllViews();
                        initAppList();
                        if (!portrait){
                            offset = zoneXSize * 2;
                        }else {
                            offset = zoneXSize;
                        }
                        setIconSizePos(screenWidth - offset);
                        for (int i = 0; i < numZones; i++) {
                            gl.addView(g[i]);
                        }

                        //
                        gl.addView(t);
                        Log.v("touch", "Touch detected.");

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));

                        int distFingerTraveled = (int) sqrt((event.getRawX() - touchedX)*(event.getRawX() - touchedX) + (event.getRawY() - touchedY)*(event.getRawY() - touchedY));
                        if ( distFingerTraveled > distFingerTraveledTolerance && viewAdded != 1) {
                            wm.addView(gl, gparameters);
                            viewAdded = 1;
                        }

                        //////user touching Groups or Apps?
                        if (event.getRawX() > gl.getWidth() - zoneXSize ) {
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

                        if (viewAdded == 0){
                            //remove activation area for n seconds
                            CountDownTimer timer = new CountDownTimer(disappearDelay, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    rhs.setVisibility(View.GONE);
                                }

                                @Override
                                public void onFinish() {
                                    rhs.setVisibility(View.VISIBLE); //(or GONE)
                                }
                            }.start();
                        }else{
                            viewAdded = 0;
                        }


                        if (coords[0] != -1 || coords[1] !=-1){
                            AppInfo a = appPositions[coords[0]][coords[1]];
                            if (a.launchIntent != null && event.getRawX() < screenWidth * .85){
                                editor.putInt(a.label.toString()+"_launchCount", settingsPrefs.getInt(a.label.toString()+"_launchCount", 0)+1);
                                editor.commit();
                                Log.v("launchCount", "LaunchCount for "+a.label+" is: " +settingsPrefs.getInt(a.label.toString()+"_launchCount", 0));
                                Intent launchApp = a.launchIntent;
                                launchApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    startActivity(launchApp);
                                } catch (Exception e) {
                                    Log.v("touch", "App failed to launch.  Removing from this Group.");
                                    Toast.makeText(FloatingWindow.this, "This app failed to launch.  Removing it from this group", Toast.LENGTH_SHORT).show();
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G1SelectedItems.G1SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G2SelectedItems.G2SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G3SelectedItems.G3SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G4SelectedItems.G4SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G5SelectedItems.G5SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G6SelectedItems.G6SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G7SelectedItems.G7SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}


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

            private WindowManager.LayoutParams updatedParameters = rhsparameters;
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
                        initAppList();
                        setIconSizePos(0);
                        gl.addView(t);
                        for (int i=0;i<numZones;i++){
                            gl.addView(g[i]);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));


                        int distFingerTraveled = (int) sqrt((event.getRawX() - touchedX)*(event.getRawX() - touchedX) + (event.getRawY() - touchedY)*(event.getRawY() - touchedY));

                        if ( distFingerTraveled > distFingerTraveledTolerance && viewAdded != 1) {
                            wm.addView(gl, gparameters);
                            viewAdded = 1;
                        }


                        //////user touching Groups or Apps?

                        int leftBarOffset;
                        if (leftSideNavigationBar){
                            leftBarOffset = zoneXSize;
                        }else{
                            leftBarOffset = 0;
                        }
                        if (event.getRawX() < (zoneXSize*.8)+leftBarOffset) {
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

                        if (viewAdded == 0){
                            //remove activation area for n seconds
                            CountDownTimer timer = new CountDownTimer(disappearDelay, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    lhs.setVisibility(View.GONE);
                                }

                                @Override
                                public void onFinish() {
                                    lhs.setVisibility(View.VISIBLE); //(or GONE)
                                }
                            }.start();
                        }else{
                            viewAdded = 0;
                        }

                        if (coords[0] != -1 || coords[1] != -1){
                            AppInfo a = appPositions[coords[0]][coords[1]];
                            if (a.launchIntent != null && event.getRawX() > screenWidth * .10){
                                editor.putInt(a.label.toString()+"_launchCount", settingsPrefs.getInt(a.label.toString()+"_launchCount", 0)+1);
                                editor.commit();
                                Log.v("launchCount", "LaunchCount for "+a.label+" is: " +settingsPrefs.getInt(a.label.toString()+"_launchCount", 0));
                                Intent launchApp = a.launchIntent;
                                launchApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    startActivity(launchApp);
                                } catch (Exception e) {
                                    Log.v("touch", "App failed to launch.  Removing from this Group.");
                                    Toast.makeText(FloatingWindow.this, "This app failed to launch.  Removing it from this group", Toast.LENGTH_SHORT).show();
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G1SelectedItems.G1SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G2SelectedItems.G2SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G3SelectedItems.G3SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G4SelectedItems.G4SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G5SelectedItems.G5SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G6SelectedItems.G6SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            G7SelectedItems.G7SelectedApps.removeIf(obj -> (obj.label.toString().equals(a.label.toString())));
                                        }
                                    }catch (Exception f){}

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



    private void init(){

        ////////set all previous app lists if available
        ArrayList<AppInfo> res = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            try{
                if(packageManager.getLaunchIntentForPackage(p.packageName) != null){

                    AppInfo newInfo = new AppInfo();
                    newInfo.setLabel(p.applicationInfo.loadLabel(getPackageManager()).toString());
                    newInfo.setPackageName(p.packageName);
                    newInfo.versionName = p.versionName;
                    newInfo.versionCode = p.versionCode;
                    newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
                    newInfo.setLaunchIntent(getPackageManager().getLaunchIntentForPackage(p.packageName));
                    res.add(newInfo);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Collections.sort(res, AppInfo.appNameComparator);
        SharedListPreferencesHelper sh = new SharedListPreferencesHelper();

        ArrayList[] groupAppList = new ArrayList[10];
        groupAppList[1] = G1SelectedItems.G1SelectedApps;
        groupAppList[2] = G2SelectedItems.G2SelectedApps;
        groupAppList[3] = G3SelectedItems.G3SelectedApps;
        groupAppList[4] = G4SelectedItems.G4SelectedApps;
        groupAppList[5] = G5SelectedItems.G5SelectedApps;
        groupAppList[6] = G6SelectedItems.G6SelectedApps;
        groupAppList[7] = G7SelectedItems.G7SelectedApps;

        for (int g=1;g<7+1;g++){
            ArrayList saveList = sh.getFavorites(getApplicationContext(),g);
            if (saveList == null){
                saveList = new ArrayList<>(0);
            }
            groupAppList[g] = new ArrayList<>(0);
            if(res.size() > 0 ){
                for (int i=0;i<res.size();i++) {
                    //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                    for (int f=0;f<saveList.size();f++) {
                        //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                        if(res.get(i).packageName.equals(saveList.get(f))){
                            //add the package name (ie: com.google.youtube) to groupAppList
                            groupAppList[g].add(res.get(i));
                        }
                    }
                }
            }
        }
        new G1SelectedItems().setG1Apps(groupAppList[1]);
        new G2SelectedItems().setG2Apps(groupAppList[2]);
        new G3SelectedItems().setG3Apps(groupAppList[3]);
        new G4SelectedItems().setG4Apps(groupAppList[4]);
        new G5SelectedItems().setG5Apps(groupAppList[5]);
        new G6SelectedItems().setG6Apps(groupAppList[6]);
        new G7SelectedItems().setG7Apps(groupAppList[7]);
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int vibTime = 100;// Vibrate for 100 milliseconds
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

    public void getDimensions(){

        statusBarOffset = getStatusBarHeight();


        setScreenSize();

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            numAppCols = Integer.parseInt(settingsPrefs.getString("landscapeNumAppCols", "10"));
            numAppRows = Integer.parseInt(settingsPrefs.getString("landscapeNumAppRows", "6"));
            portrait = false;
        } else {
            // In portrait
            numAppCols = Integer.parseInt(settingsPrefs.getString("numAppCols", "6"));
            numAppRows = Integer.parseInt(settingsPrefs.getString("numAppRows", "10"));
            portrait = true;
        }

        //size of Group Icons
        float dip = 50f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        zoneXSize = (int) px;
        zoneYSize = (int) ((int) px*1.3);  //1.3 is for a little extra margin on top & bottom of icon
        //if zoneYsize is too big to fit all icons on the screen, reduce their size.
        if (zoneYSize*numZones > screenHeight){
            zoneYSize = screenHeight/numZones;
        }

        Log.d("screen","Screen Height: "+screenHeight+", zoneYSize: "+zoneYSize);
        if (portrait){
            maxAppSize = (int) (zoneXSize * .7);     //app icon size a little bigger in Portrait mode.
        }
        else{
            maxAppSize = (int) (zoneXSize * .5);    //app icon size a little smaller in Landscape mode.
        }



        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }



        updateRHS();
        updateLHS();

        if (!portrait && hasNavBar(getResources())){
            gparameters = new WindowManager.LayoutParams(screenWidth-zoneXSize,screenHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        }else{
            gparameters = new WindowManager.LayoutParams(screenWidth,screenHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        }

        relativeParams = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        appPositions = new AppInfo[numAppCols+numAppRows+10][numAppCols+numAppRows+10];
        initAppArray();

        t.setX((float) (screenWidth * .15));
        t.setY((float) (screenHeight * .01));

        initAppList();
    }

    public void initAppList(){
        groupAppList = new ArrayList[10];
        groupAppList[1] = G1SelectedItems.G1SelectedApps;
        groupAppList[2] = G2SelectedItems.G2SelectedApps;
        groupAppList[3] = G3SelectedItems.G3SelectedApps;
        groupAppList[4] = G4SelectedItems.G4SelectedApps;
        groupAppList[5] = G5SelectedItems.G5SelectedApps;
        groupAppList[6] = G6SelectedItems.G6SelectedApps;
        groupAppList[7] = G7SelectedItems.G7SelectedApps;

    }

    public void updateRHS(){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        SharedPreferences  settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int activationWidth = Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3)/100;
        int activationHeight = Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45)/100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int ypos = settingsPrefs.getInt("lhsYPos", 50)-50;
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        boolean on = settingsPrefs.getBoolean("rhs",true );
        boolean showInLandscape = settingsPrefs.getBoolean("landscape",false);
        if (!on || (!showInLandscape && !portrait)) {
            activationWidth = 0;
            activationHeight = 0;
            transparency = 0;
        }
        rhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        rhsparameters.x = 0;
        rhsparameters.y = -screenHeight*ypos/100;
        rhsparameters.gravity = Gravity.END;
        rhs.setBackgroundColor(Color.argb(transparency,0,200,200));
        wm.updateViewLayout(rhs, rhsparameters);
    }

    public void updateLHS(){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        SharedPreferences  settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int activationWidth = Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3)/100;
        int activationHeight = Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45)/100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int ypos = settingsPrefs.getInt("lhsYPos", 50)-50;
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        boolean on = settingsPrefs.getBoolean("lhs",true );
        boolean showInLandscape = settingsPrefs.getBoolean("landscape",false);

        if (!on || (!showInLandscape && !portrait)) {
            activationWidth = 0;
            activationHeight = 0;
            transparency = 0;
        }
        lhsparameters = new WindowManager.LayoutParams(activationWidth,activationHeight,overlayType,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsparameters.x = 0;
        lhsparameters.y = -screenHeight*ypos/100;
        lhsparameters.gravity = Gravity.START;
        lhs.setBackgroundColor(Color.argb(transparency,0,200,200));
        wm.updateViewLayout(lhs,lhsparameters);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void configImageButton(ImageButton b){
        WindowManager.LayoutParams appIconParams = new WindowManager.LayoutParams();
        int appSize = 100;
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
        display.getRealSize(screenSize);
        x = screenSize.x;
        y = screenSize.y;
        screenWidth = x;
        screenHeight = y - statusBarOffset;
    }

    private void setIconSizePos(int marginLeft){
        RelativeLayout.LayoutParams groupIconParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (marginLeft > 0 ){
            groupIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }else
            groupIconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        numZones = Integer.parseInt(settingsPrefs.getString("numZones","3"));
        int yOffset = 0;
        int ySizeNeeded = numZones * zoneYSize;
        int marginTop = (screenHeight - ySizeNeeded) / 5;
        if (portrait){
            yOffset = statusBarOffset;
            marginLeft+=zoneXSize;
        }

        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        for (int i=0;i<numZones;i++){
            g[i] = new ImageView(this);
            int n = i+1;
            int id = (int) settings.getLong("iconID"+n, R.drawable.ring_50dp);
            Drawable d = ContextCompat.getDrawable(this, id);
            g[i].setImageDrawable(d);
            groupIconParams.setMargins(0,marginTop,0,0);
            g[i].setLayoutParams(groupIconParams);
            g[i].setY((float) (  zoneYSize * i )+yOffset+marginTop);
        }
    }

    private int checkGroup (int x, int y){
        int ySizeNeeded = numZones * zoneYSize;
        int marginTop = (screenHeight - ySizeNeeded) / 2;
        for (int groupNum = 1; groupNum <= numZones ; groupNum++){
            if ((y < ((zoneYSize * groupNum) +  marginTop))){
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

    private void initAppArray(){
        for (int row = 0; row < numAppCols+numAppRows+4; row ++){
            for (int col = 0; col < numAppCols+numAppRows+4; col++){
                appPositions[row][col] = new AppInfo(
                        "",
                        col * screenWidth/(numAppCols+2),
                        row * screenHeight/(numAppRows+2));
//                appPositions[row][col].label = "";
//                appPositions[row][col].setX(col * screenWidth/(numAppCols+2));
//                appPositions[row][col].setY(row * screenHeight/(numAppRows+2));
//                appPositions[row][col].setLaunchIntent(null);
            }
        }
    }

    private void setContentsPositionGroup(int group, int ltr){

        int index = 0;
        int rowOffset = 0;
        int colOffset = 0;
        if (!portrait && (leftSideNavigationBar || rightSideNavigationBar)){
            colOffset = 1;
        }
        if (!portrait && group == 1 && numZones >= 7)
        {rowOffset = 1;}
        else if (!portrait && group == 7)
        {rowOffset = -1;}
        try {
            int ySizeNeeded = numZones * zoneYSize;
            int marginTop = (screenHeight - ySizeNeeded) / 2;
            int numAppsInGroup = groupAppList[group].size();
            int rowsNeeded = (numAppsInGroup/numAppCols);
            int nearestRow = (((group * zoneYSize)+marginTop)/Math.round(screenHeight/(numAppRows+2)))-1;
            int row = nearestRow+rowOffset;

            for(AppInfo item:groupAppList[group]){
                item.setLaunchCount(settingsPrefs.getInt(item.label.toString()+"_launchCount",0 ));
            }


            String sort = settingsPrefs.getString("sortG"+group,"Most Used" );
            //Log.v("sort", "Sorting Group "+group+" SHOULD BE: " +sort);
            assert sort != null;
            if (sort.equals("Most Used")){
                //Log.v("sort", "Sorting Group "+group+" by Most Used.");
                Collections.sort(groupAppList[group], AppInfo.appLaunchCount);

            }else {
                //Log.v("sort", "Sorting Group "+group+" by Alphabetical.");
                Collections.sort(groupAppList[group], AppInfo.appNameComparator);
            }


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
                            //this section adds the Edit Group button at the very end of the list.
                            Intent intent = null;
                            switch (group) {
                                case 1:
                                    intent = new Intent(FloatingWindow.this, G1SelectedItems.class);
                                    break;
                                case 2:
                                    intent = new Intent(FloatingWindow.this, G2SelectedItems.class);
                                    break;
                                case 3:
                                    intent = new Intent(FloatingWindow.this, G3SelectedItems.class);
                                    break;
                                case 4:
                                    intent = new Intent(FloatingWindow.this, G4SelectedItems.class);
                                    break;
                                case 5:
                                    intent = new Intent(FloatingWindow.this, G5SelectedItems.class);
                                    break;
                                case 6:
                                    intent = new Intent(FloatingWindow.this, G6SelectedItems.class);
                                    break;
                                case 7:
                                    intent = new Intent(FloatingWindow.this, G7SelectedItems.class);
                                    break;
                            }
                            ImageButton appIcon = new ImageButton(this);
                            configImageButton(appIcon);
                            appPositions[col][row].setLabel("Edit this Group");
                            appPositions[col][row].setLaunchIntent(intent);
                            appIcon.setX(col * screenWidth/(numAppCols+2));
                            appIcon.setY(row * screenHeight/(numAppRows+2));
                            appIcon.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add_white_50dp));
                            tl.addView(appIcon);
                        }

                        index++;
                        if (index > numAppsInGroup) {
                            row = numAppRows + 11;
                        }
                    }

                }
                else{
                    for (int col = 1; col < numAppCols+1 ; col++){

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
                            Intent intent = null;
                            switch (group) {
                                case 1:
                                    intent = new Intent(FloatingWindow.this, G1SelectedItems.class);
                                    break;
                                case 2:
                                    intent = new Intent(FloatingWindow.this, G2SelectedItems.class);
                                    break;
                                case 3:
                                    intent = new Intent(FloatingWindow.this, G3SelectedItems.class);
                                    break;
                                case 4:
                                    intent = new Intent(FloatingWindow.this, G4SelectedItems.class);
                                    break;
                                case 5:
                                    intent = new Intent(FloatingWindow.this, G5SelectedItems.class);
                                    break;
                                case 6:
                                    intent = new Intent(FloatingWindow.this, G6SelectedItems.class);
                                    break;
                                case 7:
                                    intent = new Intent(FloatingWindow.this, G7SelectedItems.class);
                                    break;
                            }
                            ImageButton appIcon = new ImageButton(this);
                            configImageButton(appIcon);
                            appPositions[col][row].setLabel("Edit this Group");
                            appPositions[col][row].setLaunchIntent(intent);
                            appIcon.setX(col * screenWidth/(numAppCols+2));
                            appIcon.setY(row * screenHeight/(numAppRows+2));
                            appIcon.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add_white_50dp));
                            tl.addView(appIcon);
                        }

                        index++;
                        if (index > numAppsInGroup){
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
            e.printStackTrace();
        }
    }

    @NonNull
    private int[] checkWhichAppSelected(int rawX, int rawY){
        int x, y;
        int appXSize = (screenWidth/(numAppCols+2));
        int appYSize = (screenHeight/(numAppRows+2));
        x = Math.round((rawX+(appXSize/3))/appXSize);
        y = Math.round((rawY-(appYSize/3))/appYSize);

        if (!portrait && leftSideNavigationBar){
            x--;
        }

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

    public boolean hasNavBar (Resources resources)
    {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);      //this apparently does NOT work in emulators...
    }

        @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getDimensions();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        wm.removeView(rhs);
        wm.removeView(lhs);
        Intent broadcastIntent = new Intent(this, LauncherRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }


}


