package com.missilelauncher.missilelauncher;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SharedListPreferencesHelper {

    public static final String PREFS_NAME = "SettingsActivity";

    // This four methods are used for maintaining favorites.
    public static void saveFavorites(Context context, ArrayList<String> favorites, int group) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        editor = settings.edit();

        int size = settings.getInt(group+"_size", 0);

        // clear the previous data if exists
        for(int i=0; i<size; i++)
            editor.remove(group+"_"+i);

        // write the current list
        for(int i=0; i<favorites.size(); i++){
            editor.putString("G"+group+"_"+i, favorites.get(i));
            Log.v("Prefs","G"+group+"_"+i +" Saving: "+ favorites.get(i));
        }


        editor.putInt(group+"_size", favorites.size());
        editor.apply();
    }


    public ArrayList<String> getFavorites(Context context, int group) {
        SharedPreferences settings;
        List<String> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);

        int size = settings.getInt(group+"_size", 0);

        favorites = new ArrayList<String>(size);
        for(int i=0; i<size; i++){
            favorites.add(settings.getString("G"+group+"_"+i, null));
            Log.v("Prefs","G"+group+"_"+i +" Getting: "+ favorites.get(i));
        }

        return (ArrayList<String>) favorites;
    }
}