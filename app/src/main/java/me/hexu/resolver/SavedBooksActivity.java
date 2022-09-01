package me.hexu.resolver;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.database.dao.SavedBooksDao;
import me.hexu.resolver.database.entities.SavedBookEntity;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.ui.adapters.BookAdapter;

public class SavedBooksActivity extends AppCompatActivity {
    private ProgressBar savedBooksProgressBar;    // Прогресс-бар (надо же как-то показать работу)
    private BookAdapter savedBooksAdapter;
    private SavedBooksDao savedBooksDao;          // Data Access Object к сохранённым книгам

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_books);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        savedBooksProgressBar = findViewById(R.id.wip_indicator);
        savedBooksAdapter = new BookAdapter(book -> startActivity(new Intent(this, BookActivity.class)
                .putExtra("contentsTitle", book.getTitle())
                .putExtra("contentsHeader", book.getHeader())
                .putExtra("contentsAuthors", book.getAuthors())
                .putExtra("contentsPublisher", book.getPublisher())
                .putExtra("contentsYear", book.getYear())
                .putExtra("contentsUrl", book.getUrl())
                .putExtra("imageUrl", book.getImage())));

        ((RecyclerView) findViewById(R.id.saved_books_full_list)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.saved_books_full_list)).setAdapter(savedBooksAdapter);

        savedBooksDao = App.getInstance().getDatabase().getSavedBooksDao();
    }

    @Override
    public void onStart() {
        super.onStart();

        // В отдельном потоке выбираем сохранённые книги и добавляем их в список адаптера
        new Thread(() -> {
            ArrayList<BookPreviewDataModel> result = new ArrayList<>();
            List<SavedBookEntity> savedBookEntities = savedBooksDao.getAllSavedBooks();

            for (SavedBookEntity entity : savedBookEntities) {
                result.add(new BookPreviewDataModel(entity.id, entity.title, entity.header, entity.authors, entity.publisher, entity.year, entity.image, entity.url));
            }

            runOnUiThread(() -> {
                savedBooksAdapter.setItems(result);
                savedBooksProgressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
