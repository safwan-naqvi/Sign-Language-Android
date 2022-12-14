package com.example.signslanguagefyp.Dashboard;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.signslanguagefyp.HelperClasses.CommonHelper;
import com.example.signslanguagefyp.MainActivity;
import com.example.signslanguagefyp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mikhaellopez.circularimageview.CircularImageView;

public class ProfileActivity extends AppCompatActivity {

    //region Interface Variables
    Button logout;
    CircularImageView userProfileImage;
    TextView userName, userEmail;
    RelativeLayout editProfile;
    ImageView BackButton;
    //endregion

    //region Firebase and bg
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore firebaseFirestore;
    String userID;
    GoogleSignInAccount signInAccount;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        createRequestForGoogleAuth();
        //Init Interface Variables
        initWidget();


        //region Fetch Data from Fire store if available otherwise get from Google
        userID = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("USERS").document(userID);
        documentReference.addSnapshotListener(ProfileActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {

                    if (error != null) {
                        Log.i("appCheck", "Listen failed.", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        CommonHelper.userName = value.getString("userName");
                        CommonHelper.userEmail = value.getString("email");
                        CommonHelper.image = Uri.parse(value.getString("image"));
                        changeData();
                    } else {
                        Log.i("appCheck", "Current data: null ");
                    }
                    if (value.getMetadata().hasPendingWrites()) {
                        changeData();
                    }

                }
            }
        });
        //endregion

        //region Edit Profile
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this,EditProfileActivity.class));
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });
        //endregion


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });
    }

    private void createRequestForGoogleAuth() {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        // Configure Google Sign In and give a popup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("385137050622-lkbmqb3gh2d1068louc2qsp7ne7aijn6.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Building SignInClient with options specified by geo this will help us to
        // create request from app to google to access gmail.
        mGoogleSignInClient = GoogleSignIn.getClient(ProfileActivity.this, gso);
    }

    private void initWidget() {
        editProfile = findViewById(R.id.RL_btn_edit_profile);
        BackButton = findViewById(R.id.goBackToUserAct);
        userProfileImage = findViewById(R.id.circularUserProfile);
        userEmail = findViewById(R.id.user_profile_email);
        userName = findViewById(R.id.user_profile_name);
        logout = findViewById(R.id.logout_user_btn);
    }

    private void changeData() {
        if (signInAccount != null) {
            userName.setText(CommonHelper.userName);
            Glide.with(ProfileActivity.this).load(CommonHelper.image)
                    .apply(new RequestOptions()
                            .fitCenter()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                    .placeholder(R.drawable.logo)
                    .into(userProfileImage);
        }
        userEmail.setText(CommonHelper.userEmail);
    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
    }

    public void backToDashboard(View view) {
        startActivity(new Intent(ProfileActivity.this,DashboardActivity.class));
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }
}