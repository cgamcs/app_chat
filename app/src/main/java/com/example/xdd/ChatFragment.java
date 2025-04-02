package com.example.xdd;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private EditText editMessage;
    private Button btnSendMessage;
    private RecyclerView recyclerMessages;
    private MessageAdapter messageAdapter;
    private List<String> messages;
    private String chatId;
    private String otherUserId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar las vistas
        editMessage = view.findViewById(R.id.edit_message);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
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
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    if (NetworkUtils.isNetworkAvailable(getActivity())) {
                        sendMessage(message);
                    } else {
                        Toast.makeText(getActivity(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Por favor, escribe un mensaje", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void enableNetwork(FirebaseFirestore firestore) {
        firestore.enableNetwork()
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Red habilitada"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error al habilitar red: " + e.getMessage()));
    }

    private void sendMessage(String message) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", currentUserId);
        messageMap.put("text", message);
        messageMap.put("timestamp", System.currentTimeMillis());

        // Guardar el mensaje en Firestore
        db.collection("chats").document(chatId).collection("messages").add(messageMap)
                .addOnSuccessListener(documentReference -> {
                    editMessage.setText("");
                    // Actualizar el último mensaje en el documento del chat
                    db.collection("chats").document(chatId).update("lastMessage", message);
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
                        if (doc.get("text") != null) {
                            messages.add(doc.getString("text"));
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(messages.size() - 1);
                });
    }
}