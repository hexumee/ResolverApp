package me.hexu.resolver.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonUtils {

    /**
     * Соединение элементов массива запятыми.
     *
     * @param array массив элементов, которые нужно соединить
     *
     * @return строка, содержащая все элементы массива, которые соединены запятой
     *
     * @throws JSONException в случае отсутствия ключа в массиве
    **/
    public static String joinElements(JSONArray array) throws JSONException {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < array.length(); i++) {
            result.append(array.getString(i));

            if (i+1 != array.length()) {
                result.append(", ");
            }
        }

        return result.toString();
    }

    /**
     * Преобразование объекта в словарь.
     *
     * @param object преобразуемый объект
     *
     * @return словарь, где ключом является строка, а значение – объектом
     *
     * @throws JSONException вызываемая функция toMap() бросает это исключение
    **/
    public static Map<String, Object> jsonToMap(JSONObject object) throws JSONException {
        Map<String, Object> mapped = new HashMap<>();

        if (object != JSONObject.NULL) {
            mapped = toMap(object);
        }

        return mapped;
    }

    /**
     * Преобразование объекта в словарь (рекурсивно).
     *
     * @param object преобразуемый объект
     *
     * @return словарь, где ключом является строка, а значение – объектом
     *
     * @throws JSONException в случае отсутствия ключа в объекте
    **/
    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> it = object.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            map.put(key, value);
        }

        return map;
    }

    /**
     * Преобразование массива в список (рекурсивно).
     *
     * @param array преобразуемый массив
     *
     * @return список объектов, которые могут быть массивом или объектом
     *
     * @throws JSONException в случае отсутствия ключа в массиве
    **/
    public static ArrayList<Object> toList(JSONArray array) throws JSONException {
        ArrayList<Object> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            list.add(value);
        }

        return list;
    }
}
