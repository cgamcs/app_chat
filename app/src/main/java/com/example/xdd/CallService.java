package com.example.xdd;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;

public class CallService extends Service {
    private static final String TAG = "CallService";
    private static final String APP_ID = "key=11b730e2f5964ea48f47c005164adcd5";

    private final IBinder binder = new CallBinder();
    private RtcEngine agoraEngine;
    private String currentChannelName;
    private boolean isInCall = false;
    private List<CallEventListener> listeners = new ArrayList<>();

    // Para gestionar llamadas entrantes
    private List<IncomingCall> pendingCalls = new ArrayList<>();

    public class CallBinder extends Binder {
        CallService getService() {
            return CallService.this;
        }
    }

    public interface CallEventListener {
        void onCallReceived(String callerName, String channelName);
        void onCallConnected(String channelName);
        void onCallEnded(String channelName);
        void onUserJoined(int uid);
        void onUserLeft(int uid);
        void onError(int error, String message);
    }

    public static class IncomingCall {
        private String callerName;
        private String channelName;

        public IncomingCall(String callerName, String channelName) {
            this.callerName = callerName;
            this.channelName = channelName;
        }

        public String getCallerName() {
            return callerName;
        }

        public String getChannelName() {
            return channelName;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeAgoraEngine();
    }

    private void initializeAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(getApplicationContext(), APP_ID, new IRtcEngineEventHandler() {
                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    isInCall = true;
                    Log.d(TAG, "Joined channel: " + channel);
                    for (CallEventListener listener : listeners) {
                        listener.onCallConnected(channel);
                    }
                }

                @Override
                public void onUserJoined(int uid, int elapsed) {
                    Log.d(TAG, "User joined: " + uid);
                    for (CallEventListener listener : listeners) {
                        listener.onUserJoined(uid);
                    }
                }

                @Override
                public void onUserOffline(int uid, int reason) {
                    Log.d(TAG, "User left: " + uid);
                    for (CallEventListener listener : listeners) {
                        listener.onUserLeft(uid);
                    }
                }

                @Override
                public void onLeaveChannel(RtcStats stats) {
                    isInCall = false;
                    Log.d(TAG, "Left channel");
                    for (CallEventListener listener : listeners) {
                        listener.onCallEnded(currentChannelName);
                    }
                    currentChannelName = null;
                }

                @Override
                public void onError(int err) {
                    Log.e(TAG, "Error: " + err);
                    for (CallEventListener listener : listeners) {
                        listener.onError(err, "Error code: " + err);
                    }
                }
            });

            // Configurar el motor RTC
            agoraEngine.enableVideo();
            agoraEngine.enableAudio();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Agora SDK", e);
        }
    }

    public void addListener(CallEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);

            // Notificar de llamadas pendientes al nuevo listener
            for (IncomingCall call : pendingCalls) {
                listener.onCallReceived(call.getCallerName(), call.getChannelName());
            }
        }
    }

    public void removeListener(CallEventListener listener) {
        listeners.remove(listener);
    }

    // Iniciar una llamada (emisor)
    public void startCall(String recipientName, String channelName) {
        if (isInCall) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .whereEqualTo("username", recipientName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String fcmToken = querySnapshot.getDocuments().get(0).getString("fcmToken");
                        if (fcmToken != null) {
                            enviarLlamadaFCM(fcmToken, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), channelName);
                        }
                    }
                });

        currentChannelName = channelName;
        agoraEngine.joinChannel(null, channelName, "Extra Data", 0);
    }

    private void enviarLlamadaFCM(String token, String callerName, String channelName) {
        new Thread(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "key=TU_CLAVE_SERVIDOR_FCM"); // Debes obtener esta clave de la consola de Firebase

                JSONObject root = new JSONObject();
                root.put("to", token);

                // Añadir alta prioridad para que la notificación aparezca incluso con la app en segundo plano
                root.put("priority", "high");

                JSONObject data = new JSONObject();
                data.put("type", "CALL");
                data.put("callerName", callerName);
                data.put("channelName", channelName);

                // Añadir notificación para que se muestre incluso cuando la app está en segundo plano
                JSONObject notification = new JSONObject();
                notification.put("title", "Llamada entrante");
                notification.put("body", callerName + " te está llamando");
                notification.put("sound", "default");
                notification.put("click_action", "OPEN_CALL_ACTIVITY");

                root.put("data", data);
                root.put("notification", notification);

                OutputStream os = conn.getOutputStream();
                os.write(root.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("FCM", "Respuesta FCM: " + responseCode);

                // Leer la respuesta para depuración
                if (responseCode != 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.e("FCM", "Error en respuesta: " + response.toString());
                }
            } catch (Exception e) {
                Log.e("FCM", "Error enviando notificación", e);
            }
        }).start();
    }

    // Añadir este método a CallService para manejar explícitamente una llamada recibida (será llamado desde FCM):
    public void handleIncomingCall(String callerName, String channelName) {
        IncomingCall call = new IncomingCall(callerName, channelName);
        pendingCalls.add(call);

        // Notificar a todos los listeners
        for (CallEventListener listener : listeners) {
            listener.onCallReceived(callerName, channelName);
        }
    }


    // Simula recibir una llamada (en una app real, esto vendría a través de FCM)
    private void simulateCallReceived(String callerName, String channelName) {
        IncomingCall call = new IncomingCall(callerName, channelName);
        pendingCalls.add(call);

        // Notificar a todos los listeners
        for (CallEventListener listener : listeners) {
            listener.onCallReceived(callerName, channelName);
        }
    }

    // Responder a una llamada (receptor)
    public void answerCall(String channelName) {
        if (isInCall) {
            Log.d(TAG, "Already in a call");
            return;
        }

        // Buscar y eliminar la llamada pendiente
        for (int i = 0; i < pendingCalls.size(); i++) {
            if (pendingCalls.get(i).getChannelName().equals(channelName)) {
                pendingCalls.remove(i);
                break;
            }
        }

        // Guardamos el canal actual
        currentChannelName = channelName;

        // Unirse al canal
        agoraEngine.joinChannel(null, channelName, "Extra Data", 0);
    }

    // Rechazar una llamada (receptor)
    public void rejectCall(String channelName) {
        // Buscar y eliminar la llamada pendiente
        for (int i = 0; i < pendingCalls.size(); i++) {
            if (pendingCalls.get(i).getChannelName().equals(channelName)) {
                pendingCalls.remove(i);
                break;
            }
        }

        // En una app real, aquí notificarías al emisor que la llamada fue rechazada
    }

    // Finalizar una llamada (ambas partes)
    public void endCall() {
        if (!isInCall) {
            return;
        }

        agoraEngine.leaveChannel();
        isInCall = false;
    }

    public RtcEngine getAgoraEngine() {
        return agoraEngine;
    }

    public boolean isInCall() {
        return isInCall;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }
    }
}