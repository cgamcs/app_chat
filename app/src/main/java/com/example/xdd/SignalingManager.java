//package com.example.xdd;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FieldValue;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.ListenerRegistration;
//import com.google.gson.Gson;
//
//import org.webrtc.IceCandidate;
//import org.webrtc.SessionDescription;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class SignalingManager {
//    private FirebaseFirestore db;
//    private List<ListenerRegistration> registrations = new ArrayList<>();
//    private String currentUserId;
//    private Gson gson;
//
//    public SignalingManager(String currentUserId) {
//        this.db = FirebaseFirestore.getInstance();
//        this.currentUserId = currentUserId;
//        this.gson = new Gson();
//    }
//
//    public void createCallOffer(String callId, String targetUserId, SessionDescription offer, OnCallCreatedListener listener) {
//        // Crear un documento para la llamada con los IDs de ambos usuarios
//        Map<String, Object> callData = new HashMap<>();
//        callData.put("callerId", currentUserId);
//        callData.put("receiverId", targetUserId);
//        callData.put("status", "calling");
//        callData.put("offer", gson.toJson(offer));
//        callData.put("timestamp", FieldValue.serverTimestamp());
//
//        db.collection("calls").document(callId)
//                .set(callData)
//                .addOnSuccessListener(aVoid -> {
//                    if (listener != null) {
//                        listener.onCallCreated(true, "Call created successfully");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    if (listener != null) {
//                        listener.onCallCreated(false, "Failed to create call: " + e.getMessage());
//                    }
//                });
//    }
//
//    public void sendOffer(String callId, SessionDescription offer) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("offer", gson.toJson(offer));
//
//        db.collection("calls").document(callId)
//                .update(updates)
//                .addOnFailureListener(e -> {
//                    // Manejar el error
//                });
//    }
//
//    public void sendAnswer(String callId, SessionDescription answer) {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("answer", gson.toJson(answer));
//        updates.put("status", "connected");
//
//        db.collection("calls").document(callId)
//                .update(updates)
//                .addOnFailureListener(e -> {
//                    // Manejar el error
//                });
//    }
//
//    public void sendIceCandidate(String callId, String userId, IceCandidate iceCandidate) {
//        String iceCandidateString = gson.toJson(iceCandidate);
//
//        DocumentReference callRef = db.collection("calls").document(callId);
//        DocumentReference candidatesRef = callRef.collection("candidates").document(userId);
//
//        candidatesRef.get().addOnSuccessListener(documentSnapshot -> {
//            if (!documentSnapshot.exists()) {
//                // Si el documento no existe, crear una lista nueva
//                List<String> candidates = new ArrayList<>();
//                candidates.add(iceCandidateString);
//                candidatesRef.set(new HashMap<String, Object>() {{
//                    put("candidates", candidates);
//                }});
//            } else {
//                // Si el documento existe, actualizar la lista
//                candidatesRef.update("candidates", FieldValue.arrayUnion(iceCandidateString));
//            }
//        });
//    }
//
//    public void listenForAnswer(String callId, OnAnswerReceivedListener listener) {
//        ListenerRegistration registration = db.collection("calls").document(callId)
//                .addSnapshotListener((snapshot, e) -> {
//                    if (e != null || snapshot == null || !snapshot.exists()) {
//                        return;
//                    }
//
//                    String answerJson = snapshot.getString("answer");
//                    if (answerJson != null) {
//                        SessionDescription answer = gson.fromJson(answerJson, SessionDescription.class);
//                        if (listener != null) {
//                            listener.onAnswerReceived(answer);
//                        }
//                    }
//                });
//
//        registrations.add(registration);
//    }
//
//    public void listenForOffer(String callId, OnOfferReceivedListener listener) {
//        ListenerRegistration registration = db.collection("calls").document(callId)
//                .addSnapshotListener((snapshot, e) -> {
//                    if (e != null || snapshot == null || !snapshot.exists()) {
//                        return;
//                    }
//
//                    String offerJson = snapshot.getString("offer");
//                    if (offerJson != null) {
//                        SessionDescription offer = gson.fromJson(offerJson, SessionDescription.class);
//                        if (listener != null) {
//                            listener.onOfferReceived(offer);
//                        }
//                    }
//                });
//
//        registrations.add(registration);
//    }
//
//    public void listenForIceCandidates(String callId, String remoteUserId, OnIceCandidateReceivedListener listener) {
//        ListenerRegistration registration = db.collection("calls").document(callId)
//                .collection("candidates").document(remoteUserId)
//                .addSnapshotListener((snapshot, e) -> {
//                    if (e != null || snapshot == null || !snapshot.exists()) {
//                        return;
//                    }
//
//                    List<String> candidatesJson = (List<String>) snapshot.get("candidates");
//                    if (candidatesJson != null && !candidatesJson.isEmpty()) {
//                        for (String candidateJson : candidatesJson) {
//                            IceCandidate iceCandidate = gson.fromJson(candidateJson, IceCandidate.class);
//                            if (listener != null) {
//                                listener.onIceCandidateReceived(iceCandidate);
//                            }
//                        }
//                    }
//                });
//
//        registrations.add(registration);
//    }
//
//    public void listenForIncomingCalls(OnIncomingCallListener listener) {
//        ListenerRegistration registration = db.collection("calls")
//                .whereEqualTo("receiverId", currentUserId)
//                .whereEqualTo("status", "calling")
//                .addSnapshotListener((snapshots, e) -> {
//                    if (e != null || snapshots == null || snapshots.isEmpty()) {
//                        return;
//                    }
//
//                    snapshots.getDocumentChanges().forEach(documentChange -> {
//                        if (documentChange.getType().equals(com.google.firebase.firestore.DocumentChange.Type.ADDED)) {
//                            DocumentSnapshot document = documentChange.getDocument();
//                            String callId = document.getId();
//                            String callerId = document.getString("callerId");
//
//                            if (listener != null) {
//                                listener.onIncomingCall(callId, callerId);
//                            }
//                        }
//                    });
//                });
//
//        registrations.add(registration);
//    }
//
//    public void updateCallStatus(String callId, String status) {
//        db.collection("calls").document(callId)
//                .update("status", status)
//                .addOnFailureListener(e -> {
//                    // Manejar el error
//                });
//    }
//
//    public void endCall(String callId) {
//        updateCallStatus(callId, "ended");
//    }
//
//    public void stopListening() {
//        for (ListenerRegistration registration : registrations) {
//            if (registration != null) {
//                registration.remove();
//            }
//        }
//        registrations.clear();
//    }
//
//    public interface OnCallCreatedListener {
//        void onCallCreated(boolean success, String message);
//    }
//
//    public interface OnOfferReceivedListener {
//        void onOfferReceived(SessionDescription offer);
//    }
//
//    public interface OnAnswerReceivedListener {
//        void onAnswerReceived(SessionDescription answer);
//    }
//
//    public interface OnIceCandidateReceivedListener {
//        void onIceCandidateReceived(IceCandidate iceCandidate);
//    }
//
//    public interface OnIncomingCallListener {
//        void onIncomingCall(String callId, String callerId);
//    }
//}