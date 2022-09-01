package me.hexu.resolver.datamodels;

public class TaskPreviewDataModel {
    private final String title;        // Заголовок
    private final String header;       // Подзаголовок
    private final String authors;      // Авторы
    private final String publisher;    // Издательство
    private final String year;         // Год издания
    private final String image;        // URL обложки
    private final String url;          // URL книги
    private final String path;         // Путь до задачи
    private final String taskTitle;    // Заголовок задачи
    private final String taskUrl;      // URL задачи

    public TaskPreviewDataModel(String title, String header, String authors, String publisher, String year, String image, String url, String path, String taskTitle, String taskUrl) {
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

    public String getPath() {
        return path;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskUrl() {
        return taskUrl;
    }
}
