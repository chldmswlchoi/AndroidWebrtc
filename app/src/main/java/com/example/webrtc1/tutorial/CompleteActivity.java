package com.example.webrtc1.tutorial;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;
import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_CONNECTING;
import static io.socket.client.Socket.EVENT_CONNECT_ERROR;
import static io.socket.client.Socket.EVENT_CONNECT_TIMEOUT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static io.socket.client.Socket.EVENT_ERROR;
import static io.socket.client.Socket.EVENT_RECONNECT;
import static io.socket.client.Socket.EVENT_RECONNECTING;
import static io.socket.client.Socket.EVENT_RECONNECT_ATTEMPT;
import static io.socket.client.Socket.EVENT_RECONNECT_FAILED;
import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import com.example.webrtc1.R;
import com.example.webrtc1.databinding.ActivitySamplePeerConnectionBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CompleteActivity extends AppCompatActivity {
    private static final String TAG = "CompleteActivity";
    private static final int RC_CALL = 111;
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;
    private Socket socket;
    private boolean isInitiator =false;
    private boolean audio = false;

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    org.webrtc.AudioTrack localAudioTrack;

    private ActivitySamplePeerConnectionBinding binding;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;
    private VideoCapturer cameraVideoCapturer;
    private ArrayList<String> userList = new ArrayList<>();
    private String mySocketId, hostSocketId;
    private String roomName="foo";
    private JSONObject socketId_PC = new JSONObject(); // socket.id : peerConnection 형태로 저장해줌
    private Map <String,Object> socketID_SurfaceView = new HashMap<String,Object>();
    private Map <String,Object> socketID_remoteVideo = new HashMap<String,Object>();
    private Map <String,Object> socketID_remoteAudio = new HashMap<String,Object>();
    private Map <String,Object> socketID_mediaStream = new HashMap<String,Object>();

    private MediaStream mediaStream;
    private boolean hostView, userView1,userView2,reConnect,userView3 =false;
    private Handler visibleDisplayHandler, hostProgressBarHandler,goneDisplayHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        visibleDisplayHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == 1){
                    binding.remoteViewGroup.setVisibility(View.VISIBLE);
                    Log.d(TAG, "host Display 의 remoteViewGroup Visible 해주기");
                    binding.surfaceView1.setVisibility(View.VISIBLE);
                    Log.d(TAG, "host Display 의 surfaceView1 Visible 해주기");
                }

                else if (msg.what ==2){
                    binding.surfaceView2.setVisibility(View.VISIBLE);
                    Log.d(TAG, "host Display 의 surfaceView2 Visible 해주기");
                }

                else if (msg.what ==3){
                    binding.surfaceView3.setVisibility(View.VISIBLE);
                    Log.d(TAG, "host Display 의 surfaceView3 Visible 해주기");
                }

                else if (msg.what == 4){
                    binding.remoteViewGroup.setVisibility(View.VISIBLE);
                    Log.d(TAG, "참여자 Display remoteViewGroup Visible 해주기");
                    binding.surfaceView1.setVisibility(View.VISIBLE);
                    Log.d(TAG, "참여자의 자신의 영상 surfaceView1 Visible 해주기");
                }

                else if (msg.what ==5){
                    binding.surfaceView2.setVisibility(View.VISIBLE);
                    Log.d(TAG, "참여자 Display surfaceView2 Visible 해주기");
                }

                else if (msg.what == 6){
                    binding.surfaceView3.setVisibility(View.VISIBLE);
                    Log.d(TAG, "참여자 Display surfaceView3  Visible 해주기");
                }
            }
        };

        hostProgressBarHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 0){
                    binding.progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "progressBar Gone 해줌");

                }
            }
        };

        goneDisplayHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what ==0){
                    binding.surfaceView1.setVisibility(View.GONE);
                    userView1 =false;
                    binding.surfaceView2.setVisibility(View.GONE);
                    userView2 =false;
                    binding.surfaceView3.setVisibility(View.GONE);
                    userView3 =false;
                    binding.remoteViewGroup.setVisibility(View.GONE);
                    destroyRoom();
                }
                else if (msg.what==1){
                    binding.surfaceView1.setVisibility(View.GONE);
                    userView1 =false;
                }
                else if (msg.what==2){
                    binding.surfaceView2.setVisibility(View.GONE);
                    userView2 =false;
                }
                else if (msg.what==3){
                    binding.surfaceView3.setVisibility(View.GONE);
                    userView3 =false;
                }

                if(isInitiator){ //방생성자 일 때
                    if(!userView1 && !userView2 && !userView3){
                        binding.remoteViewGroup.setVisibility(View.GONE);
                    }
                }
            }
        };

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample_peer_connection);
        start();

        binding.endCall.setOnClickListener(v ->{
            socket.emit("bye", roomName);
            Log.d(TAG, "bye msg 보냄?");
            destroyRoom();
                }
          );

        binding.rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "화면 전환 버튼 클릭");
                if(cameraVideoCapturer != null){
                    Log.d(TAG, "화면 전환 : CameraVideoCapturer 변수가 null이 아닐 때");
                    if (cameraVideoCapturer instanceof CameraVideoCapturer) {
                        Log.d(TAG, "화면 전환 : CameraVideoCapturer의 타입이 CameraVideoCapturer 일 때");
                        CameraVideoCapturer switchCamera = (CameraVideoCapturer) cameraVideoCapturer;

                        switchCamera.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                            @Override
                            public void onCameraSwitchDone(boolean b) {
                                Log.d(TAG, "카메라 전환 Handler : 카메라 전환 완료 /  Boolean 값 : " + b);

                            }

                            @Override
                            public void onCameraSwitchError(String s) {
                                Log.d(TAG, "카메라 전환 Handler : 카메라 전환 에러 : "+s);

                            }
                        });

                    } else {
                        Log.d(TAG, "화면 전환 : 타입이 CameraVideoCapturer 아닐때");
                      }
                }

            }
        });

        binding.mute.setOnClickListener(v -> { // 음소거 관련 부분

            if(!audio){
                Log.d(TAG, "음소거하기");
                localAudioTrack.setEnabled(false);
                Log.d(TAG, "오디오 false 로 설정");
                binding.mute.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.mute,null)); // 다시 이해해보기
                Log.d(TAG, "오디오 이미지 변경해줌");

                audio =true;
            }

            else if (audio){
                Log.d(TAG, "음소거 해제");

                binding.mute.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.mike,null)); // 다시 이해해보기
                Log.d(TAG, "오디오 이미지 변경해줌");

                localAudioTrack.setEnabled(true);
                Log.d(TAG, "오디오 true 로 설정");
                audio = false;

            }

        });
    }

    public void destroyRoom(){
        Log.d(TAG, "destroyRoom () : 방이 파괴될 때");
        if (socket != null) {
            Log.d(TAG, "destroyRoom : 소켓이 null 이 아닐 때");
            socket.disconnect();
            Log.d(TAG, "destroyRoom : 소켓 닫음");

//            videoTrackFromCamera.setEnabled(false);
//            localAudioTrack.setEnabled(false);
//            videoTrackFromCamera.dispose();
//            localAudioTrack.dispose();
//
//            mediaStream.removeTrack(localAudioTrack);
//            mediaStream.removeTrack(videoTrackFromCamera);
            Iterator i = socketId_PC.keys();
            while (i.hasNext()) {
                String socketId = i.next().toString();
                Log.d(TAG,"destroyRoom : "+socketId+"의  PC 종료 &  mediaStream 제거하는 과정" );
                try {
                    PeerConnection removePC = (PeerConnection) socketId_PC.get(socketId);
                    removePC.close();
                    Log.d(TAG,"destroyRoom : "+socketId+"의 PC 종료 완료" );
                    MediaStream removeMediaStream = (MediaStream) socketID_mediaStream.get(socketId);
                    removePC.removeStream(removeMediaStream);
                    Log.d(TAG,"destroyRoom : "+socketId+"의 mediaStream 제거 완료" );
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG,"destroyRoom : catch : "+e );
                }
            }

        }
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // 권한 관련 함수
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

    }

    @AfterPermissionGranted(RC_CALL)
    private void start() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.d(TAG, "webrtc 관련 권한들이 허락이 모두 되었을 때 소켓 생성");
            Log.d(TAG, "소켓 및 시그널링서버와 연결하는 함수 실행");

            initializeSurfaceViews(); // 후에 추가될 가능성이 있는 sufaceview 설정해줌 (한번만 실행 해도 ok)

            initializePeerConnectionFactory(); // peerConnectionFactory 생성해줌 (1-ok)

            createVideoTrackFromCameraAndShowIt();

            connectToSignallingServer(); //시그널링와 이벤트를 주고 받을 소켓 생성및 시그널링 서버와 연결

            startStreamingVideo();
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
            Log.d(TAG, "webrtc 관련 권한들이 허락이 안됨.");
        }
    }

    private void SocketEventListener(){ // 생성된 소켓의 연결 상태를 알려주는 역할
        socket.on(EVENT_CONNECTING,args -> {
            Log.d(TAG, "소켓 연결 중 ");
        });
        socket.on(EVENT_CONNECT_ERROR,args -> {
            Log.d(TAG, "소켓 연결 에러 "+ Arrays.toString(args));
        });
        socket.on(EVENT_CONNECT_TIMEOUT,args -> {
            Log.d(TAG, "소켓 연결 Timeout");
            Log.d(TAG, Arrays.toString(args));
        });
        socket.on(EVENT_RECONNECT,args -> {

            Log.d(TAG, "소켓 재연결 완료됨");
            reConnect = true;
            Log.d(TAG, Arrays.toString(args));
        });
        socket.on(EVENT_RECONNECT_ATTEMPT,args -> {
            Log.d(TAG, "소켓 재 연결 시도");
            Log.d(TAG, Arrays.toString(args));
        });
        socket.on(EVENT_RECONNECTING,args -> {
            Log.d(TAG, "소켓 재연결 중");
            Log.d(TAG, Arrays.toString(args));
        });
        socket.on(EVENT_RECONNECT_FAILED,args -> {
            Log.d(TAG, "소켓 재 연결 실패");
            Log.d(TAG, Arrays.toString(args));
        });
        socket.on(EVENT_ERROR,args -> {
            Log.d(TAG, "소켓 에러");
            Log.d(TAG, Arrays.toString(args));

        });

        socket.on("bye",args ->{
            Log.d(TAG, args[0] +" 유저가 bye 보냄");
            String exitUser = (String) args[0];
            Log.d(TAG, "-> bye  : 나간 유저의 소켓 아이디 "+exitUser);
            int SurfaceViewNum = (int) socketID_SurfaceView.get(exitUser);
            Log.d(TAG, "-> bye  : SurfaceViewNum (나간 유저가 어느 뷰에 있는지 구분해 줌):" +SurfaceViewNum);

            if( SurfaceViewNum==0 ){
                Log.d(TAG, "-> bye 0 : 나간 유저가 host 일 때 방 파괴!");
                Log.d(TAG, "-> bye 0 : destroyRoom 함수 호출");
                goneDisplayHandler.sendEmptyMessage(0);

            }

            else if (SurfaceViewNum == 1){ //SurfaceView1 에 있는 유저가 나갔을 때
                try {
                    PeerConnection exitPC = (PeerConnection) socketId_PC.get(exitUser);
                    MediaStream disposeMediaStream = (MediaStream) socketID_mediaStream.get(exitUser);
                    exitPC.close();
                    Log.d(TAG, "-> bye 1 : 나간 유저에 해당되는 PC를 종료해주기");
                    exitPC.removeStream(disposeMediaStream);
                    Log.d(TAG, "-> bye 1 : 해당 스트림이 포함된 모든 트랙을 제거하고 스트림 자체를 종료한다");
                    goneDisplayHandler.sendEmptyMessage(1);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else if (SurfaceViewNum == 2){ //SurfaceView2 에 있는 유저가 나갔을 때
                try {
                    PeerConnection exitPC = (PeerConnection) socketId_PC.get(exitUser);
                    MediaStream disposeMediaStream = (MediaStream) socketID_mediaStream.get(exitUser);
                    exitPC.close();
                    Log.d(TAG, "-> bye 2 : 나간 유저에 해당되는 PC를 종료해주기");
                    exitPC.removeStream(disposeMediaStream);
                    Log.d(TAG, "-> bye 2 : 해당 스트림이 포함된 모든 트랙을 제거하고 스트림 자체를 종료한다");
                    goneDisplayHandler.sendEmptyMessage(2);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else if (SurfaceViewNum == 3){ //SurfaceView3 에 있는 유저가 나갔을 때
                try {
                    PeerConnection exitPC = (PeerConnection) socketId_PC.get(exitUser);
                    MediaStream disposeMediaStream = (MediaStream) socketID_mediaStream.get(exitUser);
                    exitPC.close();
                    Log.d(TAG, "-> bye 3 : 나간 유저에 해당되는 PC를 종료해주기");
                    exitPC.removeStream(disposeMediaStream);
                    Log.d(TAG, "-> bye 3 : 해당 스트림이 포함된 모든 트랙을 제거하고 스트림 자체를 종료한다");
                    goneDisplayHandler.sendEmptyMessage(3);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        });

        socket.on("got_user_media",args -> {
            Log.d(TAG, " -> got_user_media args: "+ args);
            try {
                if(!args[1].equals(mySocketId)) {
                    Log.d(TAG, " -> got_user_media args: 다른 사람이 보낸 socket event 일때만 ");
                    doCall(args[0].toString(), args[1].toString());
                }
            } catch (JSONException e) {
                Log.d(TAG, "got_user_media : Error : "+ e);

                e.printStackTrace();
            }
        });

        socket.on ("offer", args -> {
            Log.d(TAG, " -> offer 받음 pC 생성하고 answer 생성할 예정");

            try{

                JSONObject message = (JSONObject) args[0];
                Log.d(TAG, " -> offer / message : "+message);
                String receiver = message.getString("from");
                Log.d(TAG, " -> offer : offer을 보낸 유저의 소켓 아이디 : "+receiver);
                PeerConnection peerConnection = initializePeerConnections(receiver);


                Log.d(TAG, " -> offer : peerConnection 생성");

                socketId_PC.put(message.getString("from"),peerConnection);

                Log.d(TAG, "-->offer : json을 사용하여 socketId(키)-peerConnection(값) 형태로 저장해줌");
                Log.d(TAG, "-->offer : socketId_PC : "+socketId_PC);

                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("offer")));
                Log.d(TAG, "setRemoteDescripton을 offer로 설정 해준 뒤 doAnswer 함수 호출");
                peerConnection.addStream(mediaStream);


                doAnswer(roomName,receiver,peerConnection);

            }catch (Exception e){
                Log.d(TAG, "offer : Error : "+ e);

            }

        });


        socket.on ("answer", args -> {
            Log.d(TAG, " -> answer");

            try{
                JSONObject message = (JSONObject) args[0];
                Log.d(TAG, " -> answer / message : "+message);
                String sender = message.getString("from");
                Log.d(TAG, " -> answer : answer을  보낸 유저의 소켓 아이디 : "+sender);

                PeerConnection peerConnection = (PeerConnection) socketId_PC.get(sender); //? 뭐지 peerConnection이 json에서 값으로 저장이 되나? -> 됨
                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("answer")));
                Log.d(TAG, "-> answer :setRemoteDescripton에 answer 으로 설정 완료");


            }catch (Exception e){
                Log.d(TAG, "answer : Error : "+ e);

            }

        });


        socket.on("candidate",args -> {
            try{
                JSONObject message = (JSONObject) args[0];
                Log.d(TAG, " -> candidate / message : "+message);
                String sender = message.getString("from");
                Log.d(TAG, " -> candidate :"+sender +"가  candidate을 보냄 ");
                PeerConnection peerConnection = (PeerConnection) socketId_PC.get(sender); //? 뭐지 peerConnection이 json에서 값으로 저장이 되나?
                Log.d(TAG, " -> candidate : peerConnection 생성및 초기화");
               IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                Log.d(TAG, "후보자 생성");
                peerConnection.addIceCandidate(candidate);
                Log.d(TAG, "새로운 후보자를 원격 후보에 추가해줌 ");
            }catch (Exception e){
                Log.d(TAG,"-> candidate : " + e);
            }
        });

    }

    private void connectToSignallingServer() {
        try {

            String URL = "http://3.39.153.170:3000/"; // http://서버url :port번호 형식으로 작성
            Log.d(TAG, "REPLACE ME: IO Socket:" + URL);
            socket = IO.socket(URL);
            socket.connect();
            Log.d(TAG, "소켓 connect 연결 시도");

            SocketEventListener(); // 생성된 소켓의 연결 상태를 알려주는 역할

            socket.on(EVENT_CONNECT, args -> {
                Log.d(TAG, "소켓 연결 됨 args : "+args);
                if(!reConnect){
                    Log.d(TAG, "소켓 처음 연결 되었을 때 ");
                    socket.emit("first_connect");
                }
            }).on("requestUserData", args -> {
                Log.d(TAG, "-->requestUserData ");

                mySocketId = (String) args[0];
                Log.d(TAG, " --> requestUserData : mySocketId : "+ mySocketId);
                Log.d(TAG, "-->requestUserData ");
                socket.emit("create or join", "foo");
                Log.d(TAG, "<-- create or join 시그널링 서버에게 해당 방의 참여자인지 생성자인지 물어봄 & 인자로 방 식별자(방이름)을 보냄");
            }).on("created", args -> {
                Log.d(TAG, " --> created : 방을 생성한 사람일 때");
//                mySocketId = (String) args[1];
                Log.d(TAG, " --> created : mySocketId : "+ mySocketId);

                videoTrackFromCamera.addRenderer(new VideoRenderer(binding.hostView));

                Log.d(TAG, " --> created : 비디오 렌더러를 추가하여 비디오 트랙에서 비디오를 가져와  hostView 에 세팅해줌");
                hostProgressBarHandler.sendEmptyMessage(0);
                Log.d(TAG, " --> created : progressBar Gone 시켜줌");
                isInitiator = true; // host임을 나타내는 변수
                Log.d(TAG, " --> created : hostView가 active 여부 확인 : " + binding.hostView.isActivated());
                hostView= true;

            }).on("full", args -> {
                Log.d(TAG, "--> full : 방의 제한 인원수가 꽉 차서 못들어 감");
            }).on("join", args -> {
                Log.d(TAG, "--> join 새로운 유저 "+args[1]+"가" +args[0] +"방에 입장했음");
                userList.add((String) args[1]);
                Log.d(TAG, "--> join : 유저리스트 갱신 해줌 userList : "+userList);

            }).on("joined", args -> {
                Log.d(TAG, "--> joined  "+ Arrays.toString(args) + "방에 입장 완료됨" +args[0] +args[1]); // 배열형태로 오는지 확인해보기 위해서
                // 기존에 이미 있었던 유저목록 받음
                mySocketId = (String) args[1];
                hostSocketId = (String) args[3];
                Log.d(TAG, " --> joined : mySocketId : "+ mySocketId);
                Log.d(TAG, " --> joined : hostSocketId  : "+ hostSocketId);
                Log.d(TAG, " --> joined : 참여한 방의 유저 리스트  : "+ args[2]);
                JSONArray args2 = (JSONArray) args[2];
                try {
                    for(int i=0; i<args2.length();i++){
                        userList.add(args2.getString(i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, " --> joined : userList : "+ userList);

                socket.emit("got_user_media", roomName); // media 보낼 준비 완료
                Log.d(TAG, " --> joined : 방 ㅊ");


            }).on("log", args -> {
                for (Object arg : args) {
                    Log.d(TAG, "connectToSignallingServer: " + String.valueOf(arg));
                }
            }).on(EVENT_DISCONNECT, args -> {
                Log.d(TAG, "connectToSignallingServer: disconnect");
            });
        } catch (URISyntaxException e) {
            Log.d(TAG, "connectToSignallingServer: error : "+e);
            e.printStackTrace();
        }
    }
//MirtDPM4
    private void doAnswer(String roomName, String receiver, PeerConnection peerConnection) {
        Log.d(TAG, "doAnswer 함수 : Answer 생성하는 역할");

        Log.d(TAG, "doAnswer : json을 사용하여 socketId(키)-peerConnection(값) 형태로 저장해줌");

        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "doAnswer : Answer 성공적으로 생성됨");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.d(TAG, "doAnswer : setLocalDescription");
                JSONObject message = new JSONObject();
                try {
//                    candidateReceiver = receiver;
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    message.put("to", receiver);
                    message.put("from", mySocketId);
                    message.put("room", roomName);
                    Log.d(TAG, "doAnswer : answer 정보 담아서 sendMessage 호출");
                    socket.emit("answer",message);
                    Log.d(TAG, "doAnswer : answer 소켓 이벤트 생성");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }


    private void doCall(String room, String receiver) throws JSONException {
        Log.d(TAG, "doCall 함수 :  Offer 생성하는 역할 " +room +"방의" +receiver+"에게 offer 보낼거야");
        PeerConnection peerConnection = initializePeerConnections(receiver);
        peerConnection.addStream(mediaStream);

        socketId_PC.put(receiver,peerConnection); // 1) answer 받고 나서 pc.setRemoteDescription() 2) candidate 받고나서 pc.addIceCandidate() / 1,2 해주기 위해서 객체에 pc 저장함
        Log.d(TAG, "doCall : json을 사용하여 socketId(키)-peerConnection(값) 형태로 저장해줌");
        Log.d(TAG, "doCall : socketId_PC : "+socketId_PC);
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        // 만약 OfferToReceiveAudio 값이 false 이면 remotePeer는 audio 관련 정보를 보내지 않는다.
        Log.d(TAG, "doCall : remotePeer 가 audio와 OfferToReceiveAudiovideo를 보낼 수 있도록 설정");

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "doCall : Offer 가 성공적으로 생성이 되면");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.d(TAG, "doCall : 생성된 Offer localDescription 세팅 ");
//                candidateReceiver = receiver;
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    message.put("to", receiver);
                    message.put("from", mySocketId);
                    message.put("room", room);
                    // Json 형태로 offer 형태의 sdp 보냄
                    socket.emit("offer",message);
                    Log.d(TAG, "doCall : offer 소켓 이벤트 생성");
//                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);

        Log.d(TAG, "doCall : Offer 생성함");

    }


    private void initializeSurfaceViews() {
        Log.d(TAG, "initializeSurfaceViews() 역할 : 비디오 렌더링 환경 설정");
        rootEglBase = EglBase.create(); //?????
        Log.d(TAG, "initializeSurfaceViews() : EglBase 생성");
        binding.hostView.init(rootEglBase.getEglBaseContext(), null); // surfaceView 초기화
        Log.d(TAG, "initializeSurfaceViews() : host의 surfaceViewRender 초기화");
        binding.hostView.setEnableHardwareScaler(true);
        Log.d(TAG, "initializeSurfaceViews() : 비디오 프레임 크기 조정 설정 가능하게 함");
        binding.hostView.setMirror(true);
        Log.d(TAG, "initializeSurfaceViews() : 거울 효과를 주어 좌우 반전 효과 설정");

        binding.surfaceView1.init(rootEglBase.getEglBaseContext(), null);
        Log.d(TAG, "initializeSurfaceViews() : 상대방1의 surfaceViewRender 초기화");
        binding.surfaceView1.setEnableHardwareScaler(true);
        binding.surfaceView1.setMirror(true);
        Log.d(TAG, "initializeSurfaceViews() : 상대방 비디오 크기 설정 및 거울 효과 세팅 완료");

        binding.surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        Log.d(TAG, "initializeSurfaceViews() : 상대방2의 surfaceViewRender 초기화");
        binding.surfaceView2.setEnableHardwareScaler(true);
        binding.surfaceView2.setMirror(true);
        Log.d(TAG, "initializeSurfaceViews() : 상대방 비디오 크기 설정 및 거울 효s과 세팅 완료");

        binding.surfaceView3.init(rootEglBase.getEglBaseContext(), null);
        Log.d(TAG, "initializeSurfaceViews() : 상대방3의 surfaceViewRender 초기화");
        binding.surfaceView3.setEnableHardwareScaler(true);
        binding.surfaceView3.setMirror(true);
        Log.d(TAG, "initializeSurfaceViews() : 상대방 비디오 크기 설정 및 거울 효과 세팅 완료");


    }

    private void initializePeerConnectionFactory() {
        Log.d(TAG, "initializePeerConnectionFactory 역할 : PeerConnectionFactory 생성 하드웨어 가속화 설정");
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        // 인자 : context, 오디오 초기화, 비디오초기화, 비디오하드웨어 가속화(비디오 처리 속도 향상시킴)
        Log.d(TAG, "initializePeerConnectionFactory() : context, 오디오 초기화, 비디오초기화, 비디오하드웨어 가속화");

        factory = new PeerConnectionFactory(null);
        // PeerConnectionFatory는 클라이언트용  PeerConnectin API의 주 진입점이다.
        Log.d(TAG, "initializePeerConnectionFactory() : PeerConnetionFactory 생성");
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
        Log.d(TAG, "initializePeerConnectionFactory() : 하드웨어 가속화(비디오 처리 속도 향상 위해) 설정");
        // 인자 : 나의 EGlBase context , 원격 피어의 EGLBase context
    }

    private void createVideoTrackFromCameraAndShowIt() {
        Log.d(TAG, "createVideoTrackFromCameraAndShowIt 역할 : 카메라에서 비디오 정보를 컴퓨터가 읽거나 사용할 수 있는 형태로 가져오는 과정");

        audioConstraints = new MediaConstraints(); // 비디오 및 오디오 제약 조건을 지정하는 역할
        cameraVideoCapturer = createVideoCapturer();
        Log.d(TAG, "createVideoTrackFromCameraAndShowIt () : VideoCapturer 생성");
        VideoSource videoSource = factory.createVideoSource(cameraVideoCapturer);
        Log.d(TAG, "createVideoTrackFromCameraAndShowIt () : Capturer로부터 videoSource 생성");
        cameraVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
        Log.d(TAG, "createVideoTrackFromCameraAndShowIt () : 캡처 시작 & 캡처할 비디오의 가로, 세로 해상도 초당 프레임 설정");
        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        // 인자 :  비디오 트랙 식별자, 비디오 소스
        Log.d(TAG, "createVideoTrackFromCameraAndShowIt () : 비디오 트랙 생성");
        videoTrackFromCamera.setEnabled(true);// enable 속성은 track 이 source stream을 렌더링 할 수 있는지 여부를 나타내준다.
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);
        Log.d(TAG, "createVideoTrackFromCameraAndShowIt () : videoTrack 생성");


    }

    private PeerConnection initializePeerConnections(String candidateReceiver) {
        Log.d(TAG, "initializePeerConnections 역할 : peerConnection 생성");
        PeerConnection peerConnection = createPeerConnection(factory,candidateReceiver);
        Log.d(TAG, "initializePeerConnections () : peerConnection 생성 완료");
        return peerConnection;
    }

    private void startStreamingVideo() {
        while(true) {
            Log.d(TAG, "startStreamingVideo  : 소켓 연결됨?");

            if(socket.connected()) {
                Log.d(TAG, "startStreamingVideo 역할 : 자신의 mediaStream 생성");
                mediaStream = factory.createLocalMediaStream("ARDAMS");
                Log.d(TAG, "startStreamingVideo () : MediaStream 생성");
                mediaStream.addTrack(videoTrackFromCamera);
                Log.d(TAG, "startStreamingVideo () : 비디오 트랙 추가");
                mediaStream.addTrack(localAudioTrack);
                Log.d(TAG, "startStreamingVideo () : 오디오 트랙 추가");
//                socket.emit("got_user_media", roomName); // media 보낼 준비 완료
//                Log.d(TAG, "startStreamingVideo () : media 보낼 준비 되었다고 signallingServer에게 신호 보냄");
                break;

            }
        }

    }


    private PeerConnection createPeerConnection(PeerConnectionFactory factory,String candidateReceiver) { //Peer 생성 및 turn, stun 서버 구축 과정
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        String URL = "stun:stun.l.google.com:19302";
        iceServers.add(new PeerConnection.IceServer(URL));
        String TurnURL ="turn:3.39.153.170";
        String userName ="choi";
        String TurnPW ="choiWebrtc";
        iceServers.add(new PeerConnection.IceServer(TurnURL,userName,TurnPW));
        Log.d(TAG, "Turn 서버 생성 및 추가");


        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: "+signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: "+ iceConnectionState);
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: "+ iceGatheringState );

            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);
                    message.put("from",mySocketId);
                    message.put("to",candidateReceiver);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    socket.emit("candidate",message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {

                Log.d(TAG, "addStream을 했을 때 실행되는 함수 ");
                Log.d(TAG, "onAddStream: hostSocketId : " + hostSocketId +"candidateReceiver : " + candidateReceiver );
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                socketID_mediaStream.put(candidateReceiver,mediaStream);
                socketID_remoteVideo.put(candidateReceiver,remoteVideoTrack);
                socketID_remoteAudio.put(candidateReceiver,remoteAudioTrack);



                if(isInitiator){ // 방 생성자 host 인 경우

                    if (!userView1){
                        userView1 =true;
                        visibleDisplayHandler.sendEmptyMessage(1);
                        Log.d(TAG, "onAddStream: " + " host방의 surfaceView1 Renderer 추가  : "+binding.surfaceView1.isActivated());
                        remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView1));
                        Log.d(TAG, "onAddStream: " + " host방의 surfaceView1 Renderer 추가  후 : "+binding.surfaceView1.isActivated());
                        socketID_SurfaceView.put(candidateReceiver,1);
                    }
                    else if (!userView2){
                        userView2 =true;
                        visibleDisplayHandler.sendEmptyMessage(2);
                        Log.d(TAG, "onAddStream: " + " host방의 surfaceView2 Renderer 추가  : `"+binding.surfaceView2.isActivated());
                        remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));
                        Log.d(TAG, "onAddStream: " + " host방의 surfaceView2 Renderer 추가 후 : "+binding.surfaceView2.isActivated());
                        socketID_SurfaceView.put(candidateReceiver,2);

                    }
                    else if (!userView3){
                        userView3 =true;
                        visibleDisplayHandler.sendEmptyMessage(3);
                        Log.d(TAG, "onAddStream: " + " host방의 surfaceView3 Renderer 추가  : "+binding.surfaceView3.isActivated());
                        remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView3));
                        Log.d(TAG, "onAddStream: " + " host방의 surfaceView2 Renderer 추가 후 : "+binding.surfaceView2.isActivated());
                        socketID_SurfaceView.put(candidateReceiver,3);
                    }
                }

                else {

                    if (candidateReceiver.equals(hostSocketId) && !hostView) {
                        hostView = true;
                        Log.d(TAG, "onAddStream: " + " candidateReceiver 변수와 hostSocketId 가 같고 hostView false 일 때 ");
                        remoteVideoTrack.addRenderer(new VideoRenderer(binding.hostView));
                        Log.d(TAG, " --> created : 비디오 렌더러를 추가하여 비디오 트랙에서 비디오를 가져와  hostView 에 세팅해줌");
                        hostProgressBarHandler.sendEmptyMessage(0);
                        visibleDisplayHandler.sendEmptyMessage(4);
                        videoTrackFromCamera.addRenderer(new VideoRenderer(binding.surfaceView1));
                        Log.d(TAG, "onAddStream:  hostView.isActivated 확인" + binding.hostView.isActivated());
                        socketID_SurfaceView.put(candidateReceiver,0);

                    } else if (!userView2) {
                        userView2 = true;
                        visibleDisplayHandler.sendEmptyMessage(5);
                        Log.d(TAG, "onAddStream: " + " surfaceView2 Renderer 추가  : " + binding.surfaceView2.isActivated());
                        remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));
                        Log.d(TAG, "onAddStream: " + " surfaceView2 Renderer 추가 후 : " + binding.surfaceView2.isActivated());
                        socketID_SurfaceView.put(candidateReceiver,2);
                    } else if (!userView3) {
                        userView3 = true;
                        visibleDisplayHandler.sendEmptyMessage(6);
                        Log.d(TAG, "onAddStream: " + " surfaceView3 Renderer 추가  : " + binding.surfaceView3.isActivated());
                        remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView3));
                        Log.d(TAG, "onAddStream: " + " surfaceView2 Renderer 추가 후 : " + binding.surfaceView2.isActivated());
                        socketID_SurfaceView.put(candidateReceiver,3);
                    }

                }
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        }; //협상 이벤트 리스너

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }



    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
            Log.d(TAG, "카메라 캡처 이벤트 핸들러 - onFirstFrameAvailable 뭔데");

        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames(); // 사용 가능한 장치 목록 반환
        CameraVideoCapturer.CameraEventsHandler CEH = new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(String s) {
                Log.d(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 에러 :  "+s);
            }

            @Override
            public void onCameraDisconnected() {
                Log.d(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 disConnect");
            }

            @Override
            public void onCameraFreezed(String s) {
                Log.d(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 정지");
            }

            @Override
            public void onCameraOpening(String s) {
                Log.d(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 열리는 중");
            }

            @Override
            public void onFirstFrameAvailable() {
                Log.d(TAG, "카메라 캡처 이벤트 핸들러 - onFirstFrameAvailable 뭔데");
            }

            @Override
            public void onCameraClosed() {
                Log.d(TAG, "카메라 캡처 이벤트 핸들러 - 카메라 닫힘");
            }
        };

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) { //카메라가 앞이면
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, CEH);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) { // 카메라가 뒤이면 변수를 CameraVideoCapturer로 해줌 (VideoCapturer을 상속받은 클래스임)
                CameraVideoCapturer cameraVideoCapturer = enumerator.createCapturer(deviceName, CEH);
                if (cameraVideoCapturer != null) {
                    return cameraVideoCapturer;
                }
            }
        }

        return null;
    }



    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
        //isSupported : api 지원 여부 확인 및 모든 카메라가 기존 지원보다 나은지 확인한다.
    }

}
