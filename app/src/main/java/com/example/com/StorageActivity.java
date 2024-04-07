package com.example.com;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.com.Models.Box;
import com.example.com.Models.ProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;


public class StorageActivity extends AppCompatActivity {
    public StorageActivity() {
        currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }
    private String searchText = "";
    private final String currentUserUid;


    FloatingActionButton add_button, settings_button, search_button;
    EditText search_field;
    FirebaseAuth auth;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView filter_text;
    Button scan_button;

    DatabaseReference usersRef;

    RecyclerView recyclerView;
    ProductAdapter productAdapter;
    ArrayList<Box> productList;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);


        add_button = findViewById(R.id.add_button);
        settings_button = findViewById(R.id.settings_button);
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


        swipeRefreshLayout.setOnRefreshListener(() -> refreshData(currentUserUid));

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        productList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);



        add_button.setOnClickListener(v -> showDialogToAddProduct(currentUserUid));

        settings_button.setOnClickListener(v -> {
            // Ваш код для запуску нової активності або фрагмента
            Intent intent = new Intent(StorageActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


        final String[] filterOptions = {"Категорія: Від А до Я", "Категорія: Від Я до А", "Назва: Від А до Я", "Назва: Від Я до А", "За зростанням кількості", "За спаданням кількості"};

        filter_text.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StorageActivity.this);
            builder.setTitle("Виберіть фільтр");

            builder.setItems(filterOptions, (dialog, which) -> {
                // Отримання вибраного фільтру
                String selectedFilter = filterOptions[which];

                // Обробка вибраного фільтру
                switch (which) {
                    case 0:
                        // Сортування від А до Я
                        productList.sort(Comparator.comparing(Box::getCategory));
                        productAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        // Сортування від Я до А
                        productList.sort((o1, o2) -> o2.getCategory().compareTo(o1.getCategory()));
                        productAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        // Сортування від А до Я
                        productList.sort(Comparator.comparing(Box::getName));
                        productAdapter.notifyDataSetChanged();
                        break;
                    case 3:
                        // Сортування від Я до А
                        productList.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
                        productAdapter.notifyDataSetChanged();
                        break;
                    case 4:
                        // Сортування за зростанням кількості
                        productList.sort(Comparator.comparing(o -> Integer.valueOf(o.getQuantity())));
                        productAdapter.notifyDataSetChanged();
                        break;
                    case 5:
                        // Сортування за спаданням кількості
                        productList.sort((o1, o2) -> Integer.valueOf(o2.getQuantity()).compareTo(Integer.valueOf(o1.getQuantity())));
                        productAdapter.notifyDataSetChanged();
                        break;
                }

                // Змінюємо текст в filter_text залежно від вибраного фільтру
                filter_text.setText(selectedFilter);
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });



        search_button.setOnClickListener(v -> {
            searchText = search_field.getText().toString().trim();
            if (searchText.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Спочатку введіть текст у поле пошуку", Toast.LENGTH_SHORT).show();
            } else {
                // Отримання фокусу від поле вводу
                search_field.clearFocus();
                // Виклик методу для оновлення даних з новим текстом пошуку
                updateSearchResults(currentUserUid);
            }
        });
        search_field.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                for (int i = s.length(); i > 0; i--) {
                    if (s.subSequence(i - 1, i).toString().equals("\n"))
                        s.replace(i - 1, i, "");
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });


        swipeRefreshLayout.setOnRefreshListener(() -> {
            search_field.setText(""); // Очистити поле пошуку при оновленні сторінки
            refreshData(currentUserUid);
        });

        displayData(currentUserUid);
        productAdapter.setOnItemClickListener(this::showEditBoxDialog);
    }


    private EditText barcodeInput;
    private void showDialogToAddProduct(final String currentUserUid) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_box, null);
        dialogBuilder.setView(dialogView);

        barcodeInput = dialogView.findViewById(R.id.barcode_input);
        final EditText nameInput = dialogView.findViewById(R.id.name_input);
        final EditText quantityInput = dialogView.findViewById(R.id.quantity_input);
        final EditText categoryInput = dialogView.findViewById(R.id.category_input);

        scan_button = dialogView.findViewById(R.id.scan_button);

        barcodeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        scan_button.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(StorageActivity.this);
            integrator.setPrompt("Відскануйте штрих-код");
            integrator.initiateScan();
        });

        dialogBuilder.setTitle("Додати продукт");
        dialogBuilder.setCancelable(false); // Не дозволяти закривати вікно при неправильних або відсутніх даних
        dialogBuilder.setPositiveButton("Додати", null); // Встановлення пустого обробника подій

        dialogBuilder.setNegativeButton("Скасувати", (dialog, whichButton) -> {
            dialog.dismiss(); // закрити діалогове вікно при натисканні кнопки "Скасувати"
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String barcode = barcodeInput.getText().toString().trim();
            String productName = nameInput.getText().toString().trim();
            String quantity = quantityInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (!barcode.isEmpty() && !productName.isEmpty() && !quantity.isEmpty() && !category.isEmpty()) {
                // Перевірка чи баркод є додатнім цілим числом
                try {
                    for (Box existingBox : productList) {
                        if (existingBox.getBarcode().equals(barcode)) {
                            Toast.makeText(getApplicationContext(), "Штрих-код вже існує, введіть унікальний штрих-код", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!barcode.matches("\\d+")) {
                        Toast.makeText(getApplicationContext(), "Невірно введений штрихкод", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Невірно введений штрихкод", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Перевірка чи кількість є додатним цілим числом
                if (quantity.startsWith("0")) {
                    Toast.makeText(getApplicationContext(), "Введіть правильну кількість", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int number = Integer.parseInt(quantity);
                    if (number <= 0) {
                        Toast.makeText(getApplicationContext(), "Введіть натуральне число", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Введіть ціле число", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Якщо всі перевірки виконані успішно, викликаємо метод addProduct
                addProduct(currentUserUid, barcode, productName, quantity, category);
                alertDialog.dismiss(); // закрити діалогове вікно після успішного додавання продукту
            } else {
                Toast.makeText(getApplicationContext(), "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
            }
        });
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Сканування скасовано", Toast.LENGTH_SHORT).show();
            } else {
                // Отримано результат сканування
                barcodeInput.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addProduct(String currentUserUid, String barcode, String productName, String quantity, String category) {

        Box newBox = new Box(barcode, productName, quantity, category);
        usersRef.child(currentUserUid).child("box").push().setValue(newBox)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.add(newBox);
                        productAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Успішно додано!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Помилка при додаванні товару", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayData(String currentUserUid) {
        usersRef.child(currentUserUid).child("box").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String barcode = snapshot.child("barcode").getValue(String.class);
                    String productName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("quantity").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);


                    productList.add(new Box(barcode, productName, quantity, category));
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Помилка при отриманні даних", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditBoxDialog(final Box box) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_box, null);
        dialogBuilder.setView(dialogView);

        final EditText nameInput = dialogView.findViewById(R.id.edit_name_input);
        final EditText quantityInput = dialogView.findViewById(R.id.edit_quantity_input);
        final EditText categoryInput = dialogView.findViewById((R.id.edit_category_input));
        TextView barcodeInput = dialogView.findViewById((R.id.barcode_text_view));

        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        nameInput.setText(box.getName());
        barcodeInput.setText(box.getBarcode());
        quantityInput.setText(box.getQuantity());
        categoryInput.setText(box.getCategory());


        dialogBuilder.setTitle("Редагувати продукт");
        dialogBuilder.setPositiveButton("Зберегти", (dialog, whichButton) -> {
            String newName = nameInput.getText().toString().trim();
            String newQuantity = quantityInput.getText().toString().trim();
            String newCategory = categoryInput.getText().toString().trim();
            if (newName.isEmpty() || newQuantity.isEmpty() || newCategory.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }
            // Перевірка чи нова кількість є додатнім цілим числом
            if (newQuantity.startsWith("0")) {
                Toast.makeText(getApplicationContext(), "Введіть правильну кількість", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int number = Integer.parseInt(newQuantity);
                if (number <= 0) {
                    Toast.makeText(getApplicationContext(), "Введіть натуральне число", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Введіть ціле число", Toast.LENGTH_SHORT).show();
                return;
            }


            updateProduct(box, newName, newQuantity, newCategory);
        });
        dialogBuilder.setNegativeButton("Видалити", (dialog, whichButton) -> deleteProduct(box));
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateProduct(Box box, String newName, String newQuantity, String newCategory) {
        String currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference boxRef = usersRef.child(currentUserUid).child("box");
        Query query = boxRef.orderByChild("barcode").equalTo(box.getBarcode());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Удаление старой записи
                        snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Добавление новой записи с тем же штрих-кодом, но с обновленными данными
                                Box newBox = new Box(box.getBarcode(), newName, newQuantity, newCategory);
                                usersRef.child(currentUserUid).child("box").child(box.getBarcode()).setValue(newBox)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Обновление данных в ArrayList
                                                box.setName(newName);
                                                box.setQuantity(newQuantity);
                                                box.setCategory(newCategory);
                                                productAdapter.notifyDataSetChanged();
                                                Toast.makeText(getApplicationContext(), "Продукт обновлен", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Ошибка при обновлении продукта", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(), "Ошибка при удалении старой записи", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Продукт не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка при обновлении продукта", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void deleteProduct(Box box) {
        String currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference boxRef = usersRef.child(currentUserUid).child("box");
        Query query = boxRef.orderByChild("barcode").equalTo(box.getBarcode());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                productList.remove(box);
                                productAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "Продукт видалено", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Помилка при видаленні продукту", Toast.LENGTH_SHORT).show();
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
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String barcode = snapshot.child("barcode").getValue(String.class);
                    String productName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("quantity").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);

                    // Перевірка, чи відповідає поточний продукт пошуковому тексту
                    assert productName != null;
                    if (productName.toLowerCase().contains(searchText.toLowerCase())) {
                        productList.add(new Box(barcode, productName, quantity,category));
                    }
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
        offKeyboard();
    }

    private void refreshData(String currentUserUid) {
        filter_text.setText("Вибраний фільтр");
        productList.clear();
        productAdapter.clear();

        // Оновлення даних при свайпі вниз
        usersRef.child(currentUserUid).child("box").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear(); // Очищення списку перед додаванням нових даних
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String barcode = snapshot.child("barcode").getValue(String.class);
                    String productName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("quantity").getValue(String.class);
                    String category= snapshot.child("category").getValue(String.class);
                    productList.add(new Box(barcode, productName, quantity, category));
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
        offKeyboard();
    }
    public void offKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_field.getWindowToken(), 0);
    }

}
