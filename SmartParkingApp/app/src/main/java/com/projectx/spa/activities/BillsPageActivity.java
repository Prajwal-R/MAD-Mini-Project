package com.projectx.spa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.projectx.spa.R;
import com.projectx.spa.helpers.Constants;
import com.projectx.spa.helpers.UserSession;
import com.projectx.spa.models.ParkedHistory;
import com.projectx.spa.models.ParkedVehicle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BillsPageActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    TextView vehicleNumberTextView, entryTimeTextView, exitTimeTextView, amountTextView;
    String availableSpace, totalSpace, vehicleNumber, id;
    boolean flag = false;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    Timestamp exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills_page);

        vehicleNumberTextView = findViewById(R.id.bill_activity_vehicle_number);
        entryTimeTextView = findViewById(R.id.bill_activity_entry_time);
        exitTimeTextView = findViewById(R.id.bill_activity_exit_time);
        amountTextView = findViewById(R.id.amount);

        Intent intent = getIntent();
        vehicleNumber = intent.getStringExtra("number");
        id = new UserSession(this).getUserDetails().get(Constants.PREF_ID);

        DocumentReference docRef = firebaseFirestore.collection(Constants.PARKING_SLOTS).document(id);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        availableSpace = documentSnapshot.get("availableSpace").toString();
                        totalSpace = documentSnapshot.get("totalSpace").toString();
                        Log.d(TAG, "avail=" + availableSpace);
                        Log.d(TAG, "total=" + totalSpace);

                        database();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "avail failed");
                    }
                });

//        database();
    }

    private void database() {
        if (Integer.parseInt(availableSpace) <= Integer.parseInt(totalSpace)) {
            String collectionReference = Constants.PARKING_SLOTS + "/" + id + "/" + Constants.PARKED_VEHICLES;
            firebaseFirestore.collection(collectionReference)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    ParkedVehicle parkedVehicle = document.toObject(ParkedVehicle.class);

                                    if (parkedVehicle.getVehicleNumber().equals(vehicleNumber)) {
                                        flag = true;
                                        int val = Integer.parseInt(availableSpace) + 1;
                                        Date entryDate = parkedVehicle.getEntryTime().toDate();
                                        vehicleNumberTextView.setText(vehicleNumber);
                                        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                                        exitTime = Timestamp.now();
                                        Date exittime = exitTime.toDate();
                                        String entime = timeFormat.format(entryDate);
                                        String extime = timeFormat.format(exittime);
                                        entryTimeTextView.append(" " + entime);
                                        exitTimeTextView.append(" " + extime);

                                        DocumentReference doc = firebaseFirestore.collection(collectionReference).document(document.getId());
                                        doc.update("exitTime", exitTime)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // TODO: 09-07-2021 onsuccess for updating
                                                        firebaseFirestore.collection(Constants.PARKING_SLOTS).document(id).update("availableSpace", val);
                                                        Log.d(TAG, "updated successfully");
                                                        long time_difference = exittime.getTime() - entryDate.getTime();
                                                        long minutes_difference = (time_difference / 1000) / 60;
                                                        int amt = (int) Math.ceil(minutes_difference * 10 / 20);
                                                        amountTextView.append("" + amt);
                                                        Log.d(TAG, String.valueOf(minutes_difference));
                                                        CollectionReference collectionReference2 = firebaseFirestore.collection(Constants.PARKING_SLOTS).document(id).collection(Constants.PARKED_HISTORY);
                                                        DocumentReference historyDocument = collectionReference2.document();
                                                        ParkedHistory parkedHistory = new ParkedHistory(historyDocument.getId(),
                                                                parkedVehicle.getVehicleNumber(), parkedVehicle.getEntryTime(), exitTime, amt);
                                                        moveFirestoreDocument(document.getReference(), historyDocument, parkedHistory);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "update failed");
                                                    }
                                                });
                                        Log.d(TAG, exitTime.toDate().toString());


                                    }

                                }
                                if (!flag) {
                                    show("data not found");
                                }
                            } else {
                                show("Error getting documents." + task.getException());
                            }
                        }
                    });
        } else {
            Log.d(TAG, "error");
        }
    }


    private void show(String toastMessage) {
        Log.d(TAG, toastMessage);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    public void moveFirestoreDocument(DocumentReference fromPath, final DocumentReference toPath, ParkedHistory parkedHistory) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        toPath.set(parkedHistory)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        fromPath.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting document", e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "move error" + e);
                    }
                });
    }

    /*@Override
    public void onBackPressed() {
        Intent setIntent = new Intent(BillsPageActivity.this, AdminHomeActivity.class);
        startActivity(setIntent);
        finish();
    }*/
}
