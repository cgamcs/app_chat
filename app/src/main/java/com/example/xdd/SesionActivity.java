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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SesionActivity extends AppCompatActivity {
    private EditText usuario, contra;
    private Button btningresar;
    private TextView lblregistrar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sesion);

        // Inicializar Firebase Auth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Referencias a los elementos de la interfaz
        usuario = findViewById(R.id.textusuario);
        contra = findViewById(R.id.txtcontra);
        btningresar = findViewById(R.id.btningresar);
        lblregistrar = findViewById(R.id.lblregistrate);

        // Listener para el botón de inicio de sesión
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

        // Listener para el texto "Registrar"
        lblregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(reg);
                finish();
            }
        });
    }

    // Método para iniciar sesión
    private void signIn(String email, String password) {
        Log.d("SesionActivity", "Intentando iniciar sesión con: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Obtener datos del usuario desde Firebase Realtime Database
                                getUserData(user.getUid());
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

    // Método para obtener datos del usuario desde Firebase Realtime Database
    private void getUserData(String userId) {
        mDatabase.child("usuarios").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener los datos del usuario
                    String nomape = dataSnapshot.child("nomape").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String telefono = dataSnapshot.child("telefono").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);

                    // Mostrar mensaje de éxito y redirigir al MainActivity
                    Toast.makeText(SesionActivity.this, "Acceso exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SesionActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SesionActivity.this, "Datos del usuario no encontrados.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SesionActivity", "Error al obtener datos del usuario: ", databaseError.toException());
                Toast.makeText(SesionActivity.this, "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para validar el formato de correo
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}