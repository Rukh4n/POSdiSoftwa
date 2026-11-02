package com.example.pos_disoftwa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pos_disoftwa.helpers.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddCategory extends AppCompatActivity {

    private EditText etNamaKategori;
    private Button btnSimpan;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        etNamaKategori = findViewById(R.id.etNamaKategori);
        btnSimpan = findViewById(R.id.btnSimpan);
        databaseHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSimpan.setOnClickListener(v -> {
            String namaKategori = etNamaKategori.getText().toString().trim();
            if (namaKategori.isEmpty()) {
                Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long result = databaseHelper.insertCategory(namaKategori, currentTime, currentTime);

            if (result != -1) {
                Toast.makeText(this, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show();
                etNamaKategori.setText("");
                finish(); // kembali ke fragment sebelumnya
            } else {
                Toast.makeText(this, "Gagal menyimpan kategori", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
