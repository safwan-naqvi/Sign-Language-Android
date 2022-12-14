package com.example.signslanguagefyp.Dashboard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.signslanguagefyp.R;
import com.example.signslanguagefyp.databinding.ActivitySignSchoolBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SignSchoolActivity extends AppCompatActivity {


    ActivitySignSchoolBinding binding;
    public static final String TAG = "SignSchool";
    public static final String URL = "https://psl.org.pk/learningTutorials";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignSchoolBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        checkConnection();

        //region Webview Setup
        binding.signsWebview.loadUrl(URL);
        binding.signsWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(URL);
                return true;
            }
        }); //To open links on same webview

        binding.signsWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        binding.toggleMenu.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.nav_prev:
                            onBackPressed();
                            break;
                        case R.id.nav_next:
                            if (binding.signsWebview.canGoForward()) {
                                binding.signsWebview.goForward();
                            }
                            break;
                        case R.id.nav_reload:
                            binding.signsWebview.reload();
                            checkConnection();
                            break;
                    }
                }
            }
        });

        //endregion

        //region Optimizing Webview
        WebSettings webSettings = binding.signsWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        binding.signsWebview.getSettings().setBuiltInZoomControls(false);
        binding.signsWebview.getSettings().setSupportZoom(false);
        binding.signsWebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        binding.signsWebview.getSettings().setAllowFileAccess(true);
        binding.signsWebview.getSettings().setDomStorageEnabled(true);
        WebViewClient webViewClient = new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        };
        binding.signsWebview.setWebViewClient(webViewClient);
        binding.signsWebview.loadUrl(URL);


        //endregion
        binding.backDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert();
            }
        });
        binding.refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });
    }

    private void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isAvailable() || mobile.isAvailable()) {
            if (wifi.isConnected() || mobile.isConnected()) {
                binding.signsWebview.setVisibility(View.VISIBLE);
                binding.noInternetContainer.setVisibility(View.GONE);
            } else {
                binding.signsWebview.setVisibility(View.GONE);
                binding.noInternetContainer.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "Your Device Have no Mobile or Wifi Services!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.signsWebview.canGoBack()) {
            binding.signsWebview.goBack();
        } else {
            alert();
        }
    }

    private void alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are You Sure?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(SignSchoolActivity.this, DashboardActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finishAffinity();
                    }
                }).show();

    }
}