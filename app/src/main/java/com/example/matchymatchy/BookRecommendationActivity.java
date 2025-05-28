package com.example.matchymatchy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookRecommendationActivity extends AppCompatActivity {
    private static final String TAG = "BookRecommendationActivity";
    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // UI Elements
    private TextView textViewBookTitle;
    private TextView textViewBookDescription;
    private ImageView imageViewBook;
    private Button buttonWhyRecommended;
    private Button buttonTakeAnotherQuiz;
    private Button buttonGoHome;

    // Data
    private String bookTitle;
    private String bookDescription;
    private String quizDataJson;
    private String recommendationReason = "";

    // HTTP Client
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_recommendation);

        // Initialize HTTP client
        client = new OkHttpClient();

        // Get data from intent
        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("book_title");
        bookDescription = intent.getStringExtra("book_description");
        quizDataJson = intent.getStringExtra("quiz_data");

        // Setup toolbar
        setupToolbar();

        // Initialize views
        initializeViews();

        // Setup button listeners
        setupButtons();

        // Display book information
        displayBookInfo();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Your Book Match");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        textViewBookTitle = findViewById(R.id.textViewBookTitle);
        textViewBookDescription = findViewById(R.id.textViewBookDescription);
        imageViewBook = findViewById(R.id.imageViewBook);
        buttonWhyRecommended = findViewById(R.id.buttonWhyRecommended);
        buttonTakeAnotherQuiz = findViewById(R.id.buttonTakeAnotherQuiz);
        buttonGoHome = findViewById(R.id.buttonGoHome);
    }

    private void setupButtons() {
        buttonWhyRecommended.setOnClickListener(v -> {
            if (recommendationReason.isEmpty()) {
                getRecommendationReason();
            } else {
                showReasonModal();
            }
        });

        buttonTakeAnotherQuiz.setOnClickListener(v -> {
            // Go back to quiz activity
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
            finish();
        });

        buttonGoHome.setOnClickListener(v -> {
            // Go back to main activity or home
            finish();
        });
    }

    private void displayBookInfo() {
        if (bookTitle != null && bookDescription != null) {
            textViewBookTitle.setText(bookTitle);
            textViewBookDescription.setText(bookDescription);

            // You can add book cover images based on the title
            // For now, using a placeholder
            imageViewBook.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    private void getRecommendationReason() {
        // Show loading state
        buttonWhyRecommended.setText("Loading...");
        buttonWhyRecommended.setEnabled(false);

        try {
            String prompt = createReasonPrompt();
            makeReasonAPICall(prompt);
        } catch (Exception e) {
            Log.e(TAG, "Error getting recommendation reason: " + e.getMessage());
            buttonWhyRecommended.setText("Why this book?");
            buttonWhyRecommended.setEnabled(true);
            Toast.makeText(this, "Error getting explanation", Toast.LENGTH_SHORT).show();
        }
    }

    private String createReasonPrompt() {
        return "You are a personality expert. Based on the following quiz responses, explain in 2-3 sentences why you recommended the book '" +
                bookTitle + "' to this user. Be specific about how their personality traits match the book's themes and characters. " +
                "Keep it personal and insightful.\n\n" +
                "Quiz Data: " + quizDataJson;
    }

    private void makeReasonAPICall(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "meta-llama/llama-3.3-8b-instruct:free");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 150);
            requestBody.put("temperature", 0.7);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Reason API call failed: " + e.getMessage());
                    runOnUiThread(() -> {
                        buttonWhyRecommended.setText("Why this book?");
                        buttonWhyRecommended.setEnabled(true);
                        Toast.makeText(BookRecommendationActivity.this, "Failed to get explanation", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Reason API Response: " + responseBody);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            JSONArray choices = jsonResponse.getJSONArray("choices");
                            recommendationReason = choices.getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content")
                                    .trim();

                            Log.d(TAG, "Recommendation Reason: " + recommendationReason);

                            runOnUiThread(() -> {
                                buttonWhyRecommended.setText("Why this book?");
                                buttonWhyRecommended.setEnabled(true);
                                showReasonModal();
                            });

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing reason API response: " + e.getMessage());
                            runOnUiThread(() -> {
                                buttonWhyRecommended.setText("Why this book?");
                                buttonWhyRecommended.setEnabled(true);
                                Toast.makeText(BookRecommendationActivity.this, "Error processing explanation", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Log.e(TAG, "Reason API call unsuccessful. Response code: " + response.code());
                        runOnUiThread(() -> {
                            buttonWhyRecommended.setText("Why this book?");
                            buttonWhyRecommended.setEnabled(true);
                            Toast.makeText(BookRecommendationActivity.this, "Failed to get explanation", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating reason API request: " + e.getMessage());
            buttonWhyRecommended.setText("Why this book?");
            buttonWhyRecommended.setEnabled(true);
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    private void showReasonModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Why We Recommended This Book")
                .setMessage(recommendationReason)
                .setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_lightbulb)
                .create()
                .show();
    }
}