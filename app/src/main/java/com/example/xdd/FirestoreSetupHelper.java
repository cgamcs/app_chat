package com.example.xdd;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreSetupHelper {

    private static final String TAG = "FirestoreSetupHelper";

    /**
     * Verifica y restaura las colecciones necesarias en Firestore
     * pero solo para el usuario actual (cumpliendo con las reglas de seguridad)
     */
    public static void checkAndRestoreCollections(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado para restaurar colecciones");
            return;
        }
        // Restaurar/verificar la colección de usuarios solo para el usuario actual
        restoreCurrentUserData(context);
    }

    /**
     * Restaura los datos del usuario actual en Firestore.
     * Si el documento ya existe, se actualizan únicamente los campos faltantes,
     * sin sobrescribir el "username" que fue elegido al registrarse.
     * En caso de no tener displayName, se usará el correo como nombre de usuario.
     */
    public static void restoreCurrentUserData(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        Log.d(TAG, "Intentando restaurar datos para usuario: " + uid);

        // Consultar si ya existe el documento del usuario
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // El documento no existe, se crea con los datos iniciales.
                        Map<String, Object> userData = new HashMap<>();
                        // Se utiliza el displayName, o en su defecto el correo del usuario.
                        userData.put("username", user.getDisplayName() != null ?
                                user.getDisplayName() : (user.getEmail() != null ? user.getEmail() : "Usuario desconocido"));
                        userData.put("email", user.getEmail());
                        userData.put("telefono", user.getPhoneNumber());
                        db.collection("usuarios").document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Datos de usuario creados correctamente para: " + uid);
                                    // Toast.makeText(context, "Datos de usuario restaurados", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al crear datos para " + uid + ": " + e.getMessage());
                                    Toast.makeText(context, "Error al restaurar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        // El documento ya existe, se actualizan solo los campos faltantes sin sobrescribir "username".
                        Map<String, Object> updateData = new HashMap<>();
                        if (!documentSnapshot.contains("email") && user.getEmail() != null) {
                            updateData.put("email", user.getEmail());
                        }
                        if (!documentSnapshot.contains("telefono") && user.getPhoneNumber() != null) {
                            updateData.put("telefono", user.getPhoneNumber());
                        }
                        if (!updateData.isEmpty()) {
                            db.collection("usuarios").document(uid)
                                    .set(updateData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Datos de usuario actualizados sin sobrescribir 'username'"))
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al actualizar datos para " + uid + ": " + e.getMessage());
                                        Toast.makeText(context, "Error al actualizar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Log.d(TAG, "Documento de usuario ya existe y contiene 'username'.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar datos para " + uid + ": " + e.getMessage());
                    Toast.makeText(context, "Error al restaurar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Verifica si la colección de usuarios existe y muestra información.
     */
    public static void debugUsersCollection(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "Colección de usuarios está vacía");
                        Toast.makeText(context, "No hay usuarios en la base de datos", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "La colección tiene " + querySnapshot.size() + " usuarios");
                        Toast.makeText(context, "Encontrados " + querySnapshot.size() + " usuarios", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar colección: " + e.getMessage());
                    Toast.makeText(context, "Error al verificar colección: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}