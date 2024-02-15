package com.example.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.com.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
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

        auth =  FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });
        btn_log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                MaterialEditText login = findViewById(R.id.text_login);
                MaterialEditText pass= findViewById(R.id.text_password);
                if (login.getText().toString().isEmpty()) {
                    login.setHintTextColor(Color.parseColor("#e03a40"));
                    login.setUnderlineColor(Color.parseColor("#e03a40"));
                    Toast.makeText(getApplicationContext(), "Логін або пароль введений некоректно", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                {
                    login.setHintTextColor(Color.parseColor("#c89cf7"));
                    login.setUnderlineColor(Color.parseColor("#c89cf7"));
                }

                if (pass.getText().toString().isEmpty()) {
                    pass.setHintTextColor(Color.parseColor("#e03a40"));
                    pass.setUnderlineColor(Color.parseColor("#e03a40"));
                    Toast.makeText(getApplicationContext(), "Логін або пароль введений некоректно", Toast.LENGTH_LONG).show();
                    return;

                } else
                {
                    pass.setHintTextColor(Color.parseColor("#c89cf7"));
                    pass.setUnderlineColor(Color.parseColor("#c89cf7"));
                }


                auth.signInWithEmailAndPassword(login.getText().toString(), pass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainActivity.this, storageActivity.class));
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Помилка авторизації: " + e.getMessage() , Toast.LENGTH_LONG).show();
                            }
                        });



            }
        });
    }


    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Реєстрація");
        dialog.setMessage("Введіть всі дані");
        LayoutInflater inflator = LayoutInflater.from(this);
        View register_window = inflator.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        final MaterialEditText email = register_window.findViewById(R.id.field_email);
        final MaterialEditText name = register_window.findViewById(R.id.field_login);
        final MaterialEditText number = register_window.findViewById(R.id.field_phonenumber);
        final MaterialEditText password = register_window.findViewById(R.id.field_password);

        dialog.setNegativeButton("Відхилити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which)
            {
               dialog1.dismiss();
            }
        });
        dialog.setPositiveButton("Прийняти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(root, "Введіть пошту", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(name.getText().toString())) {
                    Snackbar.make(root, "Введіть ім'я", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(number.getText().toString())) {
                    Snackbar.make(root, "Введіть номер телефону", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (password.getText().toString().length() < 5) {
                    Snackbar.make(root, "Пароль повинен містити щонайменше 5 символів", Snackbar.LENGTH_LONG).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setNumber(number.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPassword(password.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Snackbar.make(root, "Користувач успішно створений", Snackbar.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Помилка : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Помилка авторизації: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

        }
    });

        dialog.show();
 }
}












