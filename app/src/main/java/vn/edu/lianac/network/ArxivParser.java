package vn.edu.lianac.network;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.Category;
import vn.edu.lianac.models.SearchResult;

public class ArxivParser {
    private static final String TAG = "ArxivParser";
    private static final String OPENSEARCH_NAMESPACE = "http://a9.com/-/spec/opensearch/1.1/";
    private static final String ARXIV_NAMESPACE = "http://arxiv.org/schemas/atom";

    /**
     * Parse arXiv Atom feed XML into a SearchResult object containing articles and metadata
     */
    public static SearchResult parseWithMetadata(InputStream in) throws XmlPullParserException, IOException {
        if (in == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeedWithMetadata(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Legacy method for backward compatibility - returns only articles
     */
    public static List<Article> parse(InputStream in) throws XmlPullParserException, IOException {
        SearchResult result = parseWithMetadata(in);
        return result.getArticles();
    }

    private static SearchResult readFeedWithMetadata(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        List<Article> entries = new ArrayList<>();
        int totalResults = 0;
        int startIndex = 0;
        int itemsPerPage = 0;

        // Verify we're at a feed element
        parser.require(XmlPullParser.START_TAG, null, "feed");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            String namespace = parser.getNamespace();

            if ("entry".equals(tagName)) {
                try {
                    Article article = readEntry(parser);
                    if (article != null) {
                        entries.add(article);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing entry, skipping", e);
                    skip(parser);
                }
            } else if (OPENSEARCH_NAMESPACE.equals(namespace)) {
                // Parse OpenSearch metadata
                switch (tagName) {
                    case "totalResults":
                        totalResults = readIntText(parser);
                        break;
                    case "startIndex":
                        startIndex = readIntText(parser);
                        break;
                    case "itemsPerPage":
                        itemsPerPage = readIntText(parser);
                        break;
                    default:
                        skip(parser);
                        break;
                }
            } else {
                skip(parser);
            }
        }

        Log.d(TAG, "Parsed " + entries.size() + " articles (total available: " + totalResults + ")");
        return new SearchResult(entries, totalResults, startIndex, itemsPerPage);
    }

    private static int readIntText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String text = readText(parser);
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse integer from: " + text, e);
            return 0;
        }
    }

    private static Article readEntry(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "entry");

        Article article = new Article();
        List<String> authors = new ArrayList<>();
        List<Category> categories = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            String namespace = parser.getNamespace();

            try {
                // Check for arxiv namespace elements
                if (ARXIV_NAMESPACE.equals(namespace)) {
                    switch (tagName) {
                        case "doi":
                            String doi = readText(parser);
                            if (doi != null && !doi.trim().isEmpty()) {
                                article.setDoi(doi.trim());
                                Log.d(TAG, "Found DOI: " + doi.trim());
                            }
                            break;
                        case "comment":
                        case "journal_ref":
                        case "primary_category":
                            // Skip other arxiv namespace elements for now
                            skip(parser);
                            break;
                        default:
                            skip(parser);
                            break;
                    }
                } else {
                    // Handle standard Atom elements
                    switch (tagName) {
                        case "id":
                            article.setId(extractArxivId(readText(parser)));
                            break;
                        case "title":
                            article.setTitle(cleanText(readText(parser)));
                            break;
                        case "summary":
                            article.setSummary(cleanText(readText(parser)));
                            break;
                        case "published":
                            article.setPublishedDateRaw(readText(parser).trim());
                            break;
                        case "updated":
                            article.setUpdatedDateRaw(readText(parser).trim());
                            break;
                        case "author":
                            String author = readAuthor(parser);
                            if (author != null && !author.isEmpty()) {
                                authors.add(author);
                            }
                            break;
                        case "link":
                            readLink(parser, article);
                            break;
                        case "category":
                            String cat = readCategory(parser);
                            if (cat != null && !cat.isEmpty()) {
                                categories.add(new Category(cat));
                            }
                            break;
                        default:
                            skip(parser);
                            break;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing tag: " + tagName, e);
                skip(parser);
            }
        }

        article.setAuthors(authors);
        article.setCategories(categories);

        // Validate essential fields
        if (article.getId() == null || article.getTitle() == null) {
            Log.w(TAG, "Article missing essential fields, skipping");
            return null;
        }

        return article;
    }

    private static String readAuthor(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "author");
        String authorName = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if ("name".equals(parser.getName())) {
                authorName = readText(parser);
            } else {
                skip(parser);
            }
        }

        return authorName != null ? authorName.trim() : null;
    }

    private static String readCategory(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "category");

        String term = parser.getAttributeValue(null, "term");

        // Move to end tag
        while (parser.next() != XmlPullParser.END_TAG) {
            // Skip any content (categories are usually empty)
        }

        parser.require(XmlPullParser.END_TAG, null, "category");

        return (term != null && !term.isEmpty()) ? term : null;
    }

    private static void readLink(XmlPullParser parser, Article article)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "link");

        String rel = parser.getAttributeValue(null, "rel");
        String href = parser.getAttributeValue(null, "href");
        String title = parser.getAttributeValue(null, "title");

        // Move to end tag
        while (parser.next() != XmlPullParser.END_TAG) {
            // Links are usually empty
        }

        parser.require(XmlPullParser.END_TAG, null, "link");

        if (href == null || href.isEmpty()) {
            return;
        }

        // Determine link type
        if ("alternate".equals(rel) && article.getAbsUrl() == null) {
            article.setAbsUrl(href);
        } else if ("related".equals(rel) && "pdf".equals(title) && article.getPdfUrl() == null) {
            article.setPdfUrl(href);
        }
    }

    private static String readText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }

        return result != null ? result : "";
    }

    private static void skip(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException("Skip must be called at START_TAG");
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static String extractArxivId(String idUrl) {
        if (idUrl == null || idUrl.isEmpty()) {
            return null;
        }

        int lastSlash = idUrl.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < idUrl.length() - 1) {
            return idUrl.substring(lastSlash + 1);
        }

        return idUrl;
    }

    private static String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.trim().replaceAll("\\s+", " ");
    }
}