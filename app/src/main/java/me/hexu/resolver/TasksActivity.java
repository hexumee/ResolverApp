package me.hexu.resolver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import me.hexu.resolver.ui.adapters.TasksAdapter;
import me.hexu.resolver.ui.decorations.GridItemDecoration;
import me.hexu.resolver.ui.decorations.ListItemDecoration;
import me.hexu.resolver.utils.DisplayUtils;
import me.hexu.resolver.utils.ObjectCastUtils;

public class TasksActivity extends AppCompatActivity {
    private final SharedPreferences prefs = App.getInstance().getPreferences();
    private RecyclerView tasksList;                                                     // Список задач (UI)
    private EditText taskSearchBar;                                                     // Поле поиска
    private GridItemDecoration gridDecoration;
    private ListItemDecoration listDecoration;
    private GridLayoutManager glm;
    private LinearLayoutManager llm;
    private TasksAdapter tasksAdapter;
    private final ArrayList<HashMap<String, Object>> tasksArray = new ArrayList<>();    // Список задач
    private final ArrayList<String> path = new ArrayList<>();                           // Путь к разделу
    private final HashMap<String, String> bookInfo = new HashMap<>();                   // Информация о книге
    private boolean listViewTypeRows = false;                                           // Переменная, определяющая вид представления списка задач
    private boolean isTaskSearchBarVisible = false;                                     // Переменная, определяющая видимость поля ввода

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        setTitle(getIntent().getStringExtra("topicTitle"));

        tasksList = findViewById(R.id.tasks_list);
        taskSearchBar = findViewById(R.id.task_search_bar);

        // Применяем разные параметры декоратора в зависимости от ориентации экрана
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridDecoration = new GridItemDecoration(4, DisplayUtils.convertDpToPx(this, 10));
            glm = new GridLayoutManager(this, 4);
        } else {
            gridDecoration = new GridItemDecoration(6, DisplayUtils.convertDpToPx(this, 17));
            glm = new GridLayoutManager(this, 6);
        }

        listDecoration = new ListItemDecoration(DisplayUtils.convertDpToPx(this, 10));
        llm = new LinearLayoutManager(this);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tasksArray.addAll(ObjectCastUtils.toArrayListOfStringObjectHashMap(getIntent().getSerializableExtra("tasks")));
        path.addAll(ObjectCastUtils.toArrayListOfString(getIntent().getSerializableExtra("path")));
        bookInfo.putAll(ObjectCastUtils.toStringStringHashMap(getIntent().getSerializableExtra("bookInfo")));

        path.add(getIntent().getStringExtra("topicTitle"));

        tasksList.setItemAnimator(null);
        tasksList.addItemDecoration(prefs.getInt("tasks_view_type", 0) < 2 ? gridDecoration : listDecoration);
        tasksList.setLayoutManager(prefs.getInt("tasks_view_type", 0) < 2 ? glm : llm);

        tasksAdapter = new TasksAdapter((task, index) -> startActivity(new Intent(this, TaskActivity.class)
                .putExtra("id", getIntent().getStringExtra("id"))
                .putExtra("taskTitle", Objects.requireNonNull(task.get("title")).toString())
                .putExtra("taskUrl", Objects.requireNonNull(task.get("url")).toString())
                .putExtra("path", path)
                .putExtra("bookInfo", bookInfo)
                .putExtra("tasks", tasksArray)
                .putExtra("taskIndex", tasksArray.size() == tasksAdapter.getItemCount() ? index : computeArrayIndex(index))
                .putExtra("isOffline", getIntent().getBooleanExtra("isOffline", false))));

        // Если в настройках приложения "Вид списка задач" имеет значение "Список"
        if (prefs.getInt("tasks_view_type", 0) == 2) {
            tasksAdapter.setViewType(true);
            listViewTypeRows = true;
        }

        tasksAdapter.setItems(tasksArray);

        // Добавляем "наблюдатель" за вводимым текстом в поле поиска
        taskSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    tasksAdapter.setItems(tasksArray);
                } else {
                    tasksAdapter.filter(tasksArray, editable.toString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.tasks_actionbar_menu, menu);

        listViewTypeRows = tasksAdapter.getViewType();

        if (listViewTypeRows) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_tasks_view_grid_24));
        }

        // Если адаптером запрещено использование табличного представления
        if (tasksAdapter.isUsingGridRestricted()) {
            menu.getItem(0).setVisible(false);

            if (prefs.getInt("tasks_view_type", 0) < 2) {
                tasksList.removeItemDecoration(gridDecoration);
                tasksList.addItemDecoration(listDecoration);
                tasksList.setLayoutManager(llm);
            }
        }

        tasksList.setAdapter(tasksAdapter);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.tasks_view_switch) {
            listViewTypeRows = !listViewTypeRows;
            tasksAdapter.setViewType(listViewTypeRows);

            if (!listViewTypeRows) {
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_tasks_view_rows_20));
                tasksList.removeItemDecoration(listDecoration);
                tasksList.addItemDecoration(gridDecoration);
                tasksList.setLayoutManager(glm);
            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_tasks_view_grid_24));
                tasksList.removeItemDecoration(gridDecoration);
                tasksList.addItemDecoration(listDecoration);
                tasksList.setLayoutManager(llm);
            }

            tasksList.setAdapter(tasksAdapter);
        } else if (item.getItemId() == R.id.tasks_search) {
            isTaskSearchBarVisible = !isTaskSearchBarVisible;
            taskSearchBar.setVisibility(isTaskSearchBarVisible ? View.VISIBLE : View.GONE);

            if (!isTaskSearchBarVisible) {
                tasksAdapter.setItems(tasksArray);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Вычисление абсолютного индекса задачи в массиве из относительного.
     *
     * @param relativeIndex относительный индекс задачи (индекс в оверлее)
     *
     * @return абсолютный индекс задачи (такой как в tasksArray)
    **/
    private int computeArrayIndex(int relativeIndex) {
        for (int i = 0; i < tasksArray.size(); i++) {
            if (Objects.equals(tasksAdapter.getOverlay().get(relativeIndex).get("title"), tasksArray.get(i).get("title"))) {
                return i;
            }
        }

        return 0;
    }
}
