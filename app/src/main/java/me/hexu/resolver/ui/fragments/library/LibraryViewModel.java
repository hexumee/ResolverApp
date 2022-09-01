package me.hexu.resolver.ui.fragments.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.App;
import me.hexu.resolver.database.dao.SavedBooksDao;
import me.hexu.resolver.database.dao.SavedTasksDao;
import me.hexu.resolver.database.entities.SavedBookEntity;
import me.hexu.resolver.database.entities.SavedTaskEntity;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.datamodels.TaskPreviewDataModel;

public class LibraryViewModel extends ViewModel {
    private final MutableLiveData<List<BookPreviewDataModel>> mSavedBooks;    // Список сохранённых книг
    private final MutableLiveData<List<TaskPreviewDataModel>> mSavedTasks;    // Список сохранённых задач
    private final SavedBooksDao mSavedBooksDao;                               // Data Access Object к сохранённым книгам
    private final SavedTasksDao mSavedTasksDao;                               // Data Access Object к сохранённым задачам
    private final MutableLiveData<Boolean> isBooksPreviewExpandable;          // Переменная, определяющая будет ли видна кнопка для показа полного списка кинг
    private final MutableLiveData<Boolean> isTasksPreviewExpandable;          // Переменная, определяющая будет ли видна кнопка для показа полного списка задач

    public LibraryViewModel() {
        mSavedBooks = new MutableLiveData<>();
        mSavedTasks = new MutableLiveData<>();
        mSavedBooksDao = App.getInstance().getDatabase().getSavedBooksDao();
        mSavedTasksDao = App.getInstance().getDatabase().getSavedTasksDao();
        isBooksPreviewExpandable = new MutableLiveData<>();
        isTasksPreviewExpandable = new MutableLiveData<>();
    }

    public LiveData<List<BookPreviewDataModel>> getSavedBooks() {
        new Thread(() -> {
           ArrayList<BookPreviewDataModel> result = new ArrayList<>();
           List<SavedBookEntity> savedBookEntities = mSavedBooksDao.getSavedBooksPreview();

            for (SavedBookEntity entity : savedBookEntities) {
                result.add(new BookPreviewDataModel(entity.id, entity.title, entity.header, entity.authors, entity.publisher, entity.year, entity.image, entity.url));
            }

            mSavedBooks.postValue(result);

            if (mSavedBooksDao.getAllSavedBooks().size() > 2) {
                isBooksPreviewExpandable.postValue(true);
            } else {
                isBooksPreviewExpandable.postValue(false);
            }
        }).start();

        return mSavedBooks;
    }

    public LiveData<List<TaskPreviewDataModel>> getSavedTasks() {
        new Thread(() -> {
           ArrayList<TaskPreviewDataModel> result = new ArrayList<>();
           List<SavedTaskEntity> savedTaskEntities = mSavedTasksDao.getSavedTasksPreview();

            for (SavedTaskEntity entity : savedTaskEntities) {
                result.add(new TaskPreviewDataModel(entity.title, entity.header, entity.authors, entity.publisher, entity.year, entity.image, entity.url, entity.path, entity.taskTitle, entity.taskUrl));
            }

            mSavedTasks.postValue(result);

            if (mSavedTasksDao.getAllSavedTasks().size() > 2) {
                isTasksPreviewExpandable.postValue(true);
            } else {
                isTasksPreviewExpandable.postValue(false);
            }
        }).start();

        return mSavedTasks;
    }

    public LiveData<Boolean> getBooksPreviewExpandability() {
        return isBooksPreviewExpandable;
    }

    public LiveData<Boolean> getTasksPreviewExpandability() {
        return isTasksPreviewExpandable;
    }

    public void updateView() {
        getSavedBooks();
        getSavedTasks();
    }
}
