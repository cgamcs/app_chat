package com.example.xdd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText nomapellidos, email, telefono, usuario, clave, confirmClave;
    private Button registrar;
    private TextView ingresar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Referencias a los elementos de la interfaz
        nomapellidos = findViewById(R.id.txtnomapellidos);
        email = findViewById(R.id.txtemail);
        telefono = findViewById(R.id.txttelefono);
        usuario = findViewById(R.id.txtusuario);
        clave = findViewById(R.id.txtclave);
        confirmClave = findViewById(R.id.txtconfirmclave);
        registrar = findViewById(R.id.btnregistrar);
        ingresar = findViewById(R.id.lbliniciars);

        // Listener para el botón de registro
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString().trim();
                String userPassword = clave.getText().toString().trim();
                String confirmPassword = confirmClave.getText().toString().trim();

                if (!validarCampos(userEmail, userPassword, confirmPassword)) {
                    return;
                }

                registerUser(userEmail, userPassword);
            }
        });

        // Listener para el texto "Ingresar"
        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ingresar = new Intent(getApplicationContext(), SesionActivity.class);
                startActivity(ingresar);
                finish();
            }
        });
    }

    // Método para validar los campos del formulario
    private boolean validarCampos(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                nomapellidos.getText().toString().trim().isEmpty() ||
                telefono.getText().toString().trim().isEmpty() ||
                usuario.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo no válido.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Método para registrar al usuario con Firebase Authentication
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToDatabase(user); // Guardar datos en Firebase Realtime Database
                                sendEmailVerification(user); // Enviar correo de verificación
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("RegisterError", "Error: ", task.getException());
                        }
                    }
                });
    }

    // Método para guardar los datos del usuario en Firebase Realtime Database
    private void saveUserToDatabase(FirebaseUser user) {
        String userId = user.getUid(); // Obtener el ID único del usuario
        String nomape = nomapellidos.getText().toString().trim();
        String userEmail = user.getEmail();
        String userTelefono = telefono.getText().toString().trim();
        String username = usuario.getText().toString().trim();
        String rol = "usuario";

        // Crear un objeto Usuario
        Usuario usuario = new Usuario(nomape, userEmail, userTelefono, username, rol);

        // Guardar el usuario en la base de datos
        mDatabase.child("usuarios").child(userId).setValue(usuario)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al guardar datos en la base de datos.", Toast.LENGTH_SHORT).show();
                            Log.e("DatabaseError", "Error: ", task.getException());
                        }
                    }
                });
    }

    // Método para enviar el correo de verificación
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Correo de verificación enviado a " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, SesionActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al enviar el correo de verificación.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Clase Usuario para representar los datos en Firebase
    public static class Usuario {
        public String nomape;
        public String email;
        public String telefono;
        public String username;
        public String rol;

        public Usuario() {
            // Constructor vacío requerido para Firebase
        }

        public Usuario(String nomape, String email, String telefono, String username, String rol) {
            this.nomape = nomape;
            this.email = email;
            this.telefono = telefono;
            this.username = username;
            this.rol = rol;
        }
    }
}