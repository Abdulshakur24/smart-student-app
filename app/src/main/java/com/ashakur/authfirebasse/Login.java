package com.ashakur.authfirebasse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String AUTH_KEY = "isAuthenticated";

    private EditText email;
    private EditText password;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // User is already signed in, redirect to MainActivity
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView dontHaveAnAccount = findViewById(R.id.dont_have_account);

        String text = getString(R.string.dont_have_account);

        SpannableString spannableString = new SpannableString(text);

        int start = text.indexOf("here");
        int end = start + "here".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        };

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        dontHaveAnAccount.setText(spannableString);
        dontHaveAnAccount.setMovementMethod(LinkMovementMethod.getInstance());

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });
    }

    private boolean validateInputs() {
        String user = email.getText().toString().trim();
        String pass = password.getText().toString();

        boolean isValid = true;

        email.setError(null);
        password.setError(null);

        if (user.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            isValid = false;
        } else if (user.length() < 3) {
            email.setError("Email must be at least 3 characters");
            email.requestFocus();
            isValid = false;
        }

        if (pass.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            isValid = false;
        } else if (pass.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void performLogin() {
        String emailAddress = email.getText().toString().trim();
        String pass = password.getText().toString();

        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(emailAddress, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Login.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                    loginButton.setEnabled(true);
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, save authentication state
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(AUTH_KEY, true);
            editor.apply();

            // Navigate to MainActivity
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // User is signed out, clear authentication state
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(AUTH_KEY, false);
            editor.apply();
        }
    }
}