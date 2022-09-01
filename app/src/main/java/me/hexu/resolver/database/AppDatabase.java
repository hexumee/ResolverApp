package me.hexu.resolver.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import me.hexu.resolver.database.dao.DownloadedBooksDao;
import me.hexu.resolver.database.dao.RecentlyViewedDao;
import me.hexu.resolver.database.dao.SavedBooksDao;
import me.hexu.resolver.database.dao.SavedTasksDao;
import me.hexu.resolver.database.entities.DownloadedBookEntity;
import me.hexu.resolver.database.entities.RecentlyViewedEntity;
import me.hexu.resolver.database.entities.SavedBookEntity;
import me.hexu.resolver.database.entities.SavedTaskEntity;

@Database(entities = {RecentlyViewedEntity.class, SavedBookEntity.class, SavedTaskEntity.class, DownloadedBookEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Получаем объект доступа к недавно просмотренным книгам.
     *
     * @return Data Access Object к недавно просмотренным книгам
    **/
    public abstract RecentlyViewedDao getRecentlyViewedDao();

    /**
     * Получаем объект доступа к сохранённым книгам.
     *
     * @return Data Access Object к сохранённым книгам
    **/
    public abstract SavedBooksDao getSavedBooksDao();

    /**
     * Получаем объект доступа к сохранённым задачам.
     *
     * @return Data Access Object к сохранённым задачам
    **/
    public abstract SavedTasksDao getSavedTasksDao();

    /**
     * Получаем объект доступа к загруженным книгам.
     *
     * @return Data Access Object к загруженным книгам
    **/
    public abstract DownloadedBooksDao getDownloadedBooksDao();
}
