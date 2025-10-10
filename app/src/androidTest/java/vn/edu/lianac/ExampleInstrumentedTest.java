package vn.edu.lianac;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import vn.edu.lianac.utils.QueryBuilder;
import vn.edu.lianac.utils.QueryOptions;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    // --- EXISTING TESTS (ADJUSTED FOR NEW LOGIC) ---

    @Test
    public void testBuild_BasicQuery_AllFields() {
        // This now tests the default "all" field search
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("einstein relativity")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should contain the search query for all fields.", url.contains("search_query=%22einstein+relativity%22"));
    }

    @Test
    public void testBuild_AdvancedQuery() {
        // This test now represents a multi-field advanced search, without a basic search term.
        QueryOptions options = new QueryOptions.Builder()
                .author("John Doe")
                .title("Quantum Computing")
                .addCategory("cs.AI")
                .addCategory("cs.LG")
                .dateFrom("20230101")
                .build();
        String url = new QueryBuilder(options).build();

        assertTrue("URL should contain author field.", url.contains("au%3A%22John+Doe%22"));
        assertTrue("URL should contain title field.", url.contains("ti%3A%22Quantum+Computing%22"));
        assertTrue("URL should contain combined category query.", url.contains("cat%3A(cs.AI+OR+cs.LG)"));
        assertTrue("URL should contain date range query.", url.contains("submittedDate%3A[20230101+TO+*]"));
    }

    @Test
    public void testBuild_InvalidEmptyQuery() {
        // This test is updated: an empty query is no longer valid and should produce an empty string.
        QueryOptions options = new QueryOptions.Builder().searchTerm("").build();
        String url = new QueryBuilder(options).build();
        assertEquals("An empty query should result in an empty URL string.", "", url);
    }

    @Test
    public void testBuild_FullDateRangeQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("*") // Use a wildcard to make the query valid
                .dateFrom("2022-01-01")
                .dateTo("2022-12-31")
                .build();
        String url = new QueryBuilder(options).build();
        // Changed from %2A to just * since encode() preserves asterisks
        assertTrue("URL should contain the wildcard search term.", url.contains("search_query=*"));
        assertTrue("URL should contain a full date range.", url.contains("submittedDate%3A[20220101+TO+20221231]"));
    }

    @Test
    public void testBuild_CustomSortOrder() {
        // This test requires a search term to be valid.
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("LIGO") // Added search term
                .sortBy("relevance")
                .sortOrder("ascending")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should reflect custom sortBy.", url.contains("sortBy=relevance"));
        assertTrue("URL should reflect custom sortOrder.", url.contains("sortOrder=ascending"));
    }

    @Test
    public void testBuild_CustomPagination() {
        // This test requires a search term to be valid.
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("Higgs Boson") // Added search term
                .start(50)
                .maxResults(100)
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should reflect custom start index.", url.contains("start=50"));
        assertTrue("URL should reflect custom max_results.", url.contains("max_results=100"));
    }

    @Test
    public void testBuild_ComplexAndQuery() {
        QueryOptions options = new QueryOptions.Builder()
                .author("Kip Thorne")
                .abstractTerm("gravitational waves")
                .build();
        String url = new QueryBuilder(options).build();
        String expectedQueryPart = "au%3A%22Kip+Thorne%22+AND+abs%3A%22gravitational+waves%22";
        assertTrue("URL should contain correctly encoded complex query.", url.contains(expectedQueryPart));
    }

    @Test
    public void testBuild_WithSpecialCharacters() {
        // Using the new basic search with a specific field
        QueryOptions options = new QueryOptions.Builder()
                .searchField("ti")
                .searchTerm("A-B-C's of X:Y")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should correctly handle special characters in the title.", url.contains("ti%3A%22A-B-C's+of+X%3AY%22"));
    }

    @Test
    public void testBuild_SingleCategoryQuery() {
        // This is now a filter on top of a valid search term.
        QueryOptions options = new QueryOptions.Builder()
                .searchTerm("number theory")
                .addCategory("math.NT")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should contain search term.", url.contains("%22number+theory%22"));
        assertTrue("URL should contain a single category query.", url.contains("cat%3Amath.NT"));
    }

    // --- NEW TESTS FOR EXPANDED FUNCTIONALITY ---

    @Test
    public void testBuild_BasicQuery_WithSpecificField_Author() {
        QueryOptions options = new QueryOptions.Builder()
                .searchField("au")
                .searchTerm("deligne")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should contain a prefixed author query.", url.contains("search_query=au%3Adeligne"));
    }

    @Test
    public void testBuild_BasicQuery_WithSpecificField_Title() {
        QueryOptions options = new QueryOptions.Builder()
                .searchField("ti")
                .searchTerm("Weil Conjectures")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should contain a prefixed title query.", url.contains("search_query=ti%3A%22Weil+Conjectures%22"));
    }

    @Test
    public void testBuild_BasicQuery_WithInvalidField() {
        // If an invalid field is provided, it should default to an "all" fields search.
        QueryOptions options = new QueryOptions.Builder()
                .searchField("invalidField")
                .searchTerm("hodge")
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL for invalid field should default to an 'all' fields search.", url.contains("search_query=hodge"));
        assertFalse("URL should not contain the invalid prefix.", url.contains("invalidField%3A"));
    }

    @Test
    public void testBuild_BasicQuery_WithCategoryFilter() {
        // Tests the combination of the new basic search and a category filter.
        QueryOptions options = new QueryOptions.Builder()
                .searchField("abs") // search in abstract
                .searchTerm("entropy")
                .addCategory("quant-ph") // filter by category
                .build();
        String url = new QueryBuilder(options).build();
        assertTrue("URL should contain prefixed abstract query.", url.contains("abs%3Aentropy"));
        assertTrue("URL should contain AND operator.", url.contains("+AND+"));
        assertTrue("URL should contain category filter.", url.contains("cat%3Aquant-ph"));
    }
}
