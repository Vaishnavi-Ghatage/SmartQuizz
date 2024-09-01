package com.example.smartquizz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class studentEnter extends AppCompatActivity {
    private EditText editTextTeacherCode;
    private Button buttonJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_enter);

        editTextTeacherCode = findViewById(R.id.editTextTeacherCode);
        buttonJoin = findViewById(R.id.buttonJoin);

        buttonJoin.setOnClickListener(v -> {
            String teacherCode = editTextTeacherCode.getText().toString().trim();
            if (!teacherCode.isEmpty()) {
                checkQuizCode(teacherCode);
            } else {
                showDialog("Error", "Please enter a quiz code");
            }
        });
    }

    private void checkQuizCode(String teacherCode) {
        DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");

        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean quizFound = false;
                boolean quizActive = false;
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    if (quizSnapshot.child("setup").child("quizCode").exists() &&
                            teacherCode.equals(quizSnapshot.child("setup").child("quizCode").getValue(String.class))) {
                        quizFound = true;
                        long startTimeMillis = quizSnapshot.child("setup").child("startTimeMillis").getValue(Long.class);
                        long deadlineMillis = quizSnapshot.child("setup").child("deadlineMillis").getValue(Long.class);

                        if (currentTime >= startTimeMillis && currentTime <= deadlineMillis) {
                            quizActive = true;
                            Intent intent = new Intent(studentEnter.this, CountdownActivity.class);
                            intent.putExtra("TEACHER_CODE", teacherCode);
                            startActivity(intent);
                            break;
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
