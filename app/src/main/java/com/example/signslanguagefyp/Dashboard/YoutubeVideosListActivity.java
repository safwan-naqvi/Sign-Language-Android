package com.example.signslanguagefyp.Dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.signslanguagefyp.Dashboard.Adapter.YoutubeAdapter;
import com.example.signslanguagefyp.Dashboard.Model.YoutubeModel;
import com.example.signslanguagefyp.HelperClasses.Constants;
import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.databinding.ActivityYoutubeVideosListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class YoutubeVideosListActivity extends AppCompatActivity {

    ActivityYoutubeVideosListBinding binding;
    ArrayList<YoutubeModel> model;
    YoutubeAdapter adapter;
    public static final String TAG = "YoutubeVideosList";
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYoutubeVideosListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        init();

        Task<QuerySnapshot> documentReference = firebaseFirestore.collection(Constants.VIDEOS_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    model.add(new YoutubeModel(documentSnapshot.getString("video_title"),
                            documentSnapshot.getString("video_desc"),
                            documentSnapshot.getString("video_id"),
                            documentSnapshot.getString("video_thumbnail")));
                    adapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Document Reference: on Failure Due to " + e.getMessage());
            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(YoutubeVideosListActivity.this,DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

    }

    private void init() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        model = new ArrayList<>();
        adapter = new YoutubeAdapter(this, this, model);
        LinearLayoutManager categoryLayout = new LinearLayoutManager(this);
        categoryLayout.setOrientation(LinearLayoutManager.VERTICAL);
        binding.videosRecyclerView.setLayoutManager(categoryLayout);
        binding.videosRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}