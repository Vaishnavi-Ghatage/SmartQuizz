package com.example.smartquizz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class giveQuiz extends AppCompatActivity {

    private TextView timerTextView;
    private TextView questionTextView;
    private RadioGroup answersRadioGroup;
    private Button skipButton;
    private Button submitButton;

    private String quizCode;
    private long startTimeMillis;
    private int durationMinutes;
    private CountDownTimer countDownTimer;

    private List<Map<String, Object>> questionList = new ArrayList<>();
    private List<Map<String, Object>> skippedQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int totalQuestionsAttempted = 0;
    private int correctAnswersCount = 0;
    private long quizStartTime;

    private String studentUSN;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_quiz);

        timerTextView = findViewById(R.id.timer);
        questionTextView = findViewById(R.id.question);
        answersRadioGroup = findViewById(R.id.answers);
        skipButton = findViewById(R.id.skipBtn);
        submitButton = findViewById(R.id.submitBtn);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            fetchUserUSN();
        }

        quizCode = getIntent().getStringExtra("TEACHER_CODE");

        if (quizCode != null) {
            fetchQuizData();
        }

        skipButton.setOnClickListener(v -> skipQuestion());
        submitButton.setOnClickListener(v -> submitCurrentQuestion());
    }

    private void fetchUserUSN() {
        userRef.child("usn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentUSN = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(giveQuiz.this, "Failed to fetch USN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuizData() {
        DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");

        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean found = false;

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String currentQuizCode = quizSnapshot.child("setup").child("quizCode").getValue(String.class);
                    if (quizCode.equals(currentQuizCode)) {
                        found = true;
                        DataSnapshot setupSnapshot = quizSnapshot.child("setup");
                        startTimeMillis = setupSnapshot.child("startTimeMillis").getValue(Long.class);
                        durationMinutes = setupSnapshot.child("durationMinutes").getValue(Integer.class);
                        boolean shuffleQuestions = setupSnapshot.child("shuffleQuestions").getValue(Boolean.class);

                        questionList.clear();
                        for (DataSnapshot questionSnapshot : quizSnapshot.child("questions").getChildren()) {
                            Map<String, Object> question = (Map<String, Object>) questionSnapshot.getValue();
                            questionList.add(question);
                        }

                        if (shuffleQuestions) {
                            Collections.shuffle(questionList);
                        }

                        if (!questionList.isEmpty()) {
                            quizStartTime = System.currentTimeMillis();
                            loadQuestion(currentQuestionIndex);
                            startTimer();
                        } else {
                            Toast.makeText(giveQuiz.this, "No questions available", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(giveQuiz.this, "Quiz data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(giveQuiz.this, "Failed to load quiz data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuestion(int index) {
        if (index >= 0 && index < questionList.size()) {
            Map<String, Object> questionData = questionList.get(index);
            questionTextView.setText((String) questionData.get("text"));

            RadioButton[] radioButtons = {
                    findViewById(R.id.answer1),
                    findViewById(R.id.answer2),
                    findViewById(R.id.answer3),
                    findViewById(R.id.answer4)
            };

            radioButtons[0].setText((String) questionData.get("optionA"));
            radioButtons[1].setText((String) questionData.get("optionB"));
            radioButtons[2].setText((String) questionData.get("optionC"));
            radioButtons[3].setText((String) questionData.get("optionD"));

            answersRadioGroup.clearCheck();
        } else {
            Toast.makeText(giveQuiz.this, "No more questions", Toast.LENGTH_SHORT).show();
        }
    }

    private void skipQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            skippedQuestions.add(questionList.get(currentQuestionIndex));
            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                loadQuestion(currentQuestionIndex);
            } else if (!skippedQuestions.isEmpty()) {
                questionList = skippedQuestions;
                skippedQuestions = new ArrayList<>();
                currentQuestionIndex = 0;
                loadQuestion(currentQuestionIndex);
            } else {
                Toast.makeText(giveQuiz.this, "All questions completed", Toast.LENGTH_SHORT).show();
                submitQuiz();
            }
        }
    }

    private void submitCurrentQuestion() {
        int selectedOptionId = answersRadioGroup.getCheckedRadioButtonId();
        if (selectedOptionId != -1) {
            totalQuestionsAttempted++;
            RadioButton selectedRadioButton = findViewById(selectedOptionId);
            String selectedOption = selectedRadioButton.getText().toString();

            Map<String, Object> currentQuestion = questionList.get(currentQuestionIndex);
            String correctOption = (String) currentQuestion.get("correctOption");

            // Determine if the selected option is correct
            boolean isCorrect = selectedOption.equals(getOptionTextById(correctOption));
            if (isCorrect) {
                correctAnswersCount++;
            } else {
                // Save incorrect answer to Firebase
                saveIncorrectAnswer(selectedOption, correctOption);
            }

            updateStudentProgress(isCorrect);

            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                loadQuestion(currentQuestionIndex);
            } else if (!skippedQuestions.isEmpty()) {
                questionList = skippedQuestions;
                skippedQuestions = new ArrayList<>();
                currentQuestionIndex = 0;
                loadQuestion(currentQuestionIndex);
            } else {
                submitQuiz();
            }
        } else {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
        }
    }

    private String getOptionTextById(String optionId) {
        switch (optionId) {
            case "A":
                return ((TextView) findViewById(R.id.answer1)).getText().toString();
            case "B":
                return ((TextView) findViewById(R.id.answer2)).getText().toString();
            case "C":
                return ((TextView) findViewById(R.id.answer3)).getText().toString();
            case "D":
                return ((TextView) findViewById(R.id.answer4)).getText().toString();
            default:
                return "";
        }
    }

    private void saveIncorrectAnswer(String selectedOptionChar, String correctOption) {
        DatabaseReference incorrectAnswersRef = FirebaseDatabase.getInstance().getReference("incorrectAnswers").child(quizCode).child(studentUSN);
        DatabaseReference incorrectAnswerRef = incorrectAnswersRef.push(); // Create a new entry
        Map<String, String> incorrectAnswerData = new HashMap<>();
        incorrectAnswerData.put("selectedOption", selectedOptionChar);
        incorrectAnswerData.put("correctOption", correctOption);
        incorrectAnswerRef.setValue(incorrectAnswerData).addOnCompleteListener(task -> {

        });
    }

    private void updateStudentProgress(boolean isCorrect) {
        if (studentUSN == null) {
            Toast.makeText(this, "USN not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference("progress").child(quizCode).child(studentUSN);
        progressRef.child("totalQuestionsAttempted").setValue(totalQuestionsAttempted);
        progressRef.child("correctAnswersCount").setValue(correctAnswersCount);
        progressRef.child("currentQuestionIndex").setValue(currentQuestionIndex);
        progressRef.child("isCorrect").setValue(isCorrect);
    }

    private void startTimer() {
        long endTimeMillis = quizStartTime + durationMinutes * 60 * 1000;

        countDownTimer = new CountDownTimer(endTimeMillis - System.currentTimeMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / (1000 * 60);
                long seconds = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("%dm %ds", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerTextView.setText("Time's up!");
                submitQuiz();
            }
        }.start();
    }

    private void submitQuiz() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Calculate time spent on quiz
        long timeSpentMillis = System.currentTimeMillis() - quizStartTime;
        long timeSpentMinutes = timeSpentMillis / (1000 * 60);
        long timeSpentSeconds = (timeSpentMillis / 1000) % 60;

        // Format time as "Xm Ys"
        String formattedTimeSpent = String.format("%dm %ds", timeSpentMinutes, timeSpentSeconds);

        // Save results to Firebase
        DatabaseReference resultsRef = FirebaseDatabase.getInstance().getReference("results").child(quizCode).child(studentUSN);
        resultsRef.child("totalQuestionsAttempted").setValue(totalQuestionsAttempted);
        resultsRef.child("correctAnswersCount").setValue(correctAnswersCount);
        resultsRef.child("timeSpent").setValue(formattedTimeSpent);

        // Pass results to ResultsActivity
        Intent intent = new Intent(giveQuiz.this, ResultsActivity.class);
        intent.putExtra("totalQuestionsAttempted", totalQuestionsAttempted);
        intent.putExtra("correctAnswersCount", correctAnswersCount);
        intent.putExtra("timeSpent", formattedTimeSpent);
        startActivity(intent);
        finish();
    }
}
