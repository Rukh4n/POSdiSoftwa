package com.example.pos_disoftwa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.TransactionItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class DetailTransaction extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView textTransactionId, textTransactionCode, textTotalTransaction, textCreatedAt;
    private LinearLayout layoutItems;
    private Button btnPrint, btnSave;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice printerDevice;
    private List<TransactionItem> transactionItems = new ArrayList<>();
    private ActivityResultLauncher<String> createPdfLauncher;
    private ActivityResultLauncher<String> fallbackPdfLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        textTransactionId = findViewById(R.id.textTransactionId);
        textTransactionCode = findViewById(R.id.textTransactionCode);
        textTotalTransaction = findViewById(R.id.textTotalTransaction);
        textCreatedAt = findViewById(R.id.textCreatedAt);
        layoutItems = findViewById(R.id.layoutItems);
        btnPrint = findViewById(R.id.btnPrint);
        btnSave = findViewById(R.id.btnSave);

        int transactionId = getIntent().getIntExtra("transaction_id", -1);
        if (transactionId != -1) loadTransactionDetail(transactionId);

        btnPrint.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("PrinterPrefs", MODE_PRIVATE);
            String savedMac = prefs.getString("SAVED_PRINTER_MAC", "");

            if (!savedMac.isEmpty() && bluetoothAdapter != null) {
                try {
                    printerDevice = bluetoothAdapter.getRemoteDevice(savedMac);
                    checkBluetoothPermissionForDirectPrint();
                } catch (IllegalArgumentException e) {
                    checkBluetoothPermissionWithDialog();
                }
            } else {
                checkBluetoothPermissionWithDialog();
            }
        });

        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> { if (uri != null) saveTransactionAsPdf(uri); }
        );

        fallbackPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> { if (uri != null) saveTransactionAsPdf(uri); }
        );

        btnSave.setOnClickListener(v ->
                createPdfLauncher.launch("transaction_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf")
        );
    }

    private void checkBluetoothPermissionForDirectPrint() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_BLUETOOTH_PERMISSION);
        } else {
            printReceiptWithDynamicUUID();
        }
    }

    private void checkBluetoothPermissionWithDialog() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_BLUETOOTH_PERMISSION);
        } else {
            showBluetoothDevicesDialog();
        }
    }

    @SuppressLint("MissingPermission")
    private void showBluetoothDevicesDialog() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Perangkat ini tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "Tidak ada perangkat Bluetooth yang terhubung (paired)", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] deviceNames = pairedDevices.stream().map(BluetoothDevice::getName).toArray(String[]::new);
        BluetoothDevice[] devicesArray = pairedDevices.toArray(new BluetoothDevice[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Printer Bluetooth");
        builder.setItems(deviceNames, (dialog, which) -> {
            printerDevice = devicesArray[which];

            getSharedPreferences("PrinterPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("SAVED_PRINTER_MAC", printerDevice.getAddress())
                    .apply();

            printReceiptWithDynamicUUID();
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @SuppressLint("MissingPermission")
    private void printReceiptWithDynamicUUID() {
        if (printerDevice == null) {
            Toast.makeText(this, "Printer Bluetooth tidak dipilih", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            BluetoothSocket socket = null;
            OutputStream outputStream = null;
            boolean isPrintedSuccessfully = false;

            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                socket = printerDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();

                if (socket.isConnected()) {
                    outputStream = socket.getOutputStream();
                    String receiptData = generateReceiptText();
                    outputStream.write(receiptData.getBytes());
                    outputStream.flush();
                    isPrintedSuccessfully = true;
                    runOnUiThread(() -> Toast.makeText(this, "Data terkirim ke printer", Toast.LENGTH_SHORT).show());
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Printer gagal. Menyimpan ke PDF...", Toast.LENGTH_LONG).show());
                runOnUiThread(() -> fallbackPdfLauncher.launch("transaction_fallback_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf"));
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                if (printerDevice != null) {
                    printReceiptWithDynamicUUID();
                } else {
                    showBluetoothDevicesDialog();
                }
            } else {
                Toast.makeText(this, "Izin Bluetooth/Lokasi diperlukan untuk mencetak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        String line = "--------------------------------\n";
        sb.append("\n");
        sb.append("        STRUK TRANSAKSI         \n");
        sb.append("       Toko POS-diSoftwa        \n");
        sb.append(line);
        sb.append(textTransactionCode.getText().toString()).append("\n");
        sb.append(textCreatedAt.getText().toString()).append("\n");
        sb.append(line);
        for (TransactionItem item : transactionItems) {
            String itemName = item.getProductName();
            if (itemName.length() > 16) itemName = itemName.substring(0, 16);
            String itemPriceFormatted = formatCurrency(item.getTotalPrice());
            String itemLine = String.format(Locale.US, "%-16s x%d %9s\n",
                    itemName, item.getQuantity(), itemPriceFormatted);
            sb.append(itemLine);
        }
        sb.append(line);
        String totalValue = textTotalTransaction.getText().toString()
                .replace("Total Transaksi: Rp ", "");
        sb.append(String.format(Locale.US, "%-21s %11s\n", "Total", totalValue));
        sb.append(line);
        sb.append("\n");
        sb.append("      Terima kasih telah       \n");
        sb.append("         berbelanja!          \n\n\n\n");
        return sb.toString();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        return formatter.format(amount);
    }

    @SuppressLint("SetTextI18n")
    private void loadTransactionDetail(int transactionId) {
        Cursor cursor = dbHelper.getTransactionById(transactionId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String code = cursor.getString(cursor.getColumnIndexOrThrow("transaction_code"));
                    double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total_transaction"));
                    String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                    String itemsJson = cursor.getString(cursor.getColumnIndexOrThrow("item"));

                    textTransactionId.setText("ID Transaksi: " + id);
                    textTransactionCode.setText("Kode Transaksi: " + code);
                    textTotalTransaction.setText("Total Transaksi: Rp " + formatCurrency(total));
                    textCreatedAt.setText("Tanggal: " + createdAt);
                    layoutItems.removeAllViews();

                    LayoutInflater inflater = LayoutInflater.from(this);
                    transactionItems.clear();

                    if (itemsJson != null && !itemsJson.isEmpty()) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<TransactionItem>>() {}.getType();
                        List<TransactionItem> items = gson.fromJson(itemsJson, listType);
                        if (items != null) {
                            transactionItems.addAll(items);
                            for (TransactionItem item : transactionItems) {
                                View itemView = inflater.inflate(R.layout.item_transaction_detail, layoutItems, false);
                                TextView itemName = itemView.findViewById(R.id.textItemName);
                                TextView itemQuantity = itemView.findViewById(R.id.textItemQuantity);
                                TextView itemPrice = itemView.findViewById(R.id.textItemPrice);
                                itemName.setText(item.getProductName());
                                itemQuantity.setText("x" + item.getQuantity());
                                itemPrice.setText("Rp" + formatCurrency(item.getTotalPrice()));
                                layoutItems.addView(itemView);
                            }
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void saveTransactionAsPdf(Uri uri) {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
             OutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("STRUK TRANSAKSI", titleFont));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(textTransactionCode.getText().toString(), textFont));
            document.add(new Paragraph(textCreatedAt.getText().toString(), textFont));
            document.add(new Paragraph("\n"));
            if (transactionItems != null && !transactionItems.isEmpty()) {
                for (TransactionItem item : transactionItems) {
                    document.add(new Paragraph(String.format(Locale.getDefault(),
                            "%s x%d - Rp%s", item.getProductName(), item.getQuantity(),
                            formatCurrency(item.getTotalPrice())), textFont));
                }
            }
            document.add(new Paragraph("\nTotal: " +
                    textTotalTransaction.getText().toString().replace("Total Transaksi: ", ""), titleFont));
            document.add(new Paragraph("\nTerima kasih telah berbelanja!", textFont));
            document.close();
            Toast.makeText(this, "PDF berhasil disimpan", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}