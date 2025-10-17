package vn.edu.lianac.utils;

// Helper class to convert HTTP URLs to HTTPS
// Do we need this? Probably not. Though having a helper is def better than typing converting logic everywhere in ArxivParser
public class ArxivUrlHelper {
    public static String toHttps(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // If already HTTPS, return as is
        if (url.startsWith("https://")) {
            return url;
        }

        // Convert HTTP to HTTPS
        if (url.startsWith("http://")) {
            return url.replace("http://", "https://");
        }

        // If no protocol, assume HTTPS
        if (url.startsWith("arxiv.org") || url.startsWith("export.arxiv.org")) {
            return "https://" + url;
        }

        return url;
    }

    // Both PDF and Abstract Link are HTTPS
    public static String getPdfUrlHttps(String pdfUrl) {
        return toHttps(pdfUrl);
    }

    public static String getAbsUrlHttps(String absUrl) {
        return toHttps(absUrl);
    }
}