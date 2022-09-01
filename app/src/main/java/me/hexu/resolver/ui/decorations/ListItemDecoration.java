package me.hexu.resolver.ui.decorations;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;    // Отступ (в dp?)

    public ListItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        outRect.left = 0;
        outRect.right = 0;

        if (position >= 1) {
            outRect.top = spacing;
        }
    }
}
