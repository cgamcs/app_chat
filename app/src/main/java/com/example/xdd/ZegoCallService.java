package com.example.xdd;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

/**
 * Helper class to manage ZegoCloud call service functionality
 */
public class ZegoCallService {
    private static final String TAG = "ZegoCallService";
    private static boolean isInitialized = false;

    /**
     * Initialize the ZegoCloud call service
     * @param application Application context
     * @param appID ZegoCloud AppID
     * @param appSign ZegoCloud AppSign
     * @param userID Current user ID
     * @param userName Current user name
     */
    public static void init(Application application, long appID, String appSign,
                            String userID, String userName) {
        if (!isInitialized) {
            try {
                ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig =
                        new ZegoUIKitPrebuiltCallInvitationConfig();
                ZegoUIKitPrebuiltCallInvitationService.init(
                        application, appID, appSign, userID, userName, callInvitationConfig);
                isInitialized = true;
                Log.d(TAG, "ZegoCloud call service initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize ZegoCloud call service: " + e.getMessage());
            }
        }
    }

    /**
     * Configure a video call button to start a direct call
     * @param context Context
     * @param targetUserID Target user ID
     * @param targetUserName Target user name
     * @param callButton The button to configure
     */
    public static void setupVideoCallButton(Context context, String targetUserID,
                                            String targetUserName, ZegoSendCallInvitationButton callButton) {
        callButton.setIsVideoCall(true);
        callButton.setType(ZegoInvitationType.VIDEO_CALL);
        callButton.setResourceID("zego_uikit_call");
        callButton.setInvitees(Collections.singletonList(
                new ZegoUIKitUser(targetUserID, targetUserName)));
    }

    /**
     * Configure a voice call button to start a direct call
     * @param context Context
     * @param targetUserID Target user ID
     * @param targetUserName Target user name
     * @param callButton The button to configure
     */
    public static void setupVoiceCallButton(Context context, String targetUserID,
                                            String targetUserName, ZegoSendCallInvitationButton callButton) {
        callButton.setIsVideoCall(false);
        callButton.setType(ZegoInvitationType.VOICE_CALL);
        callButton.setResourceID("zego_uikit_call");
        callButton.setInvitees(Collections.singletonList(
                new ZegoUIKitUser(targetUserID, targetUserName)));
    }

    /**
     * Start a direct video call without using the invitation button
     * @param context Context
     * @param appID ZegoCloud AppID
     * @param appSign ZegoCloud AppSign
     * @param myUserID Current user ID
     * @param myUserName Current user name
     * @param targetUserID Target user ID
     */
    public static void startDirectVideoCall(Context context, long appID, String appSign,
                                            String myUserID, String myUserName, String targetUserID) {
        // Generate a unique call ID
        String callID = "call_" + System.currentTimeMillis();

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("appID", appID);
        intent.putExtra("appSign", appSign);
        intent.putExtra("callID", callID);
        intent.putExtra("userID", myUserID);
        intent.putExtra("userName", myUserName);
        intent.putExtra("targetUserID", targetUserID);
        intent.putExtra("isVideoCall", true);
        context.startActivity(intent);
    }
}