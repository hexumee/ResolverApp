package me.hexu.resolver.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import me.hexu.resolver.R;
import me.hexu.resolver.ui.holders.TasksViewHolder;
import me.hexu.resolver.interfaces.IOnTaskItemClickListener;

public class TasksAdapter extends RecyclerView.Adapter<TasksViewHolder> {
    private final ArrayList<HashMap<String, Object>> items = new ArrayList<>();
    private final IOnTaskItemClickListener clickListener;
    private boolean usingRowViewType = false;         // Переменная, определяющая какое представление списка задач используется сейчас
    private boolean usingGridIsRestricted = false;    // Переменная, определяющая разрешено ли переключение представлений списка задач

    public TasksAdapter(IOnTaskItemClickListener listener) {
        clickListener = listener;
    }

    @NonNull
    @Override
    public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (usingRowViewType) {
            return new TasksViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_row, parent, false));
        } else {
            return new TasksViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_cell, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TasksViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public boolean getViewType() {
        return usingRowViewType;
    }

    public void setViewType(boolean value) {
        usingRowViewType = value;
        notifyItemRangeChanged(0, items.size());
    }

    public void setItems(List<HashMap<String, Object>> newItems) {
        for (HashMap<String, Object> entry : newItems) {
            // Если текст не помещается в ячейку таблицы, то используем представление "Список" и блокируем переключатель
            if (!usingGridIsRestricted && Objects.requireNonNull(entry.get("title")).toString().length() > 6) {
                usingRowViewType = true;
                usingGridIsRestricted = true;

                break;
            }
        }

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

    public ArrayList<HashMap<String, Object>> getOverlay() {
        return items;
    }

    public boolean isUsingGridRestricted() {
        return usingGridIsRestricted;
    }

    /**
     * Фильтрация списка по слову (поиск).
     *
     * @param filterList список задач
     * @param query слово, по которому будет производиться фильтрация
    **/
    public void filter(List<HashMap<String, Object>> filterList, String query) {
        ArrayList<HashMap<String, Object>> overlay = new ArrayList<>();

        for (HashMap<String, Object> entry : filterList) {
            if (Objects.requireNonNull(entry.get("title")).toString().contains(query)) {
                overlay.add(entry);
            }
        }

        if (overlay.size() > 0) {
            this.setItems(overlay);
        } else {
            this.clearItems();
        }
    }
}
