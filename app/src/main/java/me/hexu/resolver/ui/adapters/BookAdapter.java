package me.hexu.resolver.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.hexu.resolver.R;
import me.hexu.resolver.datamodels.BookPreviewDataModel;
import me.hexu.resolver.ui.holders.BookViewHolder;
import me.hexu.resolver.interfaces.IOnBookClickListener;

public class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {
    private final ArrayList<BookPreviewDataModel> items = new ArrayList<>();
    private final IOnBookClickListener clickListener;

    public BookAdapter(IOnBookClickListener listener) {
        clickListener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_book, parent, false);

        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<BookPreviewDataModel> newItems) {
        int currentSize = items.size();

        items.clear();
        notifyItemRangeRemoved(0, currentSize);
        items.addAll(newItems);
        notifyItemRangeInserted(0, newItems.size());
    }

    public void clearItems() {
        int currentSize = items.size();

        items.clear();
        notifyItemRangeRemoved(0, currentSize);
    }
}
