package me.hexu.resolver.ui.holders;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import me.hexu.resolver.R;
import me.hexu.resolver.datamodels.TaskPreviewDataModel;
import me.hexu.resolver.interfaces.IOnTaskClickListener;
import me.hexu.resolver.utils.ApiRequest;
import me.hexu.resolver.utils.DisplayUtils;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final TextView header;
    private final TextView authors;
    private final TextView publisher;
    private final TextView year;
    private final TextView url;
    private final TextView imageUrl;
    private final TextView taskPath;
    private final TextView taskTitle;
    private final TextView taskUrl;
    private final ImageView image;
    private final CardView bookCard;
    private final ProgressBar bookImageProgress;
    private final RelativeLayout.LayoutParams params;

    public TaskViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.book_title);
        header = itemView.findViewById(R.id.book_header);
        authors = itemView.findViewById(R.id.book_authors);
        publisher = itemView.findViewById(R.id.book_publisher);
        year = itemView.findViewById(R.id.book_year);
        image = itemView.findViewById(R.id.book_image);
        url = itemView.findViewById(R.id.book_url);
        imageUrl = itemView.findViewById(R.id.book_image_url);
        taskPath = itemView.findViewById(R.id.path_to_task);
        taskTitle = itemView.findViewById(R.id.task_title);
        taskUrl = itemView.findViewById(R.id.task_url);
        bookCard = itemView.findViewById(R.id.book_card);
        bookImageProgress = itemView.findViewById(R.id.wip_indicator);

        params = new RelativeLayout.LayoutParams(DisplayUtils.convertDpToPx(itemView.getContext(), 85), RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL, image.getId());
    }

    public void bind(TaskPreviewDataModel task, IOnTaskClickListener listener) {
        title.setText(task.getTitle());
        header.setText(task.getHeader());
        authors.setText(task.getAuthors());
        publisher.setText(task.getPublisher());
        year.setText(task.getYear());
        url.setText(task.getUrl());
        imageUrl.setText(task.getImage());
        taskPath.setText(task.getPath());
        taskTitle.setText(task.getTaskTitle());
        taskUrl.setText(task.getTaskUrl());
        taskPath.setSelected(true);
        bookCard.setOnClickListener(v -> listener.onClick(task));

        ApiRequest.probeUrl(isProbingSucceeded -> new Handler(Looper.getMainLooper()).post(() -> {
            if (isProbingSucceeded) {
                Picasso.get().load(imageUrl.getText().toString()).into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        bookImageProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) { }
                });
                image.setPadding(0, 0, 0, 0);
            } else {
                image.setLayoutParams(params);
                image.setPadding(32, 0, 32, 0);
                image.setImageResource(R.drawable.ic_image_not_found_64);
            }
        }), task.getImage());
    }
}
