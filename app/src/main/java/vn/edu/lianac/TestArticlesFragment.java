package vn.edu.lianac;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.edu.lianac.models.Article;
import vn.edu.lianac.models.Category;
import vn.edu.lianac.ui.ArticleAdapter;

/**
 * Test fragment to display 3 sample articles from the Query XML
 * Use this to test the DetailFragment navigation and display
 */
public class TestArticlesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_articles, container, false);

        recyclerView = view.findViewById(R.id.testRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ArticleAdapter();
        recyclerView.setAdapter(adapter);

        // Setup click listener to open DetailFragment
        adapter.setOnArticleClickListener(article -> {
            Log.d("TestArticles", "Article clicked: " + article.getTitle());
            openDetailFragment(article);
        });

        // Load test articles
        List<Article> testArticles = createTestArticles();
        adapter.setArticles(testArticles);

        Toast.makeText(getContext(), "Loaded " + testArticles.size() + " test articles", Toast.LENGTH_SHORT).show();

        return view;
    }

    /**
     * Creates 3 test articles based on the Query XML data
     */
    private List<Article> createTestArticles() {
        List<Article> articles = new ArrayList<>();

        // Article 1: Electron-Electron Cusp
        Article article1 = new Article();
        article1.setId("cond-mat/0102536v1");
        article1.setTitle("Impact of Electron-Electron Cusp on Configuration Interaction Energies");
        article1.setSummary("The effect of the electron-electron cusp on the convergence of configuration interaction (CI) wave functions is examined. By analogy with the pseudopotential approach for electron-ion interactions, an effective electron-electron interaction is developed which closely reproduces the scattering of the Coulomb interaction but is smooth and finite at zero electron-electron separation. The exact many-electron wave function for this smooth effective interaction has no cusp at zero electron-electron separation. We perform CI and quantum Monte Carlo calculations for He and Be atoms, both with the Coulomb electron-electron interaction and with the smooth effective electron-electron interaction. We find that convergence of the CI expansion of the wave function for the smooth electron-electron interaction is not significantly improved compared with that for the divergent Coulomb interaction for energy differences on the order of 1 mHartree. This shows that, contrary to popular belief, description of the electron-electron cusp is not a limiting factor, to within chemical accuracy, for CI calculations.");
        article1.setAuthors(Arrays.asList("David Prendergast", "M. Nolan", "Claudia Filippi", "Stephen Fahy", "J. C. Greer"));
        article1.setPublishedDateRaw("2001-02-28T20:12:09Z");
        article1.setUpdatedDateRaw("2001-02-28T20:12:09Z");
        article1.setPdfUrl("http://arxiv.org/pdf/cond-mat/0102536v1");
        article1.setAbsUrl("http://arxiv.org/abs/cond-mat/0102536v1");
        article1.setDoi("10.1063/1.1383585");
        article1.setCategories(Arrays.asList(new Category("cond-mat.str-el", "Strongly Correlated Electrons")));
        articles.add(article1);

        // Article 2: Electron thermal conductivity
        Article article2 = new Article();
        article2.setId("astro-ph/0608371v1");
        article2.setTitle("Electron thermal conductivity owing to collisions between degenerate electrons");
        article2.setSummary("We calculate the thermal conductivity of electrons produced by electron-electron Coulomb scattering in a strongly degenerate electron gas taking into account the Landau damping of transverse plasmons. The Landau damping strongly reduces this conductivity in the domain of ultrarelativistic electrons at temperatures below the electron plasma temperature. In the inner crust of a neutron star at temperatures T < 1e7 K this thermal conductivity completely dominates over the electron conductivity due to electron-ion (electron-phonon) scattering and becomes competitive with the the electron conductivity due to scattering of electrons by impurity ions.");
        article2.setAuthors(Arrays.asList("P. S. Shternin", "D. G. Yakovlev"));
        article2.setPublishedDateRaw("2006-08-17T14:05:46Z");
        article2.setUpdatedDateRaw("2006-08-17T14:05:46Z");
        article2.setPdfUrl("http://arxiv.org/pdf/astro-ph/0608371v1");
        article2.setAbsUrl("http://arxiv.org/abs/astro-ph/0608371v1");
        article2.setDoi("10.1103/PhysRevD.74.043004");
        article2.setCategories(Arrays.asList(new Category("astro-ph", "Astrophysics")));
        articles.add(article2);

        // Article 3: Electron-phonon coupling
        Article article3 = new Article();
        article3.setId("2505.08081v1");
        article3.setTitle("Electron-phonon coupling in correlated materials: insights from the Hubbard-Holstein model");
        article3.setSummary("Dynamical mean-field theory computations of the electron self energy of the Hubbard-Holstein model as a function of electron-phonon and electron-electron interactions are analyzed to gain insight into the dependence of electron-phonon couplings on correlation strength in quantum materials. We find that the electron-phonon interaction is strongly suppressed by electronic correlations, while electron-electron correlation effects at Fermi liquid scales are only weakly modified by coupling to phonons, with phonon-induced modifications most evident at high frequencies on the order of the electronic bandwidth. Implications for beyond-density functional theories of the electron-phonon interaction are discussed.");
        article3.setAuthors(Arrays.asList("Jennifer Coulter", "Andrew J. Millis"));
        article3.setPublishedDateRaw("2025-05-12T21:29:22Z");
        article3.setUpdatedDateRaw("2025-05-12T21:29:22Z");
        article3.setPdfUrl("http://arxiv.org/pdf/2505.08081v1");
        article3.setAbsUrl("http://arxiv.org/abs/2505.08081v1");
        article3.setCategories(Arrays.asList(new Category("cond-mat.str-el", "Strongly Correlated Electrons")));
        // Note: This article doesn't have a DOI in the XML
        articles.add(article3);

        return articles;
    }

    /**
     * Opens DetailFragment with the selected article's data
     */
    private void openDetailFragment(Article article) {
        if (getActivity() instanceof MainActivity) {
            DetailFragment detailFragment = new DetailFragment();

            // Create bundle with article data
            Bundle args = new Bundle();
            args.putString("article_id", article.getId());
            args.putString("article_title", article.getTitle());
            args.putString("article_summary", article.getSummary());
            args.putString("article_authors", article.getAllAuthorsString());
            args.putString("article_published", article.getPublishedDateFormatted());
            args.putString("article_updated", article.getUpdatedDateFormatted());
            args.putString("article_categories", article.getCategoriesString());
            args.putString("pdf_url", article.getPdfUrl());
            args.putString("abs_url", article.getAbsUrl());
            args.putString("doi", article.getDoi());

            // Get primary category for breadcrumb
            if (article.getPrimaryCategory() != null) {
                args.putString("primary_category", article.getPrimaryCategory().getShortName());
                args.putString("primary_category_name", article.getPrimaryCategory().getName());
            }

            detailFragment.setArguments(args);

            // Navigate to detail fragment
            ((MainActivity) getActivity()).replaceFragment(detailFragment);
        }
    }
}