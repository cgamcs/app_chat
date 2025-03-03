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

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.example.xdd.Connection.ConnectionBD;

public class RegisterActivity extends AppCompatActivity {
    private EditText nomapellidos, email, telefono, usuario, clave, confirmClave;
    private Button registrar;
    private TextView ingresar;
    private Connection con;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        ConnectionBD connectionBD = new ConnectionBD();
        con = connectionBD.connect();

        nomapellidos = findViewById(R.id.txtnomapellidos);
        email = findViewById(R.id.txtemail);
        telefono = findViewById(R.id.txttelefono);
        usuario = findViewById(R.id.txtusuario);
        clave = findViewById(R.id.txtclave);
        confirmClave = findViewById(R.id.txtconfirmclave);
        registrar = findViewById(R.id.btnregistrar);
        ingresar = findViewById(R.id.lbliniciars);

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

        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ingresar = new Intent(getApplicationContext(), SesionActivity.class);
                startActivity(ingresar);
                finish();
            }
        });
    }

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

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToDatabase(user);
                                sendEmailVerification(user);
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("RegisterError", "Error: ", task.getException());
                        }
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        if (con == null) {
            Toast.makeText(this, "Error de conexión con la base de datos.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String verificationToken = java.util.UUID.randomUUID().toString();

            String query = "INSERT INTO usuario (nomape, email, telefono, username, password, verification_token, verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stm = con.prepareStatement(query);
            stm.setString(1, nomapellidos.getText().toString().trim());
            stm.setString(2, user.getEmail());
            stm.setString(3, telefono.getText().toString().trim());
            stm.setString(4, usuario.getText().toString().trim());
            stm.setString(5, encriptarClave(clave.getText().toString().trim())); // Cifrado opcional
            stm.setString(6, verificationToken);
            stm.setBoolean(7, false);

            int filasAfectadas = stm.executeUpdate();
            if (filasAfectadas > 0) {
                Toast.makeText(RegisterActivity.this, "Registrado correctamente. Verifica tu correo.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Error al registrar en la base de datos.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("DatabaseError", "Error al registrar en la BD: " + e.getMessage(), e);
            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

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

    private String encriptarClave(String clave) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(clave.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (Exception e) {
            Log.e("EncryptError", "Error en el cifrado de la clave", e);
            return clave;
        }
    }
}
