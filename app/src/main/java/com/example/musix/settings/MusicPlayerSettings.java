package com.example.musix.settings;
import android.content.Context;
import android.content.SharedPreferences;

public class MusicPlayerSettings {

    private static final String PREF_NAME = "MusicPlayerPrefs";
    private static final String KEY_SHUFFLE = "shuffle";
    private static final String KEY_REPEAT = "repeat";

    private final SharedPreferences sharedPreferences;

    public MusicPlayerSettings(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save settings
    public void saveSettings(int shuffle, int repeat) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_SHUFFLE, shuffle);
        editor.putInt(KEY_REPEAT, repeat);
        editor.apply();
    }

    public int getShuffleSetting() {
        return sharedPreferences.getInt(KEY_SHUFFLE, 0);
    }

    public int getRepeatSetting() {
        return sharedPreferences.getInt(KEY_REPEAT, 0);
    }
}
