package com.example.pos_disoftwa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pos_disoftwa.adapter.OnboardingAdapter;
import com.example.pos_disoftwa.models.OnboardingModels;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvNext, tvSkip;
    private OnboardingAdapter adapter;
    private List<OnboardingModels> onboardingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Cek apakah onboarding sudah pernah dibuka
        SharedPreferences preferences = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);

        if (!isFirstTime) {
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = findViewById(R.id.viewPagerOnboarding);
        tvNext = findViewById(R.id.tvNext);
        tvSkip = findViewById(R.id.tvSkip);

        onboardingList = new ArrayList<>();

        onboardingList.add(new OnboardingModels(
                R.drawable.calculator,
                "Mulai Catan Penjualanmu",
                "Catat transaksi dan kelompokan barang berdasarkan kategorinya dengan mudah"));

        onboardingList.add(new OnboardingModels(
                R.drawable.image_1,
                "Dashboard Inventory",
                "Dapatkan hitungan proyeksi keuntungan di tokomu secara langsung dan instan"));

        onboardingList.add(new OnboardingModels(
                R.drawable.image_2,
                "Kumpulkan kategori produkmu",
                "Kumpulkan setiap barang daganganmu berdasarkan kategorinya"));

        onboardingList.add(new OnboardingModels(
                R.drawable.image_3,
                "Daftar produk di tokomu",
                "Catat jumlah stok semua barang yang ada di tokomu dengan mudah"));

        onboardingList.add(new OnboardingModels(
                R.drawable.image_4,
                "Catat semua transaksi yang kamu dapatkan",
                "Catat dan cetak setiap transaksi dengan mudah serta bikin laporan penjualan dengan cepat dan efisien"));

        adapter = new OnboardingAdapter(onboardingList);
        viewPager.setAdapter(adapter);

        tvNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < onboardingList.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                saveOnboardingStatus();
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            }
        });

        tvSkip.setOnClickListener(v -> {
            saveOnboardingStatus();
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });
    }

    private void saveOnboardingStatus() {
        SharedPreferences preferences = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }
}