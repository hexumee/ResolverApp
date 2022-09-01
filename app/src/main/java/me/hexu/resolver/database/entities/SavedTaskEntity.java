package me.hexu.resolver.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_tasks")
public class SavedTaskEntity {

    /**
     * Ключ: идентификатор книги
    **/
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final String title;        // Заголовок
    public final String header;       // Подзаголовок
    public final String authors;      // Авторы
    public final String publisher;    // Издательство
    public final String year;         // Год издания
    public final String image;        // URL обложки
    public final String url;          // URL книги
    public final String path;         // Путь до задачи
    public final String taskTitle;    // Заголовок задачи
    public final String taskUrl;      // URL задачи

    public SavedTaskEntity(String title, String header, String authors, String publisher, String year, String image, String url, String path, String taskTitle, String taskUrl) {
        this.title = title;
        this.header = header;
        this.authors = authors;
        this.publisher = publisher;
        this.year = year;
        this.image = image;
        this.url = url;
        this.path = path;
        this.taskTitle = taskTitle;
        this.taskUrl = taskUrl;
    }
}
