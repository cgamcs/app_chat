package com.example.xdd;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageHelper.applyLanguage(this);
        requestPermissions();

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

            // Inicializar ZegoCloud
            long appID = getResources().getInteger(R.integer.app_id);
            String appSign = getString(R.string.app_sign);
            ZegoCallService.init(getApplication(), appID, appSign, currentUser.getUid(), currentUser.getDisplayName());
        } else {
            Log.d(TAG, "No hay usuario autenticado");
            // Aquí podrías redirigir al usuario a la pantalla de login si es necesario
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Obtener token
                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);

                    // Guardar el token en tu base de datos con el ID del usuario
                    if (currentUser != null) {
                        String userId = currentUser.getUid(); // <- esta línea es necesaria
                        saveTokenToFirestore(userId, token);
                    }

                });

        setContentView(R.layout.activity_main);

        // Configurar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void saveTokenToFirestore(String userId, String token) {
        // Guarda el token tanto en la colección users como en usuarios para asegurar compatibilidad
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Actualiza en la colección "users"
        db.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnFailureListener(e -> {
                    // Si falla porque el documento no existe, créalo
                    db.collection("users")
                            .document(userId)
                            .set(new HashMap<String, Object>() {{
                                put("fcmToken", token);
                                put("userId", userId);
                                // Añadir otros campos si son necesarios
                            }});
                });

        // También actualiza en la colección "usuarios" si es la que realmente usas
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (username != null && !username.isEmpty()) {
            db.collection("usuarios")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Actualiza documento existente
                            querySnapshot.getDocuments().get(0).getReference().update("fcmToken", token);
                        } else {
                            // Crea nuevo documento si no existe
                            db.collection("usuarios")
                                    .add(new HashMap<String, Object>() {{
                                        put("fcmToken", token);
                                        put("userId", userId);
                                        put("username", username);
                                    }});
                        }
                    });
        }
    }

    private void requestPermissions() {
        String[] neededPermissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(neededPermissions, 123);
        }
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