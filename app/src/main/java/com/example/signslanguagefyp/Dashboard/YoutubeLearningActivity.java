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
import android.widget.Toast;

import com.example.signslanguagefyp.Dashboard.Adapter.YoutubeAdapter;
import com.example.signslanguagefyp.Dashboard.Model.YoutubeModel;
import com.example.signslanguagefyp.HelperClasses.Constants;
import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.databinding.ActivityYoutubeLearningBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import java.util.ArrayList;

public class YoutubeLearningActivity extends YouTubeBaseActivity {

    ActivityYoutubeLearningBinding binding;
    String videoId, videoTitle, videoDesc, videoThumb;

    ArrayList<YoutubeModel> model;
    YoutubeAdapter adapter;
    public static final String TAG = "YoutubeLearning";
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYoutubeLearningBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(view);
        init();

        YouTubePlayer.OnInitializedListener listener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(videoId);
                binding.videoTitle.setText(videoTitle);
                binding.videoTitleDesc.setText(videoDesc);
                youTubePlayer.play();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(YoutubeLearningActivity.this, "Initialization Failed", Toast.LENGTH_SHORT).show();
            }
        };



        binding.youtubePlayerView.initialize(Constants.API, listener);

        Task<QuerySnapshot> documentReference = firebaseFirestore.collection(Constants.VIDEOS_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (!documentSnapshot.getString("video_id").equals(videoId)) {
                        model.add(new YoutubeModel(documentSnapshot.getString("video_title"),
                                documentSnapshot.getString("video_desc"),
                                documentSnapshot.getString("video_id"),
                                documentSnapshot.getString("video_thumbnail")));
                        adapter.notifyDataSetChanged();
                    }
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
                startActivity(new Intent(YoutubeLearningActivity.this,YoutubeVideosListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

    }

    private void init() {
        videoId = getIntent().getStringExtra(Constants.VIDEO_ID);
        videoTitle = getIntent().getStringExtra(Constants.VIDEO_TITLE);
        videoDesc = getIntent().getStringExtra(Constants.VIDEO_DESC);
        videoThumb = getIntent().getStringExtra(Constants.VIDEO_THUMB);
        firebaseFirestore = FirebaseFirestore.getInstance();
        model = new ArrayList<>();
        adapter = new YoutubeAdapter(this, this, model);
        LinearLayoutManager videoLayout = new LinearLayoutManager(this);
        videoLayout.setOrientation(LinearLayoutManager.VERTICAL);
        binding.videosRecyclerView2.setLayoutManager(videoLayout);
        binding.videosRecyclerView2.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}