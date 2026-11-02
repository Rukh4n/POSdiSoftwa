// ProductAdapter.java
package com.example.pos_disoftwa.adapter.product;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos_disoftwa.EditProduct;
import com.example.pos_disoftwa.R;
import com.example.pos_disoftwa.models.Product;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final List<Product> productList;
    private OnItemClickListener listener;

    public ProductAdapter(List<Product> productList, OnItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onDeleteClick(Product product);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvTitle.setText(product.getTitle());
        holder.tvDescription.setText(product.getDescription());
        holder.tvStock.setText("Stok: " + product.getStok());

        String hargaFormatted = String.format(Locale.getDefault(), "%.0f", product.getHargaJual());
        holder.tvHarga.setText("Harga: " + hargaFormatted);

        holder.ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditProduct.class);
            intent.putExtra("id", product.getId());
            intent.putExtra("title", product.getTitle());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("stok", product.getStok());
            intent.putExtra("hargaBeli", product.getHargaBeli());
            intent.putExtra("hargaJual", product.getHargaJual());
            intent.putExtra("categoryId", product.getCategoryId()); // Tambahkan categoryId
            v.getContext().startActivity(intent);
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStock, tvHarga;
        ImageView ivEdit, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
