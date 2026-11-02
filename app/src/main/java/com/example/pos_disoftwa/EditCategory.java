package com.example.pos_disoftwa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pos_disoftwa.helpers.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditCategory extends AppCompatActivity {

    private EditText etEditCategoryName;
    private Button btnUpdateCategory;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        etEditCategoryName = findViewById(R.id.etEditCategoryName);
        btnUpdateCategory = findViewById(R.id.btnUpdateCategory);

        // Ambil data dari Intent
        int categoryId = getIntent().getIntExtra("id", -1);
        String categoryName = getIntent().getStringExtra("name");

        // Set nama kategori ke EditText
        if (categoryName != null) {
            etEditCategoryName.setText(categoryName);
        }

        // Tombol update kategori
        btnUpdateCategory.setOnClickListener(v -> {
            String newName = etEditCategoryName.getText().toString().trim();
            if (!newName.isEmpty()) {
                String updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                int rows = dbHelper.updateCategory(categoryId, newName, updatedAt);
                if (rows > 0) {
                    Toast.makeText(this, "Kategori berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish(); // kembali ke fragment sebelumnya
                } else {
                    Toast.makeText(this, "Gagal memperbarui kategori", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
