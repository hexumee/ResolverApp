package me.hexu.resolver.ui.holders;

import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.Objects;
import me.hexu.resolver.R;
import me.hexu.resolver.interfaces.IOnTopicClickListener;

public class TopicViewHolder extends RecyclerView.ViewHolder {
    private final TextView topicTitle;
    private final CardView topicCard;

    public TopicViewHolder(View itemView) {
        super(itemView);

        topicTitle = itemView.findViewById(R.id.topic_title);
        topicCard = itemView.findViewById(R.id.topic_card);
    }

    public void bind(HashMap<String, Object> topic, IOnTopicClickListener listener) {
        topicTitle.setText(Objects.requireNonNull(topic.get("title")).toString());
        topicCard.setOnClickListener(view -> listener.onClick(topic));
    }
}
