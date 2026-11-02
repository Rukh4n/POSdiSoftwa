package com.example.pos_disoftwa;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.TransactionItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class DashboardTransactionFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout layoutTransaksi;
    private TextView tvEmptyTransaction;
    private boolean isSelectionMode = false;
    private HashSet<Integer> selectedTransactions = new HashSet<>();
    private ImageView btnDelete;
    private TextView btnExport;
    private ImageButton btnSearchTransaction;
    private EditText etSearchTransaction;
    private ActivityResultLauncher<String> exportCsvLauncher;

    private String startDateFilter = null;
    private String endDateFilter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exportCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) exportTransactionsToUri(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard_transaction, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(getContext());
        layoutTransaksi = view.findViewById(R.id.layoutTransaksi);
        tvEmptyTransaction = view.findViewById(R.id.tvEmptyTransaction);

        Button btnTambah = view.findViewById(R.id.btnTambahTransaksi);
        btnTambah.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTransaction.class);
            startActivity(intent);
        });

        btnDelete = view.findViewById(R.id.btnDelete);
        btnExport = view.findViewById(R.id.btnExport);
        etSearchTransaction = view.findViewById(R.id.etSearchTransaction);
        btnSearchTransaction = view.findViewById(R.id.btnSearchTransaction);

        btnDelete.setOnClickListener(v -> {
            if (!selectedTransactions.isEmpty()) showDeleteConfirmationDialog();
        });

        btnExport.setOnClickListener(v -> showDateFilterDialog());

        btnSearchTransaction.setOnClickListener(v -> {
            String keyword = etSearchTransaction.getText().toString().trim();
            if (keyword.isEmpty()) loadTransactions();
            else searchTransactions(keyword);
        });

        loadTransactions();
        return view;
    }

    private void showDateFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_transaction_date_filter, null);

        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        Button btnFilter = dialogView.findViewById(R.id.btnFilter);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnFilter.setOnClickListener(v -> {
            startDateFilter = etStartDate.getText().toString().trim();
            endDateFilter = etEndDate.getText().toString().trim();
            if (startDateFilter.isEmpty() || endDateFilter.isEmpty()) {
                Toast.makeText(getContext(), "Isi kedua tanggal terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            exportCsvLauncher.launch("transactions_filtered.csv");
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (DatePicker view, int year, int month, int dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, (month + 1), dayOfMonth);
            target.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void exportTransactionsToUri(Uri uri) {
        Cursor cursor = dbHelper.getAllTransactions();
        if (!cursor.moveToFirst()) {
            Toast.makeText(getContext(), "Tidak ada transaksi untuk diekspor.", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat filterFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Date startDate = null, endDate = null;
        try {
            if (startDateFilter != null && endDateFilter != null) {
                startDate = filterFormat.parse(startDateFilter);
                endDate = filterFormat.parse(endDateFilter);
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                endDate = cal.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
             Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            writer.append("id_transaksi,kode_transaksi,item_id,item_name,quantity,unit_price,total_per_item,created_at\n");

            Gson gson = new Gson();
            Type type = new TypeToken<List<TransactionItem>>() {}.getType();
            SimpleDateFormat csvFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            do {
                String createdAtRaw = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                Date createdAtDate = dbFormat.parse(createdAtRaw);

                if (startDate != null && endDate != null && createdAtDate != null) {
                    if (createdAtDate.before(startDate) || createdAtDate.after(endDate)) continue;
                }

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("transaction_code"));
                String itemsJson = cursor.getString(cursor.getColumnIndexOrThrow("item"));

                String createdAtFormatted = csvFormat.format(createdAtDate);
                List<TransactionItem> items = gson.fromJson(itemsJson, type);

                if (items != null) {
                    for (TransactionItem item : items) {
                        writer.append(id + "," + code + "," + item.getProductId() + "," + item.getProductName() + ","
                                + item.getQuantity() + "," + item.getUnitTotal() + "," + item.getTotalPrice() + ","
                                + createdAtFormatted + "\n");
                    }
                }

            } while (cursor.moveToNext());
            cursor.close();

            writer.flush();
            Toast.makeText(getContext(), "Ekspor CSV berhasil", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal mengekspor CSV.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.confirmation_delete_transaction, null);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
        txtMessage.setText("Hapus " + selectedTransactions.size() + " transaksi terpilih?");

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            for (int id : selectedTransactions) dbHelper.deleteTransaction(id);
            selectedTransactions.clear();
            isSelectionMode = false;
            toggleActionButtons();
            loadTransactions();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void loadTransactions() {
        if (layoutTransaksi == null) return;
        layoutTransaksi.removeAllViews();

        Cursor cursor = dbHelper.getAllTransactions();
        boolean hasData = false;
        if (cursor.moveToFirst()) {
            hasData = true;
            do {
                createTransactionCard(cursor);
            } while (cursor.moveToNext());
        }
        cursor.close();

        updateEmptyState(hasData);
    }

    private void searchTransactions(String keyword) {
        if (layoutTransaksi == null) return;
        layoutTransaksi.removeAllViews();
        Cursor cursor = dbHelper.searchTransactions(keyword);
        boolean hasData = false;
        if (cursor.moveToFirst()) {
            hasData = true;
            do {
                createTransactionCard(cursor);
            } while (cursor.moveToNext());
        }
        cursor.close();

        updateEmptyState(hasData);
    }

    private void updateEmptyState(boolean hasData) {
        if (hasData) {
            tvEmptyTransaction.setVisibility(View.GONE);
            layoutTransaksi.setVisibility(View.VISIBLE);
        } else {
            tvEmptyTransaction.setVisibility(View.VISIBLE);
            layoutTransaksi.setVisibility(View.GONE);
        }
    }

    private void createTransactionCard(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
        double totalTransaction = cursor.getDouble(cursor.getColumnIndexOrThrow("total_transaction"));

        // Format angka agar tidak 2.2E7
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedTotal = numberFormat.format(totalTransaction);

        CardView cardView = new CardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(12);
        cardView.setCardBackgroundColor(0xFF030303);
        cardView.setContentPadding(24, 24, 24, 24);
        cardView.setCardElevation(6);
        cardView.setBackgroundResource(R.drawable.border_orange);

        TextView textView = new TextView(getContext());
        textView.setText("ID: " + id + "\nTanggal: " + createdAt + "\nTotal: Rp" + formattedTotal);
        textView.setTextColor(0xFFF1EFEC);
        textView.setTextSize(16);
        textView.setPadding(8, 8, 8, 8);
        cardView.addView(textView);

        cardView.setOnClickListener(v -> {
            if (isSelectionMode) toggleSelection(cardView, id);
            else {
                Intent intent = new Intent(getActivity(), DetailTransaction.class);
                intent.putExtra("transaction_id", id);
                startActivity(intent);
            }
        });

        cardView.setOnLongClickListener(v -> {
            isSelectionMode = true;
            toggleActionButtons();
            toggleSelection(cardView, id);
            return true;
        });

        layoutTransaksi.addView(cardView);
    }

    private void toggleSelection(CardView cardView, int id) {
        if (selectedTransactions.contains(id)) {
            selectedTransactions.remove(id);
            cardView.setAlpha(1.0f);
        } else {
            selectedTransactions.add(id);
            cardView.setAlpha(0.5f);
        }
        if (selectedTransactions.isEmpty()) {
            isSelectionMode = false;
            toggleActionButtons();
        }
    }

    private void toggleActionButtons() {
        if (isSelectionMode) {
            btnDelete.setVisibility(View.VISIBLE);
            btnExport.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.GONE);
            btnExport.setVisibility(View.VISIBLE);
        }
    }
}