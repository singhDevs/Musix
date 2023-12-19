package com.example.musix.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.musix.R;
import com.example.musix.handlers.GoogleSignInHelper;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.makeramen.roundedimageview.RoundedImageView;

public class Login extends AppCompatActivity {
    private BeginSignInRequest signInRequest;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private LinearLayout googleSignIn;
    private MaterialButton loginBtn;
    private CardView loginCard;
    private EditText edtEmail, edtPassword;
    private String email, pass;
    private VideoView videoView;
    private ToggleButton eyepass;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video; // Replace with your actual video file

        // Parse the URI and set it to the VideoView
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.setOnCompletionListener(mp -> videoView.start());
        videoView.start();

        loginCard = findViewById(R.id.loginCard);
        loginCard.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        loginCard.startAnimation(slideUp);

        // [START config_sign in]
        //Configuring Google Sign In
        GoogleSignInOptions gso = GoogleSignInHelper.getSignInOptions(this);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //[END config_sign in]

        mAuth = FirebaseAuth.getInstance();

        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .build();

        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = edtEmail.getText().toString();
                pass = edtPassword.getText().toString();

                if(email.isEmpty() || pass.isEmpty()){
                    Toast.makeText(Login.this, "Both email and password are required", Toast.LENGTH_LONG).show();
                }
                else{
                    logIn();
                }
            }
        });

        eyepass = findViewById(R.id.eyepass);
        eyepass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    //Show Password
                    edtPassword.setTransformationMethod(null);
                    eyepass.setBackground(getDrawable(R.drawable.hidepass));
                }
                else{
                    //Hide Password
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyepass.setBackground(getDrawable(R.drawable.showpass));
                }
            }
        });

        googleSignIn = findViewById(R.id.googleSignIn);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "FirebaseAuth with Google: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.d("TAG", "Google sign in failed");
                throw new RuntimeException(e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("TAG", "signInWithCredential: SUCCESS");
                            FirebaseUser user  = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else{
                            Log.w("TAG", "signInWithCredential: FAILED", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void logIn(){
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d("imp", "signInWithEmail:success");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    if(!getIntent().getBooleanExtra("isLoggedOut", false)){
                        finish();
                    }
                }
                else{
                    Log.e("imp", "signInWithEmail:failure", task.getException());
                    Toast.makeText(getApplicationContext(), "Authentication failed. Check the entered Email and the Password entered", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            startActivity(new Intent(this, MainActivity.class));
        }
        else{
//            Toast.makeText(this, "Error Signing in", Toast.LENGTH_SHORT).show();
        }
    }
}