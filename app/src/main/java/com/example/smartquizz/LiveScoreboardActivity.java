package com.example.smartquizz;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LiveScoreboardActivity extends AppCompatActivity {

    private ListView scoreboardListView;
    private ScoreboardAdapter adapter;
    private List<ScoreboardEntry> scoreboardEntries;
    private DatabaseReference resultsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_scoreboard);

        scoreboardListView = findViewById(R.id.scoreboardListView);
        scoreboardEntries = new ArrayList<>();
        adapter = new ScoreboardAdapter(this, R.layout.scoreboard_item, scoreboardEntries);
        scoreboardListView.setAdapter(adapter);

        // Replace with your actual quiz code
        String quizCode = getIntent().getStringExtra("quizCode");
        if (quizCode == null) {
            Log.e("LiveScoreboardActivity", "Quiz code not provided in Intent");
            return;
        }

        resultsRef = FirebaseDatabase.getInstance().getReference("results").child(quizCode);

        resultsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scoreboardEntries.clear();
                for (DataSnapshot usnSnapshot : snapshot.getChildren()) {
                    String usn = usnSnapshot.getKey();
                    Integer correctAnswersCount = usnSnapshot.child("correctAnswersCount").getValue(Integer.class);
                    Integer totalQuestions = usnSnapshot.child("totalQuestionsAttempted").getValue(Integer.class);

                    // Ensure totalQuestions and correctAnswersCount are not null
                    if (correctAnswersCount != null && totalQuestions != null) {
                        int percentage = (totalQuestions > 0) ? (correctAnswersCount * 100) / totalQuestions : 0;
                        ScoreboardEntry entry = new ScoreboardEntry(usn, percentage, totalQuestions);
                        scoreboardEntries.add(entry);
                    }
                }

                // Sort the list in descending order based on percentage
                Collections.sort(scoreboardEntries, new Comparator<ScoreboardEntry>() {
                    @Override
                    public int compare(ScoreboardEntry o1, ScoreboardEntry o2) {
                        return Integer.compare(o2.getCorrectAnswers(), o1.getCorrectAnswers());
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LiveScoreboardActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
