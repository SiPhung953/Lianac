package vn.edu.lianac.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.SearchResult;
import vn.edu.lianac.utils.ArxivUrlHelper;

public class ArxivAPIService {
    private static final String TAG = "ArxivAPIService";
    private static final String API_BASE_URL = "http://export.arxiv.org/api/query";
    private static final int CONNECT_TIMEOUT = 30; // seconds
    private static final int READ_TIMEOUT = 30; // seconds
    private static final int WRITE_TIMEOUT = 30; // seconds

    private final OkHttpClient client;
    private static ArxivAPIService instance;

    public ArxivAPIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    // Singleton pattern for shared client
    public static synchronized ArxivAPIService getInstance() {
        if (instance == null) {
            instance = new ArxivAPIService();
        }
        return instance;
    }

    /**
     * Fetches articles from arXiv API asynchronously
     * @param queryUrl Complete arXiv API URL with query parameters
     * @param listener Callback for success/failure
     */
    public void fetchArticles(String queryUrl, ArxivResponseListener listener) {
        if (queryUrl == null || queryUrl.isEmpty()) {
            listener.onError(new IllegalArgumentException("Query URL cannot be null or empty"));
            return;
        }

        if (listener == null) {
            Log.e(TAG, "Listener cannot be null");
            return;
        }
        queryUrl = ArxivUrlHelper.toHttps(queryUrl);
        Log.d(TAG, "Fetching articles from: " + queryUrl);

        Request request = new Request.Builder()
                .url(queryUrl)
                .addHeader("User-Agent", "ArxivMobileClient/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage(), e);
                listener.onError(new NetworkException("Network request failed", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    handleResponse(response, listener);
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error handling response", e);
                    listener.onError(e);
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Fetches a single article by its ID.
     * @param articleId The ID of the article to fetch.
     * @param listener The listener for the response.
     */
    public void fetchArticleById(String articleId, ArxivSingleArticleListener listener) {
        if (articleId == null || articleId.isEmpty()) {
            if (listener != null) {
                listener.onError(new IllegalArgumentException("Article ID cannot be null or empty."));
            }
            return;
        }

        if (listener == null) {
            Log.e(TAG, "Listener cannot be null");
            return;
        }

        String url = API_BASE_URL + "?id_list=" + articleId;

        fetchArticles(url, new ArxivResponseListener() {
            @Override
            public void onSuccess(List<Article> articles) {
                if (articles != null && !articles.isEmpty()) {
                    listener.onSuccess(articles.get(0));
                } else {
                    listener.onError(new ParseException("Article not found or empty response."));
                }
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }


    /**
     * Fetches articles with full metadata from arXiv API asynchronously
     * @param queryUrl Complete arXiv API URL with query parameters
     * @param listener Callback for success/failure with SearchResult
     */
    public void fetchArticlesWithMetadata(String queryUrl, ArxivSearchResultListener listener) {
        if (queryUrl == null || queryUrl.isEmpty()) {
            listener.onError(new IllegalArgumentException("Query URL cannot be null or empty"));
            return;
        }

        if (listener == null) {
            Log.e(TAG, "Listener cannot be null");
            return;
        }

        Log.d(TAG, "Fetching articles with metadata from: " + queryUrl);

        Request request = new Request.Builder()
                .url(queryUrl)
                .addHeader("User-Agent", "ArxivMobileClient/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage(), e);
                listener.onError(new NetworkException("Network request failed", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    handleResponseWithMetadata(response, listener);
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error handling response", e);
                    listener.onError(e);
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Synchronous fetch (for use in coroutines or background threads)
     */
    public List<Article> fetchArticlesSync(String queryUrl) throws IOException {
        if (queryUrl == null || queryUrl.isEmpty()) {
            throw new IllegalArgumentException("Query URL cannot be null or empty");
        }

        Log.d(TAG, "Fetching articles synchronously from: " + queryUrl);

        Request request = new Request.Builder()
                .url(queryUrl)
                .addHeader("User-Agent", "ArxivMobileClient/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error code: " + response.code() +
                        " - " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }

            try (InputStream stream = body.byteStream()) {
                return ArxivParser.parse(stream);
            }
        } catch (Exception e) {
            Log.e(TAG, "Sync fetch failed", e);
            throw new IOException("Failed to fetch and parse articles", e);
        }
    }

    private void handleResponse(Response response, ArxivResponseListener listener) {
        // Check HTTP status
        if (!response.isSuccessful()) {
            String errorMsg = String.format("HTTP error: %d - %s",
                    response.code(), response.message());
            Log.e(TAG, errorMsg);
            listener.onError(new HttpException(response.code(), errorMsg));
            return;
        }

        // Check response body
        ResponseBody body = response.body();
        if (body == null) {
            Log.e(TAG, "Empty response body");
            listener.onError(new IOException("Empty response body"));
            return;
        }

        // Parse XML
        try (InputStream stream = body.byteStream()) {
            List<Article> articles = ArxivParser.parse(stream);

            if (articles == null) {
                Log.w(TAG, "Parser returned null");
                listener.onError(new ParseException("Parser returned null"));
                return;
            }

            Log.d(TAG, "Successfully parsed " + articles.size() + " articles");
            listener.onSuccess(articles);

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse XML response", e);
            listener.onError(new ParseException("XML parsing failed", e));
        }
    }

    private void handleResponseWithMetadata(Response response, ArxivSearchResultListener listener) {
        // Check HTTP status
        if (!response.isSuccessful()) {
            String errorMsg = String.format("HTTP error: %d - %s",
                    response.code(), response.message());
            Log.e(TAG, errorMsg);
            listener.onError(new HttpException(response.code(), errorMsg));
            return;
        }

        // Check response body
        ResponseBody body = response.body();
        if (body == null) {
            Log.e(TAG, "Empty response body");
            listener.onError(new IOException("Empty response body"));
            return;
        }

        // Parse XML with metadata
        try (InputStream stream = body.byteStream()) {
            SearchResult result = ArxivParser.parseWithMetadata(stream);

            if (result == null) {
                Log.w(TAG, "Parser returned null");
                listener.onError(new ParseException("Parser returned null"));
                return;
            }

            Log.d(TAG, "Successfully parsed SearchResult: " + result.toString());
            listener.onSuccess(result);

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse XML response", e);
            listener.onError(new ParseException("XML parsing failed", e));
        }
    }

    /**
     * Cancel all pending requests
     */
    public void cancelAll() {
        client.dispatcher().cancelAll();
        Log.d(TAG, "All requests cancelled");
    }

    /**
     * Get the number of queued calls
     */
    public int getQueuedCallsCount() {
        return client.dispatcher().queuedCallsCount();
    }

    /**
     * Get the number of running calls
     */
    public int getRunningCallsCount() {
        return client.dispatcher().runningCallsCount();
    }

    // --- Callback Interfaces ---

    public interface ArxivResponseListener {
        void onSuccess(List<Article> articles);
        void onError(Exception e);
    }

    public interface ArxivSearchResultListener {
        void onSuccess(SearchResult result);
        void onError(Exception e);
    }

    public interface ArxivSingleArticleListener {
        void onSuccess(Article article);
        void onError(Exception e);
    }

    // --- Custom Exceptions ---

    public static class NetworkException extends IOException {
        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class HttpException extends IOException {
        private final int code;

        public HttpException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
