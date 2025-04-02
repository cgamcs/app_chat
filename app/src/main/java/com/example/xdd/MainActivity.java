package com.example.xdd;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageHelper.applyLanguage(this);

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);

        // Configurar Firestore para persistencia offline
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);

        // Forzar la habilitación de la red
        enableNetwork(firestore);

        // Verificar si hay un usuario autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Restaurar las colecciones de Firestore si es necesario
            FirestoreSetupHelper.checkAndRestoreCollections(this);
            Log.d(TAG, "Verificando y restaurando colecciones de Firestore");
        } else {
            Log.d(TAG, "No hay usuario autenticado");
            // Aquí podrías redirigir al usuario a la pantalla de login si es necesario
        }

        setContentView(R.layout.activity_main);

        // Configurar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void enableNetwork(FirebaseFirestore firestore) {
        firestore.enableNetwork()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Red habilitada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al habilitar red: " + e.getMessage()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}