package vn.edu.lianac;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import vn.edu.lianac.bookmark.BookmarkListFragment;

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
// This class is only for debugging purposes before the existence of the sidebar anyways, im not fixing ts
//        Button goDetailBtn = view.findViewById(R.id.go_detail_btn);
//        goDetailBtn.setOnClickListener(v -> {
//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
//            transaction.replace(R.id.fragment_container, new BookmarkListFragment());
//            transaction.addToBackStack(null); // Can be back
//            transaction.commit();
//        });

        return view;
    }
}
