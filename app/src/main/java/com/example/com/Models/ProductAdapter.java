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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final ArrayList<Box> productList;  // Список продуктів для відображення в RecyclerView
    private OnItemClickListener listener;  // Слухач для натискання на елементи

    // Інтерфейс для обробки натискання на елемент
    public interface OnItemClickListener {
        void onItemClick(Box box);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder для відображення елементів в RecyclerView
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
        productList.clear();  // Очистити список продуктів
        notifyDataSetChanged();  // Повідомити про зміни в адаптері
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_item, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Перевірка валідності позиції
        if (position < 0 || position >= productList.size()) {
            return;
        }

        Box currentItem = productList.get(position);

        // Встановлення даних в ViewHolder
        holder.categoryTextView.setText(currentItem.getCategory());
        holder.nameTextView.setText(currentItem.getName());
        holder.quantityTextView.setText(currentItem.getQuantity());

        // Встановлення слухача для натискання на елемент
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(productList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
