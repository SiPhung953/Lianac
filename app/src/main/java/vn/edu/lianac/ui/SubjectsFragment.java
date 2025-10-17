package vn.edu.lianac.ui;

import android.os.Bundle;
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
import java.util.List;

import vn.edu.lianac.MainActivity;
import vn.edu.lianac.R;
import vn.edu.lianac.utils.CategoryProvider;

/**
 * Simple fragment to display all main subject categories using RecyclerView
 */
public class SubjectsFragment extends Fragment implements SubjectsAdapter.OnSubjectClickListener {

    private RecyclerView subjectsRecyclerView;
    private SubjectsAdapter adapter;
    private List<String> subjects = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subjects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subjectsRecyclerView = view.findViewById(R.id.subjectsRecyclerView);
        setupRecyclerView();
        loadSubjects();
    }

    private void setupRecyclerView() {
        // Use LinearLayoutManager for vertical list
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        subjectsRecyclerView.setLayoutManager(layoutManager);

        // Create adapter with empty list initially
        adapter = new SubjectsAdapter(subjects, this);
        subjectsRecyclerView.setAdapter(adapter);

        // Remove any scroll bars and overscroll effects
        subjectsRecyclerView.setVerticalScrollBarEnabled(false);
        subjectsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void loadSubjects() {
        try {
            List<String> mainCategories = CategoryProvider.getMainCategories();

            if (mainCategories.isEmpty()) {
                showEmptyState();
                return;
            }

            // Update the adapter with new data
            subjects.clear();
            subjects.addAll(mainCategories);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            showError(getString(R.string.error_failed_load_subjects));
        }
    }

    @Override
    public void onSubjectClick(String categoryId, String displayName) {
        // Navigate to SubjectDetailFragment
        SubjectDetailFragment detailFragment = SubjectDetailFragment.newInstance(categoryId, displayName);
        ((MainActivity) requireActivity()).replaceFragment(detailFragment);
    }

    private void showEmptyState() {
        // You can add an empty state view here if needed
        Toast.makeText(requireContext(), getString(R.string.no_subjects_available), Toast.LENGTH_SHORT).show();

    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}