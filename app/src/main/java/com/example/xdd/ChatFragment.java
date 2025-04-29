package com.example.xdd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private EditText editMessage;
    private Button btnSendMessage;
    private ImageView btnVideoCall;
    private RecyclerView recyclerMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private String chatId;
    private String otherUserId;
    private String otherUserName;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar las vistas
        editMessage = view.findViewById(R.id.edit_message);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        btnVideoCall = view.findViewById(R.id.btn_video_call);
        recyclerMessages = view.findViewById(R.id.recycler_messages);

        // Configurar el RecyclerView
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerMessages.setAdapter(messageAdapter);

        // Obtener el ID del chat y el ID del otro usuario
        Bundle bundle = getArguments();
        if (bundle != null) {
            chatId = bundle.getString("chatId");
            otherUserId = bundle.getString("otherUserId");
            otherUserName = bundle.getString("otherUserName", "Usuario");
        }

        // Forzar la habilitación de la red
        enableNetwork(db);

        // Escuchar los cambios en los mensajes
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            listenForMessages();
        } else {
            Toast.makeText(getActivity(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
        }

        // Configurar el listener para el botón
        btnSendMessage.setOnClickListener(v -> {
            String messageText = editMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                if (NetworkUtils.isNetworkAvailable(getActivity())) {
                    sendMessage(messageText);
                } else {
                    Toast.makeText(getActivity(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Por favor, escribe un mensaje", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el listener para el icono de videollamada
        btnVideoCall.setOnClickListener(v -> {
            startVideoCall();
        });

        return view;
    }

    private void enableNetwork(FirebaseFirestore firestore) {
        firestore.enableNetwork()
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Red habilitada"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error al habilitar red: " + e.getMessage()));
    }

    private void sendMessage(String messageText) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", currentUserId);
        messageMap.put("text", messageText);
        messageMap.put("timestamp", System.currentTimeMillis());

        // Guardar el mensaje en Firestore
        db.collection("chats").document(chatId).collection("messages").add(messageMap)
                .addOnSuccessListener(documentReference -> {
                    editMessage.setText("");
                    // Actualizar el último mensaje en el documento del chat
                    db.collection("chats").document(chatId).update("lastMessage", messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error al enviar el mensaje: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void listenForMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.e("ChatFragment", "Error al cargar los mensajes: " + e.getMessage());
                        Toast.makeText(getActivity(), "Error al cargar los mensajes", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    messages.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String senderId = doc.getString("senderId");
                        String text = doc.getString("text");
                        Long timestamp = doc.getLong("timestamp");
                        if (senderId != null && text != null && timestamp != null) {
                            messages.add(new Message(senderId, text, timestamp));
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(messages.size() - 1);
                });
    }

    private void startVideoCall() {
        // Obtener ID y firma de la aplicación ZEGO
        long appID = getResources().getInteger(R.integer.app_id);
        String appSign = getString(R.string.app_sign);

        // Crear un ID de llamada único basado en los usuarios
        String callID = "call_" + chatId + "_" + System.currentTimeMillis();

        // Obtener información del usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        String currentUserName = currentUser.getDisplayName();

        if (currentUserName == null || currentUserName.isEmpty()) {
            currentUserName = "Usuario " + currentUserId.substring(0, 5);
        }

        // Crear intent para la actividad de llamada
        Intent intent = new Intent(getContext(), CallActivity.class);
        intent.putExtra("appID", appID);
        intent.putExtra("appSign", appSign);
        intent.putExtra("callID", callID);
        intent.putExtra("isVideoCall", true); // Determina si es video/audio
        intent.putExtra("userID", currentUserId);
        intent.putExtra("userName", currentUserName);

        startActivity(intent);
    }
}