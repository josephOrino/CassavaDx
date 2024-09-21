package com.example.cassavadx;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class DiseaseFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_disease, container, false);
        View view = inflater.inflate(R.layout.fragment_disease, container, false);

        cardViewClick(view, R.id.bacterialBlightCV, R.string.bacterial_blight, R.string.bacterial_blight_description, R.string.bacterial_blight_treatment);
        cardViewClick(view, R.id.brownStreakCV, R.string.brown_streak, R.string.brown_streak_description, R.string.brown_streak_treatment);
        cardViewClick(view, R.id.greenMottleCV, R.string.green_mottle, R.string.green_mottle_description, R.string.green_mottle_treatment);
        cardViewClick(view, R.id.mosaicDiseaseCV, R.string.mosaic_disease, R.string.mosaic_disease_description, R.string.mosaic_disease_treatment);

        // Intercept the back press event
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Programmatically select the home tab in bottom navigation
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToHome();
                }
            }
        };

        // Add the callback to handle back press in this fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return view;
    }

    private void replaceFragment(Fragment fragment){
//        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

    }

    private void cardViewClick(View view, int id, int name, int description, int treatment){
        CardView cardView = view.findViewById(id);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle result = new Bundle();
                result.putString("disease_name", getString(name));
                result.putString("disease_description", getString(description));
                result.putString("disease_treatment", getString(treatment));
                getParentFragmentManager().setFragmentResult("dataFrom1", result);

                replaceFragment(new DescriptionFragment());
            }
        });
    }
}