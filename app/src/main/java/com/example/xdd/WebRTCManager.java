//package com.example.xdd;
//
//import android.content.Context;
//
//import org.webrtc.AudioSource;
//import org.webrtc.AudioTrack;
//import org.webrtc.Camera1Enumerator;
//import org.webrtc.Camera2Enumerator;
//import org.webrtc.CameraEnumerator;
//import org.webrtc.CameraVideoCapturer;
//import org.webrtc.DataChannel;
//import org.webrtc.DefaultVideoDecoderFactory;
//import org.webrtc.DefaultVideoEncoderFactory;
//import org.webrtc.EglBase;
//import org.webrtc.IceCandidate;
//import org.webrtc.MediaConstraints;
//import org.webrtc.MediaStream;
//import org.webrtc.PeerConnection;
//import org.webrtc.PeerConnectionFactory;
//import org.webrtc.SdpObserver;
//import org.webrtc.SessionDescription;
//import org.webrtc.SurfaceTextureHelper;
//import org.webrtc.SurfaceViewRenderer;
//import org.webrtc.VideoCapturer;
//import org.webrtc.VideoSource;
//import org.webrtc.VideoTrack;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class WebRTCManager {
//    private static final String TAG = "WebRTCManager";
//    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
//    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
//    private static final String STREAM_ID = "ARDAMS";
//
//    private Context context;
//    private EglBase eglBase;
//    private PeerConnectionFactory factory;
//    private PeerConnection peerConnection;
//    private MediaStream localMediaStream;
//    private VideoTrack localVideoTrack;
//    private AudioTrack localAudioTrack;
//    private VideoCapturer videoCapturer;
//    private SurfaceViewRenderer localRenderer;
//    private SurfaceViewRenderer remoteRenderer;
//    private WebRTCListener listener;
//
//    public WebRTCManager(Context context) {
//        this.context = context;
//
//        // Crear la instancia de EglBase
//        eglBase = EglBase.create();
//    }
//
//    public void initialize() {
//        // Inicializar PeerConnectionFactory
//        PeerConnectionFactory.InitializationOptions initializationOptions =
//                PeerConnectionFactory.InitializationOptions.builder(context)
//                        .setEnableInternalTracer(true)
//                        .createInitializationOptions();
//        PeerConnectionFactory.initialize(initializationOptions);
//
//        // Crear la fábrica de PeerConnection
//        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
//        factory = PeerConnectionFactory.builder()
//                .setOptions(options)
//                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
//                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
//                .createPeerConnectionFactory();
//
//        // Crear capturador de video
//        videoCapturer = createVideoCapturer();
//        if (videoCapturer == null) {
//            // Manejar error - no se pudo crear el capturador de video
//            return;
//        }
//
//        // Crear fuente de video y track
//        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
//        VideoSource videoSource = factory.createVideoSource(videoCapturer.isScreencast());
//        videoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
//        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//
//        // Crear fuente de audio y track
//        MediaConstraints audioConstraints = new MediaConstraints();
//        AudioSource audioSource = factory.createAudioSource(audioConstraints);
//        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
//
//        // Crear el stream de medios local
//        localMediaStream = factory.createLocalMediaStream(STREAM_ID);
//        localMediaStream.addTrack(localVideoTrack);
//        localMediaStream.addTrack(localAudioTrack);
//    }
//
//    public void setupLocalVideoRenderer(SurfaceViewRenderer renderer) {
//        localRenderer = renderer;
//        localRenderer.init(eglBase.getEglBaseContext(), null);
//        localRenderer.setEnableHardwareScaler(true);
//        localRenderer.setMirror(true);
//        localVideoTrack.addSink(localRenderer);
//    }
//
//    public void setupRemoteVideoRenderer(SurfaceViewRenderer renderer) {
//        remoteRenderer = renderer;
//        remoteRenderer.init(eglBase.getEglBaseContext(), null);
//        remoteRenderer.setEnableHardwareScaler(true);
//        remoteRenderer.setMirror(false);
//    }
//
//    private VideoCapturer createVideoCapturer() {
//        CameraEnumerator enumerator;
//        if (Camera2Enumerator.isSupported(context)) {
//            enumerator = new Camera2Enumerator(context);
//        } else {
//            enumerator = new Camera1Enumerator(false);
//        }
//        final String[] deviceNames = enumerator.getDeviceNames();
//
//        // Intentar obtener la cámara frontal primero
//        for (String deviceName : deviceNames) {
//            if (enumerator.isFrontFacing(deviceName)) {
//                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//
//        // Si no hay cámara frontal, intentar con cualquier cámara
//        for (String deviceName : deviceNames) {
//            VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//            if (videoCapturer != null) {
//                return videoCapturer;
//            }
//        }
//
//        return null;
//    }
//
//    public void createPeerConnection(List<PeerConnection.IceServer> iceServers) {
//        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
//        rtcConfig.enableDtlsSrtp = true;
//        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
//
//        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
//            @Override
//            public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
//
//            @Override
//            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//                if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
//                    if (listener != null) {
//                        listener.onConnected();
//                    }
//                } else if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED ||
//                        iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
//                    if (listener != null) {
//                        listener.onDisconnected();
//                    }
//                }
//            }
//
//            @Override
//            public void onIceConnectionReceivingChange(boolean b) {}
//
//            @Override
//            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
//
//            @Override
//            public void onIceCandidate(IceCandidate iceCandidate) {
//                if (listener != null) {
//                    listener.onIceCandidateGenerated(iceCandidate);
//                }
//            }
//
//            @Override
//            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
//
//            @Override
//            public void onAddStream(MediaStream mediaStream) {}
//
//            @Override
//            public void onRemoveStream(MediaStream mediaStream) {}
//
//            @Override
//            public void onDataChannel(DataChannel dataChannel) {}
//
//            @Override
//            public void onRenegotiationNeeded() {}
//
//            @Override
//            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
//                if (rtpReceiver.track() instanceof VideoTrack) {
//                    VideoTrack remoteVideoTrack = (VideoTrack) rtpReceiver.track();
//                    if (remoteRenderer != null) {
//                        remoteVideoTrack.addSink(remoteRenderer);
//                    }
//                    if (listener != null) {
//                        listener.onRemoteVideoTrackAvailable();
//                    }
//                }
//            }
//        };
//
//        peerConnection = factory.createPeerConnection(rtcConfig, pcObserver);
//
//        // Agregar el stream local al peerConnection
//        if (localMediaStream != null) {
//            for (AudioTrack audioTrack : localMediaStream.audioTracks) {
//                peerConnection.addTrack(audioTrack, new ArrayList<String>() {{
//                    add(STREAM_ID);
//                }});
//            }
//
//            for (VideoTrack videoTrack : localMediaStream.videoTracks) {
//                peerConnection.addTrack(videoTrack, new ArrayList<String>() {{
//                    add(STREAM_ID);
//                }});
//            }
//        }
//    }
//
//    public void startCapturing() {
//        if (videoCapturer != null) {
//            videoCapturer.startCapture(1280, 720, 30);
//        }
//    }
//
//    public void stopCapturing() {
//        if (videoCapturer != null) {
//            try {
//                videoCapturer.stopCapture();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void createOffer() {
//        if (peerConnection == null) return;
//
//        MediaConstraints constraints = new MediaConstraints();
//        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
//        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
//
//        peerConnection.createOffer(new SdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                peerConnection.setLocalDescription(new SdpObserver() {
//                    @Override
//                    public void onCreateSuccess(SessionDescription sessionDescription) {}
//
//                    @Override
//                    public void onSetSuccess() {
//                        if (listener != null) {
//                            listener.onOfferCreated(sessionDescription);
//                        }
//                    }
//
//                    @Override
//                    public void onCreateFailure(String s) {}
//
//                    @Override
//                    public void onSetFailure(String s) {}
//                }, sessionDescription);
//            }
//
//            @Override
//            public void onSetSuccess() {}
//
//            @Override
//            public void onCreateFailure(String s) {}
//
//            @Override
//            public void onSetFailure(String s) {}
//        }, constraints);
//    }
//
//    public void createAnswer() {
//        if (peerConnection == null) return;
//
//        MediaConstraints constraints = new MediaConstraints();
//        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
//        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
//
//        peerConnection.createAnswer(new SdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                peerConnection.setLocalDescription(new SdpObserver() {
//                    @Override
//                    public void onCreateSuccess(SessionDescription sessionDescription) {}
//
//                    @Override
//                    public void onSetSuccess() {
//                        if (listener != null) {
//                            listener.onAnswerCreated(sessionDescription);
//                        }
//                    }
//
//                    @Override
//                    public void onCreateFailure(String s) {}
//
//                    @Override
//                    public void onSetFailure(String s) {}
//                }, sessionDescription);
//            }
//
//            @Override
//            public void onSetSuccess() {}
//
//            @Override
//            public void onCreateFailure(String s) {}
//
//            @Override
//            public void onSetFailure(String s) {}
//        }, constraints);
//    }
//
//    public void setRemoteDescription(SessionDescription sessionDescription) {
//        if (peerConnection == null) return;
//
//        peerConnection.setRemoteDescription(new SdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {}
//
//            @Override
//            public void onSetSuccess() {}
//
//            @Override
//            public void onCreateFailure(String s) {}
//
//            @Override
//            public void onSetFailure(String s) {}
//        }, sessionDescription);
//    }
//
//    public void addIceCandidate(IceCandidate iceCandidate) {
//        if (peerConnection == null) return;
//
//        peerConnection.addIceCandidate(iceCandidate);
//    }
//
//    public void release() {
//        if (videoCapturer != null) {
//            try {
//                videoCapturer.stopCapture();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            videoCapturer.dispose();
//            videoCapturer = null;
//        }
//
//        if (localVideoTrack != null) {
//            if (localRenderer != null) {
//                localVideoTrack.removeSink(localRenderer);
//            }
//            localVideoTrack.dispose();
//            localVideoTrack = null;
//        }
//
//        if (localAudioTrack != null) {
//            localAudioTrack.dispose();
//            localAudioTrack = null;
//        }
//
//        if (localMediaStream != null) {
//            localMediaStream = null;
//        }
//
//        if (peerConnection != null) {
//            peerConnection.close();
//            peerConnection = null;
//        }
//
//        if (factory != null) {
//            factory.dispose();
//            factory = null;
//        }
//
//        if (localRenderer != null) {
//            localRenderer.release();
//            localRenderer = null;
//        }
//
//        if (remoteRenderer != null) {
//            remoteRenderer.release();
//            remoteRenderer = null;
//        }
//
//        if (eglBase != null) {
//            eglBase.release();
//            eglBase = null;
//        }
//    }
//
//    public void setWebRTCListener(WebRTCListener listener) {
//        this.listener = listener;
//    }
//
//    public interface WebRTCListener {
//        void onOfferCreated(SessionDescription sessionDescription);
//        void onAnswerCreated(SessionDescription sessionDescription);
//        void onIceCandidateGenerated(IceCandidate iceCandidate);
//        void onConnected();
//        void onDisconnected();
//        void onRemoteVideoTrackAvailable();
//    }
//
//    public interface RtpReceiver {
//        org.webrtc.MediaStreamTrack track();
//    }
//}