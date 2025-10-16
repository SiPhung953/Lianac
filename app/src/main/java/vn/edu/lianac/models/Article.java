package vn.edu.lianac.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Article implements Parcelable {
    private String id;
    private String title;
    private String summary;
    private List<String> authors;
    private String publishedDate;  // ISO 8601 format (submitted date)
    private String updatedDate;    // ISO 8601 format (announced date)
    private String pdfUrl;
    private String absUrl;
    private List<String> categories;  // All categories (primary + cross-listed)
    private String primaryCategory;
    private List<String> acmMscClasses;  // ACM/MSC classification codes
    private String doi;
    private boolean bookmarked;

    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public Article() {
        this.authors = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.acmMscClasses = new ArrayList<>();
    }

    // --- Parcelable Implementation ---
    protected Article(Parcel in) {
        id = in.readString();
        title = in.readString();
        summary = in.readString();
        authors = in.createStringArrayList();
        publishedDate = in.readString();
        updatedDate = in.readString();
        pdfUrl = in.readString();
        absUrl = in.readString();
        categories = in.createStringArrayList();
        primaryCategory = in.readString();
        acmMscClasses = in.createStringArrayList();
        doi = in.readString();
        bookmarked = in.readByte() != 0;
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeStringList(authors);
        dest.writeString(publishedDate);
        dest.writeString(updatedDate);
        dest.writeString(pdfUrl);
        dest.writeString(absUrl);
        dest.writeStringList(categories);
        dest.writeString(primaryCategory);
        dest.writeStringList(acmMscClasses);
        dest.writeString(doi);
        dest.writeByte((byte) (bookmarked ? 1 : 0));
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String date) { this.publishedDate = date; }

    public String getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(String date) { this.updatedDate = date; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String url) { this.pdfUrl = url; }

    public String getAbsUrl() { return absUrl; }
    public void setAbsUrl(String url) { this.absUrl = url; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getPrimaryCategory() { return primaryCategory; }
    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public List<String> getAcmMscClasses() { return acmMscClasses; }
    public void setAcmMscClasses(List<String> classes) { this.acmMscClasses = classes; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }

    // --- Display Methods ---
    public String getFormattedDate() {
        if (publishedDate == null || publishedDate.length() < 10) {
            return "";
        }
        try {
            // Just extract the date part: YYYY-MM-DD
            String datePart = publishedDate.substring(0, 10);
            String[] parts = datePart.split("-");
            return String.format("%s %s, %s",
                    getMonthAbbr(parts[1]), parts[2], parts[0]);
        } catch (Exception e) {
            return publishedDate.substring(0, 10);
        }
    }

    public String getFormattedSubmittedDate() {
        if (publishedDate == null || publishedDate.isEmpty()) {
            return "";
        }
        return "Submitted: " + getFormattedDate();
    }

    public String getFormattedAnnouncedDate() {
        if (updatedDate == null || updatedDate.length() < 10) {
            return null;
        }
        try {
            String datePart = updatedDate.substring(0, 10);
            String[] parts = datePart.split("-");
            return String.format("Originally announced: %s %s, %s",
                    getMonthAbbr(parts[1]), parts[2], parts[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public String getFormattedAuthors() {
        if (authors == null || authors.isEmpty()) return "";
        if (authors.size() == 1) return authors.get(0);
        if (authors.size() <= 3) return String.join(", ", authors);
        return authors.get(0) + " et al.";
    }

    public String getCategoryString() {
        if (categories == null || categories.isEmpty()) return "";
        return String.join(", ", categories);
    }

    public String getAcmMscClassesString() {
        if (acmMscClasses == null || acmMscClasses.isEmpty()) return null;
        return "ACM/MSC: " + String.join(", ", acmMscClasses);
    }

    public String getFirstCategory() {
        return (categories != null && !categories.isEmpty()) ? categories.get(0) : "";
    }

    /**
     * Get the main category (e.g., "cond-mat" from "cond-mat.str-el")
     */
    public String getMainCategory() {
        if (primaryCategory == null || primaryCategory.isEmpty()) {
            return "";
        }
        // Split by dot to get main category
        int dotIndex = primaryCategory.indexOf('.');
        if (dotIndex > 0) {
            return primaryCategory.substring(0, dotIndex);
        }
        return primaryCategory;
    }

    /**
     * Get the subcategory (e.g., "str-el" from "cond-mat.str-el")
     */
    public String getSubCategory() {
        if (primaryCategory == null || primaryCategory.isEmpty()) {
            return "";
        }
        // Split by dot to get subcategory
        int dotIndex = primaryCategory.indexOf('.');
        if (dotIndex > 0 && dotIndex < primaryCategory.length() - 1) {
            return primaryCategory.substring(dotIndex + 1);
        }
        return "";
    }

    private String getMonthAbbr(String month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        try {
            int m = Integer.parseInt(month);
            return (m >= 1 && m <= 12) ? months[m] : month;
        } catch (NumberFormatException e) {
            return month;
        }
    }

    public String getPublishedDateRaw() {
        return publishedDate;
    }

    public void setPublishedDateRaw(String date) {
        this.publishedDate = date;
    }

    public String getUpdatedDateRaw() {
        return updatedDate;
    }

    public void setUpdatedDateRaw(String date) {
        this.updatedDate = date;
    }

    public boolean hasCategory(String categoryShortName) {
        if (categories == null || categoryShortName == null) {
            return false;
        }
        return categories.contains(categoryShortName);
    }

    public boolean containsAuthor(String authorName) {
        if (authors == null || authorName == null) {
            return false;
        }
        String searchTerm = authorName.toLowerCase();
        for (String author : authors) {
            if (author.toLowerCase().contains(searchTerm)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        String term = searchTerm.toLowerCase();

        if (title != null && title.toLowerCase().contains(term)) {
            return true;
        }
        if (summary != null && summary.toLowerCase().contains(term)) {
            return true;
        }
        if (containsAuthor(term)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Article{id='" + id + "', title='" + title + "'}";
    }
}