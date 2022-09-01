package me.hexu.resolver.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectCastUtils {

    /**
     * Преобразование объекта в список с хэш-картами.
     *
     * @param obj преобразуемый объект
     *
     * @return список с хэш-картами, где ключом является строка, а значение – объектом
    **/
    public static ArrayList<HashMap<String, Object>> toArrayListOfStringObjectHashMap(Object obj) {
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();

        for (Object objEntry: (ArrayList<?>) obj) {
            HashMap<String, Object> item = new HashMap<>();

            for (HashMap.Entry<?, ?> entry: ((HashMap<?, ?>) objEntry).entrySet()) {
                item.put(entry.getKey().toString(), entry.getValue());
            }

            result.add(item);
        }

        return result;
    }

    /**
     * Преобразование объекта в хэш-карту.
     *
     * @param obj преобразуемый объект
     *
     * @return хэш-карта, где ключ и значение являются строками
    **/
    public static HashMap<String, String> toStringStringHashMap(Object obj) {
        HashMap<String, String> result = new HashMap<>();

        for (HashMap.Entry<?, ?> entry : ((HashMap<?, ?>) obj).entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return result;
    }

    /**
     * Преобразование объекта в список со строками.
     *
     * @param obj преобразуемый объект
     *
     * @return список со строками
    **/
    public static ArrayList<String> toArrayListOfString(Object obj) {
        ArrayList<String> result = new ArrayList<>();

        for (Object item: (ArrayList<?>) obj) {
            result.add(item.toString());
        }

        return result;
    }
}
