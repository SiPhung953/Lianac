package vn.edu.lianac;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WebViewFragment extends Fragment {

    private static final String ARG_URL = "url";
    private WebView webView;
    private ProgressBar progressBar;

    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        webView = view.findViewById(R.id.webView);
        progressBar = view.findViewById(R.id.progressBar);

        String url = getArguments() != null ? getArguments().getString(ARG_URL) : null;

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Important for Google Docs Viewer
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        if (url != null) {
            // HTTPS 1st
            if (url.startsWith("http://")) {
                url = url.replaceFirst("http://", "https://");
            }

            // Use Google Docs Viewer for open pdf
            if (url.toLowerCase().endsWith(".pdf")) {
                String finalUrl = "https://docs.google.com/gview?embedded=true&url=" + url;
                Log.d("WebViewFragment", "Rendering PDF via Google Docs: " + finalUrl);
                webView.loadUrl(finalUrl);
            } else {
                // If not PDF, use normal
                Log.d("WebViewFragment", "Opening standard URL: " + url);
                webView.loadUrl(url);
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) webView.destroy();
    }
}
