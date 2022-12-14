package com.example.signslanguagefyp.Dashboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.signslanguagefyp.Dashboard.Adapter.SignGIFAdapter;
import com.example.signslanguagefyp.Dashboard.Model.SignGIFModel;
import com.example.signslanguagefyp.Dashboard.Helper.PreferenceManager;
import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.databinding.ActivityTextToSignBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;


public class TextToSignActivity extends AppCompatActivity {

    private ActivityTextToSignBinding binding;
    AnimationDrawable animator;
    String holdInputString;
    public static final String TAG = "TextToSign";
    int wordsCurrentPosition;
    int delay = 1200;

    ArrayList<SignGIFModel> model;
    SignGIFAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextToSignBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        PreferenceManager preferenceManager = new PreferenceManager(this);
        //region model inputs
        model = new ArrayList<>();
        model.add(new SignGIFModel("Baby"));
        model.add(new SignGIFModel("Boyfriend"));
        model.add(new SignGIFModel("Cat"));
        model.add(new SignGIFModel("Cow"));
        model.add(new SignGIFModel("Dead"));
        model.add(new SignGIFModel("Fire"));
        model.add(new SignGIFModel("Giraffee"));
        model.add(new SignGIFModel("Isolated"));
        model.add(new SignGIFModel("it"));
        model.add(new SignGIFModel("Laugh_Heartly"));
        model.add(new SignGIFModel("Money"));
        model.add(new SignGIFModel("Morning"));
        model.add(new SignGIFModel("Naughty"));
        model.add(new SignGIFModel("No"));
        model.add(new SignGIFModel("Rain"));
        model.add(new SignGIFModel("Seriously"));
        model.add(new SignGIFModel("Shark"));
        model.add(new SignGIFModel("Shocked"));
        model.add(new SignGIFModel("Strong_Wind"));
        model.add(new SignGIFModel("Weekend"));
        model.add(new SignGIFModel("I_Will_Help_You"));
        model.add(new SignGIFModel("Pour_Water_For_Me"));
        model.add(new SignGIFModel("See_You_Later"));
        model.add(new SignGIFModel("Tell_Me_The_Truth"));
        model.add(new SignGIFModel("Thank_You"));
        model.add(new SignGIFModel("What_Are_You_Doing"));
        model.add(new SignGIFModel("Where_Are_You_From"));
        model.add(new SignGIFModel("Excuse_Me"));
        model.add(new SignGIFModel("Are_You_Okay"));
        model.add(new SignGIFModel("Good_Bye"));
        adapter = new SignGIFAdapter(this, model, binding.signGifAnim);
//endregion
        animationSpeedToggler(); //Toggle Speed Between 1x 2x 4x

        animationPlay(); //Click to play fetched Text

        binding.signTheatre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferenceManager.getBoolean("theatre")) {
                    binding.inputTextToConvert.setVisibility(View.VISIBLE);
                    binding.playBtnContainer.setVisibility(View.VISIBLE);
                    binding.waitAnimContainer.setVisibility(View.VISIBLE);
                    binding.toggleSpeedGroup.setVisibility(View.VISIBLE);
                    binding.textAnimSpeed.setVisibility(View.VISIBLE);
                    binding.wordsRecycler.setVisibility(View.INVISIBLE);
                    binding.signsGifContainer.setVisibility(View.INVISIBLE);
                    binding.gifMore.setVisibility(View.INVISIBLE);
                    preferenceManager.putBoolean("theatre", false);
                } else {
                    binding.inputTextToConvert.setVisibility(View.INVISIBLE);
                    binding.playBtnContainer.setVisibility(View.INVISIBLE);
                    binding.waitAnimContainer.setVisibility(View.INVISIBLE);
                    binding.toggleSpeedGroup.setVisibility(View.INVISIBLE);
                    binding.textAnimSpeed.setVisibility(View.INVISIBLE);
                    binding.signGifAnim.setBackgroundResource(0);
                    binding.wordsRecycler.setVisibility(View.VISIBLE);
                    binding.signsGifContainer.setVisibility(View.VISIBLE);
                    binding.gifMore.setVisibility(View.VISIBLE);
                    preferenceManager.putBoolean("theatre", true);
                }
            }
        });

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        binding.wordsRecycler.setLayoutManager(staggeredGridLayoutManager);
        binding.wordsRecycler.setAdapter(adapter);
        binding.gifMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://giphy.com/signwithrobert"));
                startActivity(browserIntent);
            }
        });

    }

    private void animationPlay() {
        binding.playAnimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playActive();
                holdInputString = binding.inputTextToConvert.getEditText().getText().toString().trim().toLowerCase();
                if (holdInputString.equals("") || holdInputString == null) {
                    Toast.makeText(TextToSignActivity.this, "Please Add Text To Convert", Toast.LENGTH_SHORT).show();
                    playDeActive();
                } else {
                    displayWaiting(false);
                    //region Converting String to Character Array
                    char[] characterFromString = new char[holdInputString.length()];
                    wordsCurrentPosition = 1;
                    displayAnimationProgress(holdInputString.length(), wordsCurrentPosition);
                    // Copying character by character into array
                    // using for each loop
                    animator = new AnimationDrawable();

                    for (int i = 0; i < holdInputString.length(); i++) {
                        characterFromString[i] = holdInputString.charAt(i);

                        Log.d(TAG, "Char Array " + Arrays.toString(characterFromString));

                        //region Fetching Files from drawables
                        if (!Character.isWhitespace(characterFromString[i])) {
                            String imgURI = "@drawable/" + characterFromString[i]; //e.g a
                            int imageResource = getResources().getIdentifier(imgURI, null, getPackageName());
                            Drawable imageConvertedToDrawable = getResources().getDrawable(imageResource);
                            //endregion
                            animator.addFrame(imageConvertedToDrawable, delay);
                        }else{
                            String imgURI = "@drawable/space"; //e.g a
                            int imageResource = getResources().getIdentifier(imgURI, null, getPackageName());
                            Drawable imageConvertedToDrawable = getResources().getDrawable(imageResource);
                            //endregion
                            animator.addFrame(imageConvertedToDrawable, delay);
                        }

                    }

                    animator.setOneShot(true);
                    binding.signsFrame.setImageDrawable(animator);
                    animator.start();
                    checkIfAnimationDone(animator);
                }

            }
        });
    }

    private void animationSpeedToggler() {
        binding.toggleSpeedGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.onex:
                            delay = 1200;
                            break;
                        case R.id.twox:
                            delay = 800;
                            break;
                        case R.id.fourx:
                            delay = 400;
                            break;
                    }
                }
            }
        });
    }

    private void displayAnimationProgress(int length, int current) {
        binding.wordsCounter.setText(current + " / " + length);
    }

    private void playActive() {
        binding.playAnimBtn.setColorFilter(ContextCompat.getColor(this, R.color.playBlack), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void playDeActive() {
        binding.playAnimBtn.setColorFilter(ContextCompat.getColor(this, R.color.playWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void checkIfAnimationDone(AnimationDrawable anim) {
        final AnimationDrawable a = anim;
        int timeBetweenChecks = delay;
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                if (a.getCurrent() != a.getFrame(a.getNumberOfFrames() - 1)) {
                    ++wordsCurrentPosition;
                    displayAnimationProgress(holdInputString.length(), wordsCurrentPosition);
                    checkIfAnimationDone(a);
                } else {
                    ++wordsCurrentPosition;
                    displayAnimationProgress(holdInputString.length(), wordsCurrentPosition);
                    Toast.makeText(getApplicationContext(), "ANIMATION DONE!", Toast.LENGTH_SHORT).show();
                    resetInterface();
                }
            }
        }, timeBetweenChecks);

    }

    private void resetInterface() {
        animator.stop();
        playDeActive();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.inputTextToConvert.getEditText().setText("");
                binding.inputTextToConvert.getEditText().clearFocus();
                binding.signsFrame.setImageDrawable(null);
                binding.wordsCounter.setText("");
                displayWaiting(true);
            }
        }, 1500);

    }

    private void displayWaiting(Boolean flag) {
        if (flag) {
            binding.waitAnimContainer.setVisibility(View.VISIBLE);
            binding.signsFrame.setVisibility(View.INVISIBLE);
            binding.wordsCounter.setVisibility(View.INVISIBLE);
        } else {
            binding.waitAnimContainer.setVisibility(View.INVISIBLE);
            binding.signsFrame.setVisibility(View.VISIBLE);
            binding.wordsCounter.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TextToSignActivity.this,DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}