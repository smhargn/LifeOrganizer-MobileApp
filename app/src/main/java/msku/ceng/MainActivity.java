package msku.ceng;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private TextView registerButton;
    private Button loginButton;
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editusername);
        editTextPassword = findViewById(R.id.editpassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.textView5);

        ImageView imageView = findViewById(R.id.imageView7);
        Picasso.get()
                .load("https://i.hizliresim.com/hlon0bp.png")
                .resize(1080, 1920)
                .centerCrop()
                .into(imageView);

        loginButton.setOnClickListener(view -> loginUser());

        registerButton.setOnClickListener(view -> {
            Intent registerPage = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(registerPage);
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }


        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this,
                                "Login successful",
                                Toast.LENGTH_SHORT).show();
                        Intent loginPage = new Intent(MainActivity.this, Homepage.class);
                        loginPage.putExtra("USERNAME_KEY", email);
                        startActivity(loginPage);
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Authentication failed";
                        Toast.makeText(MainActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();

                        loginButton.setEnabled(true);
                        loginButton.setText("Log in");
                    }
                });
    }
}