package com.example.xdd;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    private HashMap<String, String> userCache = new HashMap<>();
    private OnChatClickListener onChatClickListener;

    public ChatAdapter(List<Chat> chatList, OnChatClickListener onChatClickListener) {
        this.chatList = chatList;
        this.onChatClickListener = onChatClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = "";

        for (String participant : chat.getParticipants()) {
            if (!participant.equals(currentUserId)) {
                otherUserId = participant;
                break;
            }
        }

        // Mostrar ID en lugar de intentar cargar el nombre en caso de error
        final String finalOtherUserId = otherUserId;
        holder.txtChatName.setText("Usuario: " + finalOtherUserId.substring(0, 5) + "...");

        // Crear una instancia específica de Firestore para este adaptador
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Configurar para máxima tolerancia a desconexiones
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);

        // Forzar la habilitación de la red
        enableNetwork(db);

        if (NetworkUtils.isNetworkAvailable(holder.itemView.getContext())) {
            db.collection("usuarios")
                    .document(otherUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            String username = task.getResult().getString("username");
                            holder.txtChatName.setText(username != null ? username : "Usuario desconocido");
                        } else {
                            // Registra el error específico para depuración
                            if (task.getException() != null) {
                                Log.e("ChatAdapter", "Error específico: " + task.getException().getMessage());
                            }
                            // Mantener el ID como fallback
                            holder.txtChatName.setText("Usuario: " + finalOtherUserId.substring(0, 10) + "...");
                        }
                    });
        } else {
            holder.txtChatName.setText("Usuario: " + finalOtherUserId.substring(0, 5) + "...");
        }

        holder.txtLastMessage.setText(chat.getLastMessage());

        // Configurar el OnClickListener para el elemento del chat
        holder.itemView.setOnClickListener(v -> {
            if (onChatClickListener != null) {
                onChatClickListener.onChatClick(chat);
            }
        });
    }

    private void enableNetwork(FirebaseFirestore firestore) {
        firestore.enableNetwork()
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Red habilitada"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error al habilitar red: " + e.getMessage()));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // Método auxiliar para obtener datos del servidor
    private void getServerData(String userId, ChatViewHolder holder) {
        FirebaseFirestore.getInstance().collection("usuarios")
                .document(userId)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        holder.txtChatName.setText(username != null ? username : "Usuario desconocido");
                    } else {
                        holder.txtChatName.setText("Usuario desconocido");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatAdapter", "Error al cargar usuario: " + e.getMessage());
                    holder.txtChatName.setText("Error al cargar usuario");
                });
    }

    public void updateChats(List<Chat> newChatList) {
        chatList = newChatList;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtChatName, txtLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChatName = itemView.findViewById(R.id.txt_chat_name);
            txtLastMessage = itemView.findViewById(R.id.txt_last_message);
        }
    }

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }
}