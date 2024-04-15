package com.example.mobhom2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText nameText, surNameText, emailText, passwordText, studentIDText;
    Button regButton;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    BaseAccount account;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.editTextEmail);
        nameText = findViewById(R.id.editTextName);
        surNameText = findViewById(R.id.editTextSurName);
        passwordText = findViewById(R.id.editPassword);
        studentIDText = findViewById(R.id.editTextStudentID);
        regButton = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 progressBar.setVisibility(View.VISIBLE);
                 String email, password, name, surName, studentID;
                 email = String.valueOf(emailText.getText());
                 name = String.valueOf(nameText.getText());
                 surName = String.valueOf(surNameText.getText());
                 password = String.valueOf(passwordText.getText());
                 studentID = String.valueOf(studentIDText.getText());

                 if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)
                 || TextUtils.isEmpty(surName) || TextUtils.isEmpty(studentID)){
                     Toast.makeText(Register.this, "Bilgilerinizi giriniz", Toast.LENGTH_SHORT);
                     return;
                 }
                 checkUser();

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        sendVerificationEmail();
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(Register.this, "Hesap oluşturuldu.",
                                                Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            String uid = user.getUid();
                                            // 2. Firestore veya Realtime Database'e bilgileri kaydet
                                            // Örneğin Firestore kullanalım:
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            Map<String, Object> userData = new HashMap<>();
                                            String accountLevel = getAccountLevel(email);
                                            switch (accountLevel){
                                                case "Student":
                                                     account = new StudentAccount(email,name,surName,studentID,password);
                                                    break;
                                                case "Instructor":
                                                     account = new InstructorAccount(email,name,surName,studentID,password);
                                                     break;
                                                case "Unknown":
                                                     break;

                                            }
                                            userData.put("email", email);
                                            userData.put("name", name);
                                            userData.put("surName", surName);
                                            userData.put("studentID", studentID);
                                            userData.put("password", password);
                                            userData.put("accountLevel",accountLevel);

                                            db.collection("users").document(uid)
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Kayıt başarılı
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Hata durumu
                                                    });
                                        }
                                        //updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Hesap oluşturulamadı.",
                                                Toast.LENGTH_SHORT).show();
                                        //updateUI(null);
                                    }
                                }
                            });
                }
        });
    }
    private void checkUser() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        // E-posta geçerliliğini kontrol etme
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Geçerli bir e-posta giriniz.");
            emailText.requestFocus();
            return;
        }

        // E-posta alanı kontrolü
        if (!email.endsWith("@std.yildiz.edu.tr") && !email.endsWith("@yildiz.edu.tr")) {
            emailText.setError("Sadece @std.yildiz.edu.tr veya @yildiz.edu.tr uzantılı e-posta adresleri kullanılabilir.");
            emailText.requestFocus();
            return;
        }

        // Şifre uzunluğunu kontrol etme
        if (password.length() < 6) {
            passwordText.setError("Şifre en az 6 karakter olmalıdır.");
            passwordText.requestFocus();
            return;
        }
    }
    private void sendVerificationEmail() {
        // Firebase Authentication'dan mevcut kullanıcıyı al
        final String userEmail = mAuth.getCurrentUser().getEmail();

        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // E-posta gönderildi
                            Toast.makeText(Register.this, "E-posta doğrulama e-postası gönderildi. Lütfen e-postanızı kontrol edin.",
                                    Toast.LENGTH_LONG).show();
                            // Kullanıcıyı çıkış yap
                            mAuth.signOut();
                            // Kullanıcıyı uygulama içindeki giriş ekranına yönlendir
                            startActivity(new Intent(Register.this, Login.class));
                            finish();
                        } else {
                            // E-posta gönderilemedi
                            Toast.makeText(Register.this, "E-posta doğrulama e-postası gönderilemedi. Lütfen tekrar deneyin.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public String getAccountLevel(String email) {
        if (email.endsWith("@std.yildiz.edu.tr")) {
            return "Student";
        } else if (email.endsWith("@yildiz.edu.tr")) {
            return "Instructor";
        } else {
            return "Unknown";
        }
    }
}