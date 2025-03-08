package com.example.xdd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {

    private Spinner languageSpinner;
    private String selectedLanguageCode; // Variable para almacenar el código del idioma seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        languageSpinner = findViewById(R.id.languageSpinner);

        String[] languages = {"Español", "English", "Français"};
        final String[] languageCodes = {"es", "en", "fr"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        languageSpinner.setAdapter(adapter);

        // Configura el listener del Spinner
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguageCode = languageCodes[position]; // Almacena el código del idioma seleccionado
                Log.d("LanguageActivity", "Idioma seleccionado: " + languages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("LanguageActivity", "Nada seleccionado");
            }
        });

        // Configura el botón "Aceptar"
        Button btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLanguageCode != null) {
                    LanguageHelper.setLanguage(LanguageActivity.this, selectedLanguageCode);

                    // Reiniciar la actividad principal con el nuevo idioma
                    Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // Cerrar la actividad actual
                    finish();
                } else {
                    Log.d("LanguageActivity", "No se seleccionó ningún idioma");
                }
            }
        });
    }
}