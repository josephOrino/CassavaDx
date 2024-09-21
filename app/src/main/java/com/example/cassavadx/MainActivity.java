package com.example.cassavadx;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cassavadx.ml.Model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.cassavadx.databinding.ActivityMainBinding;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FloatingActionButton fab;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);

        // Opening navigation menus
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.diseases) {
                replaceFragment(new DiseaseFragment());
            }
            return true;
        });

        // Opening camera button on navigation bar
        fab = findViewById(R.id.fab);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        // Initialize ActivityResultLauncher for camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        // Run model inference
                        String predictionResult = runModelInference(imageBitmap);

                        // Pass the bitmap and prediction result to the fragment
                        showResultFragment(imageBitmap, predictionResult);
                    }
                }
        );

        // Initialize ActivityResultLauncher for gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        try {
                            // Load the image from the URI
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                            // Run model inference
                            String predictionResult = runModelInference(bitmap);

                            // Pass the bitmap and prediction result to the fragment
                            showResultFragment(bitmap, predictionResult);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout cameraLayout = dialog.findViewById(R.id.layoutCamera);
        LinearLayout galleryLayout = dialog.findViewById(R.id.layoutGallery);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(cameraIntent, 3);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }

                replaceFragment(new PredictionFragment());

                dialog.dismiss();
//                Toast.makeText(MainActivity.this,"Open camera is clicked",Toast.LENGTH_SHORT).show();

            }
        });

        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to pick an image
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(galleryIntent);

                replaceFragment(new PredictionFragment());

                dialog.dismiss();
//                Toast.makeText(MainActivity.this,"Choose from gallery is clicked",Toast.LENGTH_SHORT).show();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showResultFragment(Bitmap bitmap, String predictionResult) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("image", bitmap);
        bundle.putString("prediction", predictionResult);

        PredictionFragment predictionFragment = new PredictionFragment();
        predictionFragment.setArguments(bundle);

        // Replace fragment and show the result fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, predictionFragment)
                .addToBackStack(null)
                .commit();
    }

    public String runModelInference(Bitmap bitmap) {
        StringBuilder resultString = new StringBuilder();

        String highestScoringLabel = "";
        float highestProbability = -1.0f;

        try {
            // Ensure the bitmap is in ARGB_8888 format
            if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            // Resize the bitmap to 224x224 as expected by the model
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            // Load the TensorFlow Lite model
            Model model = Model.newInstance(this);

            // Preprocess the image - Convert it to TensorImage and normalize the values
            TensorImage image = new TensorImage();
            image.load(resizedBitmap);

            // Perform inference
            Model.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            // Iterate through the results and find the category with the highest probability
            for (Category category : probability) {
                if (category.getScore() > highestProbability) {
                    highestProbability = category.getScore();
                    highestScoringLabel = category.getLabel();
                }
            }

            // Build the prediction result (Gets all the result with their corresponding score.
//            for (Category category : probability) {
//                resultString.append(category.getLabel())
//                        .append(": ")
//                        .append(String.format("%.2f", category.getScore() * 100))
//                        .append("%\n");
//            }

            model.close();
        } catch (IOException e) {
            resultString.append("Error loading model: ").append(e.getMessage());
        }

//        return resultString.toString();
        return highestScoringLabel + ": " + String.format("%.2f", highestProbability * 100) + "%";
    }
}