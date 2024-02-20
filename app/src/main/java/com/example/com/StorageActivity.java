package com.example.com;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class StorageActivity extends AppCompatActivity {
    private String searchText = "";
    private boolean isSearching = false;
    private ArrayList<Box> searchResults = new ArrayList<>();
    FloatingActionButton add_button, filter_button, search_button;
    EditText search_field;
    FirebaseAuth auth;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView filter_text;

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
        filter_text = findViewById(R.id.filter_text);
        search_button = findViewById(R.id.search_button);
        search_field = findViewById(R.id.search_field);
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
                // Створення списку рядків для відображення в діалоговому вікні
                final String[] filterOptions = {"Від А до Я", "Від Я до А", "За зростанням кількості", "За спаданням кількості"};

                AlertDialog.Builder builder = new AlertDialog.Builder(StorageActivity.this);
                builder.setTitle("Виберіть фільтр");

                builder.setItems(filterOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Отримання вибраного фільтру
                        String selectedFilter = filterOptions[which];

                        // Обробка вибраного фільтру
                        switch (which) {
                            case 0:
                                // Сортування від А до Я
                                Collections.sort(productList, new Comparator<Box>() {
                                    @Override
                                    public int compare(Box o1, Box o2) {
                                        return o1.getName().compareTo(o2.getName());
                                    }
                                });
                                productAdapter.notifyDataSetChanged();
                                break;
                            case 1:
                                // Сортування від Я до А
                                Collections.sort(productList, new Comparator<Box>() {
                                    @Override
                                    public int compare(Box o1, Box o2) {
                                        return o2.getName().compareTo(o1.getName());
                                    }
                                });
                                productAdapter.notifyDataSetChanged();
                                break;
                            case 2:
                                // Сортування за зростанням кількості
                                Collections.sort(productList, new Comparator<Box>() {
                                    @Override
                                    public int compare(Box o1, Box o2) {
                                        return Integer.valueOf(o1.getQuantity()).compareTo(Integer.valueOf(o2.getQuantity()));
                                    }
                                });
                                productAdapter.notifyDataSetChanged();
                                break;
                            case 3:
                                // Сортування за спаданням кількості
                                Collections.sort(productList, new Comparator<Box>() {
                                    @Override
                                    public int compare(Box o1, Box o2) {
                                        return Integer.valueOf(o2.getQuantity()).compareTo(Integer.valueOf(o1.getQuantity()));
                                    }
                                });
                                productAdapter.notifyDataSetChanged();
                                break;
                        }

                        // Змінюємо текст в filter_text залежно від вибраного фільтру
                        TextView filterTextView = findViewById(R.id.filter_text);
                        filterTextView.setText(selectedFilter);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = search_field.getText().toString().trim();
                if (searchText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Спочатку введіть текст у поле пошуку", Toast.LENGTH_SHORT).show();
                } else {
                    // Отримання фокусу від поле вводу
                    search_field.clearFocus();
                    isSearching = true;
                    // Виклик методу для оновлення даних з новим текстом пошуку
                    refreshData(currentUserUid);
                }
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                search_field.setText(""); // Очистити поле пошуку при оновленні сторінки
                refreshData(currentUserUid);
            }
        });

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
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void addProduct(String currentUserUid, String barcode, String productName, String quantity) {
        if (!isNumeric(barcode)) {
            Toast.makeText(getApplicationContext(), "Штрих-код повинен містити тільки числа", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Box existingBox : productList) {
            if (existingBox.getBarcode().equals(barcode)) {
                Toast.makeText(getApplicationContext(), "Штрих-код вже існує, введіть унікальний штрих-код", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Box newBox = new Box(barcode, productName, quantity);
        usersRef.child(currentUserUid).child("box").push().setValue(newBox)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
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

        public void setProductList(ArrayList<Box> productList) {
            this.productList = productList;
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
            ProductViewHolder viewHolder = new ProductViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            if (productList == null || position < 0 || position >= productList.size()) {
                return; // Перевірка на null і правильність позиції
            }

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

        nameInput.setText(box.getName());
        quantityInput.setText(box.getQuantity());

        dialogBuilder.setTitle("Редагувати продукт");
        dialogBuilder.setPositiveButton("Зберегти", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = nameInput.getText().toString().trim();
                String newQuantity = quantityInput.getText().toString().trim();

                updateProduct(box, newName, newQuantity);
            }
        });
        dialogBuilder.setNegativeButton("Видалити", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteProduct(box);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateProduct(Box box, String newName, String newQuantity) {
        if (newName.isEmpty() || newQuantity.isEmpty() || !isNumeric(newQuantity)) {
            Toast.makeText(getApplicationContext(), "Будь ласка, введіть коректні дані", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference boxRef = usersRef.child(currentUserUid).child("box");
        Query query = boxRef.orderByChild("barcode").equalTo(box.getBarcode());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Видалення старого запису
                        snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Додавання нового запису з тим самим штрих-кодом, але з оновленими даними
                                    Box newBox = new Box(box.getBarcode(), newName, newQuantity);
                                    usersRef.child(currentUserUid).child("box").child(box.getBarcode()).setValue(newBox)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Оновлення даних в ArrayList
                                                        box.setName(newName);
                                                        box.setQuantity(newQuantity);
                                                        productAdapter.notifyDataSetChanged();
                                                        Toast.makeText(getApplicationContext(), "Продукт оновлено", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Помилка при оновленні продукту", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(getApplicationContext(), "Помилка при видаленні старого запису", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Продукт не знайдено", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Помилка при оновленні продукту", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException | NullPointerException e) {
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
                                    productList.remove(box);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(), "Продукт видалено", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Помилка при видаленні продукту", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Продукт не знайдено", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Помилка при видаленні продукту", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateSearchResults(String currentUserUid) {
        productList.clear(); // Очищення списку перед додаванням нових даних
        usersRef.child(currentUserUid).child("box").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String barcode = snapshot.child("barcode").getValue(String.class);
                    String productName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("quantity").getValue(String.class);

                    // Перевірка, чи відповідає поточний продукт пошуковому тексту
                    if (productName.toLowerCase().contains(searchText.toLowerCase())) {
                        productList.add(new Box(barcode, productName, quantity));
                    }
                }
                productAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                isSearching = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Помилка при оновленні даних", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshData(String currentUserUid) {
        filter_text.setText("Вибраний фільтр");
        productList.clear();
        productAdapter.clear();
        searchResults.clear();


        if(isSearching)
        {
            updateSearchResults(currentUserUid);
        }
        else
        {
            // Оновлення даних при свайпі вниз
            usersRef.child(currentUserUid).child("box").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    productList.clear(); // Очищення списку перед додаванням нових даних
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String barcode = snapshot.child("barcode").getValue(String.class);
                        String productName = snapshot.child("name").getValue(String.class);
                        String quantity = snapshot.child("quantity").getValue(String.class);
                        productList.add(new Box(barcode, productName, quantity));
                    }
                    productAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Помилка при оновленні даних", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }

    // Згортання клавіатури
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_field.getWindowToken(), 0);


    }
}
