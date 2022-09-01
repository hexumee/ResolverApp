package me.hexu.resolver.ui.fragments.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.App;
import me.hexu.resolver.database.dao.RecentlyViewedDao;
import me.hexu.resolver.database.entities.RecentlyViewedEntity;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.utils.GreetingMaker;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> mGreeting;                     // Приветствие
    private final MutableLiveData<List<BookPreviewDataModel>> mBooks;    // Список недавно просмотренных книг
    private final RecentlyViewedDao mRecentlyViewedDao;                  // Data Access Object к недавно просмотренным книгам
    private final MutableLiveData<Boolean> mIsPreviewExpandable;         // Переменная, определяющая будет ли видна кнопка для показа полного списка кинг

    public HomeViewModel() {
        mGreeting = new MutableLiveData<>();
        mBooks = new MutableLiveData<>();
        mRecentlyViewedDao = App.getInstance().getDatabase().getRecentlyViewedDao();
        mIsPreviewExpandable = new MutableLiveData<>();
    }

    public LiveData<String> getGreeting() {
        mGreeting.setValue(new GreetingMaker().getGreeting());

        return mGreeting;
    }

    public LiveData<List<BookPreviewDataModel>> getRecentlyOpened() {
        new Thread(() -> {
           ArrayList<BookPreviewDataModel> result = new ArrayList<>();
           List<RecentlyViewedEntity> recentlyViewedEntities = mRecentlyViewedDao.getRecentlyViewedPreview();

            for (RecentlyViewedEntity entity : recentlyViewedEntities) {
                result.add(new BookPreviewDataModel(entity.id, entity.title, entity.header, entity.authors, entity.publisher, entity.year, entity.image, entity.url));
            }

            mBooks.postValue(result);

            if (mRecentlyViewedDao.getAllRecentlyViewed().size() > 3) {
                mIsPreviewExpandable.postValue(true);
            } else {
                mIsPreviewExpandable.postValue(false);
            }
        }).start();

        return mBooks;
    }

    public LiveData<Boolean> getPreviewExpandability() {
        return mIsPreviewExpandable;
    }

    public void updateView() {
        getGreeting();
        getRecentlyOpened();
    }
}
