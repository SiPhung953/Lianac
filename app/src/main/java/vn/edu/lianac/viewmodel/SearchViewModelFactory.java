package vn.edu.lianac.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.lianac.repository.PaperRepository;

/**
 * Factory for creating SearchViewModel with injected dependencies.
 * Required because ViewModels with constructor parameters need custom factories.
 */
public class SearchViewModelFactory implements ViewModelProvider.Factory {

    private final PaperRepository repository;

    /**
     * @param repository Repository implementation to inject into ViewModel
     */
    public SearchViewModelFactory(PaperRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}