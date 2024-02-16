package com.example.com;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.com.Models.User;
import com.example.com.PasswordHasher;
import com.example.com.R;
import com.example.com.storageActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {

    Button btn_log_in, btn_sign_up;
    RelativeLayout root;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_log_in = findViewById(R.id.btn_log_in);
        btn_sign_up = findViewById(R.id.btn_sign_up);
        root = findViewById(R.id.root_element);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btn_sign_up.setOnClickListener(v -> showRegisterWindow());
        btn_log_in.setOnClickListener(v -> login());
    }

    private void login() {
        MaterialEditText login = findViewById(R.id.text_login);
        MaterialEditText pass = findViewById(R.id.text_password);

        if (TextUtils.isEmpty(login.getText()) || TextUtils.isEmpty(pass.getText())) {
            showError("Логін або пароль введений некоректно");
            return;
        }

        auth.signInWithEmailAndPassword(login.getText().toString(), pass.getText().toString())
                .addOnSuccessListener(authResult -> {
                    startActivity(new Intent(MainActivity.this, storageActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showError("Помилка авторизації: " + e.getMessage()));
    }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Реєстрація");
        dialog.setMessage("Введіть всі дані");
        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindowView = inflater.inflate(R.layout.register_window, null);
        dialog.setView(registerWindowView);

        final MaterialEditText email = registerWindowView.findViewById(R.id.field_email);
        final MaterialEditText name = registerWindowView.findViewById(R.id.field_login);
        final MaterialEditText number = registerWindowView.findViewById(R.id.field_phonenumber);
        final MaterialEditText password = registerWindowView.findViewById(R.id.field_password);

        dialog.setNegativeButton("Відхилити", (dialog1, which) -> dialog1.dismiss());
        dialog.setPositiveButton("Прийняти", null); // Залишаємо null, щоб вікно не закривалося автоматично

        final AlertDialog alertDialog = dialog.create();

        alertDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                if (isValidRegistration(email, name, number, password)) {
                    createUser(email.getText().toString(), password.getText().toString(),
                            name.getText().toString(), number.getText().toString(), alertDialog);
                } else {
                    showError("Будь ласка, заповніть всі поля коректно");
                }
            });
        });

        alertDialog.show();
    }

    private boolean isValidRegistration(MaterialEditText email, MaterialEditText name,
                                        MaterialEditText number, MaterialEditText password) {
        boolean isValid = true;
        if (TextUtils.isEmpty(email.getText()) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
            email.setError("Некоректна електронна пошта");
            isValid = false;
        }
        if (TextUtils.isEmpty(name.getText())) {
            name.setError("Поле не може бути порожнім");
            isValid = false;
        }
        if (TextUtils.isEmpty(number.getText())) {
            number.setError("Поле не може бути порожнім");
            isValid = false;
        }
        if (TextUtils.isEmpty(password.getText())) {
            password.setError("Поле не може бути порожнім");
            isValid = false;
        }
        return isValid;
    }

    private void createUser(String email, String password, String name, String number, AlertDialog alertDialog) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String hashedPass = PasswordHasher.hashPassword(password);

                    User user = new User();
                    user.setEmail(email);
                    user.setNumber(number);
                    user.setName(name);
                    user.setPassword(hashedPass);

                    users.child(auth.getCurrentUser().getUid())
                            .setValue(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getApplicationContext(),"Користувач успішно створений!", Toast.LENGTH_LONG).show();
                                alertDialog.dismiss(); // Закриваємо вікно реєстрації після успішного створення користувача
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getApplicationContext(), "Помилка : " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> showError("Помилка авторизації: " + e.getMessage()));
    }

    private void showError(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
