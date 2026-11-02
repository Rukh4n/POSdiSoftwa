package com.example.pos_disoftwa;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_disoftwa.adapter.transaction.TransactionItemAdapter;
import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.TransactionItem;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddTransaction extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionItemAdapter itemAdapter;
    private ArrayList<TransactionItem> itemList;
    private AutoCompleteTextView etSearchProduct;
    private Button btnAddItem, btnSaveTransaction;
    private TextView tvTotalPrice;
    private DatabaseHelper dbHelper;
    private double totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        etSearchProduct = findViewById(R.id.etSearchProduct);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        recyclerView = findViewById(R.id.recyclerViewItems);
        itemList = new ArrayList<>();
        itemAdapter = new TransactionItemAdapter(itemList);
        itemAdapter.setOnTotalPriceChangeListener(this::updateTotalPrice);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemAdapter);

        setupProductSuggestions();

        btnAddItem.setOnClickListener(v -> {
            String keyword = etSearchProduct.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(this, "Masukkan nama atau ID produk", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean found = false;
            Cursor cursor = dbHelper.searchProducts(keyword);
            if (cursor.moveToFirst()) {
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                int productId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stok"));
                double unitPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("harga_jual"));
                int quantity = 1;

                if (stock < quantity) {
                    Toast.makeText(this, "Stok tidak cukup untuk produk: " + productName, Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }

                double itemTotal = unitPrice * quantity;
                TransactionItem item = new TransactionItem(productName, quantity, unitPrice, itemTotal);
                item.setProductId(productId);
                itemList.add(item);
                itemAdapter.notifyDataSetChanged();

                updateTotalPrice();
                found = true;
            }
            cursor.close();

            if (!found) {
                Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            } else {
                etSearchProduct.setText("");
            }
        });

        btnSaveTransaction.setOnClickListener(v -> {
            if (itemList.isEmpty()) {
                Toast.makeText(this, "Daftar item kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cek stok untuk semua item sebelum simpan
            for (TransactionItem item : itemList) {
                Cursor cursor = dbHelper.searchProducts(item.getProductName());
                if (cursor.moveToFirst()) {
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stok"));
                    if (stock < item.getQuantity()) {
                        Toast.makeText(this, "Stok tidak cukup untuk produk: " + item.getProductName(), Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return;
                    }
                }
                cursor.close();
            }

            // Konversi itemList menjadi JSON
            Gson gson = new Gson();
            String itemsJson = gson.toJson(itemList);

            // Buat kode transaksi unik berdasarkan timestamp
            String transactionCode = "TRX" + System.currentTimeMillis();

            // Format tanggal saat ini
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Simpan ke database
            long result = dbHelper.insertTransaction(transactionCode, itemsJson, totalPrice, createdAt);
            if (result != -1) {
                // Kurangi stok setiap item
                for (TransactionItem item : itemList) {
                    dbHelper.reduceProductStock(item.getProductId(), item.getQuantity());
                }

                Toast.makeText(this, "Transaksi berhasil disimpan! Total: Rp" + totalPrice, Toast.LENGTH_SHORT).show();
                itemList.clear();
                itemAdapter.notifyDataSetChanged();
                updateTotalPrice();
                finish();
            } else {
                Toast.makeText(this, "Gagal menyimpan transaksi!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProductSuggestions() {
        Cursor cursor = dbHelper.getAllProducts();
        ArrayList<String> productNames = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                productNames.add(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productNames);
        etSearchProduct.setAdapter(adapter);
        etSearchProduct.setThreshold(1);
    }

    private void updateTotalPrice() {
        totalPrice = 0;
        for (TransactionItem item : itemList) {
            totalPrice += item.getTotalPrice();
        }
        tvTotalPrice.setText("Rp" + totalPrice);
    }
}
