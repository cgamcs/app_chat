package com.example.xdd;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectUserFragment extends Fragment {

    private EditText etSearchUser;
    private Button btnSearch;
    private RecyclerView recyclerUsers;
    private TextView tvNoUsers;

    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_user, container, false);

        etSearchUser = view.findViewById(R.id.et_search_user);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerUsers = view.findViewById(R.id.recycler_users);
        tvNoUsers = view.findViewById(R.id.tv_no_users);

        // Configurar RecyclerView
        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        userAdapter = new UserAdapter(userList);
        recyclerUsers.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(position -> {
            User selectedUser = userList.get(position);

            // Enviar resultado al fragment anterior
            Bundle result = new Bundle();
            result.putString("selectedUserId", selectedUser.getId());
            result.putString("selectedUserName", selectedUser.getUsername());
            getParentFragmentManager().setFragmentResult("user_selected", result);

            // Volver al fragment anterior
            getParentFragmentManager().popBackStack();
        });

        // Configurar buscador
        btnSearch.setOnClickListener(v -> searchUserByEmail(etSearchUser.getText().toString()));

        // Buscar automáticamente al escribir
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    searchUserByEmail(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchUserByEmail(String email) {
        if (email.isEmpty()) {
            showNoUsersMessage("Ingresa un correo para buscar");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoUsersMessage("No se encontraron usuarios con ese correo");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        String username = document.getString("username");
                        String userEmail = document.getString("email");

                        User user = new User(userId, username, userEmail);
                        userList.add(user);
                    }

                    userAdapter.notifyDataSetChanged();
                    recyclerUsers.setVisibility(View.VISIBLE);
                    tvNoUsers.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al buscar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoUsersMessage("Error al buscar usuarios");
                });
    }

    private void showNoUsersMessage(String message) {
        userList.clear();
        userAdapter.notifyDataSetChanged();
        recyclerUsers.setVisibility(View.GONE);
        tvNoUsers.setText(message);
        tvNoUsers.setVisibility(View.VISIBLE);
    }
}