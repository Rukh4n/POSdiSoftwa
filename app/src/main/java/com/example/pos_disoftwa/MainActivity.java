package com.example.pos_disoftwa;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.example.pos_disoftwa.helpers.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navCategory, navProducts, navTransactions, navInfo;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Menyembunyikan navigation bar/system bar (Immersive Mode) dan bisa dimunculkan dengan swipe dari bawah
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }

        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        navHome = findViewById(R.id.nav_home);
        navCategory = findViewById(R.id.nav_category);
        navProducts = findViewById(R.id.nav_products);
        navTransactions = findViewById(R.id.nav_transactions);
        navInfo = findViewById(R.id.nav_info);

        if (savedInstanceState == null) {
            replaceFragment(new DashboardHomeFragment());
            updateNavBackground(navHome);
        }

        navHome.setOnClickListener(v -> {
            replaceFragment(new DashboardHomeFragment());
            updateNavBackground(navHome);
        });

        navCategory.setOnClickListener(v -> {
            replaceFragment(new DashboardCategoryFragment());
            updateNavBackground(navCategory);
        });

        navProducts.setOnClickListener(v -> {
            replaceFragment(new DashboardProductFragment());
            updateNavBackground(navProducts);
        });

        navTransactions.setOnClickListener(v -> {
            replaceFragment(new DashboardTransactionFragment());
            updateNavBackground(navTransactions);
        });

        navInfo.setOnClickListener(v -> {
            replaceFragment(new InfoFragment());
            updateNavBackground(navInfo);
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .setReorderingAllowed(true)
                .commit();
    }

    private void updateNavBackground(LinearLayout selectedNav) {
        GradientDrawable defaultBg = new GradientDrawable();
        defaultBg.setColor(0xFF1A1A1A);
        defaultBg.setCornerRadius(16);

        navHome.setBackground(defaultBg);
        navCategory.setBackground(defaultBg);
        navProducts.setBackground(defaultBg);
        navTransactions.setBackground(defaultBg);
        navInfo.setBackground(defaultBg);

        GradientDrawable selectedBg = new GradientDrawable();
        selectedBg.setColor(0xFFD4C9BE);
        selectedBg.setCornerRadius(16);
        selectedNav.setBackground(selectedBg);
    }
}