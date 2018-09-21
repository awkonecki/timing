package com.nebo.timing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_firebase_db_event);

        // 1. setup the firebase database instance.
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // 1. setup the firebase auth instance.
        mFirebaseAuth = FirebaseAuth.getInstance();

        // 2. setup the reference position.
        mDatabaseReference = mFirebaseDatabase
                                .getReference()
                                .child(getString(R.string.firebase_database_timed_activities));

        // 3. creation and addition of child listener to handle firebase events.
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded " + dataSnapshot.getValue(TimedActivity.class).getActivityName());

                // Will need to do something with the data.
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // 4. Creation of a event callback to force an event with the Firebase database.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimedActivity activity = new TimedActivity("workout");
                mDatabaseReference.push().setValue(activity);
            }
        });

        // 2. Create the Auth State listener.
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged.");
            }
        };

        // 3. Add to the auth instance.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
