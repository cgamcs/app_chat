package com.example.xdd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnCerrarSesion, btnCambiarIdioma;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyLanguage(this); // Aplica el idioma correctamente
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCambiarIdioma = findViewById(R.id.btnCambiarIdioma);

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });

        // Ir a la pantalla de cambio de idioma
        btnCambiarIdioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, SesionActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String language = prefs.getString("language", "es"); // Idioma predeterminado: Español
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
