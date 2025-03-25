package com.example.xdd;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnSaveChanges, btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Cargar la información actual del usuario
        loadUserInfo();

        // Guardar cambios
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        // Cambiar contraseña
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void loadUserInfo() {
        // Obtén el ID del usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        Log.d("EditProfileActivity", "Usuario autenticado: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d("EditProfileActivity", "Buscando datos en Realtime Database...");

        // Referencia a la base de datos Realtime (usar "usuarios" en lugar de "users")
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtén los datos del usuario
                    String name = dataSnapshot.child("nomape").getValue(String.class); // Usar "nomape"
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phone = dataSnapshot.child("telefono").getValue(String.class); // Usar "telefono"

                    // Establece los valores en los campos
                    etName.setText(name);
                    etEmail.setText(email);
                    etPhone.setText(phone);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Error al cargar la información", Toast.LENGTH_SHORT).show();
                Log.e("RealtimeDB", "Error al cargar datos", databaseError.toException());
            }
        });
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtén el ID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Crear un mapa con los nuevos datos
        Map<String, Object> userData = new HashMap<>();
        userData.put("nomape", name); // Usar "nomape"
        userData.put("email", email);
        userData.put("telefono", phone); // Usar "telefono"

        // Referencia a la base de datos Realtime (usar "usuarios" en lugar de "users")
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);

        // Actualizar los datos en Realtime Database
        databaseRef.updateChildren(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                    Log.e("RealtimeDB", "Error al actualizar datos", e);
                });
    }

    private void changePassword() {
        // Abre un diálogo o una nueva actividad para que el usuario ingrese su nueva contraseña
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Contraseña");

        // Configura el diseño del diálogo
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etNewPassword = view.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        builder.setView(view);

        // Configura los botones del diálogo
        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validar que las contraseñas coincidan
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cambiar la contraseña en Firebase Authentication
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setNegativeButton("Cancelar", null);

        // Mostrar el diálogo
        builder.create().show();
    }
}