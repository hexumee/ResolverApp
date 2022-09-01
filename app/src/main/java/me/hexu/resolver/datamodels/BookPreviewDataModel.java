package me.hexu.resolver.datamodels;

public class BookPreviewDataModel {
    private final int id;              // Идентификатор книги
    private final String title;        // Заголовок
    private final String header;       // Подзаголовок
    private final String authors;      // Авторы
    private final String publisher;    // Издательство
    private final String year;         // Год издания
    private final String image;        // URL обложки
    private final String url;          // URL книги

    public BookPreviewDataModel(int id, String title, String header, String authors, String publisher, String year, String image, String url) {
        this.id = id;
        this.title = title;
        this.header = header;
        this.authors = authors;
        this.publisher = publisher;
        this.year = year;
        this.image = image;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public String getAuthors() {
        return authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getYear() {
        return year;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }
}
