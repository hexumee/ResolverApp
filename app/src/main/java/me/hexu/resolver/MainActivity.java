package me.hexu.resolver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private final SharedPreferences sharedPreferences = App.getInstance().getPreferences();
    private final SharedPreferences.Editor prefsEditor = App.getInstance().getPreferencesEditor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Скрываем верхнюю панель, так как она нам не нужна
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Устанавливаем контроллер навигации
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            NavigationUI.setupWithNavController((BottomNavigationView)findViewById(R.id.nav_view), navHostFragment.getNavController());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Если приложение запускается впервые, то устанавливаем значения по умолчанию
        if (!sharedPreferences.getBoolean("not_first_time", false)) {
            prefsEditor.putInt("theme_type", 0)
                       .putInt("tasks_view_type", 0)
                       .putBoolean("not_first_time", true)
                       .apply();
        }

        // Если нет доступа к Интернету, перенаправляем пользователя на "Загруженные книги"
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.main_activity_no_internet_toast_text), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, DownloadsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            finish();
        }
    }

    /**
     * Проверяем, есть ли на устройстве подключение к Интернету.
     *
     * @return если доступ к Интернету имеется, то возращаем true, иначе false
    **/
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
