package com.example.xdd;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class MenuFragment extends Fragment {

    private FirebaseAuth mAuth;

    private MaterialButton btnEditProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Referencias a los botones
        MaterialButton btnChangeLanguage = view.findViewById(R.id.btn_change_language);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        // Listener para el botón de cambiar idioma
        btnChangeLanguage.setOnClickListener(v -> cambiarIdioma());

        // Listener para el botón de cerrar sesión
        btnLogout.setOnClickListener(v -> cerrarSesion());

        // Inicializar el botón
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Configurar el listener del botón
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e("MainActivity", "btnEditProfile no encontrado en el layout");
        }

        return view;
    }

    // Método para cambiar idioma
    private void cambiarIdioma() {
        Intent intent = new Intent(getActivity(), LanguageActivity.class);
        startActivity(intent);
    }

    // Método para cerrar sesión
    private void cerrarSesion() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), SesionActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}