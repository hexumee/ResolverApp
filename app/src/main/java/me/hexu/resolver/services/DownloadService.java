package me.hexu.resolver.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.hexu.resolver.App;
import me.hexu.resolver.R;
import me.hexu.resolver.database.dao.DownloadedBooksDao;
import me.hexu.resolver.datamodels.BookFullDataModel;
import me.hexu.resolver.datamodels.TaskImageDataModel;
import me.hexu.resolver.utils.ApiRequest;
import me.hexu.resolver.utils.FileUtils;
import me.hexu.resolver.utils.ObjectCastUtils;

public class DownloadService extends Service {
    private DownloadedBooksDao downloadsDao;                              // Data Access Object к загруженным книгам
    private BookFullDataModel downloadableBook;                           // Модель данных загружаемой книги
    private String bookPath;                                              // Абсолютный путь к папке книги
    private File bookDir;                                                 // Папка удаляемой книгн
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private HandlerThread downloadThread;                                 // Поток, в котором происходит загрузка
    private final HashMap<String, Object> book = new HashMap<>();         // Информация о книге
    private final HashMap<String, Object> tasksList = new HashMap<>();    // Список задач книги
    private final CountDownLatch latch = new CountDownLatch(1);           // Затвор с отсчётом – когда значение счётчика становится равным 0, начинает выполнятся код после await();
    private boolean isStoppedManually = false;                            // Переменная, определяющая остановку сервиса пользователем или по завершении

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Начиная с Android 8, нужно создавать канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(getString(R.string.service_download_name), getString(R.string.service_download_name), NotificationManager.IMPORTANCE_LOW));
            builder = new NotificationCompat.Builder(this, getString(R.string.service_download_name));
        } else {
            builder = new NotificationCompat.Builder(this, "");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Если в действии интента описано, что нужно завершить сервис, то нужно сначала будет остановить поток загрузки (описано ниже)
        if (intent.getAction() != null && intent.getAction().equals("ACTION_STOP_SERVICE")) {
            isStoppedManually = true;
        } else {
            this.downloadsDao = App.getInstance().getDatabase().getDownloadedBooksDao();
            this.downloadableBook = (BookFullDataModel) intent.getSerializableExtra("book");
            this.bookPath = FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), String.valueOf(downloadableBook.getId()) }, false);
            this.bookDir = new File(bookPath);
            this.manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Начиная с Android 8, требуется передача в PendingIntent флага действия вместе с FLAG_IMMUTABLE (при этом начиная с Android 6, данное действие носит рекомендательный характер)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.builder.addAction(
                        R.drawable.ic_cancel_24,
                        getString(R.string.cancel),
                        PendingIntent.getForegroundService(this, downloadableBook.getId(), new Intent(this, DownloadService.class).setAction("ACTION_STOP_SERVICE"),
                        (PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                );
            } else {
                this.builder.addAction(
                        R.drawable.ic_cancel_24,
                        getString(R.string.cancel),
                        PendingIntent.getService(this, downloadableBook.getId(), new Intent(this, DownloadService.class).setAction("ACTION_STOP_SERVICE"),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? (PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE) : PendingIntent.FLAG_CANCEL_CURRENT)
                );
            }

            // Запускаем сервис с уведомлением (т.н. сервис переднего плана)
            startForeground(downloadableBook.getId(), builder.build());

            // Создаём и запускаем новый поток, присоединяя его к главному потоку (нам нужен доступ к UI)
            downloadThread = new HandlerThread("downloadThread");
            downloadThread.start();

            new Handler(downloadThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    downloadBook();
                }
            }.sendMessage(new Message());
        }

        return START_NOT_STICKY;    // START_NOT_STICKY означает, что сервис не будет перезапущен после его завершения системой (например, при нехватке ресурсов)
    }

    public void downloadBook() {
        ArrayList<HashMap<String, Object>> bookTasks = new ArrayList<>();

        // "Строим" уведомление
        builder.setContentTitle(downloadableBook.getTitle()).setContentText(getString(R.string.service_download_preparing_notification_title)).setSmallIcon(R.drawable.ic_download_notification);

        // Если папка существует, то очищаем её
        if (!bookDir.mkdirs()) {
            FileUtils.deleteFilesInsideDirectory(bookDir);
        }

        book.putAll(new HashMap<>() {{
            put("id", downloadableBook.getId());
            put("title", downloadableBook.getTitle());
            put("header", downloadableBook.getHeader());
            put("authors", downloadableBook.getAuthors());
            put("publisher", downloadableBook.getPublisher());
            put("year", downloadableBook.getYear());
            put("image", downloadableBook.getImage());
            put("url", downloadableBook.getUrl());
            put("structure", downloadableBook.getTopics());
        }});

        for (HashMap<String, Object> entry : downloadableBook.getTopics()) {
            this.getTasks(bookTasks, entry);
        }

        this.getImages(bookTasks);

        try {
            latch.await();    // Ждём "открытия" затвора

            // Записываем в файлы результаты работы и финализируем запись в таблице базы данных
            FileUtils.writeMapObject(book, FileUtils.joinWithSeparator(new String[]{ bookPath, "book.map" }, false));
            FileUtils.writeMapObject(tasksList, FileUtils.joinWithSeparator(new String[]{ bookPath, "tasks.map" }, false));
            downloadsDao.finish(downloadableBook.getId());

            // Останавливаем сервис переднего плана и завершаем работу
            stopForeground(true);
            stopSelf();

            // Оповещаем приложение о том, что загрузка завершена
            sendBroadcast(new Intent().setAction("ACTION_DOWNLOAD_COMPLETE"));
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getTasks(ArrayList<HashMap<String, Object>> result, HashMap<String, Object> entry) {
        ArrayList<HashMap<String, Object>> entryTopics = ObjectCastUtils.toArrayListOfStringObjectHashMap(entry.get("topics"));

        if (entryTopics.size() > 0) {
            for (int i = 0; i < entryTopics.size(); i++) {
                HashMap<String, Object> innerTopics = entryTopics.get(i);
                ArrayList<HashMap<String, Object>> innerEntryTopics = ObjectCastUtils.toArrayListOfStringObjectHashMap(innerTopics.get("topics"));

                if (innerEntryTopics.size() > 0) {
                    getTasks(result, entryTopics.get(i));
                } else {
                    result.addAll(ObjectCastUtils.toArrayListOfStringObjectHashMap(entryTopics.get(i).get("tasks")));
                }
            }
        } else {
            result.addAll(ObjectCastUtils.toArrayListOfStringObjectHashMap(entry.get("tasks")));
        }
    }

    private void getImages(ArrayList<HashMap<String, Object>> tasks) {
        // Создаем пул потоков, который запускает их один за другим (максимальное количество одновременно работающих потоков равно 1)
        ExecutorService pool = Executors.newFixedThreadPool(1);

        // Сначала скачаем обложку книги
        pool.execute(() -> ApiRequest.rawResponseRequestUrl(rawResponse -> {
            try {
                String[] pathSplit = downloadableBook.getImage().split("/");
                FileUtils.writeRawObject(rawResponse, FileUtils.joinWithSeparator(new String[]{ bookPath, pathSplit[pathSplit.length-1] }, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, downloadableBook.getImage()));

        // Потом скачиваем все изображения из задач
        for (HashMap<String, Object> entry : tasks) {
            pool.execute(ApiRequest.requestRunnable(response -> {
                ArrayList<TaskImageDataModel> downloadableImages = new ArrayList<>();

                try {
                    JSONObject task = new JSONObject(response);
                    JSONArray editions = task.getJSONArray("editions");

                    for (int i = 0; i < editions.length(); i++) {
                        JSONObject edition = editions.getJSONObject(i);
                        JSONArray images = edition.getJSONArray("images");

                        for (int j = 0; j < images.length(); j++) {
                            JSONObject image = images.getJSONObject(j);
                            downloadableImages.add(new TaskImageDataModel(image.getString("url")));

                            // Индекс текущего изображения в задаче
                            int currentImageIndex = j;
                            ApiRequest.rawResponseRequestUrl(rawResponse -> {
                                try {
                                    String[] pathSplit = image.getString("url").split("/");
                                    FileUtils.writeRawObject(rawResponse, FileUtils.joinWithSeparator(new String[]{ bookPath, pathSplit[pathSplit.length-1] }, false));

                                    // Если это была последняя задача и изображение было последним, то декрементируем значение затвора
                                    if (tasks.indexOf(entry) == tasks.size()-1 && currentImageIndex == images.length()-1) {
                                        latch.countDown();
                                    }
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }, image.getString("url"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                tasksList.put((String) entry.get("url"), downloadableImages);

                // "Строим" уведомление и выводим его в центр уведомлений
                builder.setContentText(String.format(getResources().getConfiguration().locale, getString(R.string.service_download_progress_template), tasks.indexOf(entry)+1, tasks.size())).setProgress(tasks.size(), tasks.indexOf(entry)+1, false);
                manager.notify(downloadableBook.getId(), builder.build());

                // Если сервис был остановлен пользователем
                if (isStoppedManually) {
                    // Принудительно завершаем потоки из пула
                    pool.shutdownNow();

                    // Останавливаем сервис переднего плана и завершаем работу
                    stopForeground(true);
                    stopSelf();

                    // Оповещаем приложение о том, что загрузка отменена
                    sendBroadcast(new Intent().setAction("ACTION_DOWNLOAD_CANCELLED"));
                }
            }, (String) entry.get("url")));
        }
    }
}
