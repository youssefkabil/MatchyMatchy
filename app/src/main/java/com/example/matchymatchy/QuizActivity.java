package com.example.matchymatchy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Quiz data structure
    private List<Map<String, Object>> quizData; // Array of maps with question and answer
    private List<Book> featuredBooks; // Array of books

    // Current state
    private int currentQuestionIndex = 0;
    private String currentAnswer = "";

    // UI Elements
    private TextView textViewQuestion;
    private Button buttonOption1, buttonOption2, buttonOption3, buttonOption4;
    private Button buttonNext;

    // HTTP Client
    private OkHttpClient client;

    // Quiz questions and options
    private Question[] questions = {
            new Question("What kind of stories do you enjoy the most?",
                    new String[]{"Adventure and Fantasy", "Romance and Drama", "Mystery and Thriller", "Sci-Fi and Technology"}),

            new Question("What's your ideal way to spend a weekend?",
                    new String[]{"Exploring new places", "Cozy time at home", "Solving puzzles or games", "Learning something new"}),

            new Question("Which character trait appeals to you most?",
                    new String[]{"Brave and adventurous", "Romantic and caring", "Analytical and clever", "Innovative and curious"}),

            new Question("What type of setting interests you?",
                    new String[]{"Magical worlds", "Historical periods", "Dark and mysterious", "Futuristic societies"}),

            new Question("How do you prefer to face challenges?",
                    new String[]{"Head-on with courage", "With heart and emotion", "Through careful analysis", "With creative solutions"})
    };

    // Book data class
    public static class Book {
        public String id;
        public String name;
        public String description;

        public Book(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    // Question data class
    private static class Question {
        String question;
        String[] options;

        Question(String question, String[] options) {
            this.question = question;
            this.options = options;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize HTTP client
        client = new OkHttpClient();

        // Initialize data structures
        quizData = new ArrayList<>();
        featuredBooks = new ArrayList<>();
        initializeBooksData();

        // Setup toolbar
        setupToolbar();

        // Initialize UI elements
        initializeViews();

        // Setup button listeners
        setupButtons();

        // Load first question
        loadQuestion();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeViews() {
        textViewQuestion = findViewById(R.id.textViewQuestion);
        buttonOption1 = findViewById(R.id.buttonOption1);
        buttonOption2 = findViewById(R.id.buttonOption2);
        buttonOption3 = findViewById(R.id.buttonOption3);
        buttonOption4 = findViewById(R.id.buttonOption4);
        buttonNext = findViewById(R.id.buttonNext);
    }

    private void setupButtons() {
        buttonOption1.setOnClickListener(v -> selectAnswer(questions[currentQuestionIndex].options[0]));
        buttonOption2.setOnClickListener(v -> selectAnswer(questions[currentQuestionIndex].options[1]));
        buttonOption3.setOnClickListener(v -> selectAnswer(questions[currentQuestionIndex].options[2]));
        buttonOption4.setOnClickListener(v -> selectAnswer(questions[currentQuestionIndex].options[3]));

        buttonNext.setOnClickListener(v -> {
            if (!currentAnswer.isEmpty()) {
                saveCurrentAnswer();
                proceedToNext();
            }
        });
    }

    private void loadQuestion() {
        if (currentQuestionIndex < questions.length) {
            Question currentQuestion = questions[currentQuestionIndex];

            // Update question text
            textViewQuestion.setText(currentQuestion.question);

            // Update button texts
            buttonOption1.setText(currentQuestion.options[0]);
            buttonOption2.setText(currentQuestion.options[1]);
            buttonOption3.setText(currentQuestion.options[2]);
            buttonOption4.setText(currentQuestion.options[3]);

            // Reset selection
            currentAnswer = "";
            resetButtonStyles();

            // Update next button text
            if (currentQuestionIndex == questions.length - 1) {
                buttonNext.setText("Finish");
            } else {
                buttonNext.setText("Next");
            }
        }
    }

    private void selectAnswer(String answer) {
        currentAnswer = answer;

        // Reset all button styles first
        resetButtonStyles();

        // Highlight selected button
        if (answer.equals(buttonOption1.getText().toString())) {
            highlightButton(buttonOption1);
        } else if (answer.equals(buttonOption2.getText().toString())) {
            highlightButton(buttonOption2);
        } else if (answer.equals(buttonOption3.getText().toString())) {
            highlightButton(buttonOption3);
        } else if (answer.equals(buttonOption4.getText().toString())) {
            highlightButton(buttonOption4);
        }
    }

    private void resetButtonStyles() {
        // Reset to default style - you might want to adjust this based on your button styling
        buttonOption1.setAlpha(1.0f);
        buttonOption2.setAlpha(1.0f);
        buttonOption3.setAlpha(1.0f);
        buttonOption4.setAlpha(1.0f);
    }

    private void highlightButton(Button button) {
        // Highlight selected button - you might want to adjust this based on your styling
        button.setAlpha(0.5f);
    }

    private void saveCurrentAnswer() {
        // Create map with question and answer
        Map<String, Object> questionAnswerMap = new HashMap<>();
        questionAnswerMap.put("question", questions[currentQuestionIndex].question);
        questionAnswerMap.put("answer", currentAnswer);

        // Add to quiz data
        quizData.add(questionAnswerMap);
    }

    private void proceedToNext() {
        currentQuestionIndex++;

        if (currentQuestionIndex < questions.length) {
            // Load next question
            loadQuestion();
        } else {
            // Quiz finished - get AI recommendation and save
            getAIRecommendationAndSave();
        }
    }

    private void initializeBooksData() {
        featuredBooks.clear();
        featuredBooks.add(new Book("1", "To Kill a Mockingbird",
                "A gripping tale of racial injustice and childhood innocence in the American South."));
        featuredBooks.add(new Book("2", "The Great Gatsby",
                "A classic story of love, wealth, and the American Dream in the Jazz Age."));
        featuredBooks.add(new Book("3", "1984",
                "A dystopian masterpiece about surveillance, control, and the power of truth."));
        featuredBooks.add(new Book("4", "Pride and Prejudice",
                "A witty romance exploring themes of love, class, and social expectations."));
        featuredBooks.add(new Book("5", "The Catcher in the Rye",
                "A coming-of-age story following a teenager's journey through New York City."));
        featuredBooks.add(new Book("6", "Harry Potter and the Sorcerer's Stone",
                "A magical adventure about a young wizard discovering his destiny."));
        featuredBooks.add(new Book("7", "The Hobbit",
                "A reluctant hero embarks on an epic journey to reclaim a lost kingdom."));
        featuredBooks.add(new Book("8", "Moby-Dick",
                "A sea captain's obsessive quest for vengeance against a giant white whale."));
        featuredBooks.add(new Book("9", "Brave New World",
                "A futuristic society where pleasure replaces freedom, and humanity is engineered."));
        featuredBooks.add(new Book("10", "Jane Eyre",
                "A passionate and resilient orphan fights for love and independence."));
        featuredBooks.add(new Book("11", "The Lord of the Rings",
                "An epic saga of friendship, courage, and the battle between good and evil."));
        featuredBooks.add(new Book("12", "Fahrenheit 451",
                "In a world where books are banned, one man dares to remember."));
        featuredBooks.add(new Book("13", "The Alchemist",
                "A shepherd's journey to fulfill his destiny teaches life’s spiritual lessons."));
        featuredBooks.add(new Book("14", "Wuthering Heights",
                "A haunting love story set on the desolate Yorkshire moors."));
        featuredBooks.add(new Book("15", "The Chronicles of Narnia",
                "Children discover a magical land where good and evil battle for control."));
        featuredBooks.add(new Book("16", "Dracula",
                "A chilling tale of the world's most infamous vampire."));
        featuredBooks.add(new Book("17", "Little Women",
                "The lives and struggles of four sisters growing up during the Civil War."));
        featuredBooks.add(new Book("18", "Frankenstein",
                "A scientist’s creation turns into a monster that questions the nature of humanity."));
        featuredBooks.add(new Book("19", "Crime and Punishment",
                "A philosophical crime novel exploring guilt, redemption, and morality."));
        featuredBooks.add(new Book("20", "The Picture of Dorian Gray",
                "A young man's portrait ages while he remains forever youthful—and corrupted."));
    }


    private void getAIRecommendationAndSave() {
        try {
            // Show loading state
            buttonNext.setText("Getting Recommendation...");
            buttonNext.setEnabled(false);

            // Create the JSON data
            JSONObject mainObject = createQuizResultsJSON();

            // Log the quiz data
            logQuizResults(mainObject);

            // Create AI prompt
            String prompt = createAIPrompt(mainObject);

            // Make API call
            makeAIAPICall(prompt);

        } catch (Exception e) {
            Log.e(TAG, "Error getting AI recommendation: " + e.getMessage());
            Toast.makeText(this, "Error getting recommendation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private JSONObject createQuizResultsJSON() throws JSONException {
        JSONObject mainObject = new JSONObject();

        // Create quiz answers array
        JSONArray quizAnswers = new JSONArray();
        for (Map<String, Object> questionAnswer : quizData) {
            JSONObject qaObject = new JSONObject();
            qaObject.put("question", questionAnswer.get("question"));
            qaObject.put("answer", questionAnswer.get("answer"));
            quizAnswers.put(qaObject);
        }

        // Create books array
        JSONArray booksArray = new JSONArray();
        for (Book book : featuredBooks) {
            JSONObject bookObject = new JSONObject();
            bookObject.put("id", book.id);
            bookObject.put("name", book.name);
            bookObject.put("description", book.description);
            booksArray.put(bookObject);
        }

        // Add arrays to main object
        mainObject.put("quiz_answers", quizAnswers);
        mainObject.put("featured_books", booksArray);

        return mainObject;
    }

    private String createAIPrompt(JSONObject quizData) {
        return "You are a personality expert that gives book recommendations based on available books and user personality responses. " +
                "Based on the following quiz responses and available books, recommend ONE book that best matches the user's personality. " +
                "IMPORTANT: You must respond with ONLY the book title from the provided list. Do not add any explanation, description, or additional text. " +
                "Just return the exact book title.\n\n" +
                "Quiz Data: " + quizData.toString();
    }

    private void makeAIAPICall(String prompt) {
        try {
            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "meta-llama/llama-3.3-8b-instruct:free");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 50);
            requestBody.put("temperature", 0.7);

            Log.d(TAG, "API Request Body: " + requestBody.toString());

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
                    Log.e(TAG, "API call failed: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this, "Failed to get recommendation", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API Response: " + responseBody);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            JSONArray choices = jsonResponse.getJSONArray("choices");
                            String recommendation = choices.getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content")
                                    .trim();

                            Log.d(TAG, "AI Recommendation: " + recommendation);

                            runOnUiThread(() -> saveRecommendationToFirestore(recommendation));

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing API response: " + e.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(QuizActivity.this, "Error processing recommendation", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    } else {
                        Log.e(TAG, "API call unsuccessful. Response code: " + response.code());
                        runOnUiThread(() -> {
                            Toast.makeText(QuizActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating API request: " + e.getMessage());
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveRecommendationToFirestore(String recommendedBook) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        Log.d(TAG, "Saving recommended book: " + recommendedBook);
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "User Email: " + userEmail);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", userEmail);
        userMap.put("book", recommendedBook);
        userMap.put("createdAt", System.currentTimeMillis());

        db.collection("user").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Book recommendation saved successfully: " + recommendedBook);

                    // Find the book description
                    String bookDescription = findBookDescription(recommendedBook);

                    // Create quiz data JSON for the reasoning API
                    JSONObject quizDataJson = null;
                    try {
                        quizDataJson = createQuizResultsJSON();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error creating quiz data JSON: " + e.getMessage());
                    }

                    // Navigate to BookRecommendationActivity
                    Intent intent = new Intent(QuizActivity.this, BookRecommendationActivity.class);
                    intent.putExtra("book_title", recommendedBook);
                    intent.putExtra("book_description", bookDescription);
                    if (quizDataJson != null) {
                        intent.putExtra("quiz_data", quizDataJson.toString());
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save book recommendation", e);
                    Toast.makeText(QuizActivity.this,
                            "Failed to save recommendation: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private String findBookDescription(String bookTitle) {
        // Find the book description from featuredBooks list
        for (Book book : featuredBooks) {
            if (book.name.equals(bookTitle)) {
                return book.description;
            }
        }
        // Return a default description if book not found
        return "A wonderful book that matches your personality perfectly.";
    }
    private void logQuizResults(JSONObject mainObject) {
        try {
            // Log the complete JSON
            Log.d(TAG, "=== QUIZ RESULTS JSON ===");
            Log.d(TAG, mainObject.toString(2)); // Pretty printed with indent

            // Also log individual components for easier reading
            Log.d(TAG, "=== Quiz Answers ===");
            for (int i = 0; i < quizData.size(); i++) {
                Map<String, Object> qa = quizData.get(i);
                Log.d(TAG, "Q" + (i + 1) + ": " + qa.get("question"));
                Log.d(TAG, "A" + (i + 1) + ": " + qa.get("answer"));
                Log.d(TAG, "---");
            }

            Log.d(TAG, "=== Featured Books ===");
            for (Book book : featuredBooks) {
                Log.d(TAG, "Book: " + book.name + " - " + book.description);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error logging JSON: " + e.getMessage());
        }
    }
}