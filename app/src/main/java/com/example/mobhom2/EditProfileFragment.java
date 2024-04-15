package com.example.mobhom2;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private Button editButton;
    private EditText nameText, surNameText, phoneText, studentIdText, instagramText, twitterText;
    private Spinner educationTypeText, classText, departmentText;
    private CheckBox emailPrivate, phonePrivate;
    private ImageView mImageView;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_CAMERA_CAPTURE = 102;
    private static final int REQUEST_GALLERY_PICK = 103;

    private String[] educationItems = {"Lisans", "Yüksek Lisans", "Doktora"};
    private String[] classItems = {"Hazırlık", "1", "2", "3", "4"};

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        initializeViews(view);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserDataToFirestore();
                navigateToAccountFragment();
            }
        });

        loadUserData();

        setSpinnerAdapters();

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSelectionDialog();
            }
        });

        return view;
    }

    private void initializeViews(View view) {
        editButton = view.findViewById(R.id.editButton);
        nameText = view.findViewById(R.id.editTextName);
        surNameText = view.findViewById(R.id.editTextSurName);
        phoneText = view.findViewById(R.id.editTextPhone);
        studentIdText = view.findViewById(R.id.editTextStudentno);
        educationTypeText = view.findViewById(R.id.spinner1);
        classText = view.findViewById(R.id.spinner2);
        instagramText = view.findViewById(R.id.editTextInstagram);
        twitterText = view.findViewById(R.id.editTextTwitter);
        departmentText = view.findViewById(R.id.spinner3);
        mImageView = view.findViewById(R.id.profilImage);
        emailPrivate = view.findViewById(R.id.checkbox2);
        phonePrivate = view.findViewById(R.id.checkbox1);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private void navigateToAccountFragment() {
        AccountFragment secondFragment = new AccountFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, secondFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void saveUserDataToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);

            // Kullanıcı verilerini toplamak
            String name = nameText.getText().toString();
            String surname = surNameText.getText().toString();
            String phone = phoneText.getText().toString();
            String studentId = studentIdText.getText().toString();
            String educationType = educationTypeText.getSelectedItem().toString();
            String classYear = classText.getSelectedItem().toString();
            String instagram = instagramText.getText().toString();
            String twitter = twitterText.getText().toString();



            // Kullanıcı verilerini bir HashMap'e ekleme
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("surname", surname);
            userData.put("phone", phone);
            userData.put("studentID", studentId);
            userData.put("educationType", educationType);
            userData.put("class", classYear);
            userData.put("instagram", instagram);
            userData.put("twitter", twitter);
            userData.put("emailPrivate", emailPrivate.isChecked());
            userData.put("phonePrivate", phonePrivate.isChecked());

            // Firestore'a kullanıcı verilerini kaydetme
            userRef.update(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "User data saved successfully");
                        Toast.makeText(requireContext(), "User data saved successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving user data: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error saving user data", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String studentId = documentSnapshot.getString("studentID");
                            String name = documentSnapshot.getString("name");
                            String surname = documentSnapshot.getString("surName");
                            String phone = documentSnapshot.getString("phone");
                            String department = documentSnapshot.getString("department");
                            String classYear = documentSnapshot.getString("class");
                            String educationType = documentSnapshot.getString("educationType");

                            nameText.setText(name);
                            surNameText.setText(surname);
                            phoneText.setText(phone);
                            studentIdText.setText(studentId);

                            loadProfileImage();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading user data: " + e.getMessage());
                    });
        }
    }

    private void setSpinnerAdapters() {
        ArrayAdapter<String> educationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, educationItems);
        educationTypeText.setAdapter(educationAdapter);

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, classItems);
        classText.setAdapter(classAdapter);
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image");
        builder.setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (checkCameraPermission()) {
                        openCamera();
                    } else {
                        requestCameraPermission();
                    }
                } else {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_GALLERY_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        mImageView.setImageBitmap(imageBitmap);
                        uploadImageToFirebaseStorage(imageBitmap);
                    }
                    break;
                case REQUEST_GALLERY_PICK:
                    if (data != null && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        CropImage.activity(selectedImageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(requireContext(), this);
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (result != null && result.getUri() != null) {
                        mImageView.setImageURI(result.getUri());
                        uploadImageToFirebaseStorage(result.getUri());
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            StorageReference userImagesRef = mStorageRef.child("images").child(uid).child("profile_image.jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = userImagesRef.putBytes(data);
            uploadTask.addOnFailureListener(exception -> {
                Log.e("Firebase", "Upload failed: " + exception.getMessage());
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d("Firebase", "Upload success!");
                userImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveImageUrlToFirestore(downloadUrl);
                });
            });
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            StorageReference userImagesRef = mStorageRef.child("images").child(uid).child("profile_image.jpg");

            UploadTask uploadTask = userImagesRef.putFile(imageUri);
            uploadTask.addOnFailureListener(exception -> {
                Log.e("Firebase", "Upload failed: " + exception.getMessage());
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d("Firebase", "Upload success!");
                userImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveImageUrlToFirestore(downloadUrl);
                });
            });
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.update("profileImage", imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Image URL saved to Firestore: " + imageUrl);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving image URL to Firestore: " + e.getMessage());
                    });
        }
    }

    private void loadProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            StorageReference userImagesRef = mStorageRef.child("images").child(uid).child("profile_image.jpg");

            userImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(requireContext())
                        .load(uri)
                        .into(mImageView);
            }).addOnFailureListener(exception -> {
                Log.e("Firebase", "Image loading failed: " + exception.getMessage());
            });
        }
    }
}
