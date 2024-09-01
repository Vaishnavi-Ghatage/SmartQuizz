package com.example.smartquizz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QuizCodeActivity extends AppCompatActivity {

    private TextView quizCodeTextView;
    private Button downloadScorecardButton;
    private String quizCode;
    private DatabaseReference quizzesRef, progressRef;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_code);

        // Request necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        quizCodeTextView = findViewById(R.id.quizCodeTextView);
        downloadScorecardButton = findViewById(R.id.downloadScorecardButton);
        downloadScorecardButton.setVisibility(View.GONE); // Initially hide the button

        // Initialize Firebase Database references
        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");
        progressRef = FirebaseDatabase.getInstance().getReference().child("progress");

        // Get quizName from intent
        String quizName = getIntent().getStringExtra("quizName");

        if (quizName != null) {
            fetchQuizCode(quizName);
        } else {
            // If quizName is not available, get quizCode directly from intent
            quizCode = getIntent().getStringExtra("quizCode");
            if (quizCode != null) {
                quizCodeTextView.setText("Quiz Code: " + quizCode);
                checkQuizTime(); // Directly check the quiz time
            }
        }

        downloadScorecardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadScorecard();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchQuizCode(final String quizName) {
        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean quizFound = false;

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String storedQuizName = quizSnapshot.child("quizName").getValue(String.class);

                    if (quizName.equals(storedQuizName)) {
                        quizCode = quizSnapshot.child("setup").child("quizCode").getValue(String.class);
                        if (quizCode != null) {
                            quizCodeTextView.setText("Quiz Code: " + quizCode);
                            quizFound = true;
                            checkQuizTime();
                            break;
                        }
                    }
                }

                if (!quizFound) {
                    Toast.makeText(QuizCodeActivity.this, "Quiz not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuizCodeActivity.this, "Failed to load quiz code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkQuizTime() {
        quizzesRef.child(quizCode).child("setup").child("endTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long endTime = dataSnapshot.getValue(Long.class);
                long currentTime = System.currentTimeMillis();

                if (endTime != null) {
                    if (currentTime > endTime) {
                        downloadScorecardButton.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(QuizCodeActivity.this, "Quiz is still ongoing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuizCodeActivity.this, "End time not set", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuizCodeActivity.this, "Failed to check quiz time", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadScorecard() {
        progressRef.child(quizCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Document document = new Document();
                String filePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Scorecard_" + quizCode + ".pdf";

                try {
                    PdfWriter.getInstance(document, new FileOutputStream(filePath));
                    document.open();

                    // Create a table with 2 columns
                    PdfPTable table = new PdfPTable(2);
                    table.addCell("USN");
                    table.addCell("Correct Answers");

                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String usn = studentSnapshot.getKey();
                        Long correctAnswersCount = studentSnapshot.child("correctAnswersCount").getValue(Long.class);
                        if (usn != null && correctAnswersCount != null) {
                            table.addCell(usn);
                            table.addCell(correctAnswersCount.toString());
                        }
                    }

                    document.add(table);
                    document.close();

                    Toast.makeText(QuizCodeActivity.this, "Scorecard downloaded", Toast.LENGTH_SHORT).show();
                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                    Toast.makeText(QuizCodeActivity.this, "Failed to download scorecard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuizCodeActivity.this, "Failed to load score data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void shareQuizCode(View view) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my quiz using this code: " + quizCode);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Share quiz code via"));
    }

    public void viewLiveScoreboard(View view) {
        Intent intent = new Intent(QuizCodeActivity.this, LiveScoreboardActivity.class);
        intent.putExtra("quizCode", quizCode);
        startActivity(intent);
    }
}
