package me.hexu.resolver.utils;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import me.hexu.resolver.interfaces.callbacks.ISearchBaseResponseCallback;
import me.hexu.resolver.interfaces.callbacks.IProbeCallback;
import me.hexu.resolver.interfaces.callbacks.IRawResponseCallback;
import me.hexu.resolver.interfaces.callbacks.IRequestCallback;

public class ApiRequest {
    public static final String API_ENDPOINT = "https://gdz-ru.com";    // URL сервер, на который будут посылаться запросы
    private static final String USER_AGENT = "okhttp/4.10.0";          // Требуется для обхода ошибки 403 Forbidden

    /**
     * Производим асинхронное обращение к заданному URL и возвращаем ответ.
     *
     * @param gotUrl URL, к которому будем обращаться
     *
     * @return строка, содержащая ответ сервера
     *
     * @throws InterruptedException в случае преждевременной остановки потока
    **/
    public static String requestAsync(String gotUrl) throws InterruptedException {
        final AtomicReference<HttpURLConnection> urlConnection = new AtomicReference<>();    // Сетевое поключение
        final AtomicReference<BufferedReader> bufferedReader = new AtomicReference<>();      // Чтение с сетевого потока
        final AtomicReference<String> responseLine = new AtomicReference<>();                // Ответ с сервера
        StringBuilder response = new StringBuilder();
        ExecutorService executor = Executors.newSingleThreadExecutor();                      // Исполнитель потоков (один поток)

        executor.execute(() -> {
            // Если по какой-то причине URL не начинается с API_ENDPOINT, то нам следует это исправить
            String correctUrl = gotUrl.startsWith(API_ENDPOINT) ? gotUrl : API_ENDPOINT.concat(gotUrl);

            try {
                urlConnection.set((HttpURLConnection) new URL(correctUrl).openConnection());
                urlConnection.get().setRequestMethod("GET");
                urlConnection.get().setRequestProperty("User-Agent", USER_AGENT);
                bufferedReader.set(new BufferedReader(new InputStreamReader(urlConnection.get().getInputStream())));

                responseLine.set(bufferedReader.get().readLine());
                while (responseLine.get() != null) {
                    response.append(responseLine.get());
                    responseLine.set(bufferedReader.get().readLine());
                }

                bufferedReader.get().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Завершаем исполнитель
        executor.shutdown();

        // Если через 5 минут исполнитель так и не завершился
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            Log.e("ApiRequest", "Still waiting for response...");
        }

        return response.toString();
    }

    /**
     * Производим асинхронное обращение к заданному URL.
     *
     * @param gotUrl URL, к которому будем обращаться
     *
     * @return если обращение к сайту произведено успешно, то возвращаем true, иначе false
     *
     * @throws InterruptedException в случае преждевременной остановки потока
    **/
    public static boolean probeAsync(String gotUrl) throws InterruptedException {
        final AtomicReference<HttpURLConnection> urlConnection = new AtomicReference<>();    // Сетевое поключение
        final AtomicInteger responseCode = new AtomicInteger();                              // HTTP-код ответа с сервера
        final AtomicReference<String> probableUrl = new AtomicReference<>();
        AtomicBoolean isFileProtocol = new AtomicBoolean(false);                    // Переменная, определяющая имеет ли ссылка протокол файла (file://)
        ExecutorService executor = Executors.newSingleThreadExecutor();                      // Исполнитель потоков (один поток)

        executor.execute(() -> {
            // Если ссылка использует протокол HTTP
            if (!gotUrl.startsWith("file")) {
                // Если по какой-то причине URL не начинается с API_ENDPOINT, то нам следует это исправить
                probableUrl.set(gotUrl.startsWith(API_ENDPOINT) ? gotUrl : API_ENDPOINT.concat(gotUrl));

                try {
                    urlConnection.set((HttpURLConnection) new URL(probableUrl.get()).openConnection());
                    urlConnection.get().setRequestMethod("GET");
                    urlConnection.get().setRequestProperty("User-Agent", USER_AGENT);
                    responseCode.set(urlConnection.get().getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                isFileProtocol.set(true);
            }
        });

        // Завершаем исполнитель
        executor.shutdown();

        // Если через 5 минут исполнитель так и не завершился
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            Log.e("ApiRequest", "Still waiting for response...");
        }

        return responseCode.get() == HttpURLConnection.HTTP_OK || isFileProtocol.get();
    }

    /**
     * Производим асинхронное обращение к заданному URL и возвращаем ответ в виде последовательности байт.
     *
     * @param gotUrl URL, к которому будем обращаться
     *
     * @return ответ сервера в виде последовательности байт
     *
     * @throws InterruptedException в случае преждевременной остановки потока
    **/
    public static byte[] rawResponseRequestAsync(String gotUrl) throws InterruptedException {
        final AtomicReference<HttpURLConnection> urlConnection = new AtomicReference<>();    // Сетевое поключение
        final AtomicReference<InputStream> stream = new AtomicReference<>();                 // Чтение с сетевого потока
        ExecutorService executor = Executors.newSingleThreadExecutor();                      // Исполнитель потоков (один поток)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();                    // Поток вывода в объект

        executor.execute(() -> {
            // Если по какой-то причине URL не начинается с API_ENDPOINT, то нам следует это исправить
            String correctUrl = gotUrl.startsWith(API_ENDPOINT) ? gotUrl : API_ENDPOINT.concat(gotUrl);

            byte[] chunk = new byte[4096];    // Один блок данных
            int bytesRead;                    // Количество прочитанных байт

            try {
                urlConnection.set((HttpURLConnection) new URL(correctUrl).openConnection());
                urlConnection.get().setRequestMethod("GET");
                urlConnection.get().setRequestProperty("User-Agent", USER_AGENT);
                stream.set(urlConnection.get().getInputStream());

                // Пока есть что читать – читаем и записываем
                while ((bytesRead = stream.get().read(chunk)) > 0) {
                    outputStream.write(chunk, 0, bytesRead);
                }

                outputStream.close();
                stream.get().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Завершаем исполнитель
        executor.shutdown();

        // Если через 5 минут исполнитель так и не завершился
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            Log.e("ApiRequest", "Still waiting for response...");
        }

        return outputStream.toByteArray();
    }

    /**
     * Производим асинхронное обращение к заданному URL и направляем ответ в функцию обратного вызова.
     *
     * @param callback функция обратного вызова, к которой будет выполнено обращение после выполнения основной задачи
     * @param url URL, к которому будем обращаться
    **/
    public static void requestUrl(IRequestCallback callback, String url) {
        new Thread(() -> {
            String result = "";

            try {
                result = requestAsync(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callback.onResponseReceived(result);
        }).start();
    }

    /**
     * Производим асинхронное обращение к заданному URL и сообщаем о результате в функцию обратного вызова.
     *
     * @param callback функция обратного вызова, к которой будет выполнено обращение после выполнения основной задачи
     * @param url URL, к которому будем обращаться
    **/
    public static void probeUrl(IProbeCallback callback, String url) {
        new Thread(() -> {
            boolean result = false;

            try {
                result = probeAsync(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callback.onProbeResult(result);
        }).start();
    }

    /**
     * Производим асинхронное обращение к заданному URL и направляем ответ в виде последовательности байт в функцию обратного вызова.
     *
     * @param callback функция обратного вызова, к которой будет выполнено обращение после выполнения основной задачи
     * @param url URL, к которому будем обращаться
    **/
    public static void rawResponseRequestUrl(IRawResponseCallback callback, String url) {
        new Thread(() -> {
            byte[] result = null;

            try {
                result = rawResponseRequestAsync(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callback.onResponseReceived(result);
        }).start();
    }

    /**
     * Возвращаем задачу потока, которая произведёт асинхронное обращение к заданному URL и направит ответ в функцию обратного вызова.
     *
     * @param callback функция обратного вызова, к которой будет выполнено обращение после выполнения основной задачи
     * @param url URL, к которому будем обращаться
     *
     * @return задача потока для последующего выполнения
    **/
    public static Runnable requestRunnable(IRequestCallback callback, String url) {
        return () -> {
            String result = "";

            try {
                result = requestAsync(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callback.onResponseReceived(result);
        };
    }

    /**
     * Получаем с сервера классы и предметы для заполнения соответствующих выпадающих списков.
     *
     * @param callback функция обратного вызова, к которой будет выполнено обращение после выполнения основной задачи
    **/
    public static void getSearchBase(ISearchBaseResponseCallback callback) {
        new Thread(() -> {
            HashMap<String, ArrayList<String>> result = new HashMap<>();
            ArrayList<String> classesList = new ArrayList<>();
            ArrayList<String> subjectsList = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(requestAsync(API_ENDPOINT));
                JSONArray classes = jsonObject.getJSONArray("classes");
                JSONArray subjects = jsonObject.getJSONArray("subjects");

                for (int i = 0; i < classes.length(); i++) {
                    classesList.add(classes.getJSONObject(i).getString("title"));
                }

                for (int i = 0; i < subjects.length(); i++) {
                    subjectsList.add(subjects.getJSONObject(i).getString("title"));
                }
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
            }

            result.put("classes", classesList);
            result.put("subjects", subjectsList);

            callback.onResponseReceived(result);
        }).start();
    }
}
