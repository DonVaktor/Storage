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
import android.widget.EditText;
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
        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindowView = inflater.inflate(R.layout.register_window, null);
        dialog.setView(registerWindowView);

        final MaterialEditText email = registerWindowView.findViewById(R.id.field_email);
        final MaterialEditText name = registerWindowView.findViewById(R.id.field_login);
        final MaterialEditText number = registerWindowView.findViewById(R.id.field_phonenumber);
        final MaterialEditText password = registerWindowView.findViewById(R.id.field_password);

        dialog.setNegativeButton("Відхилити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                dialog1.dismiss();
            }
        });

        dialog.setPositiveButton("Прийняти", null); // null для того, щоб діалог не закривався автоматично

        final AlertDialog alertDialog = dialog.create(); // Створення AlertDialog

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Обробник кліку кнопки "Прийняти"
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean emailInvalid = email.getText().toString().isEmpty();
                        boolean nameInvalid = name.getText().toString().isEmpty();
                        boolean numberInvalid = number.getText().toString().isEmpty();
                        boolean passwordInvalid = password.getText().toString().isEmpty();

                        if (emailInvalid) {
                            email.setHintTextColor(Color.parseColor("#e03a40"));
                            email.setUnderlineColor(Color.parseColor("#e03a40"));
                        } else {
                            email.setHintTextColor(Color.parseColor("#c89cf7"));
                            email.setUnderlineColor(Color.parseColor("#c89cf7"));
                        }

                        if (nameInvalid) {
                            name.setHintTextColor(Color.parseColor("#e03a40"));
                            name.setUnderlineColor(Color.parseColor("#e03a40"));
                        } else {
                            name.setHintTextColor(Color.parseColor("#c89cf7"));
                            name.setUnderlineColor(Color.parseColor("#c89cf7"));
                        }

                        if (numberInvalid) {
                            number.setHintTextColor(Color.parseColor("#e03a40"));
                            number.setUnderlineColor(Color.parseColor("#e03a40"));
                        } else {
                            number.setHintTextColor(Color.parseColor("#c89cf7"));
                            number.setUnderlineColor(Color.parseColor("#c89cf7"));
                        }

                        if (passwordInvalid) {
                            password.setHintTextColor(Color.parseColor("#e03a40"));
                            password.setUnderlineColor(Color.parseColor("#e03a40"));
                        } else {
                            password.setHintTextColor(Color.parseColor("#c89cf7"));
                            password.setUnderlineColor(Color.parseColor("#c89cf7"));
                        }

                        if (emailInvalid || nameInvalid || numberInvalid || passwordInvalid) {
                            Toast.makeText(getApplicationContext(), "Будь ласка, заповніть всі поля коректно", Toast.LENGTH_LONG).show();
                            return;
                        }


                            String hashedPass = PasswordHasher.hashPassword(password.getText().toString()); // хешування паролю

                            auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            //Створення користувача
                                            User user = new User();
                                            user.setEmail(email.getText().toString());
                                            user.setNumber(number.getText().toString());
                                            user.setName(name.getText().toString());
                                            user.setPassword(hashedPass);

                                            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(getApplicationContext(),"Користувач успішно створений!", Toast.LENGTH_LONG).show();
                                                            alertDialog.dismiss(); // Закриття діалогу після успішного введення даних
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
            }
        });

        alertDialog.show(); // Показ діалогу
    }
}












