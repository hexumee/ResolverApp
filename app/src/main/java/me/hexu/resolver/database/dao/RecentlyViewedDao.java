package me.hexu.resolver.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import me.hexu.resolver.database.entities.RecentlyViewedEntity;

@Dao
public interface RecentlyViewedDao {

    /**
     * Выбираем все недавно просмотренные книги из таблицы базы данных.
     *
     * @return список недавно просмотренных книг, отсортированный по убыванию
    **/
    @Query("SELECT * FROM recently_viewed ORDER BY internalId DESC")
    List<RecentlyViewedEntity> getAllRecentlyViewed();

    /**
     * Выбираем первые 3 недавно просмотренные книги из таблицы базы данных.
     *
     * @return список из трёх недавно просмотренных книг, отсортированный по убыванию
    **/
    @Query("SELECT * FROM recently_viewed ORDER BY internalId DESC LIMIT 3")
    List<RecentlyViewedEntity> getRecentlyViewedPreview();

    /**
     * Добавляем новую запись в таблицу базы данных.
     *
     * @param book сущность недавно просмотренной книги для базы данных
    **/
    @Insert
    void insert(RecentlyViewedEntity book);

    /**
     * Удаляем запись из таблицы базы данных по заданному id.
     *
     * @param id идентификатор книги из её сущности
    **/
    @Query("DELETE FROM recently_viewed WHERE id = :id")
    void delete(int id);

    /**
     * Проверяем по заданному id, существует ли книга в таблице базы данных.
     *
     * @param id идентификатор книги из её сущности
     *
     * @return если книга найдена, то возвращаем true, иначе false
    **/
    @Query("SELECT EXISTS(SELECT * FROM recently_viewed WHERE id = :id)")
    boolean isBookAlreadyExists(int id);

    /**
     * Удаление всех записей из таблицы базы данных.
    **/
    @Query("DELETE FROM recently_viewed")
    void wipe();
}
