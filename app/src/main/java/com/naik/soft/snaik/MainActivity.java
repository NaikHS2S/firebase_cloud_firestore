package com.naik.soft.snaik;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String COLLECTION_NAME = "users";
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        getUsersData();
        dataChangeListener();

        findViewById(R.id.addUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addOrUpdateContacts(((EditText)findViewById(R.id.name)).getText().toString()
                        , ((EditText)findViewById(R.id.email)).getText().toString()
                        , ((EditText)findViewById(R.id.phone)).getText().toString());
            }
        });



        findViewById(R.id.removeUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //removeUser();
                deleteContact();
            }
        });

    }

    private void removeUsers() {
        String documentPath = ((EditText)findViewById(R.id.documentPath)).getText().toString();

        db.collection(COLLECTION_NAME).document(documentPath)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        getUsersData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }

    private void getUsersData() {

        db.collection(COLLECTION_NAME)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            StringBuilder userData = new StringBuilder();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                userData.append(" id: ");
                                userData.append(String.valueOf(document.getId()));
                                userData.append(" data: ");
                                userData.append(String.valueOf(document.getData()));
                                userData.append("\n");
                            }

                            ((TextView)findViewById(R.id.textView)).setText(userData.toString());

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void dataChangeListener() {

        db.collection(COLLECTION_NAME)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Log.d(TAG, "New city: " + dc.getDocument().getData());
                            }
                        }

                        getUsersData();

                    }
                });

    }

    private void contactsChangeListener() {
        db.collection(COLLECTION_NAME).document("contacts").
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        getUsersData();

                    }
                });

    }



    private void addNewDocument( final String name, String email, String phone) {

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);

        // Add a new document with a generated ID
        db.collection(COLLECTION_NAME)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: "
                                + documentReference.getId());
                        displayMessage("DocumentSnapshot added with ID: "
                                + documentReference.getId());

                        getUsersData();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        displayMessage("Error adding document");
                    }
                });

    }

    private void addOrUpdateContacts( final String name, String email, String phone) {

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);

        // Add a new document with a generated ID
        db.collection(COLLECTION_NAME)
                .document("contacts").set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        displayMessage("success");
                        getUsersData();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displayMessage("failure");
            }
        });

    }

    private void deleteContact() {

        // Add a new document with a generated ID
        db.collection(COLLECTION_NAME)
                .document("contacts").delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        displayMessage("success");
                        //getUsersData();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displayMessage("failure");
            }
        });

    }



    private void displayMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

}
