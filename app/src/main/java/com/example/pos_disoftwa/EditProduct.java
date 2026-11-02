// EditProduct.java
package com.example.pos_disoftwa;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pos_disoftwa.helpers.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditProduct extends AppCompatActivity {

    private EditText etTitle, etDescription, etStock, etHargaBeli, etHargaJual;
    private Spinner spinnerCategory;
    private Button btnSimpan;
    private DatabaseHelper dbHelper;
    private HashMap<String, Integer> categoryMap = new HashMap<>();
    private List<String> categoryNames = new ArrayList<>();
    private int productId = -1;
    private int currentCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etStock = findViewById(R.id.etStock);
        etHargaBeli = findViewById(R.id.etHargaBeli);
        etHargaJual = findViewById(R.id.etHargaJual);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSimpan = findViewById(R.id.btnSimpan);

        dbHelper = new DatabaseHelper(this);

        if (getIntent() != null) {
            productId = getIntent().getIntExtra("id", -1);
            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etStock.setText(String.valueOf(getIntent().getIntExtra("stok", 0)));

            double hargaBeli = getIntent().getDoubleExtra("hargaBeli", 0);
            double hargaJual = getIntent().getDoubleExtra("hargaJual", 0);
            etHargaBeli.setText(String.format(Locale.getDefault(), "%.0f", hargaBeli));
            etHargaJual.setText(String.format(Locale.getDefault(), "%.0f", hargaJual));

            currentCategoryId = getIntent().getIntExtra("categoryId", -1);
        }

        loadCategories();

        btnSimpan.setOnClickListener(v -> updateProduct());
    }

    private void loadCategories() {
        categoryNames.clear();
        categoryMap.clear();

        Cursor cursor = dbHelper.getAllCategories();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                categoryNames.add(name);
                categoryMap.put(name, id);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(adapter);

        setSelectedCategory(currentCategoryId);
    }

    private void setSelectedCategory(int categoryId) {
        if (categoryId == -1) return;

        for (int i = 0; i < categoryNames.size(); i++) {
            String name = categoryNames.get(i);
            if (categoryMap.get(name) == categoryId) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void updateProduct() {
        if (productId == -1) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String stokStr = etStock.getText().toString().trim();
        String hargaBeliStr = etHargaBeli.getText().toString().trim();
        String hargaJualStr = etHargaJual.getText().toString().trim();

        if (title.isEmpty() || stokStr.isEmpty() || hargaBeliStr.isEmpty() || hargaJualStr.isEmpty()) {
            Toast.makeText(this, "Field title, stok, harga beli, dan harga jual harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        int stok;
        double hargaBeli, hargaJual;

        try {
            stok = Integer.parseInt(stokStr);
            hargaBeli = Double.parseDouble(hargaBeliStr);
            hargaJual = Double.parseDouble(hargaJualStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Masukkan angka yang valid", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = -1;
        if (spinnerCategory.getSelectedItem() != null) {
            String selectedCategory = spinnerCategory.getSelectedItem().toString();
            categoryId = categoryMap.getOrDefault(selectedCategory, -1);
        }

        int result = dbHelper.updateProduct(productId, title, description, categoryId, stok, hargaBeli, hargaJual);

        if (result > 0) {
            Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal memperbarui produk", Toast.LENGTH_SHORT).show();
        }
    }
}
