package com.example.pos_disoftwa.adapter.category;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_disoftwa.DetailItemCategory;
import com.example.pos_disoftwa.EditCategory;
import com.example.pos_disoftwa.R;
import com.example.pos_disoftwa.helpers.DatabaseHelper;
import com.example.pos_disoftwa.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private final DatabaseHelper dbHelper;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        // Klik item kategori → buka DetailItemCategory
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailItemCategory.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            context.startActivity(intent);
        });

        // Tombol Edit
        holder.ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditCategory.class);
            intent.putExtra("id", category.getId());
            intent.putExtra("name", category.getName());
            context.startActivity(intent);
        });

        // Tombol Delete
        holder.ivDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.confirmation_delete_category, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            Button btnDelete = dialogView.findViewById(R.id.btnDelete);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            btnDelete.setOnClickListener(d -> {
                int deleted = dbHelper.deleteCategory(category.getId());
                if (deleted > 0) {
                    categoryList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, categoryList.size());
                    Toast.makeText(context, "Kategori dihapus", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(d -> dialog.dismiss());

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public void updateData(List<Category> newList) {
        categoryList.clear();
        categoryList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivEdit, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
