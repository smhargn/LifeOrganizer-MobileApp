package msku.ceng.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import msku.ceng.R;

public class Homepage extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.navigation_movies) {
                fragment = new MovieFragment();
            } else if (itemId == R.id.navigation_sport) {
                fragment = new SportFragment();
            } else if (itemId == R.id.navigation_budget) {
                fragment = new BudgetFragment();
            } else if (itemId == R.id.navigation_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            }
            return false;
        });


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }
}