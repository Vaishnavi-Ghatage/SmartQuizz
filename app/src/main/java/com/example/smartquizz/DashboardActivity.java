package com.example.smartquizz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private EditText quizCodeEditText, usnEditText;
    private Button enterButton;
    private FirebaseAuth auth;
    private DatabaseReference userRef, quizzesRef, progressRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");
        progressRef = FirebaseDatabase.getInstance().getReference("progress");

        // Find views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        quizCodeEditText = findViewById(R.id.quizCodeEditText);
        usnEditText = findViewById(R.id.usnEditText);
        enterButton = findViewById(R.id.enterButton);

        // Fetch and display the username
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                welcomeTextView.setText("Welcome " + username + " 😊");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        enterButton.setOnClickListener(v -> {
            String quizCode = quizCodeEditText.getText().toString().trim();
            String usn = usnEditText.getText().toString().trim();

            if (quizCode.isEmpty()) {
                quizCodeEditText.setError("Quiz Code is required");
                quizCodeEditText.requestFocus();
                return;
            }

            if (usn.isEmpty()) {
                usnEditText.setError("USN is required");
                usnEditText.requestFocus();
                return;
            }

            saveUsnToDatabase(usn);
            checkQuizCode(quizCode, usn);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    return true;
                case R.id.history:
                    startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.plus:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.help:
                    startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(), MainActivity21.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                default:
                    return false;
            }
        });
    }

    private void checkQuizCode(String quizCode, String usn) {
        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean quizFound = false;
                boolean quizActive = false;
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    if (quizSnapshot.child("setup").child("quizCode").exists() &&
                            quizCode.equals(quizSnapshot.child("setup").child("quizCode").getValue(String.class))) {
                        quizFound = true;
                        long startTimeMillis = quizSnapshot.child("setup").child("startTimeMillis").getValue(Long.class);
                        long deadlineMillis = quizSnapshot.child("setup").child("deadlineMillis").getValue(Long.class);

                        if (currentTime >= startTimeMillis && currentTime <= deadlineMillis) {
                            quizActive = true;

                            // Check if the student has already attended the quiz
                            progressRef.child(quizCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot progressSnapshot) {
                                    if (progressSnapshot.hasChild(usn)) {
                                        showDialog("Quiz Already Attended", "You can't attend the quiz twice.");
                                    } else {
                                        Intent intent = new Intent(DashboardActivity.this, CountdownActivity.class);
                                        intent.putExtra("quizCode", quizCode);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    showDialog("Database Error", "Error: " + databaseError.getMessage());
                                }
                            });
                        } else if (currentTime < startTimeMillis) {
                            String startTime = formatDate(startTimeMillis);
                            showDialog("Quiz Not Started", "Quiz hasn't started yet. Start time: " + startTime);
                        } else if (currentTime > deadlineMillis) {
                            showDialog("Quiz Over", "Quiz deadline is over.");
                        }
                    }
                }

                if (!quizFound) {
                    showDialog("Quiz Not Found", "No such quiz exists.");
                } else if (quizFound && !quizActive) {
                    showDialog("Quiz Not Active", "Quiz is not active at this time.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDialog("Database Error", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void saveUsnToDatabase(String usn) {
        String userId = auth.getCurrentUser().getUid();
        userRef.child("usn").setValue(usn).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DashboardActivity.this, "USN saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DashboardActivity.this, "Failed to save USN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
