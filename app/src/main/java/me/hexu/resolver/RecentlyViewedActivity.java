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
import me.hexu.resolver.database.dao.RecentlyViewedDao;
import me.hexu.resolver.database.entities.RecentlyViewedEntity;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.ui.adapters.BookAdapter;

public class RecentlyViewedActivity extends AppCompatActivity {
    private ProgressBar recentlyViewedProgressBar;    // Прогресс-бар (надо же как-то показать работу)
    private BookAdapter recentlyViewedAdapter;
    private RecentlyViewedDao recentlyViewedDao;      // Data Access Object к недавно просмотренным книгам

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_viewed);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recentlyViewedProgressBar = findViewById(R.id.wip_indicator);
        recentlyViewedAdapter = new BookAdapter(book -> startActivity(new Intent(this, BookActivity.class)
                .putExtra("contentsTitle", book.getTitle())
                .putExtra("contentsHeader", book.getHeader())
                .putExtra("contentsAuthors", book.getAuthors())
                .putExtra("contentsPublisher", book.getPublisher())
                .putExtra("contentsYear", book.getYear())
                .putExtra("contentsUrl", book.getUrl())
                .putExtra("imageUrl", book.getImage())));

        ((RecyclerView) findViewById(R.id.recently_viewed_full_list)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.recently_viewed_full_list)).setAdapter(recentlyViewedAdapter);

        recentlyViewedDao = App.getInstance().getDatabase().getRecentlyViewedDao();
    }

    @Override
    public void onStart() {
        super.onStart();

        // В отдельном потоке выбираем недавно просмотренные книги и добавляем их в список адаптера
        new Thread(() -> {
            ArrayList<BookPreviewDataModel> result = new ArrayList<>();
            List<RecentlyViewedEntity> recentlyViewedEntities = recentlyViewedDao.getAllRecentlyViewed();

            for (RecentlyViewedEntity entity : recentlyViewedEntities) {
                result.add(new BookPreviewDataModel(entity.id, entity.title, entity.header, entity.authors, entity.publisher, entity.year, entity.image, entity.url));
            }

            runOnUiThread(() -> {
                recentlyViewedAdapter.setItems(result);
                recentlyViewedProgressBar.setVisibility(View.GONE);
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
