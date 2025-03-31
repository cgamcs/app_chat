// ChatAdapter.java
package com.example.xdd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;

    public ChatAdapter(List<Chat> chatList) {
        this.chatList = chatList;
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

        FirebaseFirestore.getInstance().collection("usuarios")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        holder.txtChatName.setText(username != null ? username : "Usuario desconocido");
                    } else {
                        holder.txtChatName.setText("Usuario desconocido");
                    }
                })
                .addOnFailureListener(e -> holder.txtChatName.setText("Error al cargar usuario"));

        holder.txtLastMessage.setText(chat.getLastMessage());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
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
}