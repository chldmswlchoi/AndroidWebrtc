//package com.example.webrtc1.tutorial;
//
//import static org.webrtc.SessionDescription.Type.ANSWER;
//import static org.webrtc.SessionDescription.Type.OFFER;
//import static io.socket.client.Socket.EVENT_CONNECT;
//import static io.socket.client.Socket.EVENT_CONNECTING;
//import static io.socket.client.Socket.EVENT_CONNECT_ERROR;
//import static io.socket.client.Socket.EVENT_CONNECT_TIMEOUT;
//import static io.socket.client.Socket.EVENT_DISCONNECT;
//import static io.socket.client.Socket.EVENT_ERROR;
//import static io.socket.client.Socket.EVENT_RECONNECT;
//import static io.socket.client.Socket.EVENT_RECONNECTING;
//import static io.socket.client.Socket.EVENT_RECONNECT_ATTEMPT;
//import static io.socket.client.Socket.EVENT_RECONNECT_FAILED;
//
//import android.Manifest;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.res.ResourcesCompat;
//import androidx.databinding.DataBindingUtil;
//
//import com.example.webrtc1.R;
//import com.example.webrtc1.databinding.ActivitySamplePeerConnectionBinding;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.webrtc.AudioSource;
//import org.webrtc.AudioTrack;
//import org.webrtc.Camera1Enumerator;
//import org.webrtc.Camera2Enumerator;
//import org.webrtc.CameraEnumerator;
//import org.webrtc.CameraVideoCapturer;
//import org.webrtc.DataChannel;
//import org.webrtc.EglBase;
//import org.webrtc.IceCandidate;
//import org.webrtc.MediaConstraints;
//import org.webrtc.MediaStream;
//import org.webrtc.PeerConnection;
//import org.webrtc.PeerConnectionFactory;
//import org.webrtc.SessionDescription;
//import org.webrtc.VideoCapturer;
//import org.webrtc.VideoRenderer;
//import org.webrtc.VideoSource;
//import org.webrtc.VideoTrack;
//
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import pub.devrel.easypermissions.AfterPermissionGranted;
//import pub.devrel.easypermissions.EasyPermissions;
//
//public class CompleteActivity1_1videoCall extends AppCompatActivity {
//    private static final String TAG = "CompleteActivity";
//    private static final int RC_CALL = 111;
//    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
//    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
//    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
//    public static final int FPS = 30;
//    private Socket socket;
//    private boolean isInitiator;
//    private boolean isChannelReady; // 방에 2명이상일 때 true가 됨
//    private boolean isStarted;
//    private boolean audio = false;
//
//    MediaConstraints audioConstraints;
//    AudioSource audioSource;
//    AudioTrack localAudioTrack;
//
//    private ActivitySamplePeerConnectionBinding binding;
//    private PeerConnection peerConnection;
//    private EglBase rootEglBase;
//    private PeerConnectionFactory factory;
//    private VideoTrack videoTrackFromCamera;
//    private MediaStream RemoteMediaStream;
//    private VideoCapturer cameraVideoCapturer;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample_peer_connection);
////        setSupportActionBar(binding.toolbar);
//        start();
//
//        binding.endCall.setOnClickListener(v -> {
//            if (socket != null) {
//                onDestroy();
//
//            }
//        });
//
//        binding.rotate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e(TAG, "화면 전환 버튼 클릭");
//                if(cameraVideoCapturer != null){
//                    Log.e(TAG, "화면 전환 : CameraVideoCapturer 변수가 null이 아닐 때");
//                    if (cameraVideoCapturer instanceof CameraVideoCapturer) {
//                        Log.e(TAG, "화면 전환 : CameraVideoCapturer의 타입이 CameraVideoCapturer 일 때");
//                        CameraVideoCapturer switchCamera = (CameraVideoCapturer) cameraVideoCapturer;
//                        CameraVideoCapturer.CameraSwitchHandler cvc =  new CameraVideoCapturer.CameraSwitchHandler() {
//                            @Override
//                            public void onCameraSwitchDone(boolean b) {
//                                Log.e(TAG, "카메라 전환 Handler : 카메라 전환 완료 /  Boolean 값 : " + b);
//                            }
//
//                            @Override
//                            public void onCameraSwitchError(String s) {
//                                Log.e(TAG, "카메라 전환 Handler : 카메라 전환 에러 : "+s);
//                            }
//                        };
//
//                        switchCamera.switchCamera(cvc);
//
//                    } else {
//                        Log.e(TAG, "화면 전환 : 타입이 CameraVideoCapturer 아닐때");
//                        // Will not switch camera, video capturer is not a camera
//                    }
//                }
//
//            }
//        });
//
//        binding.mute.setOnClickListener(v -> { // 음소거 관련 부분
//
//            if(!audio){
//                Log.e(TAG, "음소거하기");
//                localAudioTrack.setEnabled(false);
//                Log.e(TAG, "오디오 false 로 설정");
//                binding.mute.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.mute,null)); // 다시 이해해보기
//                Log.e(TAG, "오디오 이미지 변경해줌");
//
//                audio =true;
//            }
//
//            else if (audio){
//                Log.e(TAG, "음소거 해제");
//
//                binding.mute.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.audio,null)); // 다시 이해해보기
//                Log.e(TAG, "오디오 이미지 변경해줌");
//
//                localAudioTrack.setEnabled(true);
//                Log.e(TAG, "오디오 true 로 설정");
//                audio = false;
//
//            }
//
//        });
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // 권한 관련 함수
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
//    }
//
//    @Override
//    protected void onDestroy() {
//        Log.e(TAG, "onDestroy");
//        if (socket != null) {
//            sendMessage("bye");
//            Log.e(TAG, "bye msg 보냄?");
//            socket.close();
//            Log.e(TAG, "소켓 닫음");
//            finish();
//
//        }
//        super.onDestroy();
//    }
//
//    @AfterPermissionGranted(RC_CALL)
//    private void start() {
//        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
//        if (EasyPermissions.hasPermissions(this, perms)) {
//            Log.e(TAG, "webrtc 관련 권한들이 허락이 모두 되었을 때 소켓 생성");
//            Log.e(TAG, "소켓 및 시그널링서버와 연결하는 함수 실행");
//            connectToSignallingServer(); //소켓 생성및 시그널링 서버와 연결
//
//            initializeSurfaceViews();
//
//            initializePeerConnectionFactory();
//
//            createVideoTrackFromCameraAndShowIt();
//
//            initializePeerConnections();
//
//            startStreamingVideo();
//        } else {
//            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
//            Log.e(TAG, "webrtc 관련 권한들이 허락이 안됨.");
//        }
//    }
//
//    private void SocketEventListener(){ // 생성된 소켓의 연결 상태를 알려주는 역할
//        socket.on(EVENT_CONNECTING,args -> {
//            Log.e(TAG, "소켓 연결 중 ");
//        });
//        socket.on(EVENT_CONNECT_ERROR,args -> {
//            Log.e(TAG, "소켓 연결 에러 "+ Arrays.toString(args));
//        });
//        socket.on(EVENT_CONNECT_TIMEOUT,args -> {
//            Log.e(TAG, "소켓 연결 Timeout");
//            Log.e(TAG, Arrays.toString(args));
//        });
//        socket.on(EVENT_RECONNECT,args -> {
//            Log.e(TAG, "소켓 재연결 완료됨");
//            Log.e(TAG, Arrays.toString(args));
//        });
//        socket.on(EVENT_RECONNECT_ATTEMPT,args -> {
//            Log.e(TAG, "소켓 재 연결 시도");
//            Log.e(TAG, Arrays.toString(args));
//        });
//        socket.on(EVENT_RECONNECTING,args -> {
//            Log.e(TAG, "소켓 재연결 중");
//            Log.e(TAG, Arrays.toString(args));
//        });
//        socket.on(EVENT_RECONNECT_FAILED,args -> {
//            Log.e(TAG, "소켓 재 연결 실패");
//            Log.e(TAG, Arrays.toString(args));
//        });
//        socket.on(EVENT_ERROR,args -> {
//            Log.e(TAG, "소켓 에러");
//            Log.e(TAG, Arrays.toString(args));
//
//        });
//
//    }
//
//    // 핸들러로 전달할 runnable 객체. 수신 스레드 실행.
//    final Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//
//            binding.remoteView.setVisibility(View.VISIBLE);
//            binding.MyView.setVisibility(View.GONE);
//            Log.e(TAG, "runnable 스레드: View_GONE 나의 큰 화면 활동 상태 체크" + binding.surfaceView.isActivated());
//            videoTrackFromCamera.removeRenderer(new VideoRenderer(binding.surfaceView));
//            Log.e(TAG, "runnable 스레드: " + " 나의 큰 화면의 Renderer 제거");
//            Log.e(TAG, "runnable 스레드: 나의 큰 화면 활동 상태 체크" + binding.surfaceView.isActivated());
//            binding.surfaceView.pauseVideo();
//            Log.e(TAG, "runnable 스레드: pauseVideo 나의 큰 화면 활동 상태 체크" + binding.surfaceView.isActivated());
//
//
//        }
//    } ;
//
//    // 핸들러로 전달할 runnable 객체. 수신 스레드 실행.
//    final Runnable goneUI = new Runnable() {
//        @Override
//        public void run() {
//            binding.remoteView.setVisibility(View.GONE);
//        }
//    } ;
//
//
//    private void connectToSignallingServer() {
//        try {
//
//            String URL = "http://3.39.153.170:3000/"; // http://서버url :port번호 형식으로 작성
//            Log.e(TAG, "REPLACE ME: IO Socket:" + URL);
//            socket = IO.socket(URL);
//            socket.connect();
//            Log.e(TAG, "소켓 connect 연결 시도");
//
//            SocketEventListener(); // 생성된 소켓의 연결 상태를 알려주는 역할
//
//            socket.on(EVENT_CONNECT, args -> {
//                Log.e(TAG, "소켓 연결 됨 ");
//            }).on("requestUserData", args -> {
//                Log.e(TAG, "-->requestUserData ");
//                socket.emit("create or join", "foo");
//                Log.e(TAG, "<-- create or join 시그널링 서버에게 해당 방의 참여자인지 생성자인지 물어봄 & 인자로 방 식별자(방이름)을 보냄");
//            }).on("created", args -> {
//                Log.e(TAG, " --> created : 방을 생성한 사람일 때");
//                Log.e(TAG, String.valueOf(args));
//                isInitiator = true;
//            }).on("full", args -> {
//                Log.e(TAG, "--> full : 방의 제한 인원수가 꽉 차서 못들어 감");
//            }).on("join", args -> {
//                Log.e(TAG, "--> join 새로운 유저가 입장했음");
////                Log.e(TAG, "connectToSignallingServer: Another peer made a request to join room");
////                Log.e(TAG, "connectToSignallingServer: This peer is the initiator of room");
//
//                isChannelReady = true;
//            }).on("joined", args -> {
//                Log.e(TAG, "--> joined  "+ Arrays.toString(args) + "방에 입장 완료됨" +args[0] +args[1]); // 배열형태로 오는지 확인해보기 위해서
//                isChannelReady = true;
//            }).on("log", args -> {
//                for (Object arg : args) {
//                    Log.e(TAG, "connectToSignallingServer: " + String.valueOf(arg));
//                }
//            }).on("message", args -> {
//                Log.e(TAG, "connectToSignallingServer: got a message");
//            }).on("message", args -> {
//                Log.e(TAG, "connectToSignallingServer: 2 got a message"); // 위 소켓 이벤트와 뭔차인지 알아보려고 로그 찍음
//                try {
//                    if (args[0] instanceof String) { // instanceof 는 객체 타입을 확인하는 연산자/ 형변환 가능 여부를 확인하여 t/f 로 결과 반환
//                        Log.e(TAG, "connectToSignallingServer : args[0]의 값의 타입이 String 으로 변환 가능할 때"); // 위 소켓 이벤트와 뭔차인지 알아보려고 로그 찍음
//                        String message = (String) args[0];
//                        if (message.equals("got user media")) {
//                            Log.e(TAG, "connectToSignallingServer : 유저로부터 media를 받으면 maybeStart 함수 호출");
//                            maybeStart();
//                        }
//                        else if ("bye".equals(args[0]) && isStarted) { // 홈화면 버튼 클릭 및 뒤로가기 말고 통화 끊기 버튼을 끊었을 때 해당 방에 나가도록 처리 할 예정
//                            runOnUiThread(goneUI);
//                            isStarted=false;
//                            Log.e(TAG, "connectToSignallingServer: BYE");
//                            finish();//3명 이상일 때는 남아있는 인원 수 확인 하는 과정 추가하기
//                            peerConnection.close();
//                            cameraVideoCapturer.dispose();
//                            rootEglBase.releaseSurface();
//                            rootEglBase.release();
//                            videoTrackFromCamera.dispose();
//                            factory.dispose();
//
//                        }
//                    } else {
//                        Log.e(TAG, "connectToSignallingServer : args[0]의 값이 Json객체 형태일 때"); // 위 소켓 이벤트와 뭔차인지 알아보려고 로그 찍음
//                        JSONObject message = (JSONObject) args[0];
//                        Log.e(TAG, "connectToSignallingServer: got message : " + message);
//                        if (message.getString("type").equals("offer")) {
//                            Log.e(TAG, "메시지 type 이 offer 타입이면");
//                            Log.e(TAG, "connectToSignallingServer: received an offer " + isInitiator + " " + isStarted);
//                            if (!isInitiator && !isStarted) {
//                                Log.e(TAG, "생성자가 아니고 maybeStart 함수가 한번도 호출 되지 않다면 maybeStart 함수 호출");
//                                maybeStart();
//                            }
//                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
//                            Log.e(TAG, "setRemoteDescripton을 offer로 설정 해준 뒤 doAnswer 함수 호출");
//                            doAnswer();
//                        } else if (message.getString("type").equals("answer") && isStarted) {
//                            Log.e(TAG, "메시지 type 이 answer 이면");
//                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));
//                            Log.e(TAG, "setRemoteDescripton에 answer 으로 설정 완료");
//                        } else if (message.getString("type").equals("candidate") && isStarted) {
//                            Log.e(TAG, "메시지 type 이 candidate 이면");
//                            Log.e(TAG, "connectToSignallingServer: receiving candidates");
//                            IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
//                            Log.e(TAG, "후보자 생성");
//                            peerConnection.addIceCandidate(candidate);
//                            Log.e(TAG, "새로운 후보자를 원격 후보에 추가해줌 ");
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }).on(EVENT_DISCONNECT, args -> {
//                Log.e(TAG, "connectToSignallingServer: disconnect");
//            });
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//    }
////MirtDPM4
//    private void doAnswer() {
//        Log.e(TAG, "doAnswer 함수 : Answer 생성하는 역할");
//        peerConnection.createAnswer(new SimpleSdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                Log.e(TAG, "doAnswer : Answer 성공적으로 생성됨");
//                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
//                Log.e(TAG, "doAnswer : setLocalDescription");
//                JSONObject message = new JSONObject();
//                try {
//                    message.put("type", "answer");
//                    message.put("sdp", sessionDescription.description);
//                    Log.e(TAG, "doAnswer : answer 정보 담아서 sendMessage 호출");
//                    sendMessage(message);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new MediaConstraints());
//    }
//
//    private void maybeStart() {
//        Log.e(TAG, "maybeStart 처음 호출 된 경우에만 함수의 코드 실행됨 " +
//                " 역할 : 1. isStarted 변수를 true 로 초기화 해줌, maybeStart 2. 방 생성자 이면 doCall 함수 호출");
//        Log.e(TAG, "maybeStart: " + isStarted + " " + isChannelReady);
//        if (!isStarted && isChannelReady) {
//            Log.e(TAG, "maybeStart: " +"방에 유저가 두명 이상이고,  isStarated 가 false 이면 (= maybeStart 함수가 처음 호출 될 때)");
//            isStarted = true;
//            Log.e(TAG, "maybeStart: " +"isStarted 변수 true로 초기화 해줌");
//            if (isInitiator) { // 방 생성자 일 때
//                Log.e(TAG, "maybeStart: " + "방 생정자 이면 doCall 함수 호출");
//                doCall();
//            }
//        }
//    }
//
//    private void doCall() {
//        Log.e(TAG, "doCall 함수 :  Offer 생성하는 역할");
//        MediaConstraints sdpMediaConstraints = new MediaConstraints();
//
//        sdpMediaConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
//        sdpMediaConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
//        // 만약 OfferToReceiveAudio 값이 false 이면 remotePeer는 audio 관련 정보를 보내지 않는다.
//        Log.e(TAG, "doCall : remotePeer 가 audio와 OfferToReceiveAudiovideo를 보낼 수 있도록 설정");
//
//        peerConnection.createOffer(new SimpleSdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                Log.e(TAG, "doCall : Offer 가 성공적으로 생성이 되면");
//                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
//                Log.e(TAG, "doCall : 생성된 Offer localDescription 세팅 ");
//                JSONObject message = new JSONObject();
//                try {
//                    message.put("type", "offer");
//                    message.put("sdp", sessionDescription.description);
//                    message.put("receiver", sessionDescription.description);
//                    // Json 형태로 offer 형태의 sdp 보냄
//                    Log.e(TAG, "doCall : Json 형태로 offer 형태의 sdp 라는 정보를 생성하고, sendMessage 호출해줌");
//                    sendMessage(message);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, sdpMediaConstraints);
//
//        Log.e(TAG, "doCall : Offer 생성함");
//
//    }
//
//    private void sendMessage(Object message) {
//        Log.e(TAG, "sendMessage : 서버로 소켓 message 이벤트 전송" );
//
//        socket.emit("message", message);
//        Log.e(TAG, "<--message : "+ message );
//
//    }
//
//    private void initializeSurfaceViews() {
//        Log.e(TAG, "initializeSurfaceViews() 역할 : 비디오 렌더링 환경 설정");
//        rootEglBase = EglBase.create();
//        Log.e(TAG, "initializeSurfaceViews() : EglBase 생성");
//        binding.surfaceView.init(rootEglBase.getEglBaseContext(), null); // surfaceView 초기화
//        Log.e(TAG, "initializeSurfaceViews() : 자신의 surfaceViewRender 초기화");
//        binding.surfaceView.setEnableHardwareScaler(true);
//        Log.e(TAG, "initializeSurfaceViews() : 비디오 프레임 크기 조정 설정 가능하게 함");
//        binding.surfaceView.setMirror(true);
//        Log.e(TAG, "initializeSurfaceViews() : 거울 효과를 주어 좌우 반전 효과 설정");
//
//        binding.surfaceView2.init(rootEglBase.getEglBaseContext(), null);
//        Log.e(TAG, "initializeSurfaceViews() : 상대방의 surfaceViewRender 초기화");
//        binding.surfaceView2.setEnableHardwareScaler(true);
//        binding.surfaceView2.setMirror(true);
//        Log.e(TAG, "initializeSurfaceViews() : 상대방 비디오 크기 설정 및 거울 효과 세팅 완료");
//
//        binding.myView.init(rootEglBase.getEglBaseContext(), null);
//        Log.e(TAG, "initializeSurfaceViews() : 상대방의 surfaceViewRender 초기화");
//        binding.myView.setEnableHardwareScaler(true);
//        binding.myView.setMirror(true);
//        Log.e(TAG, "initializeSurfaceViews() : 상대방 비디오 크기 설정 및 거울 효과 세팅 완료");
//
//        //add one more
//    }
//
//    private void initializePeerConnectionFactory() {
//        Log.e(TAG, "initializePeerConnectionFactory 역할 : PeerConnectionFactory 생성 하드웨어 가속화 설정");
//        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
//        // 인자 : context, 오디오 초기화, 비디오초기화, 비디오하드웨어 가속화(비디오 처리 속도 향상시킴)
//        Log.e(TAG, "initializePeerConnectionFactory() : context, 오디오 초기화, 비디오초기화, 비디오하드웨어 가속화");
//
//        factory = new PeerConnectionFactory(null);
//        // PeerConnectionFatory는 클라이언트용  PeerConnectin API의 주 진입점이다.
//        Log.e(TAG, "initializePeerConnectionFactory() : PeerConnetionFactory 생성");
//        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
//        Log.e(TAG, "initializePeerConnectionFactory() : 하드웨어 가속화(비디오 처리 속도 향상 위해) 설정");
//        // 인자 : 나의 EGlBase context , 원격 피어의 EGLBase context
//    }
//
//    private void createVideoTrackFromCameraAndShowIt() {
//        Log.e(TAG, "createVideoTrackFromCameraAndShowIt 역할 : 카메라에서 비디오 정보를 컴퓨터가 읽거나 사용할 수 있는 형태로 가져오는 과정");
//
//        audioConstraints = new MediaConstraints(); // 비디오 및 오디오 제약 조건을 지정하는 역할
//        cameraVideoCapturer = createVideoCapturer();
//        Log.e(TAG, "createVideoTrackFromCameraAndShowIt () : VideoCapturer 생성");
//        VideoSource videoSource = factory.createVideoSource(cameraVideoCapturer);
//        Log.e(TAG, "createVideoTrackFromCameraAndShowIt () : Capturer로부터 videoSource 생성");
//        cameraVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
//        Log.e(TAG, "createVideoTrackFromCameraAndShowIt () : 캡처 시작 & 캡처할 비디오의 가로, 세로 해상도 초당 프레임 설정");
//        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//        // 인자 :  비디오 트랙 식별자, 비디오 소스
//        Log.e(TAG, "createVideoTrackFromCameraAndShowIt () : 비디오 트랙 생성");
//        videoTrackFromCamera.setEnabled(true);
//        videoTrackFromCamera.addRenderer(new VideoRenderer(binding.surfaceView));
//        videoTrackFromCamera.addRenderer(new VideoRenderer(binding.myView));
//
//        //create an AudioSource instance
//        audioSource = factory.createAudioSource(audioConstraints);
//        localAudioTrack = factory.createAudioTrack("101", audioSource);
//
//
//        Log.e(TAG, "createVideoTrackFromCameraAndShowIt () : videoTrack 생성");
//
//
//    }
//
//    private void initializePeerConnections() {
//        Log.e(TAG, "initializePeerConnections 역할 : peerConnection 생성");
//        peerConnection = createPeerConnection(factory);
//
//        Log.e(TAG, "initializePeerConnections () : peerConnection 생성 완료");
//
//    }
//
//    private void startStreamingVideo() {
//        Log.e(TAG, "startStreamingVideo 역할 : 자신의 mediaStream 생성");
//        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
//        Log.e(TAG, "startStreamingVideo () : MediaStream 생성");
//        mediaStream.addTrack(videoTrackFromCamera);
//        Log.e(TAG, "startStreamingVideo () : 비디오 트랙 추가");
//        mediaStream.addTrack(localAudioTrack);
//        Log.e(TAG, "startStreamingVideo () : 오디오 트랙 추가");
//        peerConnection.addStream(mediaStream);
//        Log.e(TAG, "startStreamingVideo () : sendMessage 호출");
//        sendMessage("got user media");
//    }
//
//
//    private PeerConnection createPeerConnection(PeerConnectionFactory factory) { //Peer 생성 및 turn, stun 서버 구축 과정
//        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
//        String URL = "stun:stun.l.google.com:19302";
//        iceServers.add(new PeerConnection.IceServer(URL));
//        String TurnURL ="turn:3.39.153.170";
//        String userName ="choi";
//        String TurnPW ="choiWebrtc";
//        iceServers.add(new PeerConnection.IceServer(TurnURL,userName,TurnPW));
//        Log.e(TAG, "Turn 서버 생성 및 추가");
//
//
//        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
//        MediaConstraints pcConstraints = new MediaConstraints();
//
//        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
//            @Override
//            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
//                Log.e(TAG, "onSignalingChange: ");
//            }
//
//            @Override
//            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//                Log.e(TAG, "onIceConnectionChange: ");
//            }
//
//            @Override
//            public void onIceConnectionReceivingChange(boolean b) {
//                Log.e(TAG, "onIceConnectionReceivingChange: ");
//            }
//
//            @Override
//            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
//                Log.e(TAG, "onIceGatheringChange: ");
//            }
//
//            @Override
//            public void onIceCandidate(IceCandidate iceCandidate) {
//                Log.e(TAG, "onIceCandidate: ");
//                JSONObject message = new JSONObject();
//
//                try {
//                    message.put("type", "candidate");
//                    message.put("label", iceCandidate.sdpMLineIndex);
//                    message.put("id", iceCandidate.sdpMid);
//                    message.put("candidate", iceCandidate.sdp);
//
//                    Log.e(TAG, "onIceCandidate: sending candidate " + message);
//                    sendMessage(message);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
//                Log.e(TAG, "onIceCandidatesRemoved: ");
//            }
//
//            @Override
//            public void onAddStream(MediaStream mediaStream) {
//
//                Log.e(TAG, "onAddStream: " + mediaStream.videoTracks.size());
//                RemoteMediaStream = mediaStream;
//                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
//                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
//                remoteAudioTrack.setEnabled(true);
//                remoteVideoTrack.setEnabled(true);
//                remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));
//                Log.e(TAG, "onAddStream: " + "remoteVideo에 Renderer 추가");
//                runOnUiThread(runnable) ; //상대방 비디오 띄워주기
//            }
//
//            @Override
//            public void onRemoveStream(MediaStream mediaStream) {
//                Log.e(TAG, "onRemoveStream: ");
//            }
//
//            @Override
//            public void onDataChannel(DataChannel dataChannel) {
//                Log.e(TAG, "onDataChannel: ");
//            }
//
//            @Override
//            public void onRenegotiationNeeded() {
//                Log.e(TAG, "onRenegotiationNeeded: ");
//            }
//        }; //협상 이벤트 리스너
//
//        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
//    }
//
//
//    private VideoCapturer createVideoCapturer() {
//        VideoCapturer videoCapturer;
//        if (useCamera2()) {
//            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
//        } else {
//            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
//        }
//        return videoCapturer;
//    }
//
//    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
//        final String[] deviceNames = enumerator.getDeviceNames(); // 사용 가능한 장치 목록 반환
//        CameraVideoCapturer.CameraEventsHandler CEH = new CameraVideoCapturer.CameraEventsHandler() {
//            @Override
//            public void onCameraError(String s) {
//                Log.e(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 에러 :  "+s);
//            }
//
//            @Override
//            public void onCameraDisconnected() {
//                Log.e(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 disConnect");
//            }
//
//            @Override
//            public void onCameraFreezed(String s) {
//                Log.e(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 정지");
//            }
//
//            @Override
//            public void onCameraOpening(String s) {
//                Log.e(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 열리는 중");
//            }
//
//            @Override
//            public void onFirstFrameAvailable() {
//                Log.e(TAG, "카메라 캡처 이벤트 핸들러 - onFirstFrameAvailable 뭔데");
//            }
//
//            @Override
//            public void onCameraClosed() {
//                Log.e(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 닫힘");
//            }
//        };
//
//        for (String deviceName : deviceNames) {
//            if (enumerator.isFrontFacing(deviceName)) { //카메라가 앞이면
//                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, CEH);
//
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//
//        for (String deviceName : deviceNames) {
//            if (!enumerator.isFrontFacing(deviceName)) { // 카메라가 뒤이면 변수를 CameraVideoCapturer로 해줌 (VideoCapturer을 상속받은 클래스임)
//                CameraVideoCapturer cameraVideoCapturer = enumerator.createCapturer(deviceName, CEH);
//                if (cameraVideoCapturer != null) {
//                    return cameraVideoCapturer;
//                }
//            }
//        }
//
//        return null;
//    }
//
//
//
//    private boolean useCamera2() {
//        return Camera2Enumerator.isSupported(this);
//        //isSupported : api 지원 여부 확인 및 모든 카메라가 기존 지원보다 나은지 확인한다.
//    }
//
//}
