package me.hexu.resolver.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recently_viewed")
public class RecentlyViewedEntity {

    /**
     * Ключ: внутренний идентификатор
    **/
    @PrimaryKey(autoGenerate = true)
    public int internalId;

    public final int id;              // Идентификатор книги
    public final String title;        // Заголовок
    public final String header;       // Подзаголовок
    public final String authors;      // Авторы
    public final String publisher;    // Издательство
    public final String year;         // Год издания
    public final String image;        // URL обложки
    public final String url;          // URL книги

    public RecentlyViewedEntity(int id, String title, String header, String authors, String publisher, String year, String image, String url) {
        this.id = id;
        this.title = title;
        this.header = header;
        this.authors = authors;
        this.publisher = publisher;
        this.year = year;
        this.image = image;
        this.url = url;
    }
}
