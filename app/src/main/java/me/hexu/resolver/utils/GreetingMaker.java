package me.hexu.resolver.utils;

import java.text.SimpleDateFormat;
import me.hexu.resolver.App;
import me.hexu.resolver.R;

public class GreetingMaker {

    /**
     * Возврат привествия, соответствующее текущией локали (далее и времени)
     *
     * @return приветствие, соотвествующее текущему времени
     *
     * @throws IllegalStateException в случае несоотвествия условиям значения часа (может ли быть здесь null?)
    **/
    public String getGreeting() {
        int currentHour = Integer.parseInt(new SimpleDateFormat("HH", App.getInstance().getResources().getConfiguration().locale).format(System.currentTimeMillis()));

        if (currentHour >= 6 && currentHour <= 10) {
            return App.getInstance().getString(R.string.greeting_morning);
        } else if (currentHour >= 11 && currentHour <= 16) {
            return App.getInstance().getString(R.string.greeting_noon);
        } else if (currentHour >= 17 && currentHour <= 21) {
            return App.getInstance().getString(R.string.greeting_evening);
        } else if (currentHour >= 22 && currentHour <= 23 || currentHour >= 0 && currentHour <= 5) {
            return App.getInstance().getString(R.string.greeting_night);
        }

        throw new IllegalStateException("Bad things happened...");
    }
}
