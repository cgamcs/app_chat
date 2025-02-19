package com.example.xdd;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import com.example.xdd.Connection.ConnectionBD;

public class SesionActivity extends AppCompatActivity {
    EditText usuario, contra;
    TextView lblregistrar;
    Button btningresar;

    Connection con;

    public SesionActivity() {
        ConnectionBD instanceConnection = new ConnectionBD();
        con = instanceConnection.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sesion);

        usuario = findViewById(R.id.textusuario);
        contra = findViewById(R.id.txtcontra);
        lblregistrar = findViewById(R.id.lblregistrate);
        btningresar = findViewById(R.id.btningresar);

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SesionActivity.login().execute("");
            }
        });

        lblregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(reg);
            }
        });
    }

    public class login extends AsyncTask<String, String, String> {
        String z = null;
        Boolean exito = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            if(con == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SesionActivity.this, "Verifique su conexion", Toast.LENGTH_SHORT).show();
                    }
                });
                z = "En conexion";
            } else {
                try {
                    String sql = "SELECT * FROM usuario WHERE username = '" + usuario.getText() + "' AND password = '" + contra.getText() + "'";
                    Statement stm = con.createStatement();
                    ResultSet rs = stm.executeQuery(sql);

                    if(rs.next()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SesionActivity.this, "Acceso exitoso", Toast.LENGTH_SHORT).show();
                                Intent menu = new Intent(getApplicationContext(), SesionActivity.class);
                                startActivity(menu);
                            }
                        });

                        usuario.setText("");
                        contra.setText("");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SesionActivity.this, "Usuario/Contraseña Incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    exito = false;
                    Log.e("Error de conexion SQL: ", e.getMessage());
                }
            }



            return z;
        }
    }
}