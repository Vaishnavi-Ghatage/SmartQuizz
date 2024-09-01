package com.example.smartquizz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity21 extends AppCompatActivity {

    private ImageView profileImg;
    private TextView titleName, titleUsername, profileName, profileEmail, profileUsername;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main21);

        // Initialize views
        profileImg = findViewById(R.id.profileImg);
        titleName = findViewById(R.id.titleName);
        titleUsername = findViewById(R.id.titleUsername);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileUsername = findViewById(R.id.profileUsername);
        logoutButton = findViewById(R.id.logoutButton);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Check if user is logged in
        if (mAuth.getCurrentUser() != null) {
            // Get current user ID
            String userId = mAuth.getCurrentUser().getUid();

            // Fetch user data
            databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get user data
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        // Update UI
                        titleName.setText(name);
                        titleUsername.setText(username);
                        profileName.setText(name);
                        profileEmail.setText(email);
                        profileUsername.setText(username);

                        // Load profile image using Glide
                        Glide.with(MainActivity21.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_profile_image) // Placeholder image
                                .into(profileImg);
                    } else {
                        Toast.makeText(MainActivity21.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity21.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Redirect to login activity if user is not logged in
            startActivity(new Intent(MainActivity21.this, LoginActivity.class));
            finish();
        }

        // Log out button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity21.this, LoginActivity.class));
                finish();
            }
        });

        // Set up bottom navigation view
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                case R.id.history:
                    startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                case R.id.plus:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                case R.id.help:
                    startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                case R.id.profile:
                    // Current activity, do nothing or handle as needed
                    return true;
            }
            return false;
        });
    }
}
