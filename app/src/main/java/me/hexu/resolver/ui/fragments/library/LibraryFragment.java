package me.hexu.resolver.ui.fragments.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import me.hexu.resolver.BookActivity;
import me.hexu.resolver.DownloadsActivity;
import me.hexu.resolver.R;
import me.hexu.resolver.SavedBooksActivity;
import me.hexu.resolver.SavedTasksActivity;
import me.hexu.resolver.TaskActivity;
import me.hexu.resolver.ui.adapters.BookAdapter;
import me.hexu.resolver.ui.adapters.TaskAdapter;

public class LibraryFragment extends Fragment {
    private LibraryViewModel libraryViewModel;

    // "Здесь пока пусто"
    private RelativeLayout savedBooksPlaceholder;
    private RelativeLayout savedTasksPlaceholder;

    // "Кнопка", открывающая полный список сохранённых книг или задач
    private ImageView savedBooksButton;
    private ImageView savedTasksButton;

    // Прогресс-бар (надо же как-то показать работу)
    private ProgressBar savedBooksProgressBar;
    private ProgressBar savedTasksProgressBar;

    private ActivityResultLauncher<Intent> launchIntent;    // Действие, срабатывающее после возврата к фрагменту извне
    private BookAdapter savedBooksAdapter;
    private TaskAdapter savedTasksAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        launchIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> libraryViewModel.updateView());
        savedBooksAdapter = new BookAdapter(book -> launchIntent.launch(new Intent(getContext(), BookActivity.class)
                .putExtra("contentsTitle", book.getTitle())
                .putExtra("contentsHeader", book.getHeader())
                .putExtra("contentsAuthors", book.getAuthors())
                .putExtra("contentsPublisher", book.getPublisher())
                .putExtra("contentsYear", book.getYear())
                .putExtra("contentsUrl", book.getUrl())
                .putExtra("imageUrl", book.getImage())));
        savedTasksAdapter = new TaskAdapter(task -> {
            HashMap<String, String> bookInfo = new HashMap<>() {{
                put("title", task.getTitle());
                put("header", task.getHeader());
                put("authors", task.getAuthors());
                put("publisher", task.getPublisher());
                put("year", task.getYear());
                put("imageUrl", task.getImage());
                put("url", task.getUrl());
            }};

            launchIntent.launch(new Intent(getContext(), TaskActivity.class)
                    .putExtra("taskTitle", task.getTaskTitle())
                    .putExtra("taskUrl", task.getTaskUrl())
                    .putExtra("path", new ArrayList<>(Arrays.asList(task.getPath().split(" ⋅ "))))
                    .putExtra("bookInfo", bookInfo)
                    .putExtra("fromLibrary", true));
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_library, container, false);
        savedBooksPlaceholder = root.findViewById(R.id.saved_books_placeholder);
        savedTasksPlaceholder = root.findViewById(R.id.saved_tasks_placeholder);
        savedBooksButton = root.findViewById(R.id.view_saved_books);
        savedTasksButton = root.findViewById(R.id.view_saved_tasks);
        savedBooksProgressBar = root.findViewById(R.id.saved_books_wip_indicator);
        savedTasksProgressBar = root.findViewById(R.id.saved_tasks_wip_indicator);

        ((RecyclerView) root.findViewById(R.id.saved_books_list)).setLayoutManager(new LinearLayoutManager(getContext()));
        ((RecyclerView) root.findViewById(R.id.saved_books_list)).setAdapter(savedBooksAdapter);

        ((RecyclerView) root.findViewById(R.id.saved_tasks_list)).setLayoutManager(new LinearLayoutManager(getContext()));
        ((RecyclerView) root.findViewById(R.id.saved_tasks_list)).setAdapter(savedTasksAdapter);

        savedBooksButton.setOnClickListener(v -> launchIntent.launch(new Intent(getContext(), SavedBooksActivity.class)));
        savedTasksButton.setOnClickListener(v -> launchIntent.launch(new Intent(getContext(), SavedTasksActivity.class)));
        root.findViewById(R.id.downloaded_button).setOnClickListener(v -> launchIntent.launch(new Intent(getContext(), DownloadsActivity.class)));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        libraryViewModel.getSavedBooks().observe(getViewLifecycleOwner(), l -> {
            savedBooksProgressBar.setVisibility(View.GONE);
            savedBooksAdapter.setItems(l);
            savedBooksPlaceholder.setVisibility(savedBooksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });

        libraryViewModel.getSavedTasks().observe(getViewLifecycleOwner(), l -> {
            savedTasksProgressBar.setVisibility(View.GONE);
            savedTasksAdapter.setItems(l);
            savedTasksPlaceholder.setVisibility(savedTasksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });

        libraryViewModel.getBooksPreviewExpandability().observe(getViewLifecycleOwner(), v -> savedBooksButton.setVisibility(v ? View.VISIBLE : View.GONE));
        libraryViewModel.getTasksPreviewExpandability().observe(getViewLifecycleOwner(), v -> savedTasksButton.setVisibility(v ? View.VISIBLE : View.GONE));
    }
}
