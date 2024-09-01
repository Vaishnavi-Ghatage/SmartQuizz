package com.example.smartquizz;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class CountdownActivity extends AppCompatActivity {
    private TextView textViewCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        textViewCountdown = findViewById(R.id.textViewCountdown);

        // Center align the text horizontally
        textViewCountdown.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewCountdown.setTextColor(Color.parseColor("#800080")); // Set text color to purple

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                textViewCountdown.setText(String.valueOf(secondsLeft));
                fadeInOutAnimation(textViewCountdown);
            }

            public void onFinish() {
                textViewCountdown.setText("Let's start the quiz!");
                new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        // Do nothing
                    }

                    public void onFinish() {
                        startQuiz();
                    }
                }.start();
            }
        }.start();
    }

    private void fadeInOutAnimation(TextView textView) {
        // Fade-out animation
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(800);
        fadeOut.setFillAfter(true); // Keep the view invisible after the animation

        // Fade-in animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(800);
        fadeIn.setFillAfter(true); // Keep the view visible after the animation

        fadeIn.setStartOffset(800); // Start fade-in after fade-out completes

        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animation == fadeOut) {
                    textView.startAnimation(fadeIn);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        };

        fadeOut.setAnimationListener(animationListener);
        textView.startAnimation(fadeOut);
    }

    private void startQuiz() {
        Intent intent = new Intent(CountdownActivity.this, giveQuiz.class);
        String teacherCode = getIntent().getStringExtra("quizCode");
        intent.putExtra("TEACHER_CODE", teacherCode);
        startActivity(intent);
        finish();
    }
}
