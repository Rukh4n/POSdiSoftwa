package com.example.pos_disoftwa.adapter.transaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_disoftwa.R;
import com.example.pos_disoftwa.models.TransactionItem;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionItemAdapter extends RecyclerView.Adapter<TransactionItemAdapter.ViewHolder> {

    private final ArrayList<TransactionItem> itemList;
    private OnTotalPriceChangeListener totalPriceChangeListener;

    public TransactionItemAdapter(ArrayList<TransactionItem> itemList) {
        this.itemList = itemList;
    }

    public void setOnTotalPriceChangeListener(OnTotalPriceChangeListener listener) {
        this.totalPriceChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = itemList.get(position);
        holder.etProductName.setText(item.getProductName());
        holder.etQuantity.setText(String.valueOf(item.getQuantity()));
        holder.etUnitTotal.setText(String.format(Locale.getDefault(), "%.0f", item.getUnitTotal()));
        holder.etTotalPrice.setText(String.format(Locale.getDefault(), "%.0f", item.getTotalPrice()));

        holder.etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int qty = 0;
                if (!s.toString().isEmpty()) {
                    try {
                        qty = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        qty = 0;
                    }
                }

                item.setQuantity(qty);
                double total = item.getUnitTotal() * qty;
                item.setTotalPrice(total);

                // Format agar tidak 2.2E7
                holder.etTotalPrice.setText(String.format(Locale.getDefault(), "%.0f", total));

                if (totalPriceChangeListener != null) {
                    totalPriceChangeListener.onTotalPriceChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText etProductName, etQuantity, etUnitTotal, etTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            etProductName = itemView.findViewById(R.id.etProductName);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            etUnitTotal = itemView.findViewById(R.id.etUnitTotal);
            etTotalPrice = itemView.findViewById(R.id.etTotalPrice);
        }
    }

    public interface OnTotalPriceChangeListener {
        void onTotalPriceChanged();
    }
}
