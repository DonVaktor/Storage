package com.example.com;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StorageActivity extends AppCompatActivity {
    FloatingActionButton add_button, filter_button, search_button;
    EditText barcode_data, name_data;
    FirebaseAuth auth;
    SwipeRefreshLayout swipeRefreshLayout;

    DatabaseReference usersRef;

    RecyclerView recyclerView;
    ProductAdapter productAdapter;
    ArrayList<Box> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        add_button = findViewById(R.id.add_button);
        filter_button = findViewById(R.id.filter_button);
        search_button = findViewById(R.id.search_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "Спочатку авторизуйтесь", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String currentUserUid = currentUser.getUid();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Виклик методу для оновлення даних
                refreshData(currentUserUid);
            }
        });





        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        productList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);





        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogToAddProduct(currentUserUid);
            }
        });

        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement filter logic
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement search logic
            }
        });

        // Displaying data from Firebase when the Activity is created
        displayData(currentUserUid);
        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Box box) {
                showEditBoxDialog(box);
            }
        });
    }

    private void showDialogToAddProduct(final String currentUserUid) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_box, null);
        dialogBuilder.setView(dialogView);

        final EditText barcodeInput = dialogView.findViewById(R.id.barcode_input);
        final EditText nameInput = dialogView.findViewById(R.id.name_input);
        final EditText quantityInput = dialogView.findViewById(R.id.quantity_input);

        dialogBuilder.setTitle("Додати продукт");
        dialogBuilder.setPositiveButton("Додати", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String barcode = barcodeInput.getText().toString().trim();
                String productName = nameInput.getText().toString().trim();
                String quantity = quantityInput.getText().toString().trim();

                if (!barcode.isEmpty() && !productName.isEmpty() && !quantity.isEmpty()) {
                    addProduct(currentUserUid, barcode, productName, quantity);
                } else {
                    Toast.makeText(getApplicationContext(), "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Скасувати", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancel dialog
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void addProduct(String currentUserUid, String barcode, String productName, String quantity) {
        // Перевірка, чи штрих-код містить лише числа
        if (!isNumeric(barcode)) {
            Toast.makeText(getApplicationContext(), "Штрих-код повинен містити тільки числа", Toast.LENGTH_SHORT).show();
            return;
        }

        // Перевірка на унікальність штрих-коду
        for (Box existingBox : productList) {
            if (existingBox.getBarcode().equals(barcode)) {
                Toast.makeText(getApplicationContext(), "Штрих-код вже існує, введіть унікальний штрих-код", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Додавання продукту до бази даних
        Box newBox = new Box(barcode, productName, quantity);
        usersRef.child(currentUserUid).child("box").push().setValue(newBox)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Оновлення списку після успішного додавання об'єкта до бази даних
                            productList.add(newBox);
                            productAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Успішно додано!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Помилка при додаванні товару", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void displayData(String currentUserUid) {
        usersRef.child(currentUserUid).child("box").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String barcode = snapshot.child("barcode").getValue(String.class);
                    String productName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("quantity").getValue(String.class);

                    productList.add(new Box(barcode, productName, quantity));
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Помилка при отриманні даних", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Box {
        private String barcode;
        private String name;
        private String quantity;

        public Box() {
        }

        public Box(String barcode, String name, String quantity) {
            this.barcode = barcode;
            this.name = name;
            this.quantity = quantity;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getBarcode() {
            return barcode;
        }

        public String getName() {
            return name;
        }

        public String getQuantity() {
            return quantity;
        }
    }

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private ArrayList<Box> productList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(Box box);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {
            public TextView barcodeTextView;
            public TextView nameTextView;
            public TextView quantityTextView;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                barcodeTextView = itemView.findViewById(R.id.barcode_text_view);
                nameTextView = itemView.findViewById(R.id.name_text_view);
                quantityTextView = itemView.findViewById(R.id.quantity_text_view);
            }
        }

        public ProductAdapter(ArrayList<Box> productList) {
            this.productList = productList;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_item, parent, false);
            ProductViewHolder viewHolder = new ProductViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Box currentItem = productList.get(position);

            holder.barcodeTextView.setText(currentItem.getBarcode());
            holder.nameTextView.setText(currentItem.getName());
            holder.quantityTextView.setText(currentItem.getQuantity());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(productList.get(position));
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }
    }


    private void showEditBoxDialog(final Box box) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_box, null);
        dialogBuilder.setView(dialogView);

        final EditText nameInput = dialogView.findViewById(R.id.edit_name_input);
        final EditText quantityInput = dialogView.findViewById(R.id.edit_quantity_input);

        // Встановлення поточних значень імені та кількості у текстових полях
        nameInput.setText(box.getName());
        quantityInput.setText(box.getQuantity());

        dialogBuilder.setTitle("Редагувати продукт");
        dialogBuilder.setPositiveButton("Зберегти", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Отримання нових значень з полів вводу
                String newName = nameInput.getText().toString().trim();
                String newQuantity = quantityInput.getText().toString().trim();

                // Оновлення інформації про об'єкт у списку та базі даних
                updateProduct(box, newName, newQuantity);
            }
        });
        dialogBuilder.setNegativeButton("Видалити", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Видалення об'єкта зі списку та бази даних
                deleteProduct(box);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateProduct(Box box, String newName, String newQuantity) {
        // Перевірка чи не є поля пустими або чи не є кількість рядком
        if (newName.isEmpty() || newQuantity.isEmpty() || !isNumeric(newQuantity)) {
            Toast.makeText(getApplicationContext(), "Будь ласка, введіть коректні дані", Toast.LENGTH_SHORT).show();
            return;
        }

        // Оновлення інформації про об'єкт у списку
        box.setName(newName);
        box.setQuantity(newQuantity);
        productAdapter.notifyDataSetChanged();

        // Оновлення інформації про об'єкт у базі даних
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference boxRef = usersRef.child(currentUserUid).child("box").child(box.getBarcode());
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("name", newName);
        updateValues.put("quantity", newQuantity);
        boxRef.updateChildren(updateValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Продукт оновлено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Помилка при оновленні продукту", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Допоміжний метод для перевірки, чи є рядок числом
    private boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private void deleteProduct(Box box) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference boxRef = usersRef.child(currentUserUid).child("box");
        Query query = boxRef.orderByChild("barcode").equalTo(box.getBarcode());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Видалення успішне, тому оновлюємо список і показуємо повідомлення
                                    productList.remove(box);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(), "Продукт видалено", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Виникла помилка при видаленні, показуємо відповідне повідомлення
                                    Toast.makeText(getApplicationContext(), "Помилка при видаленні продукту", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Продукт не знайдено за вказаним штрих-кодом
                    Toast.makeText(getApplicationContext(), "Продукт не знайдено", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Виникла помилка при читанні з бази даних
                Toast.makeText(getApplicationContext(), "Помилка при видаленні продукту", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void refreshData(String currentUserUid) {
        productList.clear(); // Очистити список продуктів

        // Заново витягнути дані з Firebase
        usersRef.child(currentUserUid).child("box").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String barcode = snapshot.child("barcode").getValue(String.class);
                    String productName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("quantity").getValue(String.class);

                    productList.add(new Box(barcode, productName, quantity));
                }
                productAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false); // Припинення анімації оновлення
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Помилка при оновленні даних", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Припинення анімації оновлення
            }
        });
    }

}
