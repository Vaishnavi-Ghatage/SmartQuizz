package com.example.smartquizz;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Retrieve the quiz code from the intent
        String quizCode = getIntent().getStringExtra("QUIZ_CODE");
        // Use the quiz code as needed
    }
}
