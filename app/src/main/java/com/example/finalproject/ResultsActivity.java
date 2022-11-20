package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResultsActivity extends AppCompatActivity {

    TextView name;
    TextView rating;
    TextView location;
    TextView hours;
    TextView phoneNumber;
    TextView noResults;
    ToggleButton star;
    View map;

    //Make data instance global to access from all classes and functions in activity
    YelpData data = SearchActivity.getDataInstance();
    String openClosed;

    //.document("users/test"); alternates between storing in users and collections
    private DocumentReference docRef;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Get rid of pesky titlebar

        star = findViewById(R.id.star);
        name = findViewById(R.id.name);
        rating = findViewById(R.id.rating);
        location = findViewById(R.id.location);
        hours = findViewById(R.id.hours);
        phoneNumber = findViewById(R.id.phoneNumber);
        map = findViewById(R.id.map);
        noResults = findViewById(R.id.noResults);

        //If user has made a search and clicked on listview or is viewing a business from favorites
        //then make all elements visible and perform necessary functions
        if (SearchActivity.searchConducted == 1 || SearchActivity.searchConducted == 2) {
            name.setVisibility(View.VISIBLE); //Make all elements visible
            rating.setVisibility(View.VISIBLE);
            location.setVisibility(View.VISIBLE);
            hours.setVisibility(View.VISIBLE);
            phoneNumber.setVisibility(View.VISIBLE);
            star.setVisibility(View.VISIBLE);
            map.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.GONE);

            //Get Yelp data and place into textViews
            name.setText(data.getName());
            rating.setText("Rating: " + data.getRating());
            location.setText("Address: " + data.getAddress());

            openClosed = data.getOpenClosed();
            //Condition to make openClosed say Open or Closed instead of true/false
            if (openClosed != null) {
                if (openClosed.equals("false")) {
                    openClosed = "Open";
                } else openClosed = "Closed";
                hours.setText("Open/Closed: " + openClosed);
            }

            phoneNumber.setText("Phone Number: " + data.getPhoneNumber());

            //If user is signed in
            if (MainActivity.signIn == 1) {
                //make an instance with a collection titled after user's email and document based on business name they clicked on
                DocumentReference docRef = FirebaseFirestore.getInstance().collection(ProfileActivity.email).document(data.getName());

                //If user toggles toggle button
                star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //If user clicks button once to give a filled star
                        if (isChecked) {
                            //Change button to have it appear filled
                            star.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_heart_filled));

                            //Put Yelp data on business into document in the cloud
                            Map<String, Object> document = new HashMap<>();
                            document.put("Business Name", data.getName());
                            document.put("Rating", data.getRating());
                            document.put("Location", data.getAddress());
                            document.put("Open or Closed", openClosed);
                            document.put("Phone Number", data.getPhoneNumber());
                            document.put("Latitude", data.getLat());
                            document.put("Longitude", data.getLong());
                            //If saving to document is a success
                            docRef.set(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("RESULT", "Document has been saved");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("RESULT", "Document has not been saved");
                                }
                            });

                            Toast toast = Toast.makeText(ResultsActivity.this, data.getName() + " has been added to your favorites", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 950);
                            toast.show();
                        } else { //If user clicks button again to make it appear empty
                            star.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_heart));
                            //Get business name document from email collection and delete document
                            docRef.delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("positionCount", data.getName() + " successfully removed");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("FavoriteDelete", "Error deleting document", e);
                                        }
                                    });

                            Toast toast = Toast.makeText(ResultsActivity.this, data.getName() + " has been removed from your favorites", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 950);
                            toast.show();
                        }
                    }
                });

                //Get document info, if it exists and user views results page again, make star appear filled
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            star.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_heart_filled));
                        }
                    }
                });
            } else
                star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // Create the object of AlertDialog Builder class
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
                        // Set Alert Title
                        builder.setTitle("Alert!");
                        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                        builder.setCancelable(false);
                        //Set the message show for the Alert time
                        builder.setMessage("You must first login to be able to use this feature");
                        // Set the positive button with yes name OnClickListener method is use of DialogInterface interface.
                        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //When the user click yes button then app will close
                                dialog.cancel();
                            }
                        });
                        //Create the Alert dialog
                        AlertDialog alertDialog = builder.create();
                        //Show the Alert Dialog box
                        alertDialog.show();
                    }
                });
            //If user is not signed in, remove favorites option
        } else {
            //If user has not made a search and is attempting to view results page, then make all
            //elements gone and display that no results have been searched
            name.setVisibility(View.GONE);
            rating.setVisibility(View.GONE);
            location.setVisibility(View.GONE);
            hours.setVisibility(View.GONE);
            phoneNumber.setVisibility(View.GONE);
            star.setVisibility(View.GONE);
            map.setVisibility(View.GONE);
        }

        //Bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_results);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        //If user is not signed in, jump to MainActivity
                        //Reason for these conditions is originally, switching between search and main
                        //will show a quick preview of profile activity before going to main activity.
                        //These conditions eliminate that issue
                        if (MainActivity.signIn == 0) {
                            Intent home = new Intent(ResultsActivity.this, MainActivity.class);
                            startActivity(home);
                        } else { //If user signed in, jump to Profile Activity
                            Intent home = new Intent(ResultsActivity.this, ProfileActivity.class);
                            startActivity(home);
                        }
                        break;
                    case R.id.navigation_search:
                        Intent search = new Intent(ResultsActivity.this, SearchActivity.class);
                        startActivity(search);
                        break;
                    case R.id.navigation_favorites:
                        //If user is signed out, alert is thrown if user goes to favorites page
                        if (MainActivity.signIn == 0) {
                            // Create the object of AlertDialog Builder class
                            AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
                            // Set Alert Title
                            builder.setTitle("Alert!");
                            // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                            builder.setCancelable(false);
                            //Set the message show for the Alert time
                            builder.setMessage("You must first login to be able to use this feature");
                            // Set the positive button with yes name OnClickListener method is use of DialogInterface interface.
                            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //When the user click yes button then app will close
                                    dialog.cancel();
                                }
                            });
                            //Create the Alert dialog
                            AlertDialog alertDialog = builder.create();
                            //Show the Alert Dialog box
                            alertDialog.show();
                        } else {
                            Intent favorites = new Intent(ResultsActivity.this, FavoritesActivity.class);
                            startActivity(favorites);
                        }
                        break;
                }
                return true;
            }
        });
    }
}