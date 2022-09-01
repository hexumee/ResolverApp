package me.hexu.resolver.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.R;
import me.hexu.resolver.datamodels.TaskPreviewDataModel;
import me.hexu.resolver.ui.holders.TaskViewHolder;
import me.hexu.resolver.interfaces.IOnTaskClickListener;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private final ArrayList<TaskPreviewDataModel> items = new ArrayList<>();
    private final IOnTaskClickListener clickListener;

    public TaskAdapter(IOnTaskClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task, parent, false));
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<TaskPreviewDataModel> newItems) {
        int currentSize = items.size();

        items.clear();
        notifyItemRangeRemoved(0, currentSize);
        items.addAll(newItems);
        notifyItemRangeInserted(0, newItems.size());
    }
}
