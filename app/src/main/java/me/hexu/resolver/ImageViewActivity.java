package me.hexu.resolver;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;
import me.hexu.resolver.utils.ApiRequest;
import me.hexu.resolver.utils.FileUtils;

public class ImageViewActivity extends AppCompatActivity {
    private ZoomageView taskImage;    // Открытое изображение

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        setTitle(getResources().getString(R.string.task_image_description));

        // Принудительно показываем верхнюю панель и добавляем на неё кнопку "Назад"
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        taskImage = findViewById(R.id.task_image);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Если устройство не имеет доступа к Интернету, то используем ранее загруженный вариант, иначе обращаемся к серверу
        if (getIntent().getBooleanExtra("isOffline", false)) {
            String bookPath = FileUtils.joinWithSeparator(new String[]{ getFilesDir().getAbsolutePath(), getIntent().getStringExtra("id") }, false);
            String imageUrl = getIntent().getStringExtra("taskImageUrl");
            String[] imageUrlSplit = imageUrl.split("/");

            Picasso.get().load(FileUtils.joinWithSeparator(new String[]{ bookPath, imageUrlSplit[imageUrlSplit.length-1] }, true)).into(taskImage);
            findViewById(R.id.wip_indicator).setVisibility(View.GONE);
        } else {
            ApiRequest.rawResponseRequestUrl(response -> runOnUiThread(() -> {
                findViewById(R.id.wip_indicator).setVisibility(View.GONE);
                taskImage.setImageBitmap(BitmapFactory.decodeByteArray(response, 0, response.length));
            }), getIntent().getStringExtra("taskImageUrl"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
