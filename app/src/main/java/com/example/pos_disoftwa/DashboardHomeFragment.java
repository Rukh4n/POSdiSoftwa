// DashboardHomeFragment.java
package com.example.pos_disoftwa;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.fragment.app.Fragment;

import com.example.pos_disoftwa.helpers.DatabaseHelper;

public class DashboardHomeFragment extends Fragment {

    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard_home, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(getContext());

        // Hitung total produk dan data terkait
        Cursor productCursor = dbHelper.getAllProducts();
        int totalProducts = productCursor.getCount();
        double totalHargaBeli = 0;
        double totalProyeksiPendapatan = 0;

        if (productCursor.moveToFirst()) {
            do {
                int stok = productCursor.getInt(productCursor.getColumnIndexOrThrow("stok"));
                double hargaBeli = productCursor.getDouble(productCursor.getColumnIndexOrThrow("harga_beli"));
                double hargaJual = productCursor.getDouble(productCursor.getColumnIndexOrThrow("harga_jual"));

                totalHargaBeli += hargaBeli * stok;
                totalProyeksiPendapatan += hargaJual * stok;
            } while (productCursor.moveToNext());
        }
        productCursor.close();

        // Hitung total kategori
        Cursor categoryCursor = dbHelper.getAllCategories();
        int totalCategories = categoryCursor.getCount();
        categoryCursor.close();

        // Hitung total pendapatan dari transaksi
        double totalPendapatan = dbHelper.getTotalPendapatan();

        // Hitung potensi keuntungan
        double potensiKeuntungan = totalProyeksiPendapatan - totalHargaBeli;

        // Inisialisasi TextView
        TextView tvJumlahProduk = view.findViewById(R.id.tvJumlahProduk);
        TextView tvJumlahKategori = view.findViewById(R.id.tvJumlahKategori);
        TextView tvTotalHargaBeli = view.findViewById(R.id.tvTotalHargaBeli);
        TextView tvTotalProyeksiPendapatan = view.findViewById(R.id.tvTotalProyeksiPendapatan);
        TextView tvPotensiKeuntungan = view.findViewById(R.id.tvPotensiKeuntungan);
        TextView tvTotalPendapatan = view.findViewById(R.id.tvTotalPendapatan);

        // Tampilkan hasil dengan format tanpa notasi ilmiah
        tvJumlahProduk.setText(String.valueOf(totalProducts));
        tvJumlahKategori.setText(String.valueOf(totalCategories));
        tvTotalHargaBeli.setText(String.format("%.0f", totalHargaBeli));
        tvTotalProyeksiPendapatan.setText(String.format("%.0f", totalProyeksiPendapatan));
        tvPotensiKeuntungan.setText(String.format("%.0f", potensiKeuntungan));
        tvTotalPendapatan.setText(String.format("%.0f", totalPendapatan));

        return view;
    }
}
