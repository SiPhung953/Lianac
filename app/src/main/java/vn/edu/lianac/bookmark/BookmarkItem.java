package vn.edu.lianac.bookmark;

import vn.edu.lianac.models.Article;
public class BookmarkItem {
    private String id;
    private String title;
    private long timestamp;
    private Article article;

    public BookmarkItem(String id, String title, long timestamp, Article article) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.article = article;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public boolean hasArticle() {
        return article != null;
    }
}
