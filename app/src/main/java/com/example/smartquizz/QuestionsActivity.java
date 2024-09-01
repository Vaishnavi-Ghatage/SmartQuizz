package com.example.smartquizz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class QuestionsActivity extends AppCompatActivity {

    private static final String TAG = "QuestionsActivity";
    private EditText questionEditText, optionAEditText, optionBEditText, optionCEditText, optionDEditText;
    private RadioGroup optionsRadioGroup;
    private Button addButton, submitQuizButton;
    private DatabaseReference questionsRef;
    private String quizId;
    private int numQuestions;
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // Get the quizId and numQuestions from the intent
        quizId = getIntent().getStringExtra("quizId");
        numQuestions = getIntent().getIntExtra("numQuestions", 0);

        // Initialize Firebase Database reference
        questionsRef = FirebaseDatabase.getInstance().getReference().child("quizzes").child(quizId).child("questions");

        // Initialize views
        questionEditText = findViewById(R.id.questionEditText);
        optionAEditText = findViewById(R.id.optionAEditText);
        optionBEditText = findViewById(R.id.optionBEditText);
        optionCEditText = findViewById(R.id.optionCEditText);
        optionDEditText = findViewById(R.id.optionDEditText);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        addButton = findViewById(R.id.addButton);
        submitQuizButton = findViewById(R.id.submitQuizButton);

        addButton.setOnClickListener(v -> addQuestion());

        submitQuizButton.setOnClickListener(v -> {
            // Check if all questions have been added before proceeding
            if (currentQuestionIndex == numQuestions - 1) {
                // Add the last question if all fields are filled
                if (isQuestionValid()) {
                    addQuestion();
                    submitQuiz(); // Handle final submission tasks
                } else {
                    Toast.makeText(QuestionsActivity.this, "Please fill out all fields and select the correct option for the last question", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuestionsActivity.this, "Add all questions before submitting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addQuestion() {
        String questionText = questionEditText.getText().toString().trim();
        String optionA = optionAEditText.getText().toString().trim();
        String optionB = optionBEditText.getText().toString().trim();
        String optionC = optionCEditText.getText().toString().trim();
        String optionD = optionDEditText.getText().toString().trim();
        int selectedOptionId = optionsRadioGroup.getCheckedRadioButtonId();

        if (isQuestionValid()) {
            String correctOption = getSelectedOption(selectedOptionId);

            // Create a unique key for the question
            String questionId = questionsRef.push().getKey();
            if (questionId == null) {
                Log.e(TAG, "Failed to generate unique question ID.");
                Toast.makeText(this, "Failed to generate unique question ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create question object
            Question question = new Question(questionId, questionText, optionA, optionB, optionC, optionD, correctOption);

            // Save question to Firebase
            questionsRef.child(questionId).setValue(question)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Question saved successfully.");
                        Toast.makeText(QuestionsActivity.this, "Question Added", Toast.LENGTH_SHORT).show();
                        currentQuestionIndex++;

                        // Clear input fields
                        questionEditText.setText("");
                        optionAEditText.setText("");
                        optionBEditText.setText("");
                        optionCEditText.setText("");
                        optionDEditText.setText("");
                        optionsRadioGroup.clearCheck();

                        // Show Submit button if it's the last question
                        if (currentQuestionIndex == numQuestions - 1) {
                            addButton.setVisibility(View.GONE);
                            submitQuizButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save question.", e);
                        Toast.makeText(QuestionsActivity.this, "Failed to Add Question", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private boolean isQuestionValid() {
        String questionText = questionEditText.getText().toString().trim();
        String optionA = optionAEditText.getText().toString().trim();
        String optionB = optionBEditText.getText().toString().trim();
        String optionC = optionCEditText.getText().toString().trim();
        String optionD = optionDEditText.getText().toString().trim();
        int selectedOptionId = optionsRadioGroup.getCheckedRadioButtonId();

        if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty() || selectedOptionId == -1) {
            Toast.makeText(this, "Please fill out all fields and select the correct option", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getSelectedOption(int selectedOptionId) {
        if (selectedOptionId == R.id.optionARadioButton) {
            return "A";
        } else if (selectedOptionId == R.id.optionBRadioButton) {
            return "B";
        } else if (selectedOptionId == R.id.optionCRadioButton) {
            return "C";
        } else if (selectedOptionId == R.id.optionDRadioButton) {
            return "D";
        } else {
            return "";
        }
    }

    private void submitQuiz() {
        // Redirect to SetupQuizActivity
        Intent intent = new Intent(QuestionsActivity.this, activity_setup_quiz.class);
        intent.putExtra("quizId", quizId);
        intent.putExtra("numQuestions", numQuestions); // Pass numQuestions to SetupQuizActivity
        startActivity(intent);
        finish();
    }

    // Question class definition
    public static class Question {
        public String id, text, optionA, optionB, optionC, optionD, correctOption;

        public Question() {
            // Default constructor required for calls to DataSnapshot.getValue(Question.class)
        }

        public Question(String id, String text, String optionA, String optionB, String optionC, String optionD, String correctOption) {
            this.id = id;
            this.text = text;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctOption = correctOption;
        }
    }
}
