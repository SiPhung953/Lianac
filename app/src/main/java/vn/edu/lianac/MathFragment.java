package vn.edu.lianac;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class MathFragment extends Fragment {

    private WebView mathWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML vào Fragment
        return inflater.inflate(R.layout.fragment_math, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mathWebView = view.findViewById(R.id.mathWebView);

        // Chuỗi nội dung chứa công thức LaTeX
        String mathFormulas = "<h2>Một số công thức Toán học:</h2>"
                + "<p>Công thức nghiệm phương trình bậc hai:</p>"
                + "$$x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}$$"
                + "<p>Định lý Pythagoras:</p>"
                + "$$a^2 + b^2 = c^2$$"
                + "<p>Công thức tính tích phân:</p>"
                + "$$\\int_a^b f(x)dx$$"
                + "<p>Công thức inline \\(E = mc^2\\) nằm giữa văn bản.</p>";

        // Kiểm tra theme hiện tại (sáng hoặc tối)
        boolean isDarkMode = (requireContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // Gọi hàm LatexRenderer để hiển thị công thức
        LatexRenderer.render(mathWebView, mathFormulas, isDarkMode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Giải phóng tài nguyên WebView để tránh memory leak
        if (mathWebView != null) {
            mathWebView.destroy();
            mathWebView = null;
        }
    }
}
