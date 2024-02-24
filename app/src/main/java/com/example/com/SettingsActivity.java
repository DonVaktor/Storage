package com.example.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Підтвердження виходу")
                .setMessage("Ви дійсно хочете вийти з акаунту?")
                .setPositiveButton("Так", (dialog, which) -> {
                    // Виконати код для виходу з акаунту в Firebase
                    FirebaseAuth.getInstance().signOut();
                    // Перейти до активності входу або іншої активності
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Ні", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show());

    }
}
