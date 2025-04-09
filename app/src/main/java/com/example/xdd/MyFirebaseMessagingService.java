package com.example.xdd;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";
    private CallService callService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CallService.CallBinder binder = (CallService.CallBinder) service;
            callService = binder.getService();
            isBound = true;

            // Si hay datos de llamada pendientes, procesarlos ahora
            if (pendingCallData != null) {
                processCallData(pendingCallData[0], pendingCallData[1]);
                pendingCallData = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            callService = null;
            isBound = false;
        }
    };

    // Para almacenar datos de llamada si el servicio aún no está conectado
    private String[] pendingCallData = null;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Mensaje FCM recibido: " + remoteMessage.getData());

        if (remoteMessage.getData() != null && remoteMessage.getData().containsKey("type")) {
            if (remoteMessage.getData().get("type").equals("CALL")) {
                String channelName = remoteMessage.getData().get("channelName");
                String callerName = remoteMessage.getData().get("callerName");

                // Iniciar el servicio si no está en ejecución
                Intent serviceIntent = new Intent(this, CallService.class);
                startService(serviceIntent);

                // Vincular al servicio
                bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

                // Intentar procesar los datos de la llamada
                if (callService != null) {
                    processCallData(callerName, channelName);
                } else {
                    // Guardar para procesar cuando el servicio esté conectado
                    pendingCallData = new String[] {callerName, channelName};
                }

                // Mostrar notificación de llamada entrante
                createIncomingCallNotification(callerName, channelName);
            }
        }
    }

    private void processCallData(String callerName, String channelName) {
        // Notificar al servicio sobre la llamada entrante
        if (callService != null) {
            callService.handleIncomingCall(callerName, channelName);
        }
    }

    private void createIncomingCallNotification(String callerName, String channelName) {
        // Crear intent para abrir la actividad de llamada
        Intent intent = CallActivity.createIncomingCallIntent(this, channelName, callerName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Crear el canal de notificación para Android 8+
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            NotificationChannel channel = new NotificationChannel("llamadas", "Llamadas", NotificationManager.IMPORTANCE_HIGH);

            // Configurar sonido y vibración
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();

            channel.setSound(ringtoneUri, audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(channel);
        }

        // Crear intent pendiente para la notificación
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        // Crear los botones de acción para aceptar/rechazar
        Intent acceptIntent = new Intent(this, CallActivity.class);
        acceptIntent.setAction("ACCEPT_CALL");
        acceptIntent.putExtra(CallActivity.EXTRA_CHANNEL_NAME, channelName);
        acceptIntent.putExtra(CallActivity.EXTRA_REMOTE_USER_NAME, callerName);
        acceptIntent.putExtra(CallActivity.EXTRA_IS_OUTGOING, false);
        acceptIntent.putExtra("accept", true);
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(this, 1, acceptIntent, flags);

        Intent rejectIntent = new Intent(this, CallActivity.class);
        rejectIntent.setAction("REJECT_CALL");
        rejectIntent.putExtra(CallActivity.EXTRA_CHANNEL_NAME, channelName);
        rejectIntent.putExtra(CallActivity.EXTRA_REMOTE_USER_NAME, callerName);
        rejectIntent.putExtra(CallActivity.EXTRA_IS_OUTGOING, false);
        rejectIntent.putExtra("reject", true);
        PendingIntent rejectPendingIntent = PendingIntent.getActivity(this, 2, rejectIntent, flags);

        // Construir la notificación
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "llamadas")
                .setContentTitle("Llamada entrante")
                .setContentText(callerName + " te está llamando")
                .setSmallIcon(R.drawable.ic_videocam)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setSound(ringtoneUri)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true)
                .addAction(R.drawable.ic_call, "Aceptar", acceptPendingIntent)
                .addAction(R.drawable.ic_call_end, "Rechazar", rejectPendingIntent);

        // Mostrar la notificación
        notificationManager.notify(channelName.hashCode(), builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("usuarios")
                    .document(currentUser.getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token guardado en Firestore"))
                    .addOnFailureListener(e -> Log.e("FCM", "Error guardando token", e));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}