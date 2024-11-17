package com.ashakur.authfirebasse;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Register extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
            return;
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.loginButton);
        TextView haveAccount = findViewById(R.id.have_account);

        registerButton.setOnClickListener(v -> {
            if (validateInputs()) {
                performRegistration();
            }
        });

        String text = getString(R.string.have_account);
        SpannableString spannableString = getSpannableString(text);
        haveAccount.setText(spannableString);
        haveAccount.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private @NonNull SpannableString getSpannableString(String text) {
        SpannableString spannableString = new SpannableString(text);

        int start = text.indexOf("here");
        int end = start + "here".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
//                Intent intent = new Intent(Register.this, Login.class);
//                startActivity(intent);
                finish();
            }
        };

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private boolean validateInputs() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString();

        if (userEmail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return false;
        }

        if (userPassword.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }

        if (userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegistration() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString();

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(Register.this,  Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                    registerButton.setEnabled(true);
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}