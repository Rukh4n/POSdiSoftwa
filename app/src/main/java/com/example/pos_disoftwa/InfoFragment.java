// InfoFragment.java
package com.example.pos_disoftwa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.pos_disoftwa.helpers.DatabaseHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InfoFragment extends Fragment {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 101;
    private BluetoothAdapter bluetoothAdapter;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    private ActivityResultLauncher<String> importFileLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        importFileLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importDataFromJsonUri(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_info, container, false);

        View mainView = view.findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sharedPreferences = requireActivity().getSharedPreferences("PrinterPrefs", Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(requireContext());

        Button btnConnectPrinter = view.findViewById(R.id.btnConnectPrinter);
        if (btnConnectPrinter != null) {
            btnConnectPrinter.setOnClickListener(v -> checkAndShowBluetoothDevices());
        }

        Button btnBackupData = view.findViewById(R.id.btnBackupData);
        if (btnBackupData != null) {
            btnBackupData.setOnClickListener(v -> backupStoreDataToJSON());
        }

        Button btnImportProduct = view.findViewById(R.id.btnImportProduct);
        if (btnImportProduct != null) {
            btnImportProduct.setOnClickListener(v -> {
                // Membuka file picker untuk memilih file .json
                importFileLauncher.launch("application/json");
            });
        }

        return view;
    }

    private void backupStoreDataToJSON() {
        // Membuat dialog loading (progress dialog)
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null);
        builder.setView(dialogView);
        AlertDialog loadingDialog = builder.create();
        loadingDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String jsonString = databaseHelper.exportDatabaseToJson();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "backup_pos_" + timeStamp + ".json";

                File cacheDir = new File(requireContext().getCacheDir(), "backups");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                File jsonFile = new File(cacheDir, fileName);
                FileOutputStream fos = new FileOutputStream(jsonFile);
                fos.write(jsonString.getBytes());
                fos.close();

                Uri fileUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        jsonFile
                );

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/json");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Backup Data Toko POS diSoftwa");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                handler.post(() -> {
                    loadingDialog.dismiss();
                    startActivity(Intent.createChooser(shareIntent, "Kirim Backup Data via"));
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Gagal melakukan backup: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void importDataFromJsonUri(Uri uri) {
        // Membuat dialog loading (progress dialog)
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null);
        builder.setView(dialogView);
        AlertDialog loadingDialog = builder.create();
        loadingDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                if (inputStream != null) {
                    inputStream.close();
                }

                String jsonString = stringBuilder.toString();
                boolean isSuccess = databaseHelper.importDatabaseFromJson(jsonString);

                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (isSuccess) {
                        Toast.makeText(requireContext(), "Berhasil mengimport data toko!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Gagal memproses file JSON", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void checkAndShowBluetoothDevices() {
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Perangkat tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Mohon aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

        showPairedDevicesDialog();
    }

    @SuppressLint("MissingPermission")
    private void showPairedDevicesDialog() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<String> deviceList = new ArrayList<>();
        List<String> deviceAddresses = new ArrayList<>();

        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
                deviceAddresses.add(device.getAddress());
            }
        } else {
            deviceList.add("Tidak ada perangkat Bluetooth ter-pairing");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Pilih Printer Bluetooth");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.select_dialog_singlechoice, deviceList);

        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            String savedAddress = sharedPreferences.getString("SAVED_PRINTER_MAC", "");
            int selectedIndex = deviceAddresses.indexOf(savedAddress);

            builder.setSingleChoiceItems(adapter, selectedIndex, (dialog, which) -> {
                String selectedAddress = deviceAddresses.get(which);
                String selectedName = pairedDevices.toArray(new BluetoothDevice[0])[which].getName();

                sharedPreferences.edit().putString("SAVED_PRINTER_MAC", selectedAddress).apply();
                Toast.makeText(requireContext(), "Printer disimpan: " + selectedName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        } else {
            builder.setAdapter(adapter, (dialog, which) -> dialog.dismiss());
        }

        builder.setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                showPairedDevicesDialog();
            } else {
                Toast.makeText(requireContext(), "Izin Bluetooth ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}