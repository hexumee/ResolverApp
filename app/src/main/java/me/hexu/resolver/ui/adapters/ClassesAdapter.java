package me.hexu.resolver.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import me.hexu.resolver.R;

public class ClassesAdapter extends ArrayAdapter<String> {
    public ClassesAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View dropDownView = super.getView(position, convertView, parent);
        TextView itemTextView = (TextView) dropDownView;

        if (position == 0) {
            itemTextView.setTextColor(Color.GRAY);
        } else {
            // Получаем цвет текста из темы и устанавливаем его
            TypedValue themeTextColor = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.text_color, themeTextColor, true);
            itemTextView.setTextColor(themeTextColor.data);
        }

        return dropDownView;
    }
}
