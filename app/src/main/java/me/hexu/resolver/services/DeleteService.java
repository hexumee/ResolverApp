package me.hexu.resolver.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import androidx.core.app.NotificationCompat;
import java.io.File;
import me.hexu.resolver.R;
import me.hexu.resolver.utils.FileUtils;

public class DeleteService extends Service {
    private int bookId;      // Идентификатор удаляемой книгн
    private File bookDir;    // Папка удаляемой книгн
    private NotificationCompat.Builder builder;
    private NotificationManager manager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Начиная с Android 8, нужно создавать канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(getString(R.string.service_delete_name), getString(R.string.service_delete_name), NotificationManager.IMPORTANCE_LOW));
            builder = new NotificationCompat.Builder(this, getString(R.string.service_delete_name));
        } else {
            builder = new NotificationCompat.Builder(this, "");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.bookId = intent.getIntExtra("bookId", 0);
        this.bookDir = new File(FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), String.valueOf(intent.getIntExtra("bookId", 0)) }, false));
        this.manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Запускаем сервис с уведомлением (т.н. сервис переднего плана)
        startForeground(this.bookId, builder.build());

        // Создаём и запускаем новый поток, присоединяя его к главному потоку (нам нужен доступ к UI)
        HandlerThread thread = new HandlerThread("deleteThread");
        thread.start();

        new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                deleteBook(bookDir);
            }
        }.sendMessage(new Message());

        return START_NOT_STICKY;    // START_NOT_STICKY означает, что сервис не будет перезапущен после его завершения системой (например, при нехватке ресурсов)
    }

    private void deleteBook(File dir) {
        // "Строим" уведомление и выводим его в центр уведомлений
        builder.setContentTitle(getString(R.string.service_delete_notification_title)).setSmallIcon(R.drawable.ic_delete_notification);
        manager.notify(this.bookId, builder.build());

        // Рекурсивно удаляем папку книги
        FileUtils.deleteDirectory(dir, true);

        // Останавливаем сервис переднего плана и завершаем работу
        stopForeground(true);
        stopSelf();

        // Оповещаем приложение о том, что удаление завершено
        sendBroadcast(new Intent().setAction("ACTION_DELETE_COMPLETE"));
    }
}
