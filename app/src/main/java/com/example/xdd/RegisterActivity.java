package com.example.xdd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.example.xdd.Connection.ConnectionBD;

public class RegisterActivity extends AppCompatActivity {
    EditText nomapellidos, email, telefono, usuario, clave, confirmClave;
    Button registrar;
    TextView ingresar;
    Connection con;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
                RegistrarUsuario();
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

    public void RegistrarUsuario() {
        try {
            if(con == null) {
                Toast.makeText(this, "Vetifica tu conexion", Toast.LENGTH_SHORT).show();
            } else {
                String password = clave.getText().toString();
                String confirmPassword = confirmClave.getText().toString();

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                PreparedStatement stm = con.prepareStatement("INSERT INTO usuario (nomape, email, telefono, username, password) VALUES (?, ?, ?, ?, ?)");
                stm.setString(1, nomapellidos.getText().toString());
                stm.setString(2, email.getText().toString());
                stm.setString(3, telefono.getText().toString());
                stm.setString(4, usuario.getText().toString());
                stm.setString(5, clave.getText().toString());

                int filasAfectadas = stm.executeUpdate();
                if (filasAfectadas > 0) {
                    Toast.makeText(RegisterActivity.this, "Registrado correctamente", Toast.LENGTH_SHORT).show();

                    nomapellidos.setText("");
                    email.setText("");
                    telefono.setText("");
                    usuario.setText("");
                    clave.setText("");
                    confirmClave.setText("");
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("Error de conexion ", e.getMessage());
            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
