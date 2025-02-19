package com.example.xdd.Connection;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionBD {

    @SuppressLint("NewApi")
    public java.sql.Connection connect() {
        java.sql.Connection connection = null;
        String connectionURL;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Class.forName("com.mysql.jdbc.Driver");

            String ip = "10.0.2.2";
            String usuario = "root";
            String password = "root";
            String basedatos = "loginDismov";

            connectionURL = "jdbc:mysql://" + ip + ":3306/" + basedatos + "?user=" + usuario + "&password=" + password + "&useSSL=false&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(connectionURL);
        } catch (ClassNotFoundException e) {
            Log.e("ConnectionBD", "Driver no encontrado: " + e.getMessage());
        } catch (Exception e) {
            Log.e("ConnectionBD", "Error de conexion SQL: " + e.getMessage());
        }

        return connection;
    }
}