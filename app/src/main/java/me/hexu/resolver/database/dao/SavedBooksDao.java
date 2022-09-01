package me.hexu.resolver.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import me.hexu.resolver.database.entities.SavedBookEntity;

@Dao
public interface SavedBooksDao {

    /**
     * Выбираем все сохранённые книги из таблицы базы данных.
     *
     * @return список сохранённых книг
    **/
    @Query("SELECT * FROM saved_books")
    List<SavedBookEntity> getAllSavedBooks();

    /**
     * Выбираем первые 2 сохранённые книги из таблицы базы данных.
     *
     * @return список из двух сохранённых книг
    **/
    @Query("SELECT * FROM saved_books LIMIT 2")
    List<SavedBookEntity> getSavedBooksPreview();

    /**
     * Добавляем новую запись в таблицу базы данных.
     *
     * @param book сущность сохраняемой книги для базы данных
    **/
    @Insert
    void insert(SavedBookEntity book);

    /**
     * Удаляем запись из таблицы базы данных по заданному id.
     *
     * @param id идентификатор книги из её сущности
    **/
    @Query("DELETE FROM saved_books WHERE id = :id")
    void delete(int id);

    /**
     * Проверяем по заданному id, существует ли книга в таблице базы данных.
     *
     * @param id идентификатор книги из её сущности
     *
     * @return если книга найдена, то возвращаем true, иначе false
    **/
    @Query("SELECT EXISTS(SELECT * FROM saved_books WHERE id = :id)")
    boolean isBookAlreadyExists(int id);

    /**
     * Удаление всех записей из таблицы базы данных.
    **/
    @Query("DELETE FROM saved_books")
    void wipe();
}
