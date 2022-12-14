package com.example.signslanguagefyp.Dashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.signslanguagefyp.Dashboard.Adapter.ModeAdapter;
import com.example.signslanguagefyp.Dashboard.Model.ModesModel;
import com.example.signslanguagefyp.HelperClasses.CommonHelper;
import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.detectObject.DetectorActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    //region Interface Variables
    TextView user_name;
    CircularImageView user_image;
    RecyclerView modes_recycler;
    ImageButton sign_language;
    ModeAdapter adapter;
    //endregion

    //region Google and Firebase
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    String userID;
    GoogleSignInAccount signInAccount;
    private FirebaseFirestore firebaseFirestore;
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //init Widgets of Interface
        initWidgets();

        //region Setting Up Recycler Adapter
        List<ModesModel> model = new ArrayList<>();
        model.add(new ModesModel(R.drawable.ic_signs, "Signs School"));
        model.add(new ModesModel(R.drawable.ic_learning, "SignsTube"));
        model.add(new ModesModel(R.drawable.ic_conversion, "Text to Sign"));


        adapter = new ModeAdapter(model, this,this);
        LinearLayoutManager categoryLayout = new LinearLayoutManager(this);
        categoryLayout.setOrientation(LinearLayoutManager.HORIZONTAL);
        modes_recycler.setLayoutManager(categoryLayout);
        modes_recycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //endregion


        //Starting Up Google Services
        createRequestFromGoogle();

        //Setup for Firebase
        userID = mAuth.getCurrentUser().getUid();
        //region Checking If Users Exists or not in DB If yes then data is collected from there
        DocumentReference documentReference = firebaseFirestore.collection("USERS").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists() && error == null) {
                    CommonHelper.userName = value.getString("userName");
                    CommonHelper.userEmail = value.getString("email");
                    CommonHelper.image = Uri.parse(value.getString("image"));
                    changeData();

                    if (value.getMetadata().hasPendingWrites()) {
                        changeData();
                    }

                } else {
                    storeNewUsersData(); //For Future Logins
                }
            }
        });

        //endregion
        sign_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(DashboardActivity.this, CombineLetterActivity.class));
                startActivity(new Intent(DashboardActivity.this, CombineLetterActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    private void initWidgets() {
        sign_language = findViewById(R.id.sign_detection_btn);
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_image = findViewById(R.id.user_image);
        user_name = findViewById(R.id.username_tv);
        modes_recycler = findViewById(R.id.modes_recycler);

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

    private void storeNewUsersData() {
        //region Storing Data of User to Firestore
        userID = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("USERS").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("userName", signInAccount.getDisplayName());
        user.put("email", signInAccount.getEmail());
        user.put("image", signInAccount.getPhotoUrl().toString());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        //endregion


    }

    private void changeData() {

        if (CommonHelper.image != null) {
            user_name.setText(CommonHelper.userName);
            Glide.with(DashboardActivity.this).load(CommonHelper.image)
                    .apply(new RequestOptions()
                            .fitCenter()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                    .placeholder(R.drawable.logo)
                    .into(user_image);
        }
    }

    public void mainProfile(View view) {
        startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
        finish();
    }
}