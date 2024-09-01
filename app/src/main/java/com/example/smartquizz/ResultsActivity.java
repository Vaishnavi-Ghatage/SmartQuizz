package com.example.smartquizz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {
    private TextView totalCorrectAnswersTextView;
    private TextView totalQuestionsTextView;
    private TextView totalIncorrectAnswersTextView;
    private TextView totalTimeTakenTextView;
    private TextView percentageTextView;
    private Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        totalCorrectAnswersTextView = findViewById(R.id.totalCorrectAnswers);
        totalQuestionsTextView = findViewById(R.id.totalQuestions);
        totalIncorrectAnswersTextView = findViewById(R.id.totalIncorrectAnswers);
        totalTimeTakenTextView = findViewById(R.id.totalTimeTaken);
        percentageTextView = findViewById(R.id.percentageTextView);
        returnButton = findViewById(R.id.returnButton);

        int totalQuestionsAttempted = getIntent().getIntExtra("totalQuestionsAttempted", 0);
        int correctAnswersCount = getIntent().getIntExtra("correctAnswersCount", 0);
        String timeSpent = getIntent().getStringExtra("timeSpent");

        int incorrectAnswersCount = totalQuestionsAttempted - correctAnswersCount;
        int percentage = (totalQuestionsAttempted > 0) ? (int) ((correctAnswersCount / (double) totalQuestionsAttempted) * 100) : 0;

        totalCorrectAnswersTextView.setText(String.valueOf(correctAnswersCount));
        totalQuestionsTextView.setText(String.valueOf(totalQuestionsAttempted));
        totalIncorrectAnswersTextView.setText(String.valueOf(incorrectAnswersCount));
        totalTimeTakenTextView.setText(timeSpent);
        percentageTextView.setText(percentage + "%");

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
