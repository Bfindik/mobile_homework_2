package com.example.mobhom2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class AccountFragment extends Fragment {



    private ImageView mImageView;
    private ImageView instragramAcc;
    private ImageView twitterAcc;
    private TextView eduType;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private Button mButton;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        mImageView = view.findViewById(R.id.profilImage);
        instragramAcc = view.findViewById(R.id.instagramAccount);
        twitterAcc = view.findViewById(R.id.xAccount);
        mButton = view.findViewById(R.id.editButton);
        eduType = view.findViewById(R.id.eduInformation);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        // Set click listeners for Instagram and Twitter buttons
        instragramAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstagramProfile();
            }
        });

        twitterAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTwitterProfile();
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragment secondFragment = new EditProfileFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, secondFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });



        loadUserData();

        return view;
    }
    private void openInstagramProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            // Firestore'dan kullanıcı verilerini al
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String instagram = documentSnapshot.getString("instagram");
                            if (instagram == null || instagram.isEmpty()) {
                                instagram = "https://www.instagram.com/";
                            }
                            openUri(Uri.parse(instagram));
                        }
                    });
        }
    }

    private void openTwitterProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            // Firestore'dan kullanıcı verilerini al
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String twitter = documentSnapshot.getString("twitter");
                            if (twitter == null || twitter.isEmpty()) {
                                twitter = "https://www.twitter.com/";
                            }
                            openUri(Uri.parse(twitter));
                        }
                    });
        }
    }

    private void openUri(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.android.chrome"); // Specify the package name of the web browser app
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No app available to handle this action", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Firestore'dan kullanıcı verilerini al
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String accountLevel = documentSnapshot.getString("accountLevel");
                            String name = documentSnapshot.getString("name");
                            String surname = documentSnapshot.getString("surName");
                            String email = documentSnapshot.getString("email");
                            String department = documentSnapshot.getString("department");
                            String classYear = documentSnapshot.getString("class");
                            String eduType = documentSnapshot.getString("educationType");
                            String phone = documentSnapshot.getString("phone");
                            boolean isEmailPrivate = Boolean.TRUE.equals(documentSnapshot.getBoolean("emailPrivate"));
                            boolean isPhonePrivate = Boolean.TRUE.equals(documentSnapshot.getBoolean("phonePrivate"));

                            // Verileri görsel öğelere ata
                            TextView accountLevelText = getView().findViewById(R.id.accountLevel);
                            TextView nameSurnameText = getView().findViewById(R.id.name_surname);
                            TextView emailText = getView().findViewById(R.id.email_account);
                            TextView eduText = getView().findViewById(R.id.eduInformation);
                            TextView phoneText = getView().findViewById(R.id.phone_number);


                            accountLevelText.setText(accountLevel);
                            nameSurnameText.setText(name + " " + surname);
                            eduText.setText(eduType + " - " + department + " - " + classYear);
                            // Update UI elements accordingly
                            emailText.setText(isEmailPrivate ? "Private" : email);
                            phoneText.setText(isPhonePrivate ? "Private" : phone);


                            // Profil resmini yükle
                            loadProfileImage();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Hata durumu
                        Log.e("Firestore", "Error loading user data: " + e.getMessage());
                    });
        }
    }
    private void loadProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            StorageReference userImagesRef = mStorageRef.child("images").child(uid).child("profile_image.jpg");

            userImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(getContext()) // Change requireContext() to getContext()
                        .load(uri)
                        .into(mImageView);
            }).addOnFailureListener(exception -> {
                Log.e("Firebase", "Image loading failed: " + exception.getMessage());
            });
        }
    }








}
