package com.example.xdd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CallsFragment extends Fragment implements CallService.CallEventListener {
    private RecyclerView recyclerView;
    private CallAdapter adapter;
    private List<Call> callList = new ArrayList<>();
    private Button btnStartCall;

    private CallService callService;
    private boolean isBound = false;

    // Inside your CallsFragment.java
    private void navigateToSelectUser() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_callsFragment_to_selectUserFragment);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CallService.CallBinder binder = (CallService.CallBinder) service;
            callService = binder.getService();
            callService.addListener(CallsFragment.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("CallsFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        recyclerView = view.findViewById(R.id.recycler_calls);
        btnStartCall = view.findViewById(R.id.btn_start_call);

        setupRecyclerView();
        setupCallButton();

        // Iniciar y vincular al servicio
        Intent intent = new Intent(requireContext(), CallService.class);
        requireActivity().startService(intent);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        getParentFragmentManager().setFragmentResultListener("user_selected", this, (requestKey, result) -> {
            String userId = result.getString("selectedUserId");
            String userName = result.getString("selectedUserName");
            startCallToUser(userName);
        });

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CallAdapter(callList);
        recyclerView.setAdapter(adapter);

        // Añadir listener para manejar clics en los elementos de la lista
        adapter.setOnItemClickListener(position -> {
            Call call = callList.get(position);
            if ("Activa".equals(call.getCallStatus())) {
                // Unirse a una llamada activa
                Intent intent = CallActivity.createIncomingCallIntent(
                        requireContext(),
                        call.getChannelId(),
                        call.getCallName());
                startActivity(intent);
            }
        });
    }

    // Update the setupCallButton method in CallsFragment.java
    private void setupCallButton() {
        btnStartCall.setOnClickListener(v -> {
            Log.d("CallsFragment", "Start call button clicked");
            // Use the navigation component for transition
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            if (navController != null) {
                try {
                    navController.navigate(R.id.action_callsFragment_to_selectUserFragment);
                } catch (Exception e) {
                    Log.e("CallsFragment", "Navigation failed: " + e.getMessage());
                    // Fallback method if navigation component fails
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new SelectUserFragment())
                            .addToBackStack(null)
                            .commit();
                }
            } else {
                Log.e("CallsFragment", "NavController is null");
                // Fallback method
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new SelectUserFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void startCallToUser(String userName) {
        if (callService != null) {
            String channelId = UUID.randomUUID().toString().substring(0, 8);
            Intent intent = CallActivity.createOutgoingCallIntent(
                    requireContext(),
                    channelId,
                    userName);
            startActivity(intent);
        }
    }

    // Implementación de CallEventListener
    @Override
    public void onCallReceived(String callerName, String channelName) {
        requireActivity().runOnUiThread(() -> {
            // Añadir la llamada entrante a la lista
            Call newCall = new Call(callerName, "Entrante", channelName);
            callList.add(0, newCall);
            adapter.notifyItemInserted(0);

            // Mostrar notificación o diálogo para llamada entrante
            showIncomingCallDialog(callerName, channelName);
        });
    }

    private void showIncomingCallDialog(String callerName, String channelName) {
        // En una app real, podrías mostrar un diálogo aquí
        // O iniciar directamente la actividad de llamada
        Intent intent = CallActivity.createIncomingCallIntent(
                requireContext(),
                channelName,
                callerName);
        startActivity(intent);
    }

    @Override
    public void onCallConnected(String channel) {
        // Actualizar estado en la lista si es necesario
    }

    @Override
    public void onCallEnded(String channel) {
        // Actualizar estado en la lista si es necesario
    }

    @Override
    public void onUserJoined(int uid) {
        // No se utiliza en este fragmento
    }

    @Override
    public void onUserLeft(int uid) {
        // No se utiliza en este fragmento
    }

    @Override
    public void onError(int error, String message) {
        // Manejar errores si es necesario
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBound && callService != null) {
            callService.removeListener(this);
            requireActivity().unbindService(connection);
            isBound = false;
        }
    }
}