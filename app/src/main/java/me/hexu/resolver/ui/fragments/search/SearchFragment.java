package me.hexu.resolver.ui.fragments.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import me.hexu.resolver.BookActivity;
import me.hexu.resolver.R;
import me.hexu.resolver.ui.adapters.BookAdapter;
import me.hexu.resolver.ui.adapters.ClassesAdapter;
import me.hexu.resolver.ui.adapters.SubjectsAdapter;

public class SearchFragment extends Fragment {
    private SearchViewModel searchViewModel;
    private RelativeLayout nothingFoundHolder;                     // "Ничего не найдено"
    private ImageView searchTypeSwitch;                            // "Кнопка", переключающая метод поиска
    private EditText searchBar;                                    // Поле ввода для поиска по ключевым словам
    private RelativeLayout searchSelectors;                        // Лэйаут с выпадающими списками классов и предметов
    private BookAdapter recyclerAdapter;
    private ProgressBar searchResultsProgressBar;                  // Прогресс-бар (надо же как-то показать работу)
    private Spinner classesDropDown;                               // Выпадающий список классов
    private Spinner subjectsDropDown;                              // Выпадающий список предеметов
    private ClassesAdapter classesAdapter;
    private SubjectsAdapter subjectsAdapter;
    private ArrayList<String> classesList = new ArrayList<>();     // Список классов с сервера
    private ArrayList<String> subjectsList = new ArrayList<>();    // Список предметов с сервера

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        recyclerAdapter = new BookAdapter(book -> startActivity(new Intent(getContext(), BookActivity.class)
                .putExtra("contentsTitle", book.getTitle())
                .putExtra("contentsHeader", book.getHeader())
                .putExtra("contentsAuthors", book.getAuthors())
                .putExtra("contentsPublisher", book.getPublisher())
                .putExtra("contentsYear", book.getYear())
                .putExtra("contentsUrl", book.getUrl())
                .putExtra("imageUrl", book.getImage())));
        classesList = new ArrayList<>(Collections.singleton(getString(R.string.search_choose_class)));
        subjectsList = new ArrayList<>(Collections.singleton(getString(R.string.search_choose_subject)));
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        nothingFoundHolder = root.findViewById(R.id.nothing_found_holder);
        searchTypeSwitch = root.findViewById(R.id.search_type_switch);
        searchBar = root.findViewById(R.id.search_bar);
        searchSelectors = root.findViewById(R.id.search_selectors);
        searchResultsProgressBar = root.findViewById(R.id.search_wip_indicator);
        classesDropDown = root.findViewById(R.id.classes_spinner);
        subjectsDropDown = root.findViewById(R.id.subjects_spinner);

        ((RecyclerView) root.findViewById(R.id.search_list)).setLayoutManager(new LinearLayoutManager(getContext()));
        ((RecyclerView) root.findViewById(R.id.search_list)).setAdapter(recyclerAdapter);

        // Пока данные не загружены, выпадающие списки будут выключены
        classesDropDown.setEnabled(false);
        subjectsDropDown.setEnabled(false);

        classesAdapter = new ClassesAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, classesList);
        classesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classesDropDown.setAdapter(classesAdapter);

        subjectsAdapter = new SubjectsAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, subjectsList);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectsDropDown.setAdapter(subjectsAdapter);

        searchTypeSwitch.setOnClickListener(view -> {
            if (Objects.requireNonNull(searchViewModel.getSearchEngineType().getValue())) {
                searchTypeSwitch.setImageResource(R.drawable.ic_search_query_24);
                searchBar.setVisibility(View.GONE);
                searchSelectors.setVisibility(View.VISIBLE);
            } else {
                searchTypeSwitch.setImageResource(R.drawable.ic_alt_search_24);
                searchBar.setVisibility(View.VISIBLE);
                searchSelectors.setVisibility(View.GONE);
            }

            searchViewModel.setSearchEngineType(!searchViewModel.getSearchEngineType().getValue());
        });

        searchBar.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                recyclerAdapter.clearItems();
                nothingFoundHolder.setVisibility(View.GONE);
                searchResultsProgressBar.setVisibility(View.VISIBLE);

                // 1) ключевые слова хранятся на сервере в нижнем регистре
                // 2) в зависимости от ввода используются разные функции
                String keywords = searchBar.getText().toString().toLowerCase();
                if (keywords.contains(" ")) {
                    searchViewModel.doSearch(null, Arrays.asList(keywords.toLowerCase().split(" ")));
                } else {
                    searchViewModel.doSearch(keywords, null);
                }

                return true;
            }

            return false;
        });

        if (!Objects.requireNonNull(searchViewModel.getSearchEngineType().getValue())) {
            searchTypeSwitch.setImageResource(R.drawable.ic_search_query_24);
            searchBar.setVisibility(View.GONE);
            searchSelectors.setVisibility(View.VISIBLE);
        } else {
            searchTypeSwitch.setImageResource(R.drawable.ic_alt_search_24);
            searchBar.setVisibility(View.VISIBLE);
            searchSelectors.setVisibility(View.GONE);
        }

        classesDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos != 0 && pos != Objects.requireNonNull(searchViewModel.getClassSelectorPosition().getValue())) {
                    recyclerAdapter.clearItems();
                    nothingFoundHolder.setVisibility(View.GONE);
                    searchResultsProgressBar.setVisibility(View.VISIBLE);

                    // На время загрузки новых данных выключим выпадающие списки, чтобы не получилось одновременных запросов к серверу
                    classesDropDown.setEnabled(false);
                    subjectsDropDown.setEnabled(false);

                    // Кто-то пропустил 29 – теперь мы страдаем...
                    searchViewModel.doSearch(pos, (subjectsDropDown.getSelectedItemPosition() > 28 ? subjectsDropDown.getSelectedItemPosition()+1 : subjectsDropDown.getSelectedItemPosition()));

                    searchViewModel.setClassSelectorPosition(pos);
                }
            }
        });

        subjectsDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos != 0 && pos != Objects.requireNonNull(searchViewModel.getSubjectSelectorPosition().getValue())) {
                    recyclerAdapter.clearItems();
                    nothingFoundHolder.setVisibility(View.GONE);
                    searchResultsProgressBar.setVisibility(View.VISIBLE);

                    // На время загрузки новых данных выключим выпадающие списки, чтобы не получилось одновременных запросов к серверу
                    classesDropDown.setEnabled(false);
                    subjectsDropDown.setEnabled(false);

                    // Кто-то пропустил 29 – теперь мы страдаем...
                    searchViewModel.doSearch(classesDropDown.getSelectedItemPosition(), (pos > 28 ? pos+1 : pos));

                    searchViewModel.setSubjectSelectorPosition(pos);
                }
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchViewModel.getResults().observe(getViewLifecycleOwner(), l -> {
            searchResultsProgressBar.setVisibility(View.GONE);
            recyclerAdapter.setItems(l);
            classesDropDown.setEnabled(true);
            subjectsDropDown.setEnabled(true);
            nothingFoundHolder.setVisibility(recyclerAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });

        searchViewModel.getClassesList().observe(getViewLifecycleOwner(), l -> {
            classesList.addAll(l);
            classesAdapter.notifyDataSetChanged();
            classesDropDown.setEnabled(true);
            classesDropDown.setSelection(Objects.requireNonNull(searchViewModel.getClassSelectorPosition().getValue()), false);
        });

        searchViewModel.getSubjectsList().observe(getViewLifecycleOwner(), l -> {
            subjectsList.addAll(l);
            subjectsAdapter.notifyDataSetChanged();
            subjectsDropDown.setEnabled(true);
            subjectsDropDown.setSelection(Objects.requireNonNull(searchViewModel.getSubjectSelectorPosition().getValue()), false);
        });
    }
}
