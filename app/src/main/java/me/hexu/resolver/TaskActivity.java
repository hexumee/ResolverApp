package me.hexu.resolver;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import me.hexu.resolver.database.dao.SavedTasksDao;
import me.hexu.resolver.database.entities.SavedTaskEntity;
import me.hexu.resolver.datamodels.TaskImageDataModel;
import me.hexu.resolver.ui.adapters.ImagesAdapter;
import me.hexu.resolver.interfaces.callbacks.IRequestCallback;
import me.hexu.resolver.utils.ApiRequest;
import me.hexu.resolver.utils.FileUtils;
import me.hexu.resolver.utils.ObjectCastUtils;

public class TaskActivity extends AppCompatActivity implements IRequestCallback {
    private Menu activityMenu;                                                          // Экзмепляр меню активности
    private LinearProgressIndicator imageProgressIndicator;                             // Прогресс-бар, показывающий текущее положение альбома из количества всех изображений в задаче
    private TextView imageProgressText;                                                 // Понятное представление состояния прогресс-бара
    private ViewPager2 viewPager;                                                       // Альбом с изображениями задачи
    private ImagesAdapter imagesAdapter;
    private SavedTasksDao taskDao;                                                      // Data Access Object к сохранённым задачам
    private SavedTaskEntity taskEntity;                                                 // Сущность сохраняемой задачи для базы данных
    private String taskTitle;                                                           // Заголовок задачи
    private String taskUrl;                                                             // URL задачи
    private final ArrayList<HashMap<String, Object>> tasksArray = new ArrayList<>();    // Список задач раздела
    private HashMap<String, Object> tasksMap = new HashMap<>();                         // Оффлайн-вариант задачи (если требуется)
    private final ArrayList<String> path = new ArrayList<>();                           // Путь к задаче
    private final HashMap<String, String> bookInfo = new HashMap<>();                   // Информация о книге (для последующего сохранения, если потребуется)
    private boolean isFavorite = false;                                                 // Переменная, показывающая является ли задача сохранённой
    private boolean isOffline = false;                                                  // Переменная, показывающая имеет ли устройство доступ к Интернету
    private int currentTaskIndex = 0;                                                   // Текущий индекс задачи (для обращения к предыдущей или следующей задаче)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        setTitle(getIntent().getStringExtra("taskTitle"));

        imageProgressIndicator = findViewById(R.id.image_counter_indicator);
        imageProgressText = findViewById(R.id.image_counter_text);
        viewPager = findViewById(R.id.images_list_viewpager);
        imagesAdapter = new ImagesAdapter(url -> startActivity(new Intent(this, ImageViewActivity.class)
                .putExtra("id", bookInfo.get("id"))
                .putExtra("taskImageUrl", url)
                .putExtra("isOffline", isOffline)));

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        isOffline = getIntent().getBooleanExtra("isOffline", false);
        taskTitle = getIntent().getStringExtra("taskTitle");
        taskUrl = getIntent().getStringExtra("taskUrl");
        currentTaskIndex = getIntent().getIntExtra("taskIndex", 0);

        // Если мы пришли сюда из TasksActivity, а не из LibraryFragment, и при этом флага нет
        if (getIntent().getSerializableExtra("tasks") != null) {
            tasksArray.addAll(ObjectCastUtils.toArrayListOfStringObjectHashMap(getIntent().getSerializableExtra("tasks")));
        }
        path.addAll(ObjectCastUtils.toArrayListOfString(getIntent().getSerializableExtra("path")));
        bookInfo.putAll(ObjectCastUtils.toStringStringHashMap(getIntent().getSerializableExtra("bookInfo")));

        // Если мы пришли сюда из TasksActivity, а не из LibraryFragment, и флаг есть
        if (!getIntent().getBooleanExtra("fromLibrary", false)) {
            path.remove(0);
            path.add(taskTitle);
        }

        taskDao = App.getInstance().getDatabase().getSavedTasksDao();
        taskEntity = new SavedTaskEntity(bookInfo.get("title"), bookInfo.get("header"), bookInfo.get("authors"), bookInfo.get("publisher"), bookInfo.get("year"), bookInfo.get("imageUrl"), bookInfo.get("url"), String.join(" ⋅ ", path), taskTitle, taskUrl);

        // Регистрируем функцию обратного вызова, которая сработает при перелистывании
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    imageProgressIndicator.setProgressCompat(viewPager.getCurrentItem()+1, true);
                    imageProgressText.setText(String.format(getResources().getConfiguration().locale, "%d/%d", viewPager.getCurrentItem()+1, imagesAdapter.getItemCount()));
                }
            }
        });

        // Если мы не можем обратиться к серверу, то используем кэш
        if (isOffline) {
            String bookPath = FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), Objects.requireNonNull(bookInfo.get("id")) }, false);
            ArrayList<TaskImageDataModel> taskImages = new ArrayList<>();

            try {
                tasksMap = FileUtils.readMapObject(FileUtils.joinWithSeparator(new String[]{ bookPath, "tasks.map" }, false));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            ArrayList<TaskImageDataModel> taskList = new ArrayList<>();
            ArrayList<?> taskListObject = (ArrayList<?>) tasksMap.get(taskUrl);
            if (taskListObject != null) {
                for (Object entry: taskListObject) {
                    taskList.add((TaskImageDataModel) entry);
                }
            }

            for (int i = 0; i < taskList.size(); i++) {
                String imageUrl = taskList.get(i).getTaskImageUrl();
                String[] imageUrlSplit = imageUrl.split("/");

                taskImages.add(new TaskImageDataModel(FileUtils.joinWithSeparator(new String[]{ bookPath, imageUrlSplit[imageUrlSplit.length-1] }, true)));
            }

            viewPager.setAdapter(imagesAdapter);
            imagesAdapter.setItems(taskImages);
            imageProgressIndicator.setProgress(1);
            imageProgressIndicator.setMax(taskImages.size());
            imageProgressText.setText(String.format(getResources().getConfiguration().locale, "%d/%d", viewPager.getCurrentItem()+1, imagesAdapter.getItemCount()));
        } else {
            ApiRequest.requestUrl(this, taskUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.task_actionbar_menu, menu);
        activityMenu = menu;

        // Так как при переходе из LibraryFragment в эту активность tasksArray == null, то выключим кнопки "Назад" и "Вперёд"
        if (getIntent().getBooleanExtra("fromLibrary", false)) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
        }

        // Помним про границы
        if (currentTaskIndex == 0) {
            menu.getItem(0).setVisible(false);
        } else if (currentTaskIndex == tasksArray.size()-1) {
            menu.getItem(1).setVisible(false);
        }

        new Thread(() -> {
            if (taskDao.isTaskAlreadyExists(taskUrl)) {
                isFavorite = true;
                runOnUiThread(() -> menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_remove_24)));
            }
        }).start();

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.task_prev) {
            currentTaskIndex--;

            // Помним про границы
            activityMenu.getItem(0).setVisible(currentTaskIndex != 0);
            activityMenu.getItem(1).setVisible(true);

            getNewTaskContents();
        } else if (item.getItemId() == R.id.task_next) {
            currentTaskIndex++;

            // Помним про границы
            activityMenu.getItem(0).setVisible(true);
            activityMenu.getItem(1).setVisible(currentTaskIndex != tasksArray.size()-1);

            getNewTaskContents();
        } else if (item.getItemId() == R.id.task_favorites_switch) {
            isFavorite = !isFavorite;

            new Thread(() -> {
                if (!isFavorite) {
                    taskDao.delete(taskUrl);
                    runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_save_24)));
                } else {
                    if (!taskDao.isTaskAlreadyExists(taskUrl)) {
                        taskDao.insert(taskEntity);
                        runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_remove_24)));
                    }
                }
            }).start();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponseReceived(String response) {
        try {
            ArrayList<TaskImageDataModel> taskImages = new ArrayList<>();
            JSONObject task = new JSONObject(response);
            JSONArray editions = task.getJSONArray("editions");

            for (int i = 0; i < editions.length(); i++) {
                JSONObject edition = editions.getJSONObject(i);
                JSONArray images = edition.getJSONArray("images");

                for (int j = 0; j < images.length(); j++) {
                    JSONObject image = images.getJSONObject(j);
                    taskImages.add(new TaskImageDataModel(image.getString("url")));
                }
            }

            runOnUiThread(() -> {
                viewPager.setAdapter(imagesAdapter);
                imagesAdapter.setItems(taskImages);
                imageProgressIndicator.setProgress(1);
                imageProgressIndicator.setMax(taskImages.size());
                imageProgressText.setText(String.format(getResources().getConfiguration().locale, "%d/%d", viewPager.getCurrentItem()+1, imagesAdapter.getItemCount()));
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получение новой задачи и её содержимого.
    **/
    public void getNewTaskContents() {
        taskTitle = Objects.requireNonNull(tasksArray.get(currentTaskIndex).get("title")).toString();
        taskUrl = Objects.requireNonNull(tasksArray.get(currentTaskIndex).get("url")).toString();

        setTitle(taskTitle);

        new Thread(() -> {
            if (taskDao.isTaskAlreadyExists(taskUrl)) {
                isFavorite = true;
                runOnUiThread(() -> activityMenu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_remove_24)));
            } else {
                isFavorite = false;
                runOnUiThread(() -> activityMenu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_save_24)));
            }
        }).start();
        path.remove(path.size()-1);
        path.add(taskTitle);
        taskEntity = new SavedTaskEntity(bookInfo.get("title"), bookInfo.get("header"), bookInfo.get("authors"), bookInfo.get("publisher"), bookInfo.get("year"), bookInfo.get("imageUrl"), bookInfo.get("url"), String.join(" ⋅ ", path), taskTitle, taskUrl);

        // Если мы не можем обратиться к серверу, то используем кэш
        if (isOffline) {
            String bookPath = FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), Objects.requireNonNull(bookInfo.get("id")) }, false);
            ArrayList<TaskImageDataModel> taskImages = new ArrayList<>();
            ArrayList<?> taskList = (ArrayList<?>) tasksMap.get(taskUrl);

            if (taskList != null) {
                for (int i = 0; i < taskList.size(); i++) {
                    TaskImageDataModel image = (TaskImageDataModel) taskList.get(i);
                    String imageUrl = image.getTaskImageUrl();
                    String[] imageUrlSplit = imageUrl.split("/");

                    taskImages.add(new TaskImageDataModel(FileUtils.joinWithSeparator(new String[]{ bookPath, imageUrlSplit[imageUrlSplit.length-1] }, true)));
                }
            }

            viewPager.setAdapter(imagesAdapter);
            imagesAdapter.setItems(taskImages);
            imageProgressIndicator.setProgress(1);
            imageProgressIndicator.setMax(taskImages.size());
            imageProgressText.setText(String.format(getResources().getConfiguration().locale, "%d/%d", viewPager.getCurrentItem()+1, imagesAdapter.getItemCount()));
        } else {
            ApiRequest.requestUrl(this, taskUrl);
        }
    }
}
