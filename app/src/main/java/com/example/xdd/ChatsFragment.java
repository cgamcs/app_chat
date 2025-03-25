package com.example.xdd;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerChats;
    private ChatAdapter chatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Configurar RecyclerView para los chats
        recyclerChats = view.findViewById(R.id.recycler_chats);
        recyclerChats.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(new ArrayList<>()); // Inicializar con una lista vacía
        recyclerChats.setAdapter(chatAdapter);

        // Cargar chats (puedes cargarlos desde Firebase aquí)
        loadChats();

        return view;
    }

    // Método para cargar chats (simulado)
    private void loadChats() {
        List<Chat> chatList = new ArrayList<>();
        chatList.add(new Chat("Chat 1", "Último mensaje..."));
        chatList.add(new Chat("Chat 2", "Último mensaje..."));
        chatAdapter.updateChats(chatList);
    }
}