package vn.edu.lianac;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StartupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_startup, container, false);

        // After 2s -> MainFragment
        new Handler().postDelayed(() -> {
            getActivity().setContentView(R.layout.activity_main);
            Log.i("asdasd", "asdasdqweqwe");;
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Subjects");
            ((MainActivity) getActivity()).replaceFragment(new SubjectFragment());
        }, 2000); // Delay 2s

        return view;
    }
}
