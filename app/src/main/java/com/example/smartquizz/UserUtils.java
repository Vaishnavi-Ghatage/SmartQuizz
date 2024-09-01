package com.example.smartquizz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class UserUtils {
    private DatabaseReference usersRef;

    public UserUtils() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void getCurrentUserUSN(final Callback callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(currentUserId).child("usn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String usn = dataSnapshot.getValue(String.class);
                callback.onCallback(usn);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    public interface Callback {
        void onCallback(String usn);
    }
}
