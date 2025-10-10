package vn.edu.lianac;

import org.junit.Test;
import org.junit.runner.RunWith;import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.*;

import vn.edu.lianac.utils.QueryBuilder;
import vn.edu.lianac.utils.QueryOptions;

/**
 * A comprehensive test suite for QueryBuilder that covers all use cases
 * and serves as a mock for expected URL outputs.
 */
@RunWith(AndroidJUnit4.class)
public class QueryBuilderComprehensiveTest {

    // --- BASE URL & DEFAULTS for validation ---
    private static final String BASE_URL = "https://export.arxiv.org/api/query";
    private static final String DEFAULT_PARAMS = "start=0&max_results=25&sortBy=submittedDate&sortOrder=descending";

    // --- 1. BASIC SEARCH TESTS ---

    @Test
    public void testBuild_BasicQuery_AllFields_SingleWord() {
        QueryOptions options = new QueryOptions.Builder().searchTerm("relativity").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL for single word, all fields",
                BASE_URL + "?search_query=relativity&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_BasicQuery_AllFields_WithSpaces() {
        QueryOptions options = new QueryOptions.Builder().searchTerm("einstein relativity").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL for query with spaces, all fields",
                BASE_URL + "?search_query=%22einstein+relativity%22&" + DEFAULT_PARAMS, url);
    }

    // --- 2. SEARCH WITH SPECIFIC FIELDS ---

    @Test
    public void testBuild_BasicQuery_WithSpecificField_Author() {
        QueryOptions options = new QueryOptions.Builder().searchField("au").searchTerm("deligne").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL for prefixed author query",
                BASE_URL + "?search_query=au%3Adeligne&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_BasicQuery_WithSpecificField_Title_WithSpaces() {
        QueryOptions options = new QueryOptions.Builder().searchField("ti").searchTerm("Weil Conjectures").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL for prefixed title query with spaces",
                BASE_URL + "?search_query=ti%3A%22Weil+Conjectures%22&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_BasicQuery_WithInvalidField() {
        QueryOptions options = new QueryOptions.Builder().searchField("invalidField").searchTerm("hodge").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL for invalid field should default to 'all' fields search",
                BASE_URL + "?search_query=hodge&" + DEFAULT_PARAMS, url);
    }

    // --- 3. ADVANCED & MULTI-FIELD SEARCHES ---

    @Test
    public void testBuild_AdvancedQuery_MultipleFields() {
        QueryOptions options = new QueryOptions.Builder()
                .author("John Doe").title("Quantum Computing").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should combine advanced fields with AND",
                BASE_URL + "?search_query=ti%3A%22Quantum+Computing%22+AND+au%3A%22John+Doe%22&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_ComplexAndQuery_AuthorAndAbstract() {
        QueryOptions options = new QueryOptions.Builder()
                .author("Kip Thorne").abstractTerm("gravitational waves").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should combine author and abstract with AND",
                BASE_URL + "?search_query=au%3A%22Kip+Thorne%22+AND+abs%3A%22gravitational+waves%22&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_CombinedBasicAndAdvancedQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("physics")
                .title("review")
                .build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should combine basic search term and advanced fields",
                BASE_URL + "?search_query=physics+AND+ti%3Areview&" + DEFAULT_PARAMS, url);
    }

    // --- 4. DATE FILTERING TESTS ---

    @Test
    public void testBuild_FullDateRangeQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("*") // Wildcard to search everything
                .dateFrom("2022-01-01")
                .dateTo("2022-12-31")
                .build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should contain wildcard and a full date range",
                BASE_URL + "?search_query=*+AND+submittedDate%3A[20220101+TO+20221231]&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_DateFromOnlyQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .author("Feynman").dateFrom("2023-01-01").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should contain a 'from' date range",
                BASE_URL + "?search_query=au%3AFeynman+AND+submittedDate%3A[20230101+TO+*]&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_DateToOnlyQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .author("Feynman").dateTo("2023-12-31").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should contain a 'to' date range",
                BASE_URL + "?search_query=au%3AFeynman+AND+submittedDate%3A[*+TO+20231231]&" + DEFAULT_PARAMS, url);
    }


    // --- 5. CATEGORY FILTERING TESTS ---

    @Test
    public void testBuild_SingleCategoryQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("number theory").addCategory("math.NT").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should contain search term and a single category",
                BASE_URL + "?search_query=%22number+theory%22+AND+cat%3Amath.NT&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_MultipleCategoryQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("AI").addCategory("cs.AI").addCategory("cs.LG").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should contain search term and multiple categories with OR",
                BASE_URL + "?search_query=AI+AND+cat%3A(cs.AI+OR+cs.LG)&" + DEFAULT_PARAMS, url);
    }

    // --- 6. PAGINATION & SORTING TESTS ---

    @Test
    public void testBuild_CustomPagination() {
        QueryOptions options = new QueryOptions.Builder().searchTerm("Higgs Boson").start(50).maxResults(100).build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should reflect custom start and max_results",
                BASE_URL + "?search_query=%22Higgs+Boson%22&start=50&max_results=100&sortBy=submittedDate&sortOrder=descending", url);
    }

    @Test
    public void testBuild_CustomSortOrder() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("LIGO").sortBy("relevance").sortOrder("ascending").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should reflect custom sortBy and sortOrder",
                BASE_URL + "?search_query=LIGO&start=0&max_results=25&sortBy=relevance&sortOrder=ascending", url);
    }

    // --- 7. EDGE CASES & INVALID INPUT ---

    @Test
    public void testBuild_InvalidEmptyQuery() {
        // An empty builder should produce an invalid query.
        QueryOptions options = new QueryOptions.Builder().build();
        String url = new QueryBuilder(options).build();
        assertEquals("A completely empty query should result in an empty URL string", "", url);
    }

    @Test
    public void testBuild_EmptySearchTermQuery() {
        // A builder with an empty search term is also invalid.
        QueryOptions options = new QueryOptions.Builder().searchTerm(" ").build();
        String url = new QueryBuilder(options).build();
        assertEquals("A query with a blank search term should be empty", "", url);
    }

    @Test
    public void testBuild_OnlyDateFilterIsStillValid() {
        // A date filter alone is a valid search. It implies a wildcard search.
        QueryOptions options = new QueryOptions.Builder().dateFrom("20240101").build();
        String url = new QueryBuilder(options).build();
        assertEquals("A query with only a date filter should be valid",
                BASE_URL + "?search_query=submittedDate%3A[20240101+TO+*]&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_WithSpecialCharacters() {
        QueryOptions options = new QueryOptions.Builder().searchField("ti").searchTerm("A-B-C's of X:Y").build();
        String url = new QueryBuilder(options).build();
        assertEquals("URL should correctly handle special characters",
                BASE_URL + "?search_query=ti%3A%22A-B-C's+of+X%3AY%22&" + DEFAULT_PARAMS, url);
    }

    @Test
    public void testBuild_AllFeaturesCombined() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("networks") // Basic search term
                .searchField("abs")      // in abstract
                .author("Hinton")        // Advanced field
                .addCategory("cs.NE")    // Category filter
                .dateFrom("2020-01-01")  // Date filter
                .sortBy("relevance")     // Sorting
                .sortOrder("ascending")  // Sorting
                .start(10)               // Pagination
                .maxResults(50)          // Pagination
                .build();
        String url = new QueryBuilder(options).build();
        String expectedQuery = "search_query=abs%3Anetworks+AND+au%3AHinton+AND+cat%3Acs.NE+AND+submittedDate%3A[20200101+TO+*]";
        String expectedParams = "start=10&max_results=50&sortBy=relevance&sortOrder=ascending";
        assertEquals("URL for all features combined should be correct",
                BASE_URL + "?" + expectedQuery + "&" + expectedParams, url);
    }
}
