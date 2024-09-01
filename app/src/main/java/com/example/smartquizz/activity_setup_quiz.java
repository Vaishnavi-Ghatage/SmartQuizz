package com.example.smartquizz;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class activity_setup_quiz extends AppCompatActivity {

    private EditText durationEditText;
    private TextView selectedDateTextView, selectedTimeTextView, remainingTimeTextView, deadlineDateTextView, deadlineTimeTextView;
    private Switch startTimeSwitch, deadlineSwitch, shuffleQuestionsSwitch;
    private Button assignButton;
    private DatabaseReference quizzesRef;
    private String quizId;

    private Calendar startTimeCalendar = Calendar.getInstance();
    private Calendar deadlineCalendar = Calendar.getInstance();
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_quiz);

        // Initialize Firebase Database reference
        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        // Initialize views
        durationEditText = findViewById(R.id.durationEditText);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        deadlineDateTextView = findViewById(R.id.deadlineDateTextView);
        deadlineTimeTextView = findViewById(R.id.deadlineTimeTextView);
        startTimeSwitch = findViewById(R.id.startTimeSwitch);
        deadlineSwitch = findViewById(R.id.deadlineSwitch);
        shuffleQuestionsSwitch = findViewById(R.id.shuffleQuestionsSwitch);
        assignButton = findViewById(R.id.assignButton);

        // Set default date and time
        updateDateTimeDisplays();

        // Set listeners
        startTimeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                findViewById(R.id.dateTimeLayout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.dateTimeLayout).setVisibility(View.GONE);
            }
        });

        deadlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                findViewById(R.id.deadlineDateTimeLayout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.deadlineDateTimeLayout).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.datePickerButton).setOnClickListener(v -> showDatePickerDialog(startTimeCalendar, selectedDateTextView));
        findViewById(R.id.timePickerButton).setOnClickListener(v -> showTimePickerDialog(startTimeCalendar, selectedTimeTextView));
        findViewById(R.id.deadlineDatePickerButton).setOnClickListener(v -> showDatePickerDialog(deadlineCalendar, deadlineDateTextView));
        findViewById(R.id.deadlineTimePickerButton).setOnClickListener(v -> showTimePickerDialog(deadlineCalendar, deadlineTimeTextView));

        // Assign button click listener
        assignButton.setOnClickListener(v -> {
            // Validate and save quiz setup data
            saveQuizSetup();
        });
    }

    // Function to update date and time displays
    private void updateDateTimeDisplays() {
        String dateFormat = "EEE, MMM dd, yyyy";
        String timeFormat = "hh:mm a";

        // Update selected date and time
        selectedDateTextView.setText(new SimpleDateFormat(dateFormat, Locale.getDefault()).format(startTimeCalendar.getTime()));
        selectedTimeTextView.setText(new SimpleDateFormat(timeFormat, Locale.getDefault()).format(startTimeCalendar.getTime()));

        // Update deadline date and time (if applicable)
        if (deadlineSwitch.isChecked() && deadlineCalendar.getTimeInMillis() > startTimeCalendar.getTimeInMillis()) {
            deadlineDateTextView.setText(new SimpleDateFormat(dateFormat, Locale.getDefault()).format(deadlineCalendar.getTime()));
            deadlineTimeTextView.setText(new SimpleDateFormat(timeFormat, Locale.getDefault()).format(deadlineCalendar.getTime()));

            // Calculate remaining time
            long currentTime = System.currentTimeMillis();
            long endTime = deadlineCalendar.getTimeInMillis();
            long timeDifference = endTime - currentTime;

            if (timeDifference > 0) {
                startCountDownTimer(timeDifference);
            } else {
                remainingTimeTextView.setText("Time has already passed");
            }
        } else {
            deadlineDateTextView.setText("");
            deadlineTimeTextView.setText("");
            remainingTimeTextView.setText("");
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }
    }

    // Function to show date picker dialog
    private void showDatePickerDialog(Calendar calendar, final TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity_setup_quiz.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplays();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    // Function to show time picker dialog
    private void showTimePickerDialog(Calendar calendar, final TextView textView) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(activity_setup_quiz.this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplays();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);

        timePickerDialog.show();
    }

    // Function to start countdown timer
    private void startCountDownTimer(long durationMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                String remainingTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                remainingTimeTextView.setText("Remaining time: " + remainingTime);
            }

            @Override
            public void onFinish() {
                remainingTimeTextView.setText("Time's up!");
            }
        };

        countDownTimer.start();
    }

    // Function to validate and save quiz setup data
    private void saveQuizSetup() {
        String durationString = durationEditText.getText().toString().trim();
        int duration = 0;
        boolean shuffleQuestions = shuffleQuestionsSwitch.isChecked();

        if (!durationString.isEmpty()) {
            duration = Integer.parseInt(durationString);
        } else {
            Toast.makeText(this, "Please enter quiz duration", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a 7-digit random code
        String quizCode = generateQuizCode();

        // Get the unique quiz ID from the previous activity
        quizId = getIntent().getStringExtra("quizId");

        // Create quiz setup object
        QuizSetup quizSetup = new QuizSetup(duration, startTimeCalendar.getTimeInMillis(), deadlineCalendar.getTimeInMillis(), shuffleQuestions, quizCode);

        // Save quiz setup to Firebase under the same quizId
        quizzesRef.child(quizId).child("setup").setValue(quizSetup)
                .addOnSuccessListener(aVoid -> {
                    // Navigate to new activity with quiz code
                    Intent intent = new Intent(activity_setup_quiz.this, QuizCodeActivity.class);
                    intent.putExtra("quizCode", quizCode);
                    startActivity(intent);
                    finish(); // Finish current activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity_setup_quiz.this, "Failed to save quiz setup", Toast.LENGTH_SHORT).show();
                });
    }

    // Function to generate a 7-digit random code
    private String generateQuizCode() {
        // Generate a 7-digit random number
        int code = (int) (Math.random() * 9000000) + 1000000;
        return String.valueOf(code);
    }

    // QuizSetup class definition
    public static class QuizSetup {
        public int durationMinutes;
        public long startTimeMillis;
        public long deadlineMillis;
        public boolean shuffleQuestions;
        public String quizCode; // Added quiz code field

        public QuizSetup() {
            // Default constructor required for calls to DataSnapshot.getValue(QuizSetup.class)
        }

        public QuizSetup(int durationMinutes, long startTimeMillis, long deadlineMillis, boolean shuffleQuestions, String quizCode) {
            this.durationMinutes = durationMinutes;
            this.startTimeMillis = startTimeMillis;
            this.deadlineMillis = deadlineMillis;
            this.shuffleQuestions = shuffleQuestions;
            this.quizCode = quizCode;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
