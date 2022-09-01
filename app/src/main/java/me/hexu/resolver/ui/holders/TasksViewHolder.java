package me.hexu.resolver.ui.holders;

import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.Objects;
import me.hexu.resolver.R;
import me.hexu.resolver.interfaces.IOnTaskItemClickListener;

public class TasksViewHolder extends RecyclerView.ViewHolder {
    private final TextView taskText;
    private final TextView taskUrl;
    private final CardView taskCard;

    public TasksViewHolder(View itemView) {
        super(itemView);

        taskText = itemView.findViewById(R.id.task_text);
        taskUrl = itemView.findViewById(R.id.task_url);
        taskCard = itemView.findViewById(R.id.task_card);
    }

    public void bind(HashMap<String, Object> taskItem, IOnTaskItemClickListener listener, int position) {
        taskText.setText(Objects.requireNonNull(taskItem.get("title")).toString());
        taskUrl.setText(Objects.requireNonNull(taskItem.get("url")).toString());
        taskCard.setOnClickListener(view -> listener.onClick(taskItem, position));
    }
}
