// SelectUserFragment.java (Nuevo Fragment)
package com.example.xdd;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_user, container, false);
        recyclerView = view.findViewById(R.id.recycler_users);
        setupRecyclerView();
        loadUsersFromFirestore();
        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdapter(userList, this::onUserSelected);
        recyclerView.setAdapter(adapter);
    }

    private void loadUsersFromFirestore() {
        FirebaseFirestore.getInstance().collection("usuarios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void onUserSelected(User selectedUser) {
        // Regresar al CallsFragment con el usuario seleccionado
        Bundle result = new Bundle();
        result.putString("selectedUserId", selectedUser.getUserId());
        result.putString("selectedUserName", selectedUser.getUsername());
        getParentFragmentManager().setFragmentResult("user_selected", result);

        // Cerrar este fragment
        getParentFragmentManager().popBackStack();
    }
}