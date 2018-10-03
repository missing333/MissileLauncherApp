package com.missilelauncher.overlaytestv1;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SharedListPreferencesHelper {

    public static final String PREFS_NAME = "SettingsActivity";
    public static final String FAVORITES = "Product_Favorite";



    // This four methods are used for maintaining favorites.
    public static void saveFavorites(Context context, ArrayList<String> favorites, int group) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        editor = settings.edit();

        /*Log.v("json", "saveGroup " +"G" +group+": "+ favorites);
        Gson gson = new Gson();
        Type type = new TypeToken<AppInfo>(){}.getType();
        String jsonFavorites = gson.toJson(favorites);  // not working
        Log.v("json", "saveGroup " +"G" +group+": "+ jsonFavorites);
        editor.putString("G"+group, jsonFavorites);*/

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

/*    public void addFavorite(Context context, Product product) {
        List<Product> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<Product>();
        favorites.add(product);
        saveFavorites(context, favorites);
    }

    public void removeFavorite(Context context, Product product) {
        ArrayList<Product> favorites = getFavorites(context);
        if (favorites != null) {
            favorites.remove(product);
            saveFavorites(context, favorites);
        }
    }*/

    public ArrayList<String> getFavorites(Context context, int group) {
        SharedPreferences settings;
        List<String> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);

        /*if (settings.contains("G"+group)) {
            String jsonFavorites = settings.getString("G"+group, null);
            Log.v("json", "getGroup " +"G" +group+": "+ jsonFavorites);
            Gson gson = new Gson();
            AppInfo favoriteItems = gson.fromJson(jsonFavorites,
                    AppInfo.class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<>(favorites);
        } else
            return null;*/

        int size = settings.getInt(group+"_size", 0);

        favorites = new ArrayList<String>(size);
        for(int i=0; i<size; i++){
            favorites.add(settings.getString("G"+group+"_"+i, null));
            Log.v("Prefs","G"+group+"_"+i +" Getting: "+ favorites.get(i));
        }


        return (ArrayList<String>) favorites;
    }
}