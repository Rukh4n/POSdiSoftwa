package com.example.pos_disoftwa;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_disoftwa.adapter.product.ProductAdapter;
import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.Product;

import java.util.ArrayList;
import java.util.List;

public class DetailItemCategory extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private ProductAdapter adapter;
    private DatabaseHelper dbHelper;
    private int categoryId;
    private String categoryName;
    private TextView tvCategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_item_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        categoryId = getIntent().getIntExtra("categoryId", -1);
        categoryName = getIntent().getStringExtra("categoryName");

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvCategoryTitle.setText(categoryName);

        recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));

        loadProducts();
    }

    private void loadProducts() {
        List<Product> productList = new ArrayList<>();
        Cursor cursor = dbHelper.getProductsByCategory(categoryId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                product.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                product.setStok(cursor.getInt(cursor.getColumnIndexOrThrow("stok")));
                product.setHargaBeli(cursor.getDouble(cursor.getColumnIndexOrThrow("harga_beli")));
                product.setHargaJual(cursor.getDouble(cursor.getColumnIndexOrThrow("harga_jual")));
                productList.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Memperbaiki instansiasi ProductAdapter agar sesuai constructor yang benar
        adapter = new ProductAdapter(productList, product -> {
            // Hapus produk callback (opsional, bisa diisi sesuai kebutuhan)
            int deleted = dbHelper.deleteProduct(product.getId());
            if (deleted > 0) {
                productList.remove(product);
                adapter.notifyDataSetChanged();
            }
        });

        recyclerProducts.setAdapter(adapter);
    }
}
