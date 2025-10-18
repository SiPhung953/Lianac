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

/**
 * Fragment to display PDF files using WebView with Google Docs Viewer
 */
public class WebViewerFragment extends Fragment {
    private static final String TAG = "WebViewerFragment";
    private static final String ARG_PDF_URL = "pdf_url";
    private static final String ARG_ARTICLE_TITLE = "article_title";

    private WebView webView;
    private ProgressBar progressBar;
    private String pdfUrl;
    private String articleTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pdfUrl = getArguments().getString(ARG_PDF_URL);
            articleTitle = getArguments().getString(ARG_ARTICLE_TITLE);
        }
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

        setupWebView();

        if (pdfUrl != null) {
            loadPdf(pdfUrl);
        }

        return view;
    }

    private void setupWebView() {
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
    }

    private void loadPdf(String url) {
        // Ensure HTTPS
        if (url.startsWith("http://")) {
            url = url.replaceFirst("http://", "https://");
        }

        // Use Google Docs Viewer for PDF
        if (url.toLowerCase().endsWith(".pdf")) {
            String finalUrl = "https://docs.google.com/gview?embedded=true&url=" + url;
            Log.d(TAG, "Rendering PDF via Google Docs: " + finalUrl);
            webView.loadUrl(finalUrl);
        } else {
            // If not PDF, load normally
            Log.d(TAG, "Opening standard URL: " + url);
            webView.loadUrl(url);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }
}