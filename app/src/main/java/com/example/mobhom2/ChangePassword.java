package com.example.mobhom2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePassword extends AppCompatActivity {

    Button changeBtn;

    TextView goToRegister;
    EditText editTextEmail;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        goToRegister = findViewById(R.id.goToRegister);
        changeBtn = findViewById(R.id.btnChangePassword);
        mAuth = FirebaseAuth.getInstance();

        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress= editTextEmail.getText().toString().trim();
                if(TextUtils.isEmpty(emailAddress)){
                    Toast.makeText(ChangePassword.this, "Email adresinizi giriniz", Toast.LENGTH_SHORT);
                    return;
                }
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
            }
        });



    }

}