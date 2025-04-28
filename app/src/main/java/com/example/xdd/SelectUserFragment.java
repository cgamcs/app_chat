package com.example.xdd;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private static final String TAG = "SelectUserFragment";

    private EditText etSearchUser;
    private Button btnSearch;
    private RecyclerView recyclerUsers;
    private TextView tvNoUsers;
    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("SelectUserFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_select_user, container, false);

        etSearchUser = view.findViewById(R.id.et_search_user);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerUsers = view.findViewById(R.id.recycler_users);
        tvNoUsers = view.findViewById(R.id.tv_no_users);

        setupRecyclerView();
        setupSearchButton();

        return view;
    }

    private void setupRecyclerView() {
        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdapter(userList);
        recyclerUsers.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            User selectedUser = userList.get(position);
            // Send back the result to CallsFragment
            Bundle result = new Bundle();
            result.putString("selectedUserId", selectedUser.getId());
            result.putString("selectedUserName", selectedUser.getName());
            getParentFragmentManager().setFragmentResult("user_selected", result);

            // Go back to previous fragment
            requireActivity().onBackPressed();
        });
    }

    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> {
            String email = etSearchUser.getText().toString().trim();
            if (!email.isEmpty()) {
                searchUserByEmail(email);
            }
        });
    }

    private void searchUserByEmail(String email) {
        FirebaseFirestore.getInstance().collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();

                        if (task.getResult().isEmpty()) {
                            recyclerUsers.setVisibility(View.GONE);
                            tvNoUsers.setVisibility(View.VISIBLE);
                            tvNoUsers.setText("No se encontraron usuarios con ese correo");
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            String userName = document.getString("name");
                            String userEmail = document.getString("email");

                            User user = new User(userId, userName, userEmail);
                            userList.add(user);
                        }

                        if (userList.isEmpty()) {
                            recyclerUsers.setVisibility(View.GONE);
                            tvNoUsers.setVisibility(View.VISIBLE);
                        } else {
                            recyclerUsers.setVisibility(View.VISIBLE);
                            tvNoUsers.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e(TAG, "Error getting users", task.getException());
                        tvNoUsers.setText("Error al buscar usuarios");
                        tvNoUsers.setVisibility(View.VISIBLE);
                    }
                });
    }
}