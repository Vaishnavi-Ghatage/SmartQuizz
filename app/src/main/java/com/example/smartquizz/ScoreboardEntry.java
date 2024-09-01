package com.example.smartquizz;

public class ScoreboardEntry {
    private String usn;
    private int correctAnswers;
    private int totalAttempts;

    public ScoreboardEntry() {
        // Default constructor required for calls to DataSnapshot.getValue(ScoreboardEntry.class)
    }

    public ScoreboardEntry(String usn, int correctAnswers, int totalAttempts) {
        this.usn = usn;
        this.correctAnswers = correctAnswers;
        this.totalAttempts = totalAttempts;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
}
