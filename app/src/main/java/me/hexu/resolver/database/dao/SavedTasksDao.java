package me.hexu.resolver.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import me.hexu.resolver.database.entities.SavedTaskEntity;

@Dao
public interface SavedTasksDao {

    /**
     * Выбираем все сохранённые задачи из таблицы базы данных.
     *
     * @return список сохранённых задачи
    **/
    @Query("SELECT * FROM saved_tasks")
    List<SavedTaskEntity> getAllSavedTasks();

    /**
     * Выбираем первые 2 сохранённые задачи из таблицы базы данных.
     *
     * @return список из двух сохранённых задач
    **/
    @Query("SELECT * FROM saved_tasks LIMIT 2")
    List<SavedTaskEntity> getSavedTasksPreview();

    /**
     * Добавляем новую запись в таблицу базы данных.
     *
     * @param task сущность сохраняемой задачи для базы данных
    **/
    @Insert
    void insert(SavedTaskEntity task);

    /**
     * Удаляем запись из таблицы базы данных по заданному url.
     *
     * @param url URL задачи из её сущности
    **/
    @Query("DELETE FROM saved_tasks WHERE taskUrl = :url")
    void delete(String url);

    /**
     * Проверяем по заданному url, существует ли задача в таблице базы данных.
     *
     * @param url URL задачи из её сущности
     *
     * @return если книга найдена, то возвращаем true, иначе false
    **/
    @Query("SELECT EXISTS(SELECT * FROM saved_tasks WHERE taskUrl = :url)")
    boolean isTaskAlreadyExists(String url);

    /**
     * Удаление всех записей из таблицы базы данных.
    **/
    @Query("DELETE FROM saved_tasks")
    void wipe();
}
