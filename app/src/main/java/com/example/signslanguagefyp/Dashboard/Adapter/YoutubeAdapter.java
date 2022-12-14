package com.example.signslanguagefyp.Dashboard.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.signslanguagefyp.Dashboard.DashboardActivity;
import com.example.signslanguagefyp.Dashboard.Model.YoutubeModel;
import com.example.signslanguagefyp.Dashboard.YoutubeLearningActivity;
import com.example.signslanguagefyp.HelperClasses.CommonHelper;
import com.example.signslanguagefyp.HelperClasses.Constants;
import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.databinding.RowVideoItemBinding;

import java.util.ArrayList;

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.ViewHolder> {

    Context context;
    Activity activity;
    ArrayList<YoutubeModel> model;

    public YoutubeAdapter(Context context, Activity activity, ArrayList<YoutubeModel> model) {
        this.context = context;
        this.activity = activity;
        this.model = model;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YoutubeModel youtube = model.get(position);
        String videoId = youtube.getVideo_id();
        String videoTitle = youtube.getVideo_title();
        String videoDesc = youtube.getVideo_desc();
        String videoThumb = youtube.getVideo_thumbnail();

        holder.binding.videoTitle.setText(videoTitle);
        holder.binding.videoDesc.setText(videoDesc);
        setImage(videoThumb,holder.binding.videoThumbnail);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, YoutubeLearningActivity.class);
                intent.putExtra(Constants.VIDEO_ID,videoId);
                intent.putExtra(Constants.VIDEO_TITLE,videoTitle);
                intent.putExtra(Constants.VIDEO_DESC,videoDesc);
                intent.putExtra(Constants.VIDEO_THUMB,videoThumb);
                activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                activity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RowVideoItemBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowVideoItemBinding.bind(itemView);
        }
    }

    public void setImage(String url, ImageView videoThumbnail){
        Glide.with(activity).load(url)
                .apply(new RequestOptions()
                        .fitCenter()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL))
                .placeholder(R.drawable.ic_thumbnail)
                .into(videoThumbnail);
    }

}

