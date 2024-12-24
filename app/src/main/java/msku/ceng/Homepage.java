package msku.ceng;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Homepage extends AppCompatActivity {
    Button goTaskButton;
    Button goShoppingButton;
    Button goMoviesButton;
    Button goBudgetButton;
    EditText usernameInput;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Intent loginPage = getIntent();
        String username = loginPage.getStringExtra("USERNAME_KEY");

        TextView welcomeMessage = findViewById(R.id.welcomeMessage);
        welcomeMessage.setText("Hello, " + username);


        goBudgetButton = findViewById(R.id.button5);
        goBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTaskButton.setVisibility(View.GONE);
                goShoppingButton.setVisibility(View.GONE);
                goMoviesButton.setVisibility(View.GONE);
                goBudgetButton.setVisibility(View.GONE);
                welcomeMessage.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BudgetFragment())
                        .addToBackStack(null)
                        .commit();

                getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        goTaskButton.setVisibility(View.VISIBLE);
                        goShoppingButton.setVisibility(View.VISIBLE);
                        goMoviesButton.setVisibility(View.VISIBLE);
                        goBudgetButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });


        goMoviesButton = findViewById(R.id.button4);
        goMoviesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTaskButton.setVisibility(View.GONE);
                goShoppingButton.setVisibility(View.GONE);
                goMoviesButton.setVisibility(View.GONE);
                welcomeMessage.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MovieFragment())
                        .addToBackStack(null)  // Geri butonuyla geri dönebilmek için
                        .commit();

                getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        goTaskButton.setVisibility(View.VISIBLE);
                        goShoppingButton.setVisibility(View.VISIBLE);
                        goMoviesButton.setVisibility(View.VISIBLE);
                    }
                });

            }
        });

        // goTaskButton'u tanımlama ve click listener ekleme
        goTaskButton = findViewById(R.id.button2);
        goTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTaskButton.setVisibility(View.GONE);
                goShoppingButton.setVisibility(View.GONE);
                goMoviesButton.setVisibility(View.GONE);
                welcomeMessage.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TaskFragment())
                        .addToBackStack(null)  // Geri butonuyla geri dönebilmek için
                        .commit();

                getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        goTaskButton.setVisibility(View.VISIBLE);
                        goShoppingButton.setVisibility(View.VISIBLE);
                        goMoviesButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        goShoppingButton = findViewById(R.id.button3);
        goShoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTaskButton.setVisibility(View.GONE);
                goShoppingButton.setVisibility(View.GONE);
                goMoviesButton.setVisibility(View.GONE);
                welcomeMessage.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ShoppingFragment())
                        .addToBackStack(null)
                        .commit();

                getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        goTaskButton.setVisibility(View.VISIBLE);
                        goShoppingButton.setVisibility(View.VISIBLE);
                        goMoviesButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
