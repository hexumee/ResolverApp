package me.hexu.resolver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import me.hexu.resolver.database.dao.DownloadedBooksDao;
import me.hexu.resolver.database.dao.RecentlyViewedDao;
import me.hexu.resolver.database.dao.SavedBooksDao;
import me.hexu.resolver.database.dao.SavedTasksDao;
import me.hexu.resolver.utils.FileUtils;

public class SettingsActivity extends AppCompatActivity {
    private final SharedPreferences sharedPreferences = App.getInstance().getPreferences();
    private final SharedPreferences.Editor prefsEditor = App.getInstance().getPreferencesEditor();
    private final RecentlyViewedDao recentlyViewedDao = App.getInstance().getDatabase().getRecentlyViewedDao();
    private final SavedBooksDao savedBooksDao = App.getInstance().getDatabase().getSavedBooksDao();
    private final SavedTasksDao savedTasksDao = App.getInstance().getDatabase().getSavedTasksDao();
    private final DownloadedBooksDao downloadedBooksDao = App.getInstance().getDatabase().getDownloadedBooksDao();
    private TextView themesSubtitle;       // Подзаголовок секции выбора темы
    private TextView tasksViewSubtitle;    // Подзаголовок секции представления списка

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        themesSubtitle = findViewById(R.id.themes_subtitle);
        tasksViewSubtitle = findViewById(R.id.tasks_view_subtitle);
    }

    @Override
    public void onStart() {
        super.onStart();

        themesSubtitle.setText(getResources().getStringArray(R.array.settings_themes_list)[sharedPreferences.getInt("theme_type", 0)]);
        tasksViewSubtitle.setText(getResources().getStringArray(R.array.settings_tasks_view_types_list)[sharedPreferences.getInt("tasks_view_type", 0)]);

        findViewById(R.id.themes_section).setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, App.getInstance().isUsingDarkTheme() ? R.style.Theme_Resolver_AlertDialog_Dark : R.style.Theme_Resolver_AlertDialog_Light);
            builder.setTitle(R.string.settings_theme_header);
            builder.setSingleChoiceItems(R.array.settings_themes_list, sharedPreferences.getInt("theme_type", 0), (dialog, which) -> {
                prefsEditor.putInt("theme_type", which).apply();
                dialog.dismiss();
                themesSubtitle.setText(getResources().getStringArray(R.array.settings_themes_list)[which]);
                AppCompatDelegate.setDefaultNightMode(which == 0 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : (which == 1 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES));
            });

            builder.show();
        });

        findViewById(R.id.tasks_view_section).setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, App.getInstance().isUsingDarkTheme() ? R.style.Theme_Resolver_AlertDialog_Dark : R.style.Theme_Resolver_AlertDialog_Light);
            builder.setTitle(R.string.settings_tasks_view_header);
            builder.setSingleChoiceItems(R.array.settings_tasks_view_types_list, sharedPreferences.getInt("tasks_view_type", 0), (dialog, which) -> {
                prefsEditor.putInt("tasks_view_type", which).apply();
                dialog.dismiss();
                tasksViewSubtitle.setText(getResources().getStringArray(R.array.settings_tasks_view_types_list)[which]);
            });

            builder.show();
        });

        findViewById(R.id.wiping_section).setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, App.getInstance().isUsingDarkTheme() ? R.style.Theme_Resolver_AlertDialog_Dark : R.style.Theme_Resolver_AlertDialog_Light);
            builder.setTitle(R.string.settings_wipe_data_header);
            builder.setItems(R.array.settings_wipe_actions_list, (dialog, which) -> {
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this, App.getInstance().isUsingDarkTheme() ? R.style.Theme_Resolver_AlertDialog_Dark : R.style.Theme_Resolver_AlertDialog_Light);
                confirmBuilder.setTitle(getString(R.string.settings_confirmation_title));
                confirmBuilder.setNegativeButton(getString(R.string.no), (confirmDialog, i) -> confirmDialog.dismiss());

                switch (which) {
                    case 0:
                        {
                            confirmBuilder.setMessage(getString(R.string.settings_wipe_recently_viewed_message));
                            confirmBuilder.setPositiveButton(getString(R.string.yes), (confirmDialog, i) -> new Thread(() -> {
                                recentlyViewedDao.wipe();
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.settings_success), Toast.LENGTH_LONG).show());
                            }).start());
                        }
                        break;
                    case 1:
                        {
                            confirmBuilder.setMessage(getString(R.string.settings_wipe_saved_books_message));
                            confirmBuilder.setPositiveButton(getString(R.string.yes), (confirmDialog, i) -> new Thread(() -> {
                                savedBooksDao.wipe();
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.settings_success), Toast.LENGTH_LONG).show());
                            }).start());
                        }
                        break;
                    case 2:
                        {
                            confirmBuilder.setMessage(getString(R.string.settings_wipe_saved_tasks_message));
                            confirmBuilder.setPositiveButton(getString(R.string.yes), (confirmDialog, i) -> new Thread(() -> {
                                savedTasksDao.wipe();
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.settings_success), Toast.LENGTH_LONG).show());
                            }).start());
                        }
                        break;
                    case 3:
                        {
                            confirmBuilder.setMessage(getString(R.string.settings_wipe_downloaded_books_message));
                            confirmBuilder.setPositiveButton(getString(R.string.yes), (confirmDialog, i) -> new Thread(() -> {
                                downloadedBooksDao.wipe();
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.settings_success), Toast.LENGTH_LONG).show());
                            }).start());
                        }
                        break;
                    case 4:
                        {
                            confirmBuilder.setMessage(getString(R.string.settings_wipe_all_message));
                            confirmBuilder.setPositiveButton(getString(R.string.yes), (confirmDialog, i) -> new Thread(() -> {
                                recentlyViewedDao.wipe();
                                savedBooksDao.wipe();
                                savedTasksDao.wipe();
                                downloadedBooksDao.wipe();
                                FileUtils.deleteDirectory(getFilesDir(), true);
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.settings_success), Toast.LENGTH_LONG).show());
                            }).start());
                        }
                        break;
                }

                confirmBuilder.show();
            });

            builder.show();
        });

        findViewById(R.id.about_section).setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, App.getInstance().isUsingDarkTheme() ? R.style.Theme_Resolver_AlertDialog_Dark : R.style.Theme_Resolver_AlertDialog_Light);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings_about, findViewById(android.R.id.content), false);

            builder.setTitle(R.string.settings_about_app_header);
            builder.setView(dialogView);
            builder.setPositiveButton(getString(R.string.ok), null);
            builder.show();

            dialogView.findViewById(R.id.github_button).setOnClickListener(listener -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_repo_url)))));
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
