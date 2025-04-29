package com.example.xdd;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.uikit.prebuilt.call.config.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.fragment.ZegoUIKitPrebuiltCallFragment;

public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Obtener datos pasados desde ChatFragment
        long appID = getIntent().getLongExtra("appID", 0);
        String appSign = getIntent().getStringExtra("appSign");
        String callID = getIntent().getStringExtra("callID");
        String userID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");

        // Configurar la llamada
        ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();

        // Agregar el fragmento de llamada
        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                appID, appSign, userID, userName, callID, config);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }
}