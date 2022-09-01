package me.hexu.resolver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.hexu.resolver.database.dao.DownloadedBooksDao;
import me.hexu.resolver.database.dao.RecentlyViewedDao;
import me.hexu.resolver.database.dao.SavedBooksDao;
import me.hexu.resolver.database.entities.DownloadedBookEntity;
import me.hexu.resolver.database.entities.RecentlyViewedEntity;
import me.hexu.resolver.database.entities.SavedBookEntity;
import me.hexu.resolver.datamodels.BookFullDataModel;
import me.hexu.resolver.services.DeleteService;
import me.hexu.resolver.services.DownloadService;
import me.hexu.resolver.ui.adapters.TopicAdapter;
import me.hexu.resolver.interfaces.callbacks.IProbeCallback;
import me.hexu.resolver.interfaces.callbacks.IRequestCallback;
import me.hexu.resolver.utils.ApiRequest;
import me.hexu.resolver.utils.DisplayUtils;
import me.hexu.resolver.utils.FileUtils;
import me.hexu.resolver.utils.ObjectCastUtils;
import me.hexu.resolver.utils.ObjectParser;

public class BookActivity extends AppCompatActivity implements IRequestCallback, IProbeCallback {
    private Menu activityMenu;                                                                      // Экзмепляр меню активности
    private RecentlyViewedDao recentlyViewedDao;                                                    // Data Access Object к недавно просмотренным книгам
    private RecentlyViewedEntity recentlyViewedEntity;                                              // Сущность недавно просмотренной книги для базы данных
    private SavedBooksDao bookDao;                                                                  // Data Access Object к сохранённым книгам
    private SavedBookEntity bookEntity;                                                             // Сущность сохраняемой книги для базы данных
    private DownloadedBooksDao downloadsDao;                                                        // Data Access Object к загруженным книгам
    private ImageView bookCover;                                                                    // Обложка книги
    private TextView bookTopicTitle;                                                                // Заголовок раздела
    private RecyclerView bookTopicsList;                                                            // Список разделов книги (UI)
    private TopicAdapter topicAdapter;
    private BookFullDataModel book;                                                                 // Полная модель данных книги
    private final ArrayList<String> path = new ArrayList<>(Collections.singleton(App.getInstance().getString(R.string.book_root_topic)));    // Путь (для последующего сохранения, если потребуется)
    private ArrayList<HashMap<String, Object>> bookTopics = new ArrayList<>();                      // Список разделов книги
    private ArrayList<HashMap<String, Object>> currentTopic = new ArrayList<>();                    // Содержание текущего раздела
    private boolean isFavorite = false;                                                             // Переменная, показывающая является ли книга сохранённой
    private boolean isDownloaded = false;                                                           // Переменная, показывающая является ли книга загруженной
    private boolean isOffline = false;                                                              // Переменная, показывающая имеет ли устройство доступ к Интернету

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        bookCover = findViewById(R.id.book_image);
        bookTopicTitle = findViewById(R.id.topic_title);
        bookTopicsList = findViewById(R.id.topic_list);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ((TextView) findViewById(R.id.book_title)).setText(getIntent().getStringExtra("contentsTitle"));
        ((TextView) findViewById(R.id.book_header)).setText(getIntent().getStringExtra("contentsHeader"));
        ((TextView) findViewById(R.id.book_authors)).setText(getIntent().getStringExtra("contentsAuthors"));
        ((TextView) findViewById(R.id.book_publisher)).setText(getIntent().getStringExtra("contentsPublisher"));
        ((TextView) findViewById(R.id.book_year)).setText(getIntent().getStringExtra("contentsYear"));
        ((TextView) findViewById(R.id.book_url)).setText(getIntent().getStringExtra("contentsUrl"));
        ((TextView) findViewById(R.id.topic_title)).setText(getString(R.string.book_root_topic));

        bookTopicsList.setLayoutManager(new LinearLayoutManager(this));

        bookDao = App.getInstance().getDatabase().getSavedBooksDao();
        recentlyViewedDao = App.getInstance().getDatabase().getRecentlyViewedDao();
        downloadsDao = App.getInstance().getDatabase().getDownloadedBooksDao();

        isOffline = getIntent().getBooleanExtra("fromDownloads", false);

        // Если мы можем обратиться к серверу, то используем эту возможность
        if (!isOffline) {
            ApiRequest.probeUrl(this, getIntent().getStringExtra("imageUrl"));
            ApiRequest.requestUrl(this, getIntent().getStringExtra("contentsUrl"));
        }

        // Регистрация слушателей эфира (BroadcastReceiver)
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new Thread(() -> {
                    App.getInstance().getDatabase().getDownloadedBooksDao().delete(book.getId());

                    String path = FileUtils.joinWithSeparator(new String[]{ App.getInstance().getFilesDir().getAbsolutePath(), String.valueOf(book.getId()) }, false);
                    FileUtils.deleteDirectory(new File(path), true);

                    runOnUiThread(() -> {
                        activityMenu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_book_download_switch_do_24));
                        activityMenu.getItem(0).setVisible(true);
                    });
                }).start();

                isDownloaded = false;
            }
        }, new IntentFilter("ACTION_DOWNLOAD_CANCELLED"));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                runOnUiThread(() -> {
                    activityMenu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_book_download_switch_undo_24));
                    activityMenu.getItem(0).setVisible(true);
                });

                isDownloaded = true;
            }
        }, new IntentFilter("ACTION_DOWNLOAD_COMPLETE"));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                activityMenu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_book_download_switch_do_24));
                activityMenu.getItem(0).setVisible(true);
                isDownloaded = false;
            }
        }, new IntentFilter("ACTION_DELETE_COMPLETE"));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.book_actionbar_menu, menu);
        activityMenu = menu;

        // Пока состояние не загружено, меню будет выключено
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);

        // Если нет доступа к Интернету, то используем ранее загруженный вариант книги
        if (isOffline) {
            loadBookFromCache();
        }

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.book_favorites_switch) {
            isFavorite = !isFavorite;

            if (!isFavorite) {
                new Thread(() -> {
                    bookDao.delete(book.getId());
                    runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_save_24)));
                }).start();
            } else {
                new Thread(() -> {
                    if (!bookDao.isBookAlreadyExists(book.getId())) {
                        bookDao.insert(bookEntity);
                        runOnUiThread(() -> item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_remove_24)));
                    }
                }).start();
            }
        } else if (item.getItemId() == R.id.book_download_switch) {
            activityMenu.getItem(0).setVisible(false);

            if (isDownloaded) {
                Toast.makeText(this, getString(R.string.book_delete_action_toast), Toast.LENGTH_LONG).show();

                new Thread(() -> {
                    // Начиная с Android 8, требуется запускать сервис переднего плана вместо обычного
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(getApplicationContext(), DeleteService.class).putExtra("bookId", book.getId()));
                    } else {
                        startService(new Intent(getApplicationContext(), DeleteService.class).putExtra("bookId", book.getId()));
                    }

                    downloadsDao.delete(book.getId());
                }).start();
            } else {
                Toast.makeText(this, getString(R.string.book_download_action_toast), Toast.LENGTH_LONG).show();

                new Thread(() -> {
                    if (downloadsDao.isBookAlreadyExists(book.getId())) {
                        downloadsDao.delete(book.getId());
                    }

                    // Начиная с Android 8, требуется запускать сервис переднего плана вместо обычного
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(getApplicationContext(), DownloadService.class).putExtra("book", book));
                    } else {
                        startService(new Intent(getApplicationContext(), DownloadService.class).putExtra("book", book));
                    }

                    downloadsDao.insert(new DownloadedBookEntity(book.getId(), book.getTitle(), book.getHeader(), book.getAuthors(), book.getPublisher(), book.getYear(), book.getImage(), book.getUrl(), false));
                }).start();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        path.remove(path.size()-1);

        if (path.isEmpty()) {    // Если корневой элемент пути отсутствует
            finish();
        } else if (path.size() == 1) {    // Если присутствует только корневой элемент
            currentTopic = bookTopics;
            bookTopicTitle.setText(path.get(0));
            topicAdapter.setItems(bookTopics);
        } else {    // В остальных случаях (path.size() > 1)
            int currentDepth = 1;    // Текущая глубина обхода
            int walkIndex = 0;       // Текущий индекс обхода
            currentTopic = bookTopics;

            // Обходим структуру книги, пока не соберем путь, который был шагом ранее
            while (currentDepth != path.size()) {
                if (Objects.equals(Objects.requireNonNull(currentTopic).get(walkIndex).get("title"), path.get(currentDepth))) {
                    currentTopic = new ArrayList<>(ObjectCastUtils.toArrayListOfStringObjectHashMap(currentTopic.get(walkIndex).get("topics")));
                    currentDepth++;
                }

                walkIndex++;
            }

            bookTopicTitle.setText(path.get(path.size()-1));
            topicAdapter.setItems(currentTopic);
        }
    }

    @Override
    public void onProbeResult(boolean isProbingSucceeded) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isProbingSucceeded) {
                Picasso.get().load(getIntent().getStringExtra("imageUrl")).into(bookCover, new Callback() {
                    @Override
                    public void onSuccess() {
                        findViewById(R.id.wip_indicator).setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) { }
                });
            } else {
                RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(DisplayUtils.convertDpToPx(this, 85), RelativeLayout.LayoutParams.WRAP_CONTENT);
                rlParams.addRule(RelativeLayout.CENTER_VERTICAL, bookCover.getId());
                bookCover.setLayoutParams(rlParams);
                bookCover.setPadding(32, 0, 32, 0);
                bookCover.setImageResource(R.drawable.ic_image_not_found_64);
                findViewById(R.id.wip_indicator).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResponseReceived(String response) {
        try {
            book = ObjectParser.getParsedBook(new JSONObject(response));
            presentBookData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверка на существование и установка следующего раздела книги.
     *
     * @param newPathItem наименование следующего раздела
     *
     * @return если следующий раздел книги существует, то возращаем true, иначе false (означает, что дальше идет список задач)
    **/
    public boolean setNextTopic(String newPathItem) {

        // Проходим по книге, используя известный путь, потом переходим на следующий раздел
        for (Map<String, Object> entry : currentTopic) {
            if (Objects.requireNonNull(entry.get("title")).toString().equals(newPathItem) && ((List<?>) Objects.requireNonNull(entry.get("topics"))).size() != 0) {
                bookTopicTitle.setText((String) entry.get("title"));
                currentTopic = new ArrayList<>(ObjectCastUtils.toArrayListOfStringObjectHashMap(entry.get("topics")));

                return true;
            }
        }

        return false;
    }

    /**
     * Загрузка книги из кэша (ранее загруженный вариант).
    **/
    public void loadBookFromCache() {
        String bookPath = FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), String.valueOf(getIntent().getIntExtra("bookId", 0)) }, false);
        String[] imagePathSplit = getIntent().getStringExtra("imageUrl").split("/");

        Picasso.get().load(new File(FileUtils.joinWithSeparator(new String[]{ bookPath, imagePathSplit[imagePathSplit.length-1] }, false))).into(bookCover, new Callback() {
            @Override
            public void onSuccess() {
                findViewById(R.id.wip_indicator).setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(DisplayUtils.convertDpToPx(getApplicationContext(), 85), RelativeLayout.LayoutParams.WRAP_CONTENT);
                rlParams.addRule(RelativeLayout.CENTER_VERTICAL, bookCover.getId());
                bookCover.setLayoutParams(rlParams);
                bookCover.setPadding(32, 0, 32, 0);
                bookCover.setImageResource(R.drawable.ic_image_not_found_64);
            }
        });

        try {
            HashMap<String, Object> bookMap = FileUtils.readMapObject(FileUtils.joinWithSeparator(new String[]{ bookPath, "book.map" }, false));
            ArrayList<HashMap<String, Object>> bookStructure = new ArrayList<>(ObjectCastUtils.toArrayListOfStringObjectHashMap(bookMap.get("structure")));
            book = new BookFullDataModel((int) Objects.requireNonNull(bookMap.get("id")), (String)bookMap.get("title"), (String)bookMap.get("header"), (String)bookMap.get("authors"), (String)bookMap.get("publisher"), (String)bookMap.get("year"), (String)bookMap.get("image"), (String)bookMap.get("url"), bookStructure);
            presentBookData();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Представление кэшированного варианта книги.
    **/
    public void presentBookData() {
        bookEntity = new SavedBookEntity(book.getId(), book.getTitle(), book.getHeader(), book.getAuthors(), book.getPublisher(), book.getYear(), book.getImage(), book.getUrl());
        recentlyViewedEntity = new RecentlyViewedEntity(book.getId(), book.getTitle(), book.getHeader(), book.getAuthors(), book.getPublisher(), book.getYear(), book.getImage(), book.getUrl());

        new Thread(() -> {
            if (recentlyViewedDao.isBookAlreadyExists(book.getId())) {
                recentlyViewedDao.delete(book.getId());
            }

            recentlyViewedDao.insert(recentlyViewedEntity);
        }).start();

        new Thread(() -> {
            if (bookDao.isBookAlreadyExists(book.getId())) {
                isFavorite = true;
                runOnUiThread(() -> activityMenu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_favorites_switch_remove_24)));
            }

            runOnUiThread(() -> activityMenu.getItem(1).setVisible(true));
        }).start();

        new Thread(() -> {
            if (downloadsDao.isBookAlreadyExists(book.getId())) {
                isDownloaded = true;
                runOnUiThread(() -> activityMenu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_book_download_switch_undo_24)));
            }

            runOnUiThread(() -> {
                if (isDownloadServiceDead() && !isOffline) {
                    activityMenu.getItem(0).setVisible(true);
                }
            });
        }).start();

        bookTopics = new ArrayList<>(book.getTopics());
        currentTopic = new ArrayList<>(book.getTopics());

        topicAdapter = new TopicAdapter(topic -> {
            if (setNextTopic(Objects.requireNonNull(topic.get("title")).toString())) {
                topicAdapter.setItems(currentTopic);
                path.add(Objects.requireNonNull(topic.get("title")).toString());
            } else {
                HashMap<String, String> bookInfo = new HashMap<>() {{
                    put("id", String.valueOf(book.getId()));
                    put("title", book.getTitle());
                    put("header", book.getHeader());
                    put("authors", book.getAuthors());
                    put("publisher", book.getPublisher());
                    put("year", book.getYear());
                    put("imageUrl", book.getImage());
                    put("url", book.getUrl());
                }};

                ArrayList<HashMap<String, Object>> tasksArray = new ArrayList<>(ObjectCastUtils.toArrayListOfStringObjectHashMap(topic.get("tasks")));

                startActivity(new Intent(this, TasksActivity.class)
                        .putExtra("topicTitle", Objects.requireNonNull(topic.get("title")).toString())
                        .putExtra("tasks", tasksArray)
                        .putExtra("path", path)
                        .putExtra("bookInfo", bookInfo)
                        .putExtra("isOffline", isOffline));
            }
        });

        runOnUiThread(() -> {
            bookTopicsList.setAdapter(topicAdapter);
            topicAdapter.setItems(book.getTopics());
            findViewById(R.id.topic_wip_indicator).setVisibility(View.GONE);
        });
    }

    /**
     * Проверка на активность сервиса загрузки (чтобы не было конкуренции потоков).
     *
     * @return если сервис загрузки не активен, то возвращаем true, иначе false
    **/
    public boolean isDownloadServiceDead() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DownloadService.class.getName().equals(service.service.getClassName())) {
                return false;
            }
        }

        return true;
    }
}
