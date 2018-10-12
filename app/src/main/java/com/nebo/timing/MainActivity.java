package com.nebo.timing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nebo.timing.ui.StopWatchFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /*
    private String mCurrentUser = null;

    private final String TAG = "MainActivity";
    private final int RC_SIGN_IN = 1;

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
                Log.d(TAG, "onChildAdded " + dataSnapshot.getValue(TimedActivityDetailActivity.class).getActivityName());

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
                TimedActivityDetailActivity activity = new TimedActivityDetailActivity("workout");
                mDatabaseReference.push().setValue(activity);
            }
        });

        // 2. Create the Auth State listener.
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged.");

                if (firebaseAuth.getCurrentUser() == null) {
                    Log.d(TAG, "onAuthStateChanged - null user");
                    // No one is signed-in
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
                else {
                    // someone is already sign-in
                    Log.d(TAG, "onAuthStateChanged - valid user");
                }

            }
        };

        // 3. Add to the auth instance.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        // 4. Support for signout based on user event.
        Button signoutBtn = findViewById(R.id.btn_firebase_logout);
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "signoutBtn onClick");
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                finish();
                            }
                        });


            }
        });

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        LineChart chart = (LineChart) findViewById(R.id.chart);

        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(1, 5));
        entries.add(new Entry(2, 3));
        entries.add(new Entry(3, 2));
        entries.add(new Entry(4, 6));

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        // dataSet.setColor(...);
        // dataSet.setValueTextColor(...);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        // Fragment creation
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = new StopWatchFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_stop_watch, fragment).commit();

        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
    }
    */
}
