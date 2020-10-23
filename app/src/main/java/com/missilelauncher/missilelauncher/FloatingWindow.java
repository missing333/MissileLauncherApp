package com.missilelauncher.missilelauncher;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.util.TypedValue;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.missilelauncher.missilelauncher.GroupIconPicker.PREFS_NAME;
import static java.lang.Math.sqrt;


@SuppressWarnings({"unchecked", "ConstantConditions", "IntegerDivisionInFloatingPointContext", "deprecation"})
public class FloatingWindow extends JobIntentService {

    private static final int JOB_ID = 0x01;
    private SharedPreferences settingsPrefs;
    private SharedPreferences.Editor editor;
    private boolean portrait = true;
    private int numGroups;
    private int numAppRows;
    private int numAppCols;
    private int zoneXSize;
    private int zoneYSize;

    //coordinate variables
    private int screenWidth;
    private int screenHeight;
    int x, y;
    float touchedX, touchedY;
    private AppInfo[][] appPositions;
    private int[] appRowAndCol;
    private ArrayList<AppInfo>[] groupAppList;

    //views
    private WindowManager windowManager;
    private RelativeLayout.LayoutParams relativeParams;
    private WindowManager.LayoutParams gParameters;
    private WindowManager.LayoutParams rhsParameters;
    private WindowManager.LayoutParams lhsParameters;
    private LinearLayout rhs;
    private LinearLayout lhs;
    private RelativeLayout groupLayout;
    private RelativeLayout appLayout;
    private TextView textView;
    private ImageView[] imageView;
    private int viewAdded;

    //misc
    private int lastGroup;
    private int[] lastAppTouched;
    private static final int disappearDelay = 2000;
    private static final boolean sideRHS = true;
    private static final boolean sideLHS = false;
    private boolean leftSideNavigationBar, rightSideNavigationBar;



    public FloatingWindow() {
        Log.i("HERE", "here I am!");
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FloatingWindow.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        init();

        return START_STICKY;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();


        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settingsPrefs.edit();

        numGroups = Integer.parseInt(settingsPrefs.getString("numZones", "3"));
        numAppCols = Integer.parseInt(settingsPrefs.getString("numAppCols", "6"));
        numAppRows = Integer.parseInt(settingsPrefs.getString("numAppRows", "10"));
        editor.putString("landscapeNumAppRows", numAppCols + "");
        editor.putString("landscapeNumAppCols", numAppRows + "");
        editor.apply();
        Log.v("prefs", "numGroups = " + numGroups);
        Log.v("prefs", "numAppRows = " + numAppRows);
        Log.v("prefs", "numAppCols = " + numAppCols);

        leftSideNavigationBar = false;
        rightSideNavigationBar = false;


        //set up activation zone
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        rhs = new LinearLayout(this);
        lhs = new LinearLayout(this);

        int activationWidth = (Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3)) / 100;
        int activationHeight = Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45) / 100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int yPos = settingsPrefs.getInt("lhsYPos", 50) - 50;
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        rhsParameters = new WindowManager.LayoutParams(activationWidth, activationHeight, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsParameters = new WindowManager.LayoutParams(activationWidth, activationHeight, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsParameters.x = 0;
        lhsParameters.y = -screenHeight * yPos / 100;
        lhsParameters.gravity = Gravity.START;
        lhs.setBackgroundColor(Color.argb(transparency, 0, 200, 200));

        windowManager.addView(rhs, rhsParameters);
        windowManager.addView(lhs, lhsParameters);

        textView = new TextView(this);
        textView.setText("");
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER);
        lastAppTouched = new int[2];

        imageView = new ImageView[10];
        getDimensions();
        /////////////////////////////done with activation area/////////////////////////

        //gl = group layout.  displays the configured Groups first, before displaying apps within that group.
        groupLayout = new RelativeLayout(this.getApplicationContext());

        groupLayout.setBackgroundColor(Color.argb(155, 0, 0, 0));
        groupLayout.setLayoutParams(relativeParams);

        appLayout = new RelativeLayout(this);
        appLayout.setBackgroundColor(Color.argb(25, 0, 0, 0));

        appRowAndCol = new int[]{-1, -1};
        viewAdded = 0;


        rhs.setOnTouchListener((arg0, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MotionDownMethod(event,sideRHS);
                    break;

                case MotionEvent.ACTION_MOVE:
                    MotionMoveMethod(event, sideRHS);
                    break;

                case MotionEvent.ACTION_UP:
                    MotionUpMethod(event,sideRHS);
                    break;

                default:
                    break;
            }
            return false;
        });

        lhs.setOnTouchListener((arg0, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MotionDownMethod(event,sideLHS);
                    break;

                case MotionEvent.ACTION_MOVE:
                    MotionMoveMethod(event, sideLHS);
                    break;

                case MotionEvent.ACTION_UP:
                    MotionUpMethod(event,sideLHS);
                    break;

                default:
                    break;
            }
            return false;
        });

    }

    private void MotionUpMethod(MotionEvent event, boolean isRHS) {

        Log.v("touch", "Touch no longer detected.");

        if (viewAdded == 0) {
            //remove activation area for n seconds
            new CountDownTimer(disappearDelay, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isRHS)
                        rhs.setVisibility(View.GONE);
                    else
                        lhs.setVisibility(View.GONE);
                }

                @Override
                public void onFinish() {
                    if (isRHS)
                        rhs.setVisibility(View.VISIBLE);
                    else
                        lhs.setVisibility(View.VISIBLE);
                }
            }.start();
        } else {
            viewAdded = 0;
        }

        if (appRowAndCol[0] != -1 || appRowAndCol[1] != -1) {
            AppInfo a = appPositions[appRowAndCol[0]][appRowAndCol[1]];

            boolean isSafeToLaunch = false;
            if(isRHS){
                if (a.launchIntent != null && event.getRawX() < screenWidth * .85)
                    isSafeToLaunch = true;
            }else{
                if (a.launchIntent != null && event.getRawX() > screenWidth * .10) {
                    isSafeToLaunch = true;
                }
            }

            if (isSafeToLaunch) {
                editor.putInt(a.label + "_launchCount", settingsPrefs.getInt(a.label + "_launchCount", 0) + 1);
                editor.commit();
                Log.v("launchCount", "LaunchCount for " + a.label + " is: " + settingsPrefs.getInt(a.label + "_launchCount", 0));
                Intent launchApp = a.launchIntent;
                launchApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(launchApp);
                } catch (Exception e) {
                    Log.v("touch", "App failed to launch.  Removing from this Group.");
                    Toast.makeText(FloatingWindow.this, "This app failed to launch.  Removing it from this group", Toast.LENGTH_SHORT).show();
                    try {
                        G1SelectedItems.G1SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                        G2SelectedItems.G2SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                        G3SelectedItems.G3SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                        G4SelectedItems.G4SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                        G5SelectedItems.G5SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                        G6SelectedItems.G6SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                        G7SelectedItems.G7SelectedApps.removeIf(obj -> (obj.label.equals(a.label)));
                    } catch (Exception ignored) {
                    }

                    e.printStackTrace();
                }
            }
        }


        try {
            windowManager.removeView(groupLayout);
        } catch (Exception e) {
            Log.v("touch", e.getMessage());
        }
    }

    private void MotionMoveMethod(MotionEvent event, boolean isRHS) {
        rhsParameters.x = (int) (x + (event.getRawX() - touchedX));
        rhsParameters.y = (int) (y + (event.getRawY() - touchedY));

        int distFingerTraveled = (int) sqrt((event.getRawX() - touchedX) * (event.getRawX() - touchedX) + (event.getRawY() - touchedY) * (event.getRawY() - touchedY));

        //misc
        int distFingerTraveledTolerance = 50;
        if (distFingerTraveled > distFingerTraveledTolerance && viewAdded != 1) {
            windowManager.addView(groupLayout, gParameters);
            viewAdded = 1;
        }


        //////user touching Groups or Apps?

        int leftBarOffset;
        if (leftSideNavigationBar) {
            leftBarOffset = zoneXSize;
        } else {
            leftBarOffset = 0;
        }

        //find out if i'm touching a group depending on which side i'm touching.
        boolean touchingGroup = false;
        if(isRHS){
            if(event.getRawX() > groupLayout.getWidth() - zoneXSize){
                touchingGroup = true;
            }
        }else{
            if(event.getRawX() < (zoneXSize * .8) + leftBarOffset){
                touchingGroup = true;
            }
        }

        if (touchingGroup) {
            int group = checkGroup((int) event.getRawX(), (int) event.getRawY());
            switch (group) {
                case 1:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName1", "Group 1"));
                    setContentsForSelectedGroup(1, isRHS);
                    break;
                case 2:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName2", "Group 2"));
                    setContentsForSelectedGroup(2, isRHS);
                    break;
                case 3:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName3", "Group 3"));
                    setContentsForSelectedGroup(3, isRHS);
                    break;
                case 4:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName4", "Group 4"));
                    setContentsForSelectedGroup(4, isRHS);
                    break;
                case 5:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName5", "Group 5"));
                    setContentsForSelectedGroup(5, isRHS);
                    break;
                case 6:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName6", "Group 6"));
                    setContentsForSelectedGroup(6, isRHS);
                    break;
                case 7:
                    appLayout.removeAllViews();
                    textView.setText(settingsPrefs.getString("groupName7", "Group 7"));
                    setContentsForSelectedGroup(7, isRHS);
                    break;
            }
        } else {
            //This is where I choose the app by position.
            appRowAndCol = checkWhichAppSelected((int) event.getRawX(), (int) event.getRawY());
            textView.setText(appPositions[appRowAndCol[0]][appRowAndCol[1]].label);

        }

        appLayout.setLayoutParams(relativeParams);
        groupLayout.removeView(appLayout);
        groupLayout.addView(appLayout, relativeParams);
    }

    private void MotionDownMethod(MotionEvent event, boolean isRHS){
        x = rhsParameters.x;
        y = rhsParameters.y;
        touchedX = event.getRawX();
        touchedY = event.getRawY();
        lastGroup = -99;
        lastAppTouched[0] = -99;
        lastAppTouched[1] = -99;

        groupLayout.removeAllViews();
        initAppList();
        int offset;
        if (!portrait) {
            offset = zoneXSize * 2;
        } else {
            offset = zoneXSize;
        }
        if(isRHS){
            setGroupIconPositions(screenWidth - offset);
        }else{
            setGroupIconPositions(0);
        }

        //add all main group icons
        for (int i = 0; i < numGroups; i++) {
            groupLayout.addView(imageView[i]);
        }

        //add Label to screen
        groupLayout.addView(textView);
        Log.v("touch", "Touch detected.");

    }

    @SuppressWarnings("rawtypes")
    private void init() {

        ////////set all previous app lists if available
        ArrayList<AppInfo> res = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            try {
                if (packageManager.getLaunchIntentForPackage(p.packageName) != null) {

                    AppInfo newInfo = new AppInfo();
                    newInfo.setLabel(p.applicationInfo.loadLabel(getPackageManager()).toString());
                    newInfo.setPackageName(p.packageName);
                    newInfo.versionName = p.versionName;
                    newInfo.versionCode = p.versionCode;
                    newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
                    newInfo.setLaunchIntent(getPackageManager().getLaunchIntentForPackage(p.packageName));
                    res.add(newInfo);
                }
            } catch (Exception e) {
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

        for (int g = 1; g < 7 + 1; g++) {
            ArrayList saveList = sh.getFavorites(getApplicationContext(), g);
            if (saveList == null) {
                saveList = new ArrayList<>(0);
            }
            groupAppList[g] = new ArrayList<>(0);
            if (res.size() > 0) {
                for (int i = 0; i < res.size(); i++) {
                    //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                    for (int f = 0; f < saveList.size(); f++) {
                        //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                        if (res.get(i).packageName.equals(saveList.get(f))) {
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
        } else {
            //deprecated in API 26
            if (v != null) {
                v.vibrate(vibTime);
            }
        }
    }

    private void getDimensions() {

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        //coordination
        int statusBarOffset = result;

        //set screen size
        Display display = windowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getRealSize(screenSize);
        screenWidth = screenSize.x;
        screenHeight = screenSize.y - statusBarOffset;

        //get orientation
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
        zoneYSize = (int) ((int) px * 1.3);  //1.3 is for a little extra margin on top & bottom of icon
        //if zoneYSize is too big to fit all icons on the screen, reduce their size.
        if (zoneYSize * numGroups > screenHeight) {
            zoneYSize = screenHeight / numGroups;
        }

        Log.d("dimensions", "Screen Height: " + screenHeight + ", zoneYSize: " + zoneYSize);


        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }


        updateRHS();
        updateLHS();

        if (!portrait && hasNavBar(getResources())) {
            gParameters = new WindowManager.LayoutParams(screenWidth - zoneXSize, screenHeight, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        } else {
            gParameters = new WindowManager.LayoutParams(screenWidth, screenHeight, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        }

        relativeParams = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        //initAppList();
        appPositions = new AppInfo[numAppCols + numAppRows + 10][numAppCols + numAppRows + 10];
        clearAppArray();

        textView.setX((float) (screenWidth * .15));
        textView.setY((float) (screenHeight * .01));


    }

    private void clearAppArray() {
        for (int row = 0; row < numAppCols + numAppRows + 4; row++) {
            for (int col = 0; col < numAppCols + numAppRows + 4; col++) {
                appPositions[row][col] = new AppInfo(
                        "",
                        col * screenWidth / (numAppCols + 2),
                        row * screenHeight / (numAppRows + 2));
            }
        }
    }

    private void initAppList() {
        groupAppList = new ArrayList[10];
        groupAppList[1] = G1SelectedItems.G1SelectedApps;
        groupAppList[2] = G2SelectedItems.G2SelectedApps;
        groupAppList[3] = G3SelectedItems.G3SelectedApps;
        groupAppList[4] = G4SelectedItems.G4SelectedApps;
        groupAppList[5] = G5SelectedItems.G5SelectedApps;
        groupAppList[6] = G6SelectedItems.G6SelectedApps;
        groupAppList[7] = G7SelectedItems.G7SelectedApps;

    }

    private void updateRHS() {
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int activationWidth = Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3) / 100;
        int activationHeight = Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45) / 100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int yPos = settingsPrefs.getInt("lhsYPos", 50) - 50;
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        boolean on = settingsPrefs.getBoolean("rhs", true);
        boolean showInLandscape = settingsPrefs.getBoolean("landscape", false);
        if (!on || (!showInLandscape && !portrait)) {
            activationWidth = 0;
            activationHeight = 0;
            transparency = 0;
        }
        rhsParameters = new WindowManager.LayoutParams(activationWidth, activationHeight, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        rhsParameters.x = 0;
        rhsParameters.y = -screenHeight * yPos / 100;
        rhsParameters.gravity = Gravity.END;
        rhs.setBackgroundColor(Color.argb(transparency, 0, 200, 200));
        windowManager.updateViewLayout(rhs, rhsParameters);
    }

    private void updateLHS() {
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int activationWidth = Math.round((screenWidth + screenHeight) / 2) * settingsPrefs.getInt("lhsWidth", 3) / 100;
        int activationHeight = Math.round(screenHeight * settingsPrefs.getInt("lhsHeight", 45) / 100);
        int transparency = settingsPrefs.getInt("lhsTransparency", 25);
        int yPos = settingsPrefs.getInt("lhsYPos", 50) - 50;
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        boolean on = settingsPrefs.getBoolean("lhs", true);
        boolean showInLandscape = settingsPrefs.getBoolean("landscape", false);

        if (!on || (!showInLandscape && !portrait)) {
            activationWidth = 0;
            activationHeight = 0;
            transparency = 0;
        }
        lhsParameters = new WindowManager.LayoutParams(activationWidth, activationHeight, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        lhsParameters.x = 0;
        lhsParameters.y = -screenHeight * yPos / 100;
        lhsParameters.gravity = Gravity.START;
        lhs.setBackgroundColor(Color.argb(transparency, 0, 200, 200));
        windowManager.updateViewLayout(lhs, lhsParameters);
    }

    private void configImageButton(ImageButton b) {
        WindowManager.LayoutParams appIconParams = new WindowManager.LayoutParams();
        int appSize = 100;
        appIconParams.width = appSize;
        appIconParams.height = appSize;
        b.setLayoutParams(appIconParams);
        b.setMaxHeight(appSize);
        b.setMaxWidth(appSize);
        b.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void setGroupIconPositions(int marginLeft) {
        RelativeLayout.LayoutParams groupIconParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (marginLeft > 0) {
            groupIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else
            groupIconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        numGroups = Integer.parseInt(settingsPrefs.getString("numZones", "3"));
        int ySizeNeeded = numGroups * zoneYSize;
        int marginTop = (screenHeight - ySizeNeeded) / 2;

        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        for (int i = 0; i < numGroups; i++) {
            imageView[i] = new ImageView(this);
            int n = i + 1;
            int id = (int) settings.getLong("iconID" + n, R.drawable.ring_50dp);
            Drawable d = ContextCompat.getDrawable(this, id);
            imageView[i].setImageDrawable(d);
            imageView[i].setLayoutParams(groupIconParams);
            imageView[i].setY((float) (zoneYSize * i) + marginTop - zoneYSize/3);     // move down 1/2 of the unused space (marginTop),
                                                                                // then 1/2 of the height of an icon to center it (zoneYSize/2),
                                                                                // then mark that spot (zoneYSize * i)
        }
    }

    private int checkGroup(int x, int y) {
        int ySizeNeeded = numGroups * zoneYSize;
        int marginTop = (screenHeight - ySizeNeeded) / 2;
        for (int groupNum = 1; groupNum <= numGroups; groupNum++) {
            if ((y < ((zoneYSize * groupNum) + marginTop))) {
                Log.v("group", "pointer coordinates are : " + x + ", " + y);
                Log.v("group", "lastGroup is: " + lastGroup);
                Log.v("group", "Group found is: " + groupNum);
                if (groupNum != lastGroup) {
                    lastGroup = groupNum;
                    vibrate();
                    clearAppArray();
                }
                return groupNum;
            }
        }
        return -99; //cant find any group
    }

    private void setContentsForSelectedGroup(int group, boolean isRHS) {

        int index = 0;
        int rowOffset = 0;
        int colOffset = 0;
        if (!portrait && (leftSideNavigationBar || rightSideNavigationBar)) {
            colOffset = 1;
        }
        if (!portrait && group == 1 && numGroups >= 7) {
            rowOffset = 1;
        } else if (!portrait && group == 7) {
            rowOffset = -1;
        }
        try {
            int ySizeNeeded = numGroups * zoneYSize;
            int marginTop = (screenHeight - ySizeNeeded) / 2;
            int numAppsInGroup = groupAppList[group].size();
            int rowsNeeded = (numAppsInGroup / numAppCols);
            int nearestRow = (((group * zoneYSize) + marginTop) / Math.round(screenHeight / (numAppRows + 2))) - 1;
            int row = nearestRow + rowOffset;

            for (AppInfo item : groupAppList[group]) {
                item.setLaunchCount(settingsPrefs.getInt(item.label + "_launchCount", 0));
            }


            String sort = settingsPrefs.getString("sortG" + group, "Most Used");
            //Log.v("sort", "Sorting Group "+group+" SHOULD BE: " +sort);
            assert sort != null;
            if (sort.equals("Most Used")) {
                //Log.v("sort", "Sorting Group "+group+" by Most Used.");
                Collections.sort(groupAppList[group], AppInfo.appLaunchCount);

            } else {
                //Log.v("sort", "Sorting Group "+group+" by Alphabetical.");
                Collections.sort(groupAppList[group], AppInfo.appNameComparator);
            }


            while (row < numAppRows + 1) {
                if (isRHS) {
                    for (int col = numAppCols - colOffset; col > 0; col--) {

                        //populate apps from right to left
                        if (index < numAppsInGroup) {
                            AppInfo a = groupAppList[group].get(index);
                            ImageButton appIcon = new ImageButton(this);
                            configImageButton(appIcon);
                            appPositions[col][row].setLabel(a.label);
                            appPositions[col][row].setLaunchIntent(a.launchIntent);
                            appIcon.setX(col * screenWidth / (numAppCols + 2));
                            appIcon.setY(row * screenHeight / (numAppRows + 2));
                            appIcon.setBackground(a.icon);
                            appLayout.addView(appIcon);
                        } else if (index == numAppsInGroup) {
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
                            appIcon.setX(col * screenWidth / (numAppCols + 2));
                            appIcon.setY(row * screenHeight / (numAppRows + 2));
                            appIcon.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add_white_50dp));
                            appLayout.addView(appIcon);
                        }

                        index++;
                        if (index > numAppsInGroup) {
                            row = numAppRows + 999;
                        }
                    }

                } else {
                    for (int col = 1; col < numAppCols + 1; col++) {

                        //populate apps from left to right
                        if (index < numAppsInGroup) {
                            AppInfo a = groupAppList[group].get(index);
                            ImageButton appIcon = new ImageButton(this);
                            configImageButton(appIcon);
                            appPositions[col][row].setLabel(a.label);
                            appPositions[col][row].setLaunchIntent(a.launchIntent);
                            appIcon.setX(col * screenWidth / (numAppCols + 2));
                            appIcon.setY(row * screenHeight / (numAppRows + 2));
                            appIcon.setBackground(a.icon);
                            appLayout.addView(appIcon);
                        } else if (index == numAppsInGroup) {
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
                            appIcon.setX(col * screenWidth / (numAppCols + 2));
                            appIcon.setY(row * screenHeight / (numAppRows + 2));
                            appIcon.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add_white_50dp));
                            appLayout.addView(appIcon);
                        }

                        index++;
                        if (index > numAppsInGroup) {
                            row = numAppRows + 11;
                        }
                    }
                }
                if (nearestRow + rowsNeeded > numAppRows) {
                    row--;
                } else {
                    row++;
                }
            }

        } catch (Exception e) {
            //usual error setting appLabel
            Log.d("tag", "usual error with null AppLabel");
            e.printStackTrace();
        }
    }

    @NonNull
    private int[] checkWhichAppSelected(int rawX, int rawY) {
        int x, y;
        int appXSize = (screenWidth / (numAppCols + 2));
        int appYSize = (screenHeight / (numAppRows + 2));
        x = Math.round((rawX + (appXSize / 5)) / appXSize);     //
        y = Math.round((rawY - (appYSize / 5)) / appYSize);

        if (!portrait && leftSideNavigationBar) {
            x--;
        }

        if (x != lastAppTouched[0]) {
            lastAppTouched[0] = x;
            if (appPositions[x][y].launchIntent != null)
                vibrate();
        }
        if (y != lastAppTouched[1]) {
            lastAppTouched[1] = y;
            if (appPositions[x][y].launchIntent != null)
                vibrate();
        }
        return new int[]{x, y};
    }

    private boolean hasNavBar(Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);      //this apparently does NOT work in emulators...
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getDimensions();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        windowManager.removeView(rhs);
        windowManager.removeView(lhs);
        Intent broadcastIntent = new Intent(this, LauncherRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }


}


