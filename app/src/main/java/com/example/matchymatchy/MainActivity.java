package com.example.matchymatchy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewUserEmail;
    private LinearLayout layoutBooks;
    private Button buttonStartQuiz;
    private ImageButton buttonLogout, buttonRecentBooks;

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

    // Sample books data
    private List<Book> featuredBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        layoutBooks = findViewById(R.id.layoutBooks);
        buttonStartQuiz = findViewById(R.id.buttonStartQuiz);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonRecentBooks = findViewById(R.id.buttonRecentBooks);

        // Check authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Current user: " + currentUser.getEmail());
            textViewUserEmail.setText(currentUser.getEmail());

            // Test Firestore read
            db.collection("user")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "User document exists: " + documentSnapshot.getData());
                        } else {
                            Log.d(TAG, "User document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to read user document", e);
                    });
        } else {
            Log.d(TAG, "No user logged in, redirecting to login");
            // No user logged in, redirect to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize books data
        initializeBooksData();

        // Display books
        displayBooks();

        // Set click listeners
        buttonStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to quiz activity
                Toast.makeText(MainActivity.this, "Quiz feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        buttonRecentBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecentBooks();
            }
        });
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
    }

    private void displayBooks() {
        layoutBooks.removeAllViews();

        for (Book book : featuredBooks) {
            View bookCard = createBookCard(book);
            layoutBooks.addView(bookCard);
        }
    }

    private View createBookCard(Book book) {
        // Create main card container
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 32);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16);
        cardView.setCardElevation(8);
        cardView.setCardBackgroundColor(Color.parseColor("#20FFFFFF"));

        // Create inner linear layout
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerLayout.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        innerLayout.setLayoutParams(innerParams);

        // Create book icon
        ImageView bookIcon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(60, 60);
        iconParams.rightMargin = 20;
        bookIcon.setLayoutParams(iconParams);
        bookIcon.setImageResource(R.drawable.ic_book_open);
        bookIcon.setColorFilter(Color.WHITE);

        // Create text container
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        textLayout.setLayoutParams(textParams);

        // Book title
        TextView titleText = new TextView(this);
        titleText.setText(book.name);
        titleText.setTextSize(18);
        titleText.setTextColor(Color.WHITE);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = 8;
        titleText.setLayoutParams(titleParams);

        // Book description
        TextView descText = new TextView(this);
        descText.setText(book.description);
        descText.setTextSize(14);
        descText.setTextColor(Color.parseColor("#CCFFFFFF"));
        descText.setMaxLines(2);
        descText.setEllipsize(android.text.TextUtils.TruncateAt.END);

        // Add texts to text layout
        textLayout.addView(titleText);
        textLayout.addView(descText);

        // Create quiz button
        Button quizButton = new Button(this);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        quizButton.setLayoutParams(buttonParams);
        quizButton.setText("Take Quiz");
        quizButton.setTextSize(12);
        quizButton.setTextColor(Color.WHITE);
        quizButton.setBackgroundColor(Color.parseColor("#FF8E8E"));
        quizButton.setPadding(24, 12, 24, 12);

        // Set quiz button click listener
        quizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Starting quiz for: " + book.name, Toast.LENGTH_SHORT).show();
                // TODO: Start quiz for specific book
            }
        });

        // Add all views to inner layout
        innerLayout.addView(bookIcon);
        innerLayout.addView(textLayout);
        innerLayout.addView(quizButton);

        // Add inner layout to card
        cardView.addView(innerLayout);

        return cardView;
    }

    private void showRecentBooks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("user")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String recentBook = documentSnapshot.getString("book");
                            if (recentBook != null && !recentBook.equals("None")) {
                                Toast.makeText(MainActivity.this,
                                        "Your recent book: " + recentBook, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "No recent books found. Take a quiz to get recommendations!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this,
                                "Failed to load recent books: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}