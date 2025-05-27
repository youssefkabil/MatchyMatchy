package com.example.matchymatchy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class QuizActivity extends AppCompatActivity {
    private String[] answers; // Array to hold answers
    private int currentQuestionIndex = 0; // Track the current question

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back
            }
        });

        // Initialize answers array (size should be equal to number of questions)
        answers = new String[4]; // Example size

        // Set up buttons and their click listeners
        setupButtons();
    }

    private void setupButtons() {
        Button buttonOption1 = findViewById(R.id.buttonOption1);
        Button buttonOption2 = findViewById(R.id.buttonOption2);
        Button buttonOption3 = findViewById(R.id.buttonOption3);
        Button buttonOption4 = findViewById(R.id.buttonOption4);
        Button buttonNext = findViewById(R.id.buttonNext);

        buttonOption1.setOnClickListener(v -> saveAnswer("Adventure and Fantasy"));
        buttonOption2.setOnClickListener(v -> saveAnswer("Romance and Drama"));
        buttonOption3.setOnClickListener(v -> saveAnswer("Mystery and Thriller"));
        buttonOption4.setOnClickListener(v -> saveAnswer("Sci-Fi and Technology"));

        buttonNext.setOnClickListener(v -> {
            // Logic to proceed to the next question
            currentQuestionIndex++;
            // Update question and options here
        });
    }

    private void saveAnswer(String answer) {
        if (currentQuestionIndex < answers.length) {
            answers[currentQuestionIndex] = answer; // Save the answer
        }
    }
}