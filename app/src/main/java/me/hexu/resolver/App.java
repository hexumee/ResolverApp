package me.hexu.resolver;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;
import me.hexu.resolver.database.AppDatabase;

public class App extends Application {
    private static App instance;                           // Экземпляр приложения
    private AppDatabase database;                          // Экземпляр базы данных
    private SharedPreferences preferences;                 // Экземпляр параметров приложения
    private SharedPreferences.Editor preferencesEditor;    // Экземпляр редактора параметров приложения
    private boolean usingDarkTheme = false;                // Переменная, определяющая использование светлой или тёмной темы

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database.db").build();
        preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        preferencesEditor = preferences.edit();

        // Устанавливаем тему приложения
        switch (preferences.getInt("theme_type", 0)) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                usingDarkTheme = ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    public static App getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public SharedPreferences.Editor getPreferencesEditor() {
        return preferencesEditor;
    }

    public boolean isUsingDarkTheme() {
        return usingDarkTheme;
    }
}
