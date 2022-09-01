package me.hexu.resolver.utils;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import me.hexu.resolver.App;

public class FileUtils {

    /**
     * Чтение из файла хэш-карты.
     *
     * @param path путь к файлу
     *
     * @return хэш-карта, где ключом является строка, а значение – объектом
     *
     * @throws IOException в случае ошибки при работе с файлом/потоком
     * @throws ClassNotFoundException в случае невозможности найти такой класс в исходных данных
    **/
    public static HashMap<String, Object> readMapObject(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path));
        Object readObject = inputStream.readObject();
        inputStream.close();

        HashMap<String, Object> result = new HashMap<>(((HashMap<?, ?>) readObject).size());

        for (HashMap.Entry<?, ?> entry : ((HashMap<?, ?>) readObject).entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }

        return result;
    }

    /**
     * Запись в файл последовательности байт по указанному пути.
     *
     * @param data последовательность байт для записи
     * @param path путь к файлу, в который произойдет запись
     *
     * @throws IOException в случае ошибки при работе с файлом/потоком
    **/
    public static void writeRawObject(byte[] data, String path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        outputStream.write(data);
        outputStream.close();
    }

    /**
     * Запись в файл хэш-карты по указанному пути.
     *
     * @param map хэш-карта для записи
     * @param path путь к файлу, в который произойдет запись
     *
     * @throws IOException в случае ошибки при работе с файлом/потоком
    **/
    public static void writeMapObject(HashMap<String, Object> map, String path) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path));
        outputStream.writeObject(map);
        outputStream.close();
    }

    /**
     * Соединение массива строк в путь файловой системы.
     *
     * @param toJoin массив соединяемых строк
     * @param isUsingFileProtocol если значение true, то будет присоединен протокол файла (file://)
     *
     * @return путь файловой системы (возможно содержащий протокол файла)
    **/
    public static String joinWithSeparator(String[] toJoin, boolean isUsingFileProtocol) {
        return isUsingFileProtocol ? "file://".concat(String.join(File.separator, toJoin)) : String.join(File.separator, toJoin);
    }

    /**
     * Удааление папки по её объекту.
     *
     * @param dir объект папки файловой системы
     * @param recursively если значение true, то будет использован рекурсивный метод
    **/
    public static void deleteDirectory(File dir, boolean recursively) {
        File[] files = dir.listFiles();

        if (recursively) {
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file, true);
                }
            }
        } else {
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        Log.i("FileUtils", String.format(App.getInstance().getResources().getConfiguration().locale, "Unable to delete file: %s", file.getName()));
                    }
                }
            }
        }

        if (!App.getInstance().getFilesDir().equals(dir)) {
            if (!dir.delete()) {
                Log.i("FileUtils", String.format(App.getInstance().getResources().getConfiguration().locale, "Unable to delete directory: %s", dir.getName()));
            }
        }
    }

    /**
     * Удаление всех файлов (вложенные папки игнорируются) из папки.
     *
     * @param dir объект папки файловой системы
    **/
    public static void deleteFilesInsideDirectory(File dir) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    Log.i("FileUtils", String.format(App.getInstance().getResources().getConfiguration().locale, "Unable to delete file: %s", file.getName()));
                }
            }
        }
    }
}
