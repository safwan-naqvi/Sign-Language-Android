package com.example.signslanguagefyp.Dashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.signslanguagefyp.Dashboard.Model.SignGIFModel;
import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.databinding.RowSignGifItemBinding;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

public class SignGIFAdapter extends RecyclerView.Adapter<SignGIFAdapter.ViewHolder> {
    Context context;
    ArrayList<SignGIFModel> model;
    GifImageView signGifAnim;

    public SignGIFAdapter(Context context, ArrayList<SignGIFModel> model, GifImageView signGifAnim) {
        this.context = context;
        this.model = model;
        this.signGifAnim = signGifAnim;
    }

    @NonNull
    @Override
    public SignGIFAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_sign_gif_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SignGIFAdapter.ViewHolder holder, int position) {
        SignGIFModel sign = model.get(position);
        String text = sign.getSignText().replace('_', ' ');
        holder.binding.signText.setText(text);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playGif(sign.getSignText().toLowerCase());
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RowSignGifItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowSignGifItemBinding.bind(itemView);
        }
    }

    public void playGif(String animName){
        Animation fadeout = new AlphaAnimation(0f, 1.f);
        fadeout.setDuration(700); // You can modify the duration here
        fadeout.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                String imgURI = "@drawable/" + animName; //e.g a
                int imageResource = context.getResources().getIdentifier(imgURI, null, context.getPackageName());
                signGifAnim.setBackgroundResource(imageResource);//your gif file
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });
        signGifAnim.startAnimation(fadeout);
    }

}
