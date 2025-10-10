package vn.edu.lianac;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MathFragment extends Fragment {

    private WebView mathWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_math, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Correct the ID to match the one in the XML layout
        mathWebView = view.findViewById(R.id.math_webview);

        WebSettings webSettings = mathWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        String mathFormulas = "<h2>Một số công thức Toán học:</h2>"
                + "<p>Công thức nghiệm phương trình bậc hai:</p>"
                + "$$x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a}$$ "
                + "<p>Định lý Pythagoras:</p>"
                + "$$a^2 + b^2 = c^2$$"
                + "<p>Công thức tính tích phân:</p>"
                + "$$\\int_a^b f(x)dx$$"
                + "<p>Và đây là một công thức inline \\(E=mc^2\\) nằm giữa văn bản.</p>";

        String htmlContent = "<html><head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<style>"
                + "body { font-size: 110%; text-align: left; padding: 10px; }"
                + "</style>"
                + "<script type=\"text/javascript\" async "
                + "src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML\">"
                + "</script>"
                + "</head><body>"
                + mathFormulas
                + "</body></html>";

        mathWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }
}
