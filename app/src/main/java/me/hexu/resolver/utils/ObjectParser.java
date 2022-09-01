package me.hexu.resolver.utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import me.hexu.resolver.datamodels.BookFullDataModel;

public class ObjectParser {

    /**
     * Парсинг JSON-объекта книги.
     *
     * @param book книга в виде JSON-объекта
     *
     * @return полная модель данных книги (информация о ней и структура)
     *
     * @throws JSONException в случае отсутствия объекта/массива в объекте или ключа в массиве
    **/
    public static BookFullDataModel getParsedBook(JSONObject book) throws JSONException {
        int bookId = book.getJSONObject("book").getInt("id");
        String bookTitle = book.getJSONObject("book").getString("title");
        String bookHeader = book.getJSONObject("book").getString("header");
        String bookAuthors = JsonUtils.joinElements(book.getJSONObject("book").getJSONArray("authors"));
        String bookPublisher = book.getJSONObject("book").getString("publisher");
        String bookYear = book.getJSONObject("book").getString("year");
        String bookImage = book.getJSONObject("book").getJSONObject("cover").getString("url");
        String bookUrl = book.getJSONObject("book").getString("url");

        Map<String, Object> map = JsonUtils.jsonToMap(book);
        ArrayList<HashMap<String, Object>> bookStructure = new ArrayList<>(ObjectCastUtils.toArrayListOfStringObjectHashMap(map.get("structure")));

        return new BookFullDataModel(bookId, bookTitle, bookHeader, bookAuthors, bookPublisher, bookYear, bookImage, bookUrl, bookStructure);
    }
}
