package com.example.xdd;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "call_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Verificar si el mensaje contiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Datos del mensaje: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Verificar si el mensaje contiene notificación
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notificación recibida: " + remoteMessage.getNotification().getBody());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        if (type != null && type.equals("CALL")) {
            String callerName = data.get("callerName");
            String channelName = data.get("channelName");

            if (callerName != null && channelName != null) {
                Log.d(TAG, "Llamada recibida de: " + callerName + " en canal: " + channelName);

                // 1. Iniciar el servicio de llamadas
                Intent serviceIntent = new Intent(this, CallService.class);
                startService(serviceIntent);

                // 2. Notificar al servicio sobre la llamada entrante
                // (Es recomendable bindear con el servicio, pero para simplificar usamos intent)
                Intent callIntent = new Intent(this, CallService.class);
                callIntent.setAction("INCOMING_CALL");
                callIntent.putExtra("callerName", callerName);
                callIntent.putExtra("channelName", channelName);
                startService(callIntent);

                // 3. Mostrar notificación para que el usuario pueda contestar
                showCallNotification(callerName, channelName);
            }
        }
    }

    private void showCallNotification(String callerName, String channelName) {
        // Crear intent para abrir la actividad de llamada al tocar la notificación
        Intent intent = CallActivity.createIncomingCallIntent(this, channelName, callerName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent para aceptar la llamada
        Intent acceptIntent = CallActivity.createIncomingCallIntent(this, channelName, callerName);
        acceptIntent.putExtra("accept", true);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(this, 1, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent para rechazar la llamada
        Intent rejectIntent = CallActivity.createIncomingCallIntent(this, channelName, callerName);
        rejectIntent.putExtra("reject", true);
        rejectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent rejectPendingIntent = PendingIntent.getActivity(this, 2, rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Configurar sonido personalizado para llamada
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        // Crear canal de notificaciones para Android 8.0+
        createCallNotificationChannel();

        // Construir la notificación
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia esto al ícono adecuado
                        .setContentTitle("Llamada entrante")
                        .setContentText("Llamada de " + callerName)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setFullScreenIntent(pendingIntent, true)
                        .setContentIntent(pendingIntent)
                        .addAction(android.R.drawable.ic_menu_call, "Aceptar", acceptPendingIntent)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Rechazar", rejectPendingIntent)
                        .setOngoing(true);

        // Mostrar la notificación
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createCallNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Llamadas", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal para notificaciones de llamadas");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Nuevo token FCM: " + token);

        // Actualizar el token en la base de datos
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Necesitamos referenciar a saveTokenToFirestore pero está en MainActivity
            // Idealmente deberías tener un método estático o un helper para hacer esto
            // Por ahora, solo registramos en el log
            Log.d(TAG, "Token actualizado para usuario " + userId + ": " + token);
        }
    }
}