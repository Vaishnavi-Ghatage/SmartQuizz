package com.example.smartquizz;
public class Quiz {
    private String quizId;
    private String quizName;
    private int numQuestions;
    private String creatorEmail;

    public Quiz() {
        // Default constructor required for calls to DataSnapshot.getValue(Quiz.class)
    }

    public Quiz(String quizId, String quizName, int numQuestions, String creatorEmail) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.numQuestions = numQuestions;
        this.creatorEmail = creatorEmail;
    }

    // Getters and Setters
    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestions(int numQuestions) {
        this.numQuestions = numQuestions;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }
}
