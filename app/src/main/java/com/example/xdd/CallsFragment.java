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

public class CallsFragment extends Fragment {

    private RecyclerView recyclerCalls;
    private CallAdapter callAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        // Configurar RecyclerView para las llamadas
        recyclerCalls = view.findViewById(R.id.recycler_calls);
        recyclerCalls.setLayoutManager(new LinearLayoutManager(getContext()));
        callAdapter = new CallAdapter(new ArrayList<>()); // Inicializar con una lista vacía
        recyclerCalls.setAdapter(callAdapter);

        // Cargar llamadas (puedes cargarlas desde Firebase aquí)
        loadCalls();

        return view;
    }

    // Método para cargar llamadas (simulado)
    private void loadCalls() {
        List<Call> callList = new ArrayList<>();
        callList.add(new Call("Llamada 1", "Aceptada"));
        callList.add(new Call("Llamada 2", "Colgada"));
        callAdapter.updateCalls(callList);
    }
}