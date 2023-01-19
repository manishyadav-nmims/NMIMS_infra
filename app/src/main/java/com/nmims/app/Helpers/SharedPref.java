package com.nmims.app.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.nmims.app.R;

public class SharedPref {
    private SharedPreferences sharedPreferences;
    private Context context;
    SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences(
                context.getResources().getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        this.context = context;
    }

    public void saveStudentInfo(String programId, String acadSession){
        editor = sharedPreferences.edit();
        editor.putString("programId", programId);
        editor.putString("acadSession", acadSession);
        editor.apply();
    }

    public String getUserData(String key){
        String data = sharedPreferences.getString(key, "");
        return data;
    }
    public void clearPreference() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void saveDiv(String div) {
        editor = sharedPreferences.edit();
        editor.putString("division", div);
        editor.apply();
    }

    public void setAutoStart(int i) {
        editor = sharedPreferences.edit();
        editor.putInt("autoStart", i);
        editor.apply();
    }

    public int getAutoStart(){
        int data = sharedPreferences.getInt("autoStart", 0);
        return data;
    }
}
