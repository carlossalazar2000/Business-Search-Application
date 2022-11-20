package com.example.finalproject;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    Button signOutButton;
    Button favoritesButton;
    TextView userName, userEmail, userId;
    ImageView profileImage;
    private GoogleApiClient googleApiClient;
    public static String displayName, email; //Make username and email public for usage in other activites

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Get rid of pesky titlebar
        MainActivity.signIn = 1; //Make signIn = 1 so actions can be used in other activites

        signOutButton = findViewById(R.id.signOutButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        userName = findViewById(R.id.name);
        userEmail = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileImage);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        googleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        //Click listener for when user taps to sign out
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set sign in back to 0 to restrict access to features for user
                MainActivity.signIn = 0;
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()){
                            //Go back to MainActivity (signed in page)
                            gotoMainActivity();
                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(),"Session not close",Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 950);
                            toast.show();
                        }
                    }
                });
            }
        });

        //Click listener for favorites button when user wants to view favorites
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create an intent and have user go to favorites from ProfileActivity
                Intent favorites = new Intent(ProfileActivity.this, FavoritesActivity.class);
                startActivity(favorites);
            }
        });

        //Bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_search:
                        Intent search = new Intent(ProfileActivity.this, SearchActivity.class);
                        startActivity(search);
                        break;
                    case R.id.navigation_results:
                        Intent results = new Intent(ProfileActivity.this, ResultsActivity.class);
                        startActivity(results);
                        break;
                    case R.id.navigation_favorites:
                        Intent favorites = new Intent(ProfileActivity.this, FavoritesActivity.class);
                        startActivity(favorites);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr= Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if(opr.isDone()) {
            GoogleSignInResult result=opr.get();
            handleSignInResult(result);
        }else{ opr.setResultCallback(this::handleSignInResult); }
    }

    //Get user info and takes result of google sign in as parameter to get info
    public void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            assert account != null;
            //Get user info
            displayName = account.getDisplayName();
            email = account.getEmail();
            userName.setText(displayName);
            userEmail.setText(email);
            try{
                //Get photo URL and place into ImageView to see in app
                Glide.with(this).load(account.getPhotoUrl()).into(profileImage);
            }catch (NullPointerException e){
                Toast toast = Toast.makeText(getApplicationContext(),"image not found",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 950);
                toast.show();
            }
        }else{
            gotoMainActivity();
        }
    }

    private void gotoMainActivity() { //Go to Main Activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
}