package com.example.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends Activity {
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Ініціалізація FirebaseAuth та Realtime Database
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Знаходимо елементи інтерфейсу
        Button logoutButton = findViewById(R.id.logout_button);
        Button reportButton = findViewById(R.id.report_button);
        TextView nameTextView = findViewById(R.id.name);

        // Отримуємо поточного користувача
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userNameRef = databaseRef.child("Users").child(userId).child("name");

            // Додаємо слухача для отримання даних користувача
            userNameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.getValue(String.class);
                        nameTextView.setText(userName);
                    } else {
                        Toast.makeText(SettingsActivity.this, "Ім'я користувача не знайдено", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обробка помилки читання з бази даних
                    Toast.makeText(SettingsActivity.this, "Помилка читання даних користувача", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(SettingsActivity.this, "Користувач не залогінений", Toast.LENGTH_SHORT).show();
        }

        // Встановлюємо діалог виходу з акаунту
        logoutButton.setOnClickListener(v -> new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Підтвердження виходу")
                .setMessage("Ви дійсно хочете вийти з акаунту?")
                .setPositiveButton("Так", (dialog, which) -> {
                    // Виконання виходу з акаунту
                    auth.signOut();
                    // Перехід до активності входу або іншої активності
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Ні", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show());

        // Встановлюємо слухача для відправки електронної пошти
        reportButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822"); // Встановлюємо тип MIME

            // Вказуємо вашу електронну адресу як отримувача
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"polyyak2005@gmail.com"});

            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My Storage");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Напишіть опис вашого багу, скарги або пропозицій.");

            // Вказуємо пакет Gmail для обробки інтенції
            emailIntent.setPackage("com.google.android.gm");

            try {
                startActivity(emailIntent);
            } catch (Exception ex) {
                // Якщо не вдалося знайти додаток, відображається повідомлення про помилку
                Toast.makeText(getApplicationContext(), "Не вдалося знайти додаток Gmail для відправки електронної пошти", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
