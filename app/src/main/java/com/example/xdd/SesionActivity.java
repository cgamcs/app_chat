package com.example.xdd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.xdd.Connection.ConnectionBD;

public class SesionActivity extends AppCompatActivity {
    private EditText usuario, contra;
    private Button btningresar;
    private TextView lblregistrar;
    private FirebaseAuth mAuth;
    private Connection con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sesion);

        mAuth = FirebaseAuth.getInstance();

        usuario = findViewById(R.id.textusuario);
        contra = findViewById(R.id.txtcontra);
        btningresar = findViewById(R.id.btningresar);
        lblregistrar = findViewById(R.id.lblregistrate);

        ConnectionBD instanceConnection = new ConnectionBD();
        con = instanceConnection.connect();

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = usuario.getText().toString().trim();
                String userPassword = contra.getText().toString().trim();

                // Validación del formato de correo
                if (!isValidEmail(userEmail)) {
                    Toast.makeText(SesionActivity.this, "Correo inválido.", Toast.LENGTH_SHORT).show();
                    return;
                }

                signIn(userEmail, userPassword);
            }
        });

        lblregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(reg);
                finish();
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d("SesionActivity", "Intentando iniciar sesión con: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                Toast.makeText(SesionActivity.this, "Acceso exitoso", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SesionActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SesionActivity.this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("SesionActivity", "Error al iniciar sesión: ", task.getException());
                            Toast.makeText(SesionActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
