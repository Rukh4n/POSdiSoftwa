package com.example.pos_disoftwa.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pos_disoftwa.models.Category;
import com.example.pos_disoftwa.models.Product;
import com.example.pos_disoftwa.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pos_disoftwa.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_TRANSACTIONS = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "created_at TEXT, " +
                "updated_at TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "category_id INTEGER, " +
                "stok INTEGER, " +
                "harga_beli REAL, " +
                "harga_jual REAL, " +
                "FOREIGN KEY(category_id) REFERENCES " + TABLE_CATEGORIES + "(id))");
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "transaction_code TEXT UNIQUE NOT NULL, " +
                "item TEXT, " +
                "total_transaction REAL, " +
                "created_at TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    // CRUD Categories
    public long insertCategory(String name, String createdAt, String updatedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("created_at", createdAt);
        values.put("updated_at", updatedAt);
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES + " ORDER BY id DESC", null);
    }

    public int updateCategory(int id, String name, String updatedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("updated_at", updatedAt);
        return db.update(TABLE_CATEGORIES, values, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORIES, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor searchCategories(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES + " WHERE name LIKE ? ORDER BY id DESC",
                new String[]{"%" + keyword + "%"});
    }

    // CRUD Products
    public long insertProduct(String title, String description, int categoryId, int stok, double hargaBeli, double hargaJual) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("category_id", categoryId);
        values.put("stok", stok);
        values.put("harga_beli", hargaBeli);
        values.put("harga_jual", hargaJual);
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    public Cursor getProductsByCategory(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE category_id=? ORDER BY id DESC",
                new String[]{String.valueOf(categoryId)});
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY id DESC", null);
    }

    public int updateProduct(int id, String title, String description, int categoryId, int stok, double hargaBeli, double hargaJual) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("category_id", categoryId);
        values.put("stok", stok);
        values.put("harga_beli", hargaBeli);
        values.put("harga_jual", hargaJual);
        return db.update(TABLE_PRODUCTS, values, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PRODUCTS, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor searchProducts(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE title LIKE ? OR description LIKE ? ORDER BY id DESC",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"});
    }

    public int reduceProductStock(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT stok FROM " + TABLE_PRODUCTS + " WHERE id=?", new String[]{String.valueOf(productId)});
        if (cursor.moveToFirst()) {
            int currentStock = cursor.getInt(cursor.getColumnIndexOrThrow("stok"));
            if (currentStock < quantity) {
                cursor.close();
                return -1;
            }
            ContentValues values = new ContentValues();
            values.put("stok", currentStock - quantity);
            cursor.close();
            return db.update(TABLE_PRODUCTS, values, "id=?", new String[]{String.valueOf(productId)});
        } else {
            cursor.close();
            return -1;
        }
    }

    // CRUD Transactions
    public long insertTransaction(String transactionCode, String item, double totalTransaction, String createdAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("transaction_code", transactionCode);
        values.put("item", item);
        values.put("total_transaction", totalTransaction);
        values.put("created_at", createdAt);
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY id DESC", null);
    }

    public int deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor searchTransactions(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " +
                        "CAST(id AS TEXT) LIKE ? OR transaction_code LIKE ? OR item LIKE ? ORDER BY id DESC",
                new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"});
    }

    public Cursor getTransactionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE id=?", new String[]{String.valueOf(id)});
    }

    public double getTotalPendapatan() {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(total_transaction) AS total FROM " + TABLE_TRANSACTIONS, null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        return total;
    }

    public String exportDatabaseToJson() {
        List<Category> categoryList = new ArrayList<>();
        Cursor cCursor = getAllCategories();
        if (cCursor != null) {
            try {
                while (cCursor.moveToNext()) {
                    categoryList.add(new Category(
                            cCursor.getInt(cCursor.getColumnIndexOrThrow("id")),
                            cCursor.getString(cCursor.getColumnIndexOrThrow("name")),
                            cCursor.getString(cCursor.getColumnIndexOrThrow("created_at")),
                            cCursor.getString(cCursor.getColumnIndexOrThrow("updated_at"))
                    ));
                }
            } finally {
                cCursor.close();
            }
        }

        List<Product> productList = new ArrayList<>();
        Cursor pCursor = getAllProducts();
        if (pCursor != null) {
            try {
                while (pCursor.moveToNext()) {
                    productList.add(new Product(
                            pCursor.getInt(pCursor.getColumnIndexOrThrow("id")),
                            pCursor.getString(pCursor.getColumnIndexOrThrow("title")),
                            pCursor.getString(pCursor.getColumnIndexOrThrow("description")),
                            pCursor.getInt(pCursor.getColumnIndexOrThrow("category_id")),
                            pCursor.getInt(pCursor.getColumnIndexOrThrow("stok")),
                            pCursor.getDouble(pCursor.getColumnIndexOrThrow("harga_beli")),
                            pCursor.getDouble(pCursor.getColumnIndexOrThrow("harga_jual"))
                    ));
                }
            } finally {
                pCursor.close();
            }
        }

        List<Transaction> transactionList = new ArrayList<>();
        Cursor tCursor = getAllTransactions();
        if (tCursor != null) {
            try {
                while (tCursor.moveToNext()) {
                    transactionList.add(new Transaction(
                            tCursor.getInt(tCursor.getColumnIndexOrThrow("id")),
                            tCursor.getString(tCursor.getColumnIndexOrThrow("transaction_code")),
                            tCursor.getString(tCursor.getColumnIndexOrThrow("item")),
                            tCursor.getDouble(tCursor.getColumnIndexOrThrow("total_transaction")),
                            tCursor.getString(tCursor.getColumnIndexOrThrow("created_at"))
                    ));
                }
            } finally {
                tCursor.close();
            }
        }

        Map<String, Object> backupData = new HashMap<>();
        backupData.put("categories", categoryList);
        backupData.put("products", productList);
        backupData.put("transactions", transactionList);

        Gson gson = new Gson();
        return gson.toJson(backupData);
    }

    public boolean importDatabaseFromJson(String jsonString) {
        try {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> backupData = gson.fromJson(jsonString, mapType);

            if (backupData == null) return false;

            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                // 1. Import Categories & Map Old Category ID to New Category ID
                Map<Integer, Integer> categoryIdMap = new HashMap<>();
                List<Map<String, Object>> categories = (List<Map<String, Object>>) backupData.get("categories");
                if (categories != null) {
                    for (Map<String, Object> catMap : categories) {
                        int oldId = parseToInt(catMap.get("id"));
                        String name = (String) catMap.get("name");

                        if (name == null || name.trim().isEmpty()) continue;

                        String createdAt = (String) catMap.get("createdAt");
                        if (createdAt == null) createdAt = (String) catMap.get("created_at");

                        String updatedAt = (String) catMap.get("updatedAt");
                        if (updatedAt == null) updatedAt = (String) catMap.get("updated_at");

                        // Check if category name already exists to prevent duplicate
                        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_CATEGORIES + " WHERE name = ?", new String[]{name});
                        int newCategoryId;
                        if (cursor.moveToFirst()) {
                            newCategoryId = cursor.getInt(0);
                        } else {
                            ContentValues values = new ContentValues();
                            values.put("name", name);
                            values.put("created_at", createdAt);
                            values.put("updated_at", updatedAt);
                            newCategoryId = (int) db.insert(TABLE_CATEGORIES, null, values);
                        }
                        cursor.close();
                        categoryIdMap.put(oldId, newCategoryId);
                    }
                }

                // 2. Import Products
                List<Map<String, Object>> products = (List<Map<String, Object>>) backupData.get("products");
                if (products != null) {
                    for (Map<String, Object> prodMap : products) {
                        String title = (String) prodMap.get("title");

                        if (title == null || title.trim().isEmpty()) continue;

                        String description = (String) prodMap.get("description");

                        int oldCatId = parseToInt(prodMap.get("categoryId"));
                        if (oldCatId == 0) oldCatId = parseToInt(prodMap.get("category_id"));

                        int stok = parseToInt(prodMap.get("stok"));

                        double hargaBeli = parseToDouble(prodMap.get("hargaBeli"));
                        if (hargaBeli == 0.0) hargaBeli = parseToDouble(prodMap.get("harga_beli"));

                        double hargaJual = parseToDouble(prodMap.get("hargaJual"));
                        if (hargaJual == 0.0) hargaJual = parseToDouble(prodMap.get("harga_jual"));

                        int newCatId = categoryIdMap.containsKey(oldCatId) ? categoryIdMap.get(oldCatId) : 0;

                        // Check if product title already exists
                        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_PRODUCTS + " WHERE title = ?", new String[]{title});
                        if (!cursor.moveToFirst()) {
                            ContentValues values = new ContentValues();
                            values.put("title", title);
                            values.put("description", description);
                            values.put("category_id", newCatId);
                            values.put("stok", stok);
                            values.put("harga_beli", hargaBeli);
                            values.put("harga_jual", hargaJual);
                            db.insert(TABLE_PRODUCTS, null, values);
                        }
                        cursor.close();
                    }
                }

                // 3. Import Transactions
                List<Map<String, Object>> transactions = (List<Map<String, Object>>) backupData.get("transactions");
                if (transactions != null) {
                    for (Map<String, Object> transMap : transactions) {
                        String transactionCode = (String) transMap.get("transactionCode");
                        if (transactionCode == null) transactionCode = (String) transMap.get("transaction_code");

                        if (transactionCode == null || transactionCode.trim().isEmpty()) continue;

                        String item = (String) transMap.get("item");

                        double totalTransaction = parseToDouble(transMap.get("totalTransaction"));
                        if (totalTransaction == 0.0) totalTransaction = parseToDouble(transMap.get("total_transaction"));

                        String createdAt = (String) transMap.get("createdAt");
                        if (createdAt == null) createdAt = (String) transMap.get("created_at");

                        // Check if transaction_code already exists (Unique)
                        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_TRANSACTIONS + " WHERE transaction_code = ?", new String[]{transactionCode});
                        if (!cursor.moveToFirst()) {
                            ContentValues values = new ContentValues();
                            values.put("transaction_code", transactionCode);
                            values.put("item", item);
                            values.put("total_transaction", totalTransaction);
                            values.put("created_at", createdAt);
                            db.insert(TABLE_TRANSACTIONS, null, values);
                        }
                        cursor.close();
                    }
                }

                db.setTransactionSuccessful();
                return true;
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int parseToInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (Exception e) {
            return 0.0;
        }
    }
}