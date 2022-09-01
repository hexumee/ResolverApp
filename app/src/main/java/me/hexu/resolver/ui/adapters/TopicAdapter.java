package me.hexu.resolver.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.hexu.resolver.R;
import me.hexu.resolver.ui.holders.TopicViewHolder;
import me.hexu.resolver.interfaces.IOnTopicClickListener;

public class TopicAdapter extends RecyclerView.Adapter<TopicViewHolder> {
    private final ArrayList<HashMap<String, Object>> items = new ArrayList<>();
    private final IOnTopicClickListener clickListener;

    public TopicAdapter(IOnTopicClickListener listener) {
        clickListener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TopicViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_topic, parent, false));
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<HashMap<String, Object>> newItems) {
        int currentSize = items.size();

        items.clear();
        notifyItemRangeRemoved(0, currentSize);
        items.addAll(newItems);
        notifyItemRangeInserted(0, newItems.size());
    }
}
