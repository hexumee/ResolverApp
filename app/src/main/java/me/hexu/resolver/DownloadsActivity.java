package me.hexu.resolver;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.database.dao.DownloadedBooksDao;
import me.hexu.resolver.database.entities.DownloadedBookEntity;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.ui.adapters.BookAdapter;
import me.hexu.resolver.utils.FileUtils;

public class DownloadsActivity extends AppCompatActivity {
    private ProgressBar downloadsProgressBar;       // Прогресс-бар (надо же как-то показать работу)
    private RelativeLayout downloadsPlaceholder;    // "Здесь пока пусто"
    private BookAdapter downloadsAdapter;
    private DownloadedBooksDao downloadsDao;        // Data Access Object к загруженным книгам

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        downloadsProgressBar = findViewById(R.id.wip_indicator);
        downloadsPlaceholder = findViewById(R.id.downloads_placeholder);

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        downloadsAdapter = new BookAdapter(book -> startActivity(new Intent(this, BookActivity.class)
                .putExtra("bookId", book.getId())
                .putExtra("contentsTitle", book.getTitle())
                .putExtra("contentsHeader", book.getHeader())
                .putExtra("contentsAuthors", book.getAuthors())
                .putExtra("contentsPublisher", book.getPublisher())
                .putExtra("contentsYear", book.getYear())
                .putExtra("contentsUrl", book.getUrl())
                .putExtra("imageUrl", book.getImage())
                .putExtra("fromDownloads", true)));

        ((RecyclerView) findViewById(R.id.downloads_full_list)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.downloads_full_list)).setAdapter(downloadsAdapter);

        downloadsDao = App.getInstance().getDatabase().getDownloadedBooksDao();
    }

    @Override
    public void onStart() {
        super.onStart();

        // В отдельном потоке выбираем полностью(!) загруженные книги и добавляем их в список адаптера
        new Thread(() -> {
            ArrayList<BookPreviewDataModel> result = new ArrayList<>();
            List<DownloadedBookEntity> downloadedBooksEntities = downloadsDao.getDownloads();

            for (DownloadedBookEntity entity : downloadedBooksEntities) {
                if (entity.isDownloadFinished) {
                    String bookPath = FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), String.valueOf(entity.id) }, false);
                    String[] imagePathSplit = entity.image.split("/");

                    result.add(new BookPreviewDataModel(entity.id, entity.title, entity.header, entity.authors, entity.publisher, entity.year, FileUtils.joinWithSeparator(new String[]{ bookPath, imagePathSplit[imagePathSplit.length-1] }, true), entity.url));
                }
            }

            runOnUiThread(() -> {
                downloadsProgressBar.setVisibility(View.GONE);
                downloadsAdapter.setItems(result);
                downloadsPlaceholder.setVisibility(downloadsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
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
