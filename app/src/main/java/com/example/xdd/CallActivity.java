package com.example.xdd;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class CallActivity extends AppCompatActivity {
    private static final String TAG = "CallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        long appID = getIntent().getLongExtra("appID", 0);
        String appSign = getIntent().getStringExtra("appSign");
        String callID = getIntent().getStringExtra("callID");
        boolean isVideoCall = getIntent().getBooleanExtra("isVideoCall", true);

        // Obtener el usuario actual
        String userID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");

        if (userID == null || userID.isEmpty()) {
            // Para propósitos de prueba, si no hay userID, crear uno temporal
            userID = "user_" + System.currentTimeMillis();
            userName = "Usuario";
            Log.w(TAG, "UserID no proporcionado, usando ID temporal: " + userID);
        }

        try {
            // Configurar la llamada
            ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCallConfig();

            // Configurar si es llamada de video o audio
            config.turnOnCameraWhenJoining = isVideoCall;
            config.turnOnMicrophoneWhenJoining = true;

            // Crear usuario
            ZegoUIKitUser currentUser = new ZegoUIKitUser(userID, userName);

            // Crear el fragmento de llamada
            ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                    appID,
                    appSign,
                    userID,
                    userName,
                    callID,
                    config);

            // Añadir el fragmento al contenedor
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();

        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar la llamada: " + e.getMessage(), e);
        }
    }
}