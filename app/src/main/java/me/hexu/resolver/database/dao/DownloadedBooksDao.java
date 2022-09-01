package me.hexu.resolver.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import me.hexu.resolver.database.entities.DownloadedBookEntity;

@Dao
public interface DownloadedBooksDao {

    /**
     * Выбираем все загруженные книги из таблицы базы данных.
     *
     * @return список загруженных книг
    **/
    @Query("SELECT * FROM downloaded_books")
    List<DownloadedBookEntity> getDownloads();

    /**
     * Добавляем новую запись в таблицу базы данных.
     *
     * @param book сущность загружаемой книги для базы данных
    **/
    @Insert
    void insert(DownloadedBookEntity book);

    /**
     * Удаляем запись из таблицы базы данных по заданному id.
     *
     * @param id идентификатор книги из её сущности
    **/
    @Query("DELETE FROM downloaded_books WHERE id = :id")
    void delete(int id);

    /**
     * Проверяем по заданному id, существует ли книга в таблице базы данных.
     *
     * @param id идентификатор книги из её сущности
     *
     * @return если книга найдена, то возвращаем true, иначе false
    **/
    @Query("SELECT EXISTS(SELECT * FROM downloaded_books WHERE id = :id)")
    boolean isBookAlreadyExists(int id);

    /**
     * Финализация записи загруженной книги в таблице базы данных.
     *
     * @param id идентификатор книги из её сущности
    **/
    @Query("UPDATE downloaded_books SET isDownloadFinished = 1 WHERE id = :id")
    void finish(int id);

    /**
     * Удаление всех записей из таблицы базы данных.
    **/
    @Query("DELETE FROM downloaded_books")
    void wipe();
}
