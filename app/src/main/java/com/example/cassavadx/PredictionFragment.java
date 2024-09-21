package com.example.cassavadx;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class PredictionFragment extends Fragment {
    private ImageView imageView;
    private TextView percentageRate;
    private TextView diseaseName;
    private String name = "";
    private String percentage = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prediction, container, false);

        imageView = view.findViewById(R.id.imageView);
        percentageRate = view.findViewById(R.id.percentage_rate);
        diseaseName = view.findViewById(R.id.disease_name);

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
            diseaseName.setText(name);
            percentageRate.setText(percentage);

            switch (name){
                case "Cassava Bacterial Blight":
            }
        }

        return view;
    }

    public void splitString(String input) {
        // Split the input string by the colon and space ": "
        String[] parts = input.split(": ");

        // Assign the first part to 'name'
        name = parts[0];

        // Assign the second part to 'percentage'
        percentage = parts[1];
    }

}