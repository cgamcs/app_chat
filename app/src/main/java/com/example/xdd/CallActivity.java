package com.example.xdd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.rtc2.Constants;
import io.agora.rtc2.video.VideoCanvas;

public class CallActivity extends AppCompatActivity implements CallService.CallEventListener {
    private static final String EXTRA_CHANNEL_NAME = "channel_name";
    private static final String EXTRA_IS_OUTGOING = "is_outgoing";
    private static final String EXTRA_REMOTE_USER_NAME = "remote_user_name";

    private String channelName;
    private boolean isOutgoing;
    private String remoteUserName;

    private ViewGroup localVideoContainer;
    private ViewGroup remoteVideoContainer;
    private SurfaceView localSurfaceView;
    private SurfaceView remoteSurfaceView;
    private TextView txtStatus;
    private TextView txtUserName;
    private ImageButton btnEndCall;
    private ImageButton btnMute;
    private ImageButton btnSwitchCamera;
    private ImageButton btnVideoToggle;
    private View callControls;
    private View incomingCallControls;
    private ImageButton btnAccept;
    private ImageButton btnReject;

    private boolean isMuted = false;
    private boolean isVideoEnabled = true;

    private CallService callService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CallService.CallBinder binder = (CallService.CallBinder) service;
            callService = binder.getService();
            callService.addListener(CallActivity.this);
            isBound = true;

            // Si es una llamada saliente, iniciarla
            if (isOutgoing) {
                txtStatus.setText("Llamando...");
                callService.startCall(remoteUserName, channelName);
            } else {
                // Para llamada entrante, mostrar controles para aceptar/rechazar
                txtStatus.setText("Llamada entrante");
                callControls.setVisibility(View.GONE);
                incomingCallControls.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    public static Intent createOutgoingCallIntent(Context context, String channelName, String remoteName) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_CHANNEL_NAME, channelName);
        intent.putExtra(EXTRA_IS_OUTGOING, true);
        intent.putExtra(EXTRA_REMOTE_USER_NAME, remoteName);
        return intent;
    }

    public static Intent createIncomingCallIntent(Context context, String channelName, String remoteName) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_CHANNEL_NAME, channelName);
        intent.putExtra(EXTRA_IS_OUTGOING, false);
        intent.putExtra(EXTRA_REMOTE_USER_NAME, remoteName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Obtener datos de la intent
        channelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        isOutgoing = getIntent().getBooleanExtra(EXTRA_IS_OUTGOING, false);
        remoteUserName = getIntent().getStringExtra(EXTRA_REMOTE_USER_NAME);

        // Inicializar vistas
        localVideoContainer = findViewById(R.id.local_video_container);
        remoteVideoContainer = findViewById(R.id.remote_video_container);
        txtStatus = findViewById(R.id.txt_call_status);
        txtUserName = findViewById(R.id.txt_user_name);
        btnEndCall = findViewById(R.id.btn_end_call);
        btnMute = findViewById(R.id.btn_mute);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnVideoToggle = findViewById(R.id.btn_video_toggle);
        callControls = findViewById(R.id.call_controls);
        incomingCallControls = findViewById(R.id.incoming_call_controls);
        btnAccept = findViewById(R.id.btn_accept);
        btnReject = findViewById(R.id.btn_reject);

        // Configurar el nombre del usuario remoto
        txtUserName.setText(remoteUserName);

        // Configurar escuchadores
        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> toggleMute());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
        btnVideoToggle.setOnClickListener(v -> toggleVideo());
        btnAccept.setOnClickListener(v -> acceptCall());
        btnReject.setOnClickListener(v -> rejectCall());

        // Vincular al servicio
        Intent intent = new Intent(this, CallService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void setupVideoViews() {
        if (callService == null) return;

        // Crear SurfaceViews
        localSurfaceView = new SurfaceView(this);
        remoteSurfaceView = new SurfaceView(this);

        // Configurar video local
        localVideoContainer.removeAllViews();
        localVideoContainer.addView(localSurfaceView);
        VideoCanvas localCanvas = new VideoCanvas(localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN, 0);
        callService.getAgoraEngine().setupLocalVideo(localCanvas);
    }

    private void setupRemoteVideo(int uid) {
        if (callService == null) return;

        // Configurar video remoto
        remoteVideoContainer.removeAllViews();
        remoteVideoContainer.addView(remoteSurfaceView);
        VideoCanvas remoteCanvas = new VideoCanvas(remoteSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN, uid);
        callService.getAgoraEngine().setupRemoteVideo(remoteCanvas);
    }

    private void acceptCall() {
        if (callService != null) {
            callService.answerCall(channelName);
            incomingCallControls.setVisibility(View.GONE);
            callControls.setVisibility(View.VISIBLE);
            txtStatus.setText("Conectando...");
            setupVideoViews();
        }
    }

    private void rejectCall() {
        if (callService != null) {
            callService.rejectCall(channelName);
        }
        finish();
    }

    private void endCall() {
        if (callService != null) {
            callService.endCall();
        }
        finish();
    }

    private void toggleMute() {
        if (callService != null) {
            isMuted = !isMuted;
            callService.getAgoraEngine().muteLocalAudioStream(isMuted);
            btnMute.setImageResource(isMuted ?
                    R.drawable.ic_mic_off : R.drawable.ic_mic);
        }
    }

    private void switchCamera() {
        if (callService != null) {
            callService.getAgoraEngine().switchCamera();
        }
    }

    private void toggleVideo() {
        if (callService != null) {
            isVideoEnabled = !isVideoEnabled;
            callService.getAgoraEngine().muteLocalVideoStream(!isVideoEnabled);
            btnVideoToggle.setImageResource(isVideoEnabled ?
                    R.drawable.ic_videocam : R.drawable.ic_videocam_off);
            localVideoContainer.setVisibility(isVideoEnabled ? View.VISIBLE : View.GONE);
        }
    }

    // Implementación de CallEventListener
    @Override
    public void onCallReceived(String callerName, String channel) {
        // No se usa en esta actividad ya que ya estamos respondiendo a una llamada
    }

    @Override
    public void onCallConnected(String channel) {
        runOnUiThread(() -> {
            txtStatus.setText("Conectado");
            setupVideoViews();
        });
    }

    @Override
    public void onCallEnded(String channel) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Llamada finalizada", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onUserJoined(int uid) {
        runOnUiThread(() -> {
            txtStatus.setText("Conectado");
            setupRemoteVideo(uid);
        });
    }

    @Override
    public void onUserLeft(int uid) {
        runOnUiThread(() -> {
            Toast.makeText(this, "El usuario se desconectó", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onError(int error, String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound && callService != null) {
            callService.removeListener(this);
            unbindService(connection);
            isBound = false;
        }
    }
}
