package vn.edu.lianac;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment for viewing PDFs using WebView with Google Docs Viewer.
 * Uses Google's PDF viewer to display PDFs without downloading.
 */
public class WebViewerFragment extends Fragment {
    private static final String TAG = "PdfViewerFragment";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView titleText;

    private String pdfUrl;
    private String articleTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pdfUrl = getArguments().getString("pdf_url");
            articleTitle = getArguments().getString("article_title", "PDF Viewer");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = view.findViewById(R.id.pdfWebView);
        progressBar = view.findViewById(R.id.pdfProgressBar);
        titleText = view.findViewById(R.id.pdfTitle);

        // Set title
        if (articleTitle != null) {
            titleText.setText(articleTitle);
        }

        // Setup back button
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Setup WebView
        setupWebView();

        // Load PDF
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            loadPdf(pdfUrl);
        } else {
            Toast.makeText(getContext(), "No PDF URL provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // Enable JavaScript (required for Google Docs Viewer)
        settings.setJavaScriptEnabled(true);

        // Enable zooming
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false); // Hide zoom buttons
        settings.setSupportZoom(true);

        // Enable various features
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);

        // Set cache mode for better performance
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // WebViewClient to handle page loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "PDF loaded successfully");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Error loading PDF: " + description,
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "WebView error: " + description);
            }
        });

        // WebChromeClient to handle progress updates
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadPdf(String url) {
        Log.d(TAG, "Loading PDF: " + url);
        progressBar.setVisibility(View.VISIBLE);

        // Google Docs Viewer
        String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + url;

        webView.loadUrl(googleDocsUrl);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
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