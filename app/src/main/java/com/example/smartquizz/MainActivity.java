package com.example.smartquizz;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText quizNameEditText;
    private EditText numQuestionsEditText;
    private DatabaseReference quizzesRef;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views
        quizNameEditText = findViewById(R.id.quizName);
        numQuestionsEditText = findViewById(R.id.numQuestions);
        Button submitButton = findViewById(R.id.submitButton);
        ImageView backArrow = findViewById(R.id.backArrow);

        // Initialize Firebase Database
        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        // Set onClickListener for submitButton
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get quiz name and number of questions
                String quizName = quizNameEditText.getText().toString().trim();
                String numQuestionsStr = numQuestionsEditText.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(quizName) || TextUtils.isEmpty(numQuestionsStr)) {
                    Toast.makeText(MainActivity.this, "Please enter both quiz name and number of questions", Toast.LENGTH_SHORT).show();
                    return;
                }

                int numQuestions;
                try {
                    numQuestions = Integer.parseInt(numQuestionsStr);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid number of questions.");
                    return;
                }

                // Check if the number of questions is invalid (0 or negative)
                if (numQuestions <= 0) {
                    showAlert("Invalid Number of Questions", "Number of questions must be greater than zero.");
                    return;
                }

                // Create a unique key for the quiz
                String quizId = quizzesRef.push().getKey();

                if (quizId == null) {
                    Log.e(TAG, "Failed to generate unique quiz ID.");
                    Toast.makeText(MainActivity.this, "Failed to generate unique quiz ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the email of the currently logged-in user
                String email = currentUser.getEmail();

                // Save quiz info to Firebase
                Quiz quiz = new Quiz(quizId, quizName, numQuestions, email);
                quizzesRef.child(quizId).setValue(quiz)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Quiz saved successfully.");
                            Toast.makeText(MainActivity.this, "Quiz Saved", Toast.LENGTH_SHORT).show();

                            // Redirect to QuestionsActivity
                            Intent intent = new Intent(MainActivity.this, QuestionsActivity.class);
                            intent.putExtra("quizId", quizId); // Pass quizId to QuestionActivity
                            intent.putExtra("numQuestions", numQuestions); // Pass numQuestions to QuestionActivity
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save quiz.", e);
                            Toast.makeText(MainActivity.this, "Failed to Save Quiz", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Set onClickListener for backArrow
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to DashboardActivity
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
