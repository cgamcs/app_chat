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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsFragment extends Fragment implements ChatAdapter.OnChatClickListener {

    private static final String TAG = "ChatsFragment";
    private EditText editUserInput;
    private Button btnStartChat;
    private Button btnRestoreDb; // Nuevo botón para restaurar DB
    private RecyclerView recyclerChats;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar las vistas
        editUserInput = view.findViewById(R.id.edit_user_input);
        btnStartChat = view.findViewById(R.id.btn_start_chat);
        recyclerChats = view.findViewById(R.id.recycler_chats);

        // Iniciar restauración de datos de usuario actual
        FirestoreSetupHelper.restoreCurrentUserData(getActivity());

        // Configurar el RecyclerView
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, this);
        recyclerChats.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerChats.setAdapter(chatAdapter);

        // Configurar el listener para el botón
        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = editUserInput.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    startChat(userInput);
                } else {
                    Toast.makeText(getActivity(), "Por favor, ingrese un correo o teléfono", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Intentar añadir botón de restauración - asegúrate de que existe en tu layout
        btnRestoreDb = view.findViewById(R.id.btn_restore_db);
        if (btnRestoreDb != null) {
            btnRestoreDb.setOnClickListener(v -> {
                FirestoreSetupHelper.restoreCurrentUserData(getActivity());
                Toast.makeText(getActivity(), "Intentando restaurar datos...", Toast.LENGTH_SHORT).show();
            });
        }

        // Cargar los chats existentes
        loadChats();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadChats(); // Se recargará automáticamente si hay cambios en la BD
    }

    private void startChat(String userInput) {
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Log de depuración para verificar lo que está buscando
        Log.d(TAG, "Buscando usuario con email: " + userInput);

        // Buscar el UID del usuario ingresado en Firestore
        db.collection("usuarios")
                .whereEqualTo("email", userInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String otherUserId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "Usuario encontrado con ID: " + otherUserId);

                        // Verificar si el chat ya existe antes de crearlo
                        checkExistingChat(currentUserId, otherUserId);
                    } else {
                        Log.e(TAG, "Usuario no encontrado con email: " + userInput);

                        // Verificar si podemos crear un chat directo (si conoces el ID)
                        if (userInput.contains("@")) {
                            Toast.makeText(getActivity(), "Usuario no encontrado. Por favor, asegúrate de que el usuario esté registrado.", Toast.LENGTH_LONG).show();
                        } else {
                            // Si no es un email, podría ser un ID de usuario directamente
                            Toast.makeText(getActivity(), "Intentando crear chat con ID: " + userInput, Toast.LENGTH_SHORT).show();
                            createNewChat(currentUserId, userInput);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar usuario: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkExistingChat(String user1, String user2) {
        db.collection("chats")
                .whereArrayContains("participants", user1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(user2)) {
                            // Chat ya existe, abrirlo en vez de crear uno nuevo
                            openChat(doc.getId(), user2);
                            return;
                        }
                    }
                    // Si no existe, crear un nuevo chat
                    createNewChat(user1, user2);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar chat existente: " + e.getMessage());
                    // Intentar crear uno nuevo de todos modos
                    createNewChat(user1, user2);
                });
    }

    private void createNewChat(String user1, String user2) {
        List<String> participants = new ArrayList<>();
        participants.add(user1);
        participants.add(user2);

        Map<String, Object> chatMap = new HashMap<>();
        chatMap.put("participants", participants);
        chatMap.put("lastMessage", "");

        db.collection("chats").add(chatMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Chat creado con ID: " + documentReference.getId());
                    openChat(documentReference.getId(), user2);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al iniciar chat: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error al crear chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openChat(String chatId, String otherUserId) {
        Bundle bundle = new Bundle();
        bundle.putString("chatId", chatId);
        bundle.putString("otherUserId", otherUserId);
        Navigation.findNavController(getView()).navigate(R.id.action_chatsFragment_to_chatFragment, bundle);
    }

    private void loadChats() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error al cargar los chats: " + e.getMessage());
                        return;
                    }

                    chatList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot doc : value) {
                            List<String> participants = (List<String>) doc.get("participants");
                            if (participants != null && participants.contains(currentUserId)) {
                                String lastMessage = doc.getString("lastMessage");
                                String chatId = doc.getId();

                                Chat chat = new Chat(participants, lastMessage != null ? lastMessage : "");
                                chat.setChatId(chatId);
                                chatList.add(chat);
                            }
                        }
                        Log.d(TAG, "Chats cargados: " + chatList.size());
                    } else {
                        Log.d(TAG, "No se encontraron chats para el usuario actual");
                    }
                    chatAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onChatClick(Chat chat) {
        Bundle bundle = new Bundle();
        bundle.putString("chatId", chat.getChatId());
        bundle.putString("otherUserId", getOtherUserId(chat));
        Navigation.findNavController(getView()).navigate(R.id.action_chatsFragment_to_chatFragment, bundle);
    }

    private String getOtherUserId(Chat chat) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        for (String participant : chat.getParticipants()) {
            if (!participant.equals(currentUserId)) {
                return participant;
            }
        }
        return "";
    }
}