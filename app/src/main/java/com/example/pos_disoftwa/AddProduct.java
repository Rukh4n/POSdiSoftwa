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
import com.example.pos_disoftwa.models.Category;

import java.util.ArrayList;

public class AddProduct extends AppCompatActivity {

    private DatabaseHelper db;
    private Spinner spinnerCategory;
    private ArrayList<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        loadCategories();

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDescription = findViewById(R.id.etDescription);
        EditText etStock = findViewById(R.id.etStock);
        EditText etPurchasePrice = findViewById(R.id.etPurchasePrice);
        EditText etSellingPrice = findViewById(R.id.etSellingPrice);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String stockText = etStock.getText().toString().trim();
            String purchaseText = etPurchasePrice.getText().toString().trim();
            String sellingText = etSellingPrice.getText().toString().trim();

            // Validasi input wajib
            if (title.isEmpty() || stockText.isEmpty() || purchaseText.isEmpty() || sellingText.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi kecuali kategori dan deskripsi", Toast.LENGTH_SHORT).show();
                return;
            }

            int stok = Integer.parseInt(stockText);
            double hargaBeli = Double.parseDouble(purchaseText);
            double hargaJual = Double.parseDouble(sellingText);

            int categoryId = 0;
            if (!categoryList.isEmpty() && spinnerCategory.getSelectedItemPosition() >= 0) {
                categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();
            }

            long result = db.insertProduct(title, description, categoryId, stok, hargaBeli, hargaJual);
            if (result != -1) {
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal menambahkan produk", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        categoryList = new ArrayList<>();
        ArrayList<String> categoryNames = new ArrayList<>();
        Cursor cursor = db.getAllCategories();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                Category cat = new Category(id, name, null, null);
                categoryList.add(cat);
                categoryNames.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(adapter);
    }
}
