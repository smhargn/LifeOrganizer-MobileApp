package msku.ceng;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import com.squareup.picasso.Picasso;
public class MainActivity extends AppCompatActivity {
    public TextView registerButton;
    public Button loginButton;
    public EditText editTextusername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextusername = findViewById(R.id.editusername);



        ImageView imageView = findViewById(R.id.imageView7); // ImageView'i bulun

// Resmi Picasso ile yükleyin
        Picasso.get()
                .load("https://i.hizliresim.com/hlon0bp.png")  // Resmin URL'sini yazın (veya drawable kaynak kullanabilirsiniz)
                .resize(1080, 1920) // Resmin boyutunu belirleyin (orijinal çözünürlük)
                .centerCrop()  // Görseli orantılı şekilde ekranda merkezleyecek
                .into(imageView); // Görseli ImageView'e ekle







        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginPage = new Intent(MainActivity.this,Homepage.class);
                String username = editTextusername.getText().toString();
                loginPage.putExtra("USERNAME_KEY", username); // "USERNAME_KEY" ile username'i ekle
                startActivity(loginPage);
            }
        });

        registerButton = findViewById(R.id.textView5);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerPage = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(registerPage);
            }
        });

    }
}