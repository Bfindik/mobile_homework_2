package com.example.mobhom2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class Admin {
    static String email = "admin@gmail.com";
    static String password = "admin123";
    private static final String ADMIN_CREATED_KEY = "admin_created";

    public static void createAdminAccount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Admin hesabının daha önce oluşturulup oluşturulmadığını kontrol et
        boolean adminCreated = sharedPreferences.getBoolean(ADMIN_CREATED_KEY, false);
        if (!adminCreated) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Admin hesabı başarıyla oluşturuldu
                            Log.d("AdminAccount", "Admin account created successfully.");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(ADMIN_CREATED_KEY, true);
                            editor.apply();
                        } else {
                            // Hata durumunda kullanıcıya bildirim verilebilir
                            Log.e("AdminAccount", "Admin account creation failed.", task.getException());
                        }
                    });
        }
    }
}
