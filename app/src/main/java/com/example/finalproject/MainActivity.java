package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    String name, email, idToken;
    public static int signIn = 0; //Public variable with only two values: 0 and 1
    //Certain actions are restricted throughout the app if value is 0

    TextView signInTip;
    TextView welcome;
    View profileImage;
    TextView favoritesTip;
    Button signOutButton;
    Button favoritesButton;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Get rid of pesky titlebar


        signInTip = findViewById(R.id.signInTip);
        SignInButton signInButton;
        Button signOutButton = findViewById(R.id.signOutButton);
        favoritesButton = findViewById(R.id.favoritesButton);

        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in
                    // you could place other firebase code logic to save the user details to Firebase
                    Log.d("MainActivity", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("MainActivity", "onAuthStateChanged:signed_out");
                }
            }
        };

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        signInButton = findViewById(R.id.signInButton);

        //Upon clicking on sign in button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent for getting Google Sign in API
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });

        //Get bottom nav id so an item select listener can be set for switching between activites
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_search: //If user goes to search page
                        Intent search = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(search);
                        break;
                    case R.id.navigation_results: //If user goes to results page
                        Intent results = new Intent(MainActivity.this, ResultsActivity.class);
                        startActivity(results);
                        break;
                    case R.id.navigation_favorites: //If user goes to favorites page
                        //Going to favorites page while signed out gives alert when going to favorites page
                        // Create the object of AlertDialog Builder class
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){ //If requestCode == to 1, get sign in info
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            assert result != null;
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){ //Get info about user
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            assert account != null;
            idToken = account.getIdToken();
            name = account.getDisplayName();
            email = account.getEmail();
            // you can store user data to SharedPreference
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential);
        }else{
            // Google Sign In failed, update UI appropriately
            Log.e("MainActivity", "Login Unsuccessful. "+result);
            Toast toast = Toast.makeText(MainActivity.this, "Login Unsuccessful", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 950);
            toast.show();
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //If firebase log in successful, go to profile, else throw exception
                Log.d("MainActivity", "signInWithCredential:onComplete:" + task.isSuccessful());
                if (task.isSuccessful()) {
                    Toast toast = Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 950);
                    toast.show();
                    gotoProfile();
                } else {
                    Log.w("MainActivity", "signInWithCredential" + Objects.requireNonNull(task.getException()).getMessage());
                    task.getException().printStackTrace();

                    Toast toast = Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 950);
                    toast.show();
                }
            }
        });
    }

    private void gotoProfile(){ //After signing in, go to ProfileActivity from MainActivity
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null){
            FirebaseAuth.getInstance().signOut();
        }
        assert authStateListener != null;
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}