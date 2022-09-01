package me.hexu.resolver.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.R;
import me.hexu.resolver.datamodels.TaskImageDataModel;
import me.hexu.resolver.ui.holders.TaskImageViewHolder;
import me.hexu.resolver.interfaces.IOnTaskImageClickListener;

public class ImagesAdapter extends RecyclerView.Adapter<TaskImageViewHolder> {
    private final ArrayList<TaskImageDataModel> items = new ArrayList<>();
    private final IOnTaskImageClickListener clickListener;

    public ImagesAdapter(IOnTaskImageClickListener listener) {
        clickListener = listener;
    }

    @NonNull
    @Override
    public TaskImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskImageViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<TaskImageDataModel> newItems) {
        int currentSize = items.size();

        items.clear();
        notifyItemRangeRemoved(0, currentSize);
        items.addAll(newItems);
        notifyItemRangeInserted(0, newItems.size());
    }
}
