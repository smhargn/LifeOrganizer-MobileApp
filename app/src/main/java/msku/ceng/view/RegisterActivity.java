package msku.ceng.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.squareup.picasso.Picasso;

import msku.ceng.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText inputName, inputEmail, inputPassword;
    private Button registerButton;
    private TextView signinButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        ImageView imageView = findViewById(R.id.imageView7);
        Picasso.get()
                .load("https://i.hizliresim.com/hlon0bp.png")
                .resize(1080, 1920)
                .centerCrop()
                .into(imageView);

        // Initialize views
        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        registerButton = findViewById(R.id.register_button);
        signinButton = findViewById(R.id.textView5);

        // Register button click listener
        registerButton.setOnClickListener(view -> registerUser());

        // Sign in text click listener
        signinButton.setOnClickListener(view -> {
            Intent loginPage = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(loginPage);
            finish();
        });
    }

    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(name)) {
            inputName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            inputPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Show progress
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Update user profile with name
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        if (user != null) {
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registration successful",
                                                    Toast.LENGTH_SHORT).show();
                                            // Navigate to main activity
                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    });
                        }
                    } else {
                        // Registration failed
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Registration failed";
                        Toast.makeText(RegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true);
                        registerButton.setText("Register");
                    }
                });
    }
}