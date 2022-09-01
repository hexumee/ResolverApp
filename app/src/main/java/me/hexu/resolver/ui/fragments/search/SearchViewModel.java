package me.hexu.resolver.ui.fragments.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import me.hexu.resolver.App;
import me.hexu.resolver.R;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.utils.ApiRequest;
import me.hexu.resolver.utils.JsonUtils;

public class SearchViewModel extends ViewModel {
    private final MutableLiveData<List<BookPreviewDataModel>> mBooks;    // Список книг, подходящих под критерии
    private final MutableLiveData<List<String>> mClasses;                // Список классов с сервера
    private final MutableLiveData<List<String>> mSubjects;               // Список предметов с сервера
    private final MutableLiveData<Integer> mClassSelectorPosition;       // Индекс выбранного класса в списке
    private final MutableLiveData<Integer> mSubjectSelectorPosition;     // Индекс выбранного предмета в списке
    private final MutableLiveData<Boolean> mSearchEngineUsesQuery;       // Переменная, определяющая использование поля ввода вместо выпадающих списков

    public SearchViewModel() {
        mBooks = new MutableLiveData<>();
        mClasses = new MutableLiveData<>();
        mSubjects = new MutableLiveData<>();
        mClassSelectorPosition = new MutableLiveData<>(0);
        mSubjectSelectorPosition = new MutableLiveData<>(0);
        mSearchEngineUsesQuery = new MutableLiveData<>(false);

        ApiRequest.getSearchBase(searchBase -> {
            ArrayList<String> resultClasses = new ArrayList<>();
            ArrayList<String> resultSubjects = new ArrayList<>(Objects.requireNonNull(searchBase.get("subjects")));

            for (String klass : Objects.requireNonNull(searchBase.get("classes"))) {
                resultClasses.add(String.format(App.getInstance().getResources().getConfiguration().locale, App.getInstance().getString(R.string.search_classes_list_template), klass));
            }

            mClasses.postValue(resultClasses);
            mSubjects.postValue(resultSubjects);
        });
    }

    public LiveData<List<BookPreviewDataModel>> getResults() {
        return mBooks;
    }

    public LiveData<List<String>> getClassesList() {
        return mClasses;
    }

    public LiveData<List<String>> getSubjectsList() {
        return mSubjects;
    }

    public LiveData<Integer> getClassSelectorPosition() {
        return mClassSelectorPosition;
    }

    public LiveData<Integer> getSubjectSelectorPosition() {
        return mSubjectSelectorPosition;
    }

    public void setClassSelectorPosition(int value) {
        mClassSelectorPosition.postValue(value);
    }

    public void setSubjectSelectorPosition(int value) {
        mSubjectSelectorPosition.postValue(value);
    }

    public LiveData<Boolean> getSearchEngineType() {
        return mSearchEngineUsesQuery;
    }

    public void setSearchEngineType(boolean value) {
        mSearchEngineUsesQuery.postValue(value);
    }

    /**
     * Поиск по базе книг с использованием ключевых слов.
     * Примечание: поиск по списку слов в приоритете.
     *
     * @param keyword одно ключевое слово
     * @param keywords список ключевых слов
    **/
    public void doSearch(String keyword, List<String> keywords) {
        new Thread(() -> {
            ArrayList<BookPreviewDataModel> result = new ArrayList<>();

            try {
                JSONArray books = new JSONObject(ApiRequest.requestAsync(ApiRequest.API_ENDPOINT)).getJSONArray("books");

                for (int i = 0; i < books.length(); i++) {
                    JSONObject item = books.getJSONObject(i);
                    List<String> itemKeywords = Arrays.asList(item.getString("search_keywords").split(" "));

                    if ((keywords != null && hasMatchedAllKeywords(itemKeywords, keywords) || (keyword != null && itemKeywords.contains(keyword)))) {
                        int id = item.getInt("id");
                        String title = item.getString("title");
                        String header = item.getString("header");
                        String authors = JsonUtils.joinElements(item.getJSONArray("authors"));
                        String publisher = item.getString("publisher");
                        String year = item.getString("year");
                        String image = item.getJSONObject("cover").getString("url");
                        String url = item.getString("url");

                        result.add(new BookPreviewDataModel(id, title, header, authors, publisher, year, image, url));
                    }
                }
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
            }

            mBooks.postValue(result);
        }).start();
    }

    /**
     * Поиск по базе книг с использованием идентификаторов.
     *
     * @param klass идентификатор класса
     * @param subjectId идентификатор предмета
    **/
    public void doSearch(Integer klass, Integer subjectId) {
        new Thread(() -> {
            ArrayList<BookPreviewDataModel> result = new ArrayList<>();

            try {
                JSONArray books = new JSONObject(ApiRequest.requestAsync(ApiRequest.API_ENDPOINT)).getJSONArray("books");

                for (int i = 0; i < books.length(); i++) {
                    JSONObject item = books.getJSONObject(i);
                    int itemSubjectId = item.getInt("subject_id");
                    List<Object> itemClasses = JsonUtils.toList(item.getJSONArray("classes"));

                    if ((klass != 0 && subjectId != 0 && itemSubjectId == subjectId && itemClasses.contains(klass)) ||
                        (subjectId == 0 && itemClasses.contains(klass)) ||
                        (klass == 0 && subjectId != 0 && itemSubjectId == subjectId)
                    ) {
                        int id = item.getInt("id");
                        String title = item.getString("title");
                        String header = item.getString("header");
                        String authors = JsonUtils.joinElements(item.getJSONArray("authors"));
                        String publisher = item.getString("publisher");
                        String year = item.getString("year");
                        String image = item.getJSONObject("cover").getString("url");
                        String url = item.getString("url");

                        result.add(new BookPreviewDataModel(id, title, header, authors, publisher, year, image, url));
                    }
                }
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
            }

            mBooks.postValue(result);
        }).start();
    }

    /**
     * Проверка на содержание списка слов для поиска в списке ключевых слов книги.
     *
     * @param bookKeywords список ключевых слов книги
     * @param userKeywords список слов для поиска внутри списка ключевых слов
     *
     * @return если userKeywords полностью содержится в bookKeywords, то возвращаем true, иначе false
    **/
    private boolean hasMatchedAllKeywords(List<String> bookKeywords, List<String> userKeywords) {
        int matchesCount = 0;

        for (int i = 0; i < userKeywords.size(); i++) {
            if (bookKeywords.contains(userKeywords.get(i))) {
                matchesCount++;
            }
        }

        return matchesCount == userKeywords.size();
    }
}
