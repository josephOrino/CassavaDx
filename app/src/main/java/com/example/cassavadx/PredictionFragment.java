package com.example.cassavadx;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;


public class PredictionFragment extends Fragment {
    private ImageView imageView;
    private TextView percentageRate;
    private TextView diseaseName;
    private TextView treatment;

    private String name = "";
    private String percentage = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prediction, container, false);

        imageView = view.findViewById(R.id.imageView);
        percentageRate = view.findViewById(R.id.percentage_rate);
        diseaseName = view.findViewById(R.id.disease_name);
        treatment = view.findViewById(R.id.treatment);

        // Retrieve the image and prediction result from the arguments
        Bundle args = getArguments();
        if (args != null) {
            Bitmap bitmap = args.getParcelable("image");
            String prediction = args.getString("prediction");
            splitString(prediction);
            // Display the image and the prediction result
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            if (Objects.equals(name, "Unknown")) {
                diseaseName.setText(name);
                percentageRate.setText("No cassava leaf found.");
            } else {
                diseaseName.setText(name);
                percentageRate.setText("Prediction Confidence Rate: " + percentage);
            }
            treatment.setText(identifyTreatment(name));
        }

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

    private void splitString(String input) {
        // Split the input string by the colon and space ": "
        String[] parts = input.split(": ");

        // Assign the first part to 'name'
        name = parts[0];

        // Assign the second part to 'percentage'
        percentage = parts[1];
    }

    private String identifyTreatment(String diseaseName){
        String treatment;
        switch (diseaseName){
            case "Cassava Bacterial Blight":
                treatment = getString(R.string.bacterial_blight_treatment);
                break;
            case "Cassava Brown Streak Disease":
                treatment = getString(R.string.brown_streak_treatment);
                break;
            case "Cassava Green Mite":
                treatment = getString(R.string.green_mite_treatment);
                break;
            case "Cassava Mosaic Disease":
                treatment = getString(R.string.mosaic_disease_treatment);
                break;
            default:
                treatment = "No treatment available.";
                break;
        }
        return treatment;
    }

}