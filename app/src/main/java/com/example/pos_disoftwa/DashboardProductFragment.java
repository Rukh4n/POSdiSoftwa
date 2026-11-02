package com.example.pos_disoftwa;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_disoftwa.adapter.product.ProductAdapter;
import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.Product;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardProductFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView tvEmptyProduct;
    private ProductAdapter adapter;
    private List<Product> productList;
    private EditText etSearchProduct;
    private ImageButton btnSearchProduct;
    private Button btnExport;
    private ActivityResultLauncher<String> createFileLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            exportToCsvUri(uri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard_product, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(getActivity());
        recyclerView = view.findViewById(R.id.recyclerViewProduk);
        tvEmptyProduct = view.findViewById(R.id.tvEmptyProduct);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, this::showDeleteConfirmation);
        recyclerView.setAdapter(adapter);
        loadProducts("");

        Button btnAdd = view.findViewById(R.id.btnTambahProduk);
        btnAdd.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddProduct.class)));

        etSearchProduct = view.findViewById(R.id.etSearchProduct);
        btnSearchProduct = view.findViewById(R.id.btnSearchProduct);
        btnSearchProduct.setOnClickListener(v -> {
            String keyword = etSearchProduct.getText().toString().trim();
            loadProducts(keyword);
        });

        btnExport = view.findViewById(R.id.btnExport);
        btnExport.setOnClickListener(v -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            createFileLauncher.launch("Product_Export-" + timeStamp + ".csv");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts("");
    }

    private void loadProducts(String keyword) {
        productList.clear();
        Cursor cursor = keyword.isEmpty() ? dbHelper.getAllProducts() : dbHelper.searchProducts(keyword);
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
        adapter.notifyDataSetChanged();

        if (productList.isEmpty()) {
            tvEmptyProduct.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyProduct.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void exportToCsvUri(Uri uri) {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
             Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            writer.write("ID,Product Name,Description,Category ID,Stock,Purchase Price,Selling Price\n");

            Cursor cursor = dbHelper.getAllProducts();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                    int stok = cursor.getInt(cursor.getColumnIndexOrThrow("stok"));
                    double hargaBeli = cursor.getDouble(cursor.getColumnIndexOrThrow("harga_beli"));
                    double hargaJual = cursor.getDouble(cursor.getColumnIndexOrThrow("harga_jual"));

                    String csvLine = id + "," +
                            "\"" + title.replace("\"", "\"\"") + "\"," +
                            "\"" + description.replace("\"", "\"\"") + "\"," +
                            categoryId + "," +
                            stok + "," +
                            hargaBeli + "," +
                            hargaJual + "\n";

                    writer.write(csvLine);

                } while (cursor.moveToNext());
                cursor.close();
            }

            writer.flush();
            Toast.makeText(getActivity(), "CSV export successful", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "CSV export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmation(Product product) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.confirmation_delete_product);

        Button btnHapus = dialog.findViewById(R.id.btnHapus);
        Button btnBatal = dialog.findViewById(R.id.btnBatal);

        btnHapus.setOnClickListener(v -> {
            dbHelper.deleteProduct(product.getId());
            loadProducts("");
            dialog.dismiss();
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}