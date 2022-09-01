package me.hexu.resolver.ui.fragments.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.hexu.resolver.BookActivity;
import me.hexu.resolver.R;
import me.hexu.resolver.RecentlyViewedActivity;
import me.hexu.resolver.SettingsActivity;
import me.hexu.resolver.ui.adapters.BookAdapter;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private TextView greeting;                              // "Добр(ое/ый/ой) %s!"
    private RelativeLayout recentlyViewedPlaceholder;       // "Здесь пока пусто"
    private ProgressBar recentlyViewedProgressBar;          // Прогресс-бар (надо же как-то показать работу)
    private BookAdapter recentlyViewedAdapter;
    private ImageView recentlyViewedButton;                 // "Кнопка", открывающая полный список недавно просмотренных книг
    private ActivityResultLauncher<Intent> launchIntent;    // Действие, срабатывающее после возврата к фрагменту извне

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        launchIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> homeViewModel.updateView());
        recentlyViewedAdapter = new BookAdapter(book -> launchIntent.launch(new Intent(getContext(), BookActivity.class)
                .putExtra("contentsTitle", book.getTitle())
                .putExtra("contentsHeader", book.getHeader())
                .putExtra("contentsAuthors", book.getAuthors())
                .putExtra("contentsPublisher", book.getPublisher())
                .putExtra("contentsYear", book.getYear())
                .putExtra("contentsUrl", book.getUrl())
                .putExtra("imageUrl", book.getImage())));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        greeting = root.findViewById(R.id.greeting);
        recentlyViewedPlaceholder = root.findViewById(R.id.recently_viewed_placeholder);
        recentlyViewedProgressBar = root.findViewById(R.id.home_wip_indicator);
        recentlyViewedButton = root.findViewById(R.id.recently_viewed_button);

        ((RecyclerView) root.findViewById(R.id.recently_viewed_list)).setLayoutManager(new LinearLayoutManager(getContext()));
        ((RecyclerView) root.findViewById(R.id.recently_viewed_list)).setAdapter(recentlyViewedAdapter);

        root.findViewById(R.id.settings_button).setOnClickListener(v -> launchIntent.launch(new Intent(getContext(), SettingsActivity.class)));
        recentlyViewedButton.setOnClickListener(v -> launchIntent.launch(new Intent(getContext(), RecentlyViewedActivity.class)));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel.getGreeting().observe(getViewLifecycleOwner(), s -> greeting.setText(s));

        homeViewModel.getRecentlyOpened().observe(getViewLifecycleOwner(), l -> {
            recentlyViewedProgressBar.setVisibility(View.GONE);
            recentlyViewedAdapter.setItems(l);
            recentlyViewedPlaceholder.setVisibility(recentlyViewedAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });

        homeViewModel.getPreviewExpandability().observe(getViewLifecycleOwner(), v -> recentlyViewedButton.setVisibility(v ? View.VISIBLE : View.GONE));
    }
}
