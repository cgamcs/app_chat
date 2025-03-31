// ChatFragment.java
package com.example.xdd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }
}

class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private FirebaseFirestore db;
    private ListenerRegistration chatListener;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_chats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
        loadChats();
        return view;
    }

    private void loadChats() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        chatListener = db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatFragment", "Error cargando chats", error);
                        return;
                    }

                    if (value == null) return;

                    chatList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Chat chat = doc.toObject(Chat.class);
                        chat.setChatId(doc.getId());
                        chatList.add(chat);
                    }
                    chatAdapter.updateChats(chatList);
                });
    }

    public void createNewChat(String otherUserId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || otherUserId == null) return;

        db.collection("chats")
                .whereIn("participants", Arrays.asList(
                        Arrays.asList(currentUserId, otherUserId),
                        Arrays.asList(otherUserId, currentUserId)
                ))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Chat newChat = new Chat(null, Arrays.asList(currentUserId, otherUserId), "", System.currentTimeMillis());
                        db.collection("chats").add(newChat);
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatFragment", "Error al buscar chat existente", e));
    }
}
