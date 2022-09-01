package me.hexu.resolver.ui.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import me.hexu.resolver.R;
import me.hexu.resolver.datamodels.TaskImageDataModel;
import me.hexu.resolver.interfaces.IOnTaskImageClickListener;

public class TaskImageViewHolder extends RecyclerView.ViewHolder {
    private final ImageView taskImage;
    private final ProgressBar taskImageProgress;

    public TaskImageViewHolder(View itemView) {
        super(itemView);

        taskImage = itemView.findViewById(R.id.task_image);
        taskImageProgress = itemView.findViewById(R.id.wip_indicator);
    }

    public void bind(TaskImageDataModel taskImageItem, IOnTaskImageClickListener listener) {
        Picasso.get().load(taskImageItem.getTaskImageUrl()).into(taskImage, new Callback() {
            @Override
            public void onSuccess() {
                taskImageProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) { }
        });

        taskImage.setOnClickListener(v -> listener.onClick(taskImageItem.getTaskImageUrl()));
    }
}
