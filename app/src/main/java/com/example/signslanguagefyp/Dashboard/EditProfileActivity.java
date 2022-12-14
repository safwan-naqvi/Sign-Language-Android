package com.example.signslanguagefyp.Dashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.signslanguagefyp.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import io.paperdb.Paper;

public class EditProfileActivity extends AppCompatActivity {

    //region Interface Variables
    ConstraintLayout progress_bar;
    TextView uploadImageText, userEmail, progressIndication;
    CircularImageView adminDP;
    TextInputLayout username;
    Button updateBtn;
    //endregion

    //region Firebase and Google
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    private StorageReference AdminImagesRef;
    private DocumentReference AdminRef;
    //endregion

    //region General Variables
    private Uri imageUri;
    String userName, url, DownloadImageUrl, _email;
    String _username;
    private String checker = "null";
    String userID;
    //endregion

    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount signInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Paper.init(this);

        //init Widgets of Interface
        initWidget();
        //Starting Up Google Services
        createRequestFromGoogle();

        //Setup for Firebase
        userID = mAuth.getCurrentUser().getUid();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //region Fetch Data from Fire store if available otherwise get from Google
        DocumentReference documentReference = firebaseFirestore.collection("USERS").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {

                    if (error != null) {
                        Log.i("appCheck", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        changeData(value);
                    } else {
                        Log.i("appCheck", "Current data: null ");
                    }
                }
            }
        });
        //endregion

        uploadImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "checked";
                ImagePicker.Companion.with(EditProfileActivity.this).cropSquare().compress(1024).start();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _username = username.getEditText().getText().toString().trim();
                if (checker.equals("checked")) {
                    saveImageInFirebase();
                } else {
                    storeInfoInFirebase();
                }

            }
        });


    }

    private void createRequestFromGoogle() {
        mAuth = FirebaseAuth.getInstance();
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        firebaseFirestore = FirebaseFirestore.getInstance();
        // Configure Google Sign In and give a popup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("385137050622-lkbmqb3gh2d1068louc2qsp7ne7aijn6.apps.googleusercontent.com")
                .requestEmail()
                .build();
        //Building SignInClient with options specified by geo this will help us to
        //create request from app to google to access gmails.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initWidget() {
        AdminImagesRef = FirebaseStorage.getInstance().getReference().child("USER IMAGE");
        firebaseFirestore = FirebaseFirestore.getInstance();
        progress_bar = findViewById(R.id.admin_saving_info);
        progressIndication = findViewById(R.id.progress_percentage);
        userEmail = findViewById(R.id.admin_email_tv);
        uploadImageText = findViewById(R.id.upload_image_tv);
        adminDP = findViewById(R.id.adminEditProfileImage);
        username = findViewById(R.id.et_profile_name);
        updateBtn = findViewById(R.id.update_profile);
    }

    public void backToDashboard(View view) {
        startActivity(new Intent(EditProfileActivity.this,ProfileActivity.class));
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }

    private void saveImageInFirebase() {
        progress_bar.setVisibility(View.VISIBLE);
        //region Making Unique name for image
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        String saveCurrentTime = currentTime.format(calendar.getTime());
        //endregion

        //region Making Unique key for image
        String productRandomKey = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = AdminImagesRef.child(imageUri.getLastPathSegment() + productRandomKey);

        AdminRef = FirebaseFirestore.getInstance().collection("USERS").document(userID);
        //endregion

        //region Uploading Image to Database

        final UploadTask uploadTask = filePath.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress_bar.setVisibility(View.INVISIBLE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //if image is uploaded it will get that link of image to be stored in database

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            progressIndication.setVisibility(View.INVISIBLE);
                            progress_bar.setVisibility(View.INVISIBLE);
                            throw task.getException();
                        }
                        DownloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            DownloadImageUrl = task.getResult().toString();
                            storeInfoInFirebase();
                        }
                        progress_bar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressIndication.setVisibility(View.VISIBLE);
                progressIndication.setText((int) progress + "% Uploaded");
            }
        });

        //endregion

    }

    private void storeInfoInFirebase() {
        //region Updating Database of admin
        HashMap<String, Object> profile = new HashMap<>();
        profile.put("userName", _username);
        if (checker.equals("checked")) {
            profile.put("image", DownloadImageUrl);
        }
        firebaseFirestore.collection("USERS").document(userID).update(profile);
        //endregion
        //recreate();
        progress_bar.setVisibility(View.VISIBLE);
        progressIndication.setVisibility(View.VISIBLE);
        progressIndication.setText("Updated Successfully");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.INVISIBLE);
                progressIndication.setVisibility(View.INVISIBLE);
            }
        }, 1000);
        startActivity(new Intent(EditProfileActivity.this, DashboardActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void changeData(DocumentSnapshot value) {
        userName = value.getString("userName");
        url = value.getString("image");
        _email = value.getString("email");

        username.getEditText().setText(userName);
        userEmail.setText(_email);
        if (url != null) {
            Glide.with(this).load(url)
                    .apply(new RequestOptions()
                            .fitCenter()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                    .placeholder(R.drawable.logo)
                    .into(adminDP);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            adminDP.setImageURI(imageUri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            checker = "null";
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

}