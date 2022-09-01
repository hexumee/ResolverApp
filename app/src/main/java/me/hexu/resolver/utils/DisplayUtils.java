package me.hexu.resolver.utils;

import android.content.Context;

public class DisplayUtils {

    /**
     * Преобразование display-independent pixels в пиксели, соответствующие экрану.
     *
     * @param ctx контекст приложения/активности
     * @param dp преобразуемое значение
     *
     * @return количество пикселей экрана, которые соотвествуют преобразованному значению dp
    **/
    public static int convertDpToPx(Context ctx, int dp) {
        float displayDensity = ctx.getResources().getDisplayMetrics().density;
        return (int)(dp * displayDensity + 0.5f);
    }
}
