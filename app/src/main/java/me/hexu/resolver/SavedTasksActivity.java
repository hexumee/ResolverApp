package me.hexu.resolver;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import me.hexu.resolver.database.dao.SavedTasksDao;
import me.hexu.resolver.database.entities.SavedTaskEntity;
import me.hexu.resolver.datamodels.TaskPreviewDataModel;
import me.hexu.resolver.ui.adapters.TaskAdapter;

public class SavedTasksActivity extends AppCompatActivity {
    private ProgressBar savedTasksProgressBar;    // Прогресс-бар (надо же как-то показать работу)
    private TaskAdapter savedTasksAdapter;
    private SavedTasksDao savedTasksDao;          // Data Access Object к сохранённым задачам

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_tasks);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        savedTasksProgressBar = findViewById(R.id.wip_indicator);
        savedTasksAdapter = new TaskAdapter(task -> {
            HashMap<String, String> bookInfo = new HashMap<>() {{
                put("title", task.getTitle());
                put("header", task.getHeader());
                put("authors", task.getAuthors());
                put("publisher", task.getPublisher());
                put("year", task.getYear());
                put("imageUrl", task.getImage());
                put("url", task.getUrl());
            }};

            startActivity(new Intent(this, TaskActivity.class)
                    .putExtra("taskTitle", task.getTaskTitle())
                    .putExtra("taskUrl", task.getTaskUrl())
                    .putExtra("path", new ArrayList<>(Arrays.asList(task.getPath().split(" ⋅ "))))
                    .putExtra("bookInfo", bookInfo)
                    .putExtra("fromLibrary", true));
        });

        ((RecyclerView) findViewById(R.id.saved_tasks_full_list)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.saved_tasks_full_list)).setAdapter(savedTasksAdapter);

        savedTasksDao = App.getInstance().getDatabase().getSavedTasksDao();
    }

    @Override
    public void onStart() {
        super.onStart();

        // В отдельном потоке выбираем сохранённые задачи и добавляем их в список адаптера
        new Thread(() -> {
            ArrayList<TaskPreviewDataModel> result = new ArrayList<>();
            List<SavedTaskEntity> savedTaskEntities = savedTasksDao.getAllSavedTasks();

            for (SavedTaskEntity entity : savedTaskEntities) {
                result.add(new TaskPreviewDataModel(entity.title, entity.header, entity.authors, entity.publisher, entity.year, entity.image, entity.url, entity.path, entity.taskTitle, entity.taskUrl));
            }

            runOnUiThread(() -> {
                savedTasksAdapter.setItems(result);
                savedTasksProgressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
