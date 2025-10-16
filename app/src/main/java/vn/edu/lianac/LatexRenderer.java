package vn.edu.lianac;

import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Lớp tiện ích để hiển thị nội dung có chứa công thức LaTeX trong WebView bằng MathJax.
 */
public class LatexRenderer {

    private LatexRenderer() {}

    public static void render(WebView webView, String contentText, boolean isDarkMode) {
        // --- Cấu hình WebView ---
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // --- CSS nền theo chế độ sáng / tối ---
        String backgroundColor = isDarkMode ? "#121212" : "#FFFFFF";
        String textColor = isDarkMode ? "#FFFFFF" : "#000000";

        // --- HTML Template ---
        final String htmlTemplate = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<style>"
                + "  body {"
                + "    background-color: " + backgroundColor + ";"
                + "    color: " + textColor + ";"
                + "    font-family: sans-serif;"
                + "    padding: 16px;"
                + "    font-size: 1.1em;"
                + "    line-height: 1.6;"
                + "  }"
                + "</style>"
                + "<script type=\"text/javascript\">"
                + "  window.MathJax = {"
                + "    tex: { inlineMath: [['\\\\(','\\\\)']], displayMath: [['$$','$$']] },"
                + "    startup: { typeset: false }"
                + "  };"
                + "</script>"
                + "<script type=\"text/javascript\" src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>"
                + "</head>"
                + "<body>"
                + contentText
                + "<script>"
                + "document.addEventListener('DOMContentLoaded', function() { MathJax.typesetPromise(); });"
                + "</script>"
                + "</body></html>";

        // --- Nạp HTML ---
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        webView.loadDataWithBaseURL(null, htmlTemplate, "text/html", "UTF-8", null);
    }
}
