package com.example.com.Models;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.com.R;

import java.util.ArrayList;

public  class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final ArrayList<Box> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Box box);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryTextView;
        public TextView nameTextView;
        public TextView quantityTextView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            quantityTextView = itemView.findViewById(R.id.quantity_text_view);
        }
    }

    public ProductAdapter(ArrayList<Box> productList) {
        this.productList = productList;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        if (productList != null) {
            productList.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_item, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (productList == null || position < 0 || position >= productList.size()) {
            return; // Перевірка на null і правильність позиції
        }

        Box currentItem = productList.get(position);

        holder.categoryTextView.setText(currentItem.getCategory());
        holder.nameTextView.setText(currentItem.getName());
        holder.quantityTextView.setText(currentItem.getQuantity());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int position1 = holder.getAdapterPosition();
                if (position1 != RecyclerView.NO_POSITION) {
                    listener.onItemClick(productList.get(position1));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}