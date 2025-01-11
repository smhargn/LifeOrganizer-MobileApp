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

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import msku.ceng.R;

public class MainActivity extends AppCompatActivity {
    private TextView registerButton,googleSignInButton,forgotButton;
    private Button loginButton;
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001; // Google Sign-In için requestCode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            Intent homepageIntent = new Intent(MainActivity.this, Homepage.class);
            startActivity(homepageIntent);
            finish();
            return;
        }

        googleSignInButton = findViewById(R.id.googleSignInButton);

        editTextEmail = findViewById(R.id.editusername);
        editTextPassword = findViewById(R.id.editpassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.textView5);
        forgotButton = findViewById(R.id.forgotButton);

        ImageView imageView = findViewById(R.id.imageView7);
        Picasso.get()
                .load("https://i.hizliresim.com/hlon0bp.png")
                .resize(1080, 1920)
                .centerCrop()
                .into(imageView);

        loginButton.setOnClickListener(view -> loginUser());
        forgotButton.setOnClickListener(view -> resetPassword());

        // Google Sign-In için yapılandırma
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase Console'dan aldığınız client ID
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(view -> signInWithGoogle());

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

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent loginPage = new Intent(MainActivity.this, Homepage.class);
                        startActivity(loginPage);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword(){
        Intent forgotPasswordPage = new Intent(MainActivity.this, ForgotPasswordActivity.class);
        startActivity(forgotPasswordPage);
    }
}