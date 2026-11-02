package com.example.pos_disoftwa;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.pos_disoftwa.adapter.category.CategoryAdapter;
import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.Category;

import java.util.ArrayList;
import java.util.List;

public class DashboardCategoryFragment extends Fragment {

    private RecyclerView recyclerKategori;
    private TextView tvEmptyCategory;
    private CategoryAdapter adapter;
    private DatabaseHelper dbHelper;
    private Button btnTambahKategori;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_dashboard_category, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerKategori = view.findViewById(R.id.recyclerKategori);
        tvEmptyCategory = view.findViewById(R.id.tvEmptyCategory);
        recyclerKategori.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTambahKategori = view.findViewById(R.id.btnTambahKategori);

        dbHelper = new DatabaseHelper(getContext());
        loadCategories();

        btnTambahKategori.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddCategory.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadCategories() {
        List<Category> categories = getCategoryList();
        checkEmptyState(categories);
        adapter = new CategoryAdapter(getContext(), categories);
        recyclerKategori.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCategories();
    }

    private void refreshCategories() {
        List<Category> categories = getCategoryList();
        checkEmptyState(categories);
        if (adapter != null) {
            adapter.updateData(categories);
        } else {
            adapter = new CategoryAdapter(getContext(), categories);
            recyclerKategori.setAdapter(adapter);
        }
    }

    private void checkEmptyState(List<Category> categories) {
        if (categories.isEmpty()) {
            tvEmptyCategory.setVisibility(View.VISIBLE);
            recyclerKategori.setVisibility(View.GONE);
        } else {
            tvEmptyCategory.setVisibility(View.GONE);
            recyclerKategori.setVisibility(View.VISIBLE);
        }
    }

    private List<Category> getCategoryList() {
        List<Category> categoryList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllCategories();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                category.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                categoryList.add(category);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return categoryList;
    }
}