package com.projectx.spa.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.projectx.spa.R;
import com.projectx.spa.adapters.ParkingSpacesCardAdapter;
import com.projectx.spa.helpers.FBHelper;
import com.projectx.spa.models.ParkingSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CheckAvailability extends AppCompatActivity {
    private List<ParkingSlot> parkingSlots;
    private FBHelper fbHelper;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_availability);

        fbHelper = new FBHelper(this);
        documentReference = FirebaseFirestore.getInstance().collection("test").document("doc1");

        readRealTime();
        parkingSlots = new ArrayList<>();
    }

    //    to add data (custom class object) to the firebase firestore
    public void addData(View view) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "rkm");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firebaseFirestore.collection("test").document("doc1");
        map.put("ref", reference);
        reference
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        makeToast("Success");
                    }
                });

        Random random = new Random();
        UUID uuid = UUID.randomUUID();
        ParkingSlot parkingSlot = new ParkingSlot(
                uuid,
                "City Centre-" + random.nextInt(10),
                "Bengaluru-" + random.nextInt(10),
                random.nextInt(20),
                random.nextInt(10),
                Timestamp.now());

        fbHelper.addDataToFirestore(parkingSlot);
    }

    //    to read data from the firebase firestore
    public void read(View view) {
        readRealTime();
        parkingSlots.clear(); // clear existing data from the list
        parkingSlots = fbHelper.readDataFromFirestore();
    }

    //    for next button action
    public void updateList(View view) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ParkingSpacesCardAdapter parkingSpacesCardAdapter = new ParkingSpacesCardAdapter(this, parkingSlots);
        recyclerView.setAdapter(parkingSpacesCardAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //    for creating Toast
    private void makeToast(String toastMessage) {
        Toast.makeText(CheckAvailability.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    //    a test for real time data update of a single document
    //    https://dzone.com/articles/cloud-firestore-read-write-update-and-delete
    //    https://firebase.google.com/docs/firestore/query-data/listen
    //    Listen to multiple documents in a collection:
    //    https://firebase.google.com/docs/firestore/query-data/listen#listen_to_multiple_documents_in_a_collection
    void readRealTime() {
        Map<String, Object> map = new HashMap<>();
        Random random = new Random();
        map.put("name", "project-X123" + random.nextInt(100));
        map.put("email", "rkm@test.com" + random.nextInt(100));
        map.put("realtime", "testing" + random.nextInt(100));

        documentReference
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        makeToast("Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast("Update failed");
                    }
                });

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG1", "Listen failed.", e);
                    makeToast(e.getMessage());
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG1", "Current data: " + snapshot.getData());
                    makeToast(snapshot.getData().toString());
                } else {
                    Log.d("TAG1", "Current data: null");
                    makeToast("null");
                }
            }
        });
    }
}