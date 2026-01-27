package com.emergency.alert;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Button btnDisasterAlerts, btnPolice, btnAmbulance, btnEmergencyGuidelines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        initializeViews();
        setupWebView();
        setupListeners();
    }

    private void initializeViews() {
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        btnDisasterAlerts = findViewById(R.id.btn_disaster_alerts);
        btnPolice = findViewById(R.id.btn_police);
        btnAmbulance = findViewById(R.id.btn_ambulance);
        btnEmergencyGuidelines = findViewById(R.id.btn_emergency_guidelines);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {

        btnDisasterAlerts.setOnClickListener(v ->
                loadUrl("https://sachet.ndma.gov.in/")   // National Disaster Management Authority
        );

        btnPolice.setOnClickListener(v ->
                loadUrl("https://digitalpolice.gov.in/") // Police / Law
        );

        btnAmbulance.setOnClickListener(v ->
                loadUrl("https://mohfw.gov.in/") // National Health Portal (India)
        );

        btnEmergencyGuidelines.setOnClickListener(v ->
                loadUrl("https://www.india.gov.in/") // Ministry of Home Affairs
        );
    }

    private void loadUrl(String url) {
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
