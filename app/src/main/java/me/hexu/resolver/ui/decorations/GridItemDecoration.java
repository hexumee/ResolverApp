package me.hexu.resolver.ui.decorations;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class GridItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;    // Количество колонок
    private final int spacing;      // Отступ (в dp?)

    public GridItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        if (position >= spanCount) {
            outRect.top = spacing;
        }

        int cardWidth = (parent.getWidth() - (spanCount-1) * spacing) / spanCount;    // Определяем длину ячейки, исходя из количества колонок
        int spacingOfBorders = spanCount == 4 ? spacing * 3 / 4 : spacing * 4 / 6;    // Отступ по краям у первой и последней (в строке) ячеек таблицы

        if (column == 0) {
            outRect.left = 0;
            outRect.right = spacingOfBorders;
        } else if (column == spanCount-1) {
            outRect.left = spacingOfBorders;
            outRect.right = 0;
        } else {
            // Равномерно распределяем ячейки по строке, выставляя отступы
            if (spanCount == 4) {
                if (column == 1) {
                    outRect.left = spacing/4;
                    outRect.right = spacing/2;
                } else if (column == 2) {
                    outRect.left = spacing/2;
                    outRect.right = spacing/4;
                }
            } else {
                if (column == 1) {
                    outRect.left = spacing/6;
                    outRect.right = spacing/2;
                } else if (column == 2) {
                    outRect.left = spacing/4;
                    outRect.right = spacing/3;
                } else if (column == 3) {
                    outRect.left = spacing/3;
                    outRect.right = spacing/4;
                } else if (column == 4) {
                    outRect.left = spacing/2;
                    outRect.right = spacing/6;
                }
            }
        }

        // Растягиваем ячейку до её положенной длины
        view.setMinimumWidth(cardWidth);
    }
}
