package com.example.signslanguagefyp.Dashboard.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.signslanguagefyp.Dashboard.Model.ModesModel;
import com.example.signslanguagefyp.Dashboard.Model.YoutubeModel;
import com.example.signslanguagefyp.Dashboard.SignSchoolActivity;
import com.example.signslanguagefyp.Dashboard.TextToSignActivity;
import com.example.signslanguagefyp.Dashboard.YoutubeLearningActivity;
import com.example.signslanguagefyp.Dashboard.YoutubeVideosListActivity;
import com.example.signslanguagefyp.R;


import java.util.List;

public class ModeAdapter extends RecyclerView.Adapter<ModeAdapter.ViewHolder> {

    private List<ModesModel> model;
    private Context context;
    private Activity activity;

    public ModeAdapter(List<ModesModel> model, Context context,Activity activity) {
        this.model = model;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ModeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_modes_list, parent, false);
        return new ModeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModeAdapter.ViewHolder holder, int position) {
        int resource = model.get(position).getImage();
        String title = model.get(position).getMode_name();

        holder.setModeImg(resource);
        holder.setModeTitle(title);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (title){
                    case "Text to Sign":
                        context.startActivity(new Intent(context, TextToSignActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case "SignsTube":
                        context.startActivity(new Intent(context, YoutubeVideosListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case "Signs School":
                        context.startActivity(new Intent(context, SignSchoolActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout container;
        ImageView modeImg;
        TextView modeName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.mode_container);
            modeImg = itemView.findViewById(R.id.mode_image);
            modeName = itemView.findViewById(R.id.mode_name);
        }
        private void setModeImg(int resource) {
            modeImg.setImageResource(resource);
        }

        private void setModeTitle(String title) {
            modeName.setText(title);
        }

    }
}
