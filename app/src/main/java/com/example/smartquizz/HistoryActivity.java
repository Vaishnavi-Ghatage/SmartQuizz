package com.example.smartquizz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private FirebaseAuth auth;
    private DatabaseReference quizzesRef;
    private LinearLayout quizzesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        // Initialize views
        quizzesLayout = findViewById(R.id.quizzesLayout);

        // Fetch the current user's email
        String currentUserEmail = auth.getCurrentUser().getEmail();

        // Fetch and display quizzes created by the current user
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizzesLayout.removeAllViews(); // Clear existing views

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String creatorEmail = quizSnapshot.child("creatorEmail").getValue(String.class);

                    if (currentUserEmail.equals(creatorEmail)) {
                        String quizName = quizSnapshot.child("quizName").getValue(String.class);
                        String quizId = quizSnapshot.getKey();

                        // Add the quiz card
                        addQuizCard(quizName, quizId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(HistoryActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.history);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.history:
                    // Current activity, do nothing
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
            }
            return false;
        });
    }

    private void addQuizCard(String quizName, String quizCode) {
        LinearLayout quizCard = new LinearLayout(this);
        quizCard.setOrientation(LinearLayout.VERTICAL);
        quizCard.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 24); // Set bottom margin to create a gap between cards
        quizCard.setLayoutParams(layoutParams);
        quizCard.setBackgroundResource(R.drawable.quiz_card_background); // Ensure you have this drawable

        TextView quizNameTextView = new TextView(this);
        quizNameTextView.setText(quizName);
        quizNameTextView.setTextSize(24); // Set text size to somewhat bigger
        quizNameTextView.setTextColor(getResources().getColor(R.color.white));
        quizNameTextView.setPadding(8, 8, 8, 8);

        quizCard.addView(quizNameTextView);

        quizCard.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, QuizCodeActivity.class);
            intent.putExtra("quizName", quizName);
            startActivity(intent);
        });

        quizzesLayout.addView(quizCard);
    }
}
