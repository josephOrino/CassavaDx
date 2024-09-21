package com.example.cassavadx;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DescriptionFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_description, container, false);

        getParentFragmentManager().setFragmentResultListener("dataFrom1", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                TextView disease_name_tv = getActivity().findViewById(R.id.disease_name_label);
                disease_name_tv.setText(result.getString("disease_name"));

                TextView disease_description_tv = getActivity().findViewById(R.id.disease_description_label);
                disease_description_tv.setText(result.getString("disease_description"));

                TextView disease_treatment_tv = getActivity().findViewById(R.id.treatment_label);
                disease_treatment_tv.setText(result.getString("disease_treatment"));
            }
        });

        // Intercept the back press event
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to DiseaseFragment
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToDiseaseFragment();
                }
            }
        };

        // Add the callback to handle back press in this fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Inflate the layout for this fragment
        return view;
    }
}