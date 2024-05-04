package com.example.videosuper.webRtc

import android.content.Context
import android.util.Log
import com.example.videosuper.model.DataModel
import com.example.videosuper.util.Constant
import com.example.videosuper.util.GiveOne
import com.google.gson.Gson
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.lang.IllegalStateException

class WebRtcClient(var context: Context,var observer: Observer,var username:String) {
    private val eglBaseContext: EglBase.Context =EglBase.create().eglBaseContext
    lateinit var peerConnectionFactory: PeerConnectionFactory
    lateinit var peerConnection: PeerConnection
    private var iceServer:List<PeerConnection.IceServer>  = ArrayList()
    private lateinit var videoCapturer: CameraVideoCapturer
    lateinit var localVideoSource:VideoSource
    lateinit var localAudioSource: AudioSource
    val localTrackId:String="local_track"
    val localStreamId:String="local_stream"
    lateinit var localVideoTrack:VideoTrack
    lateinit var localAudioTrack:AudioTrack
    lateinit var localStream:MediaStream
    lateinit var mediaConstraints: MediaConstraints
    lateinit var mediaConstraintsAudio: MediaConstraints
    lateinit var listener:Listener
    lateinit var gson: Gson
    init {
        mediaConstraintsAudio= MediaConstraints()
        mediaConstraintsAudio.mandatory.addAll(
            listOf(
                MediaConstraints.KeyValuePair("googEchoCancellation", "true"),
                MediaConstraints.KeyValuePair("googAutoGainControl", "true"),
                MediaConstraints.KeyValuePair("googHighpassFilter", "true"),
                MediaConstraints.KeyValuePair("googNoiseSuppression", "true"),
                MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"),
            )
        )
        initPeerConnectionFactory()
        peerConnectionFactory=createPeerConnectionFactory()
        gson=Gson()
        iceServer=iceServer.plus(PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp").setUsername("83eebabf8b4cce9d5dbcb649").setPassword("2D7JvfkOQtBdYW3R").createIceServer())
        peerConnection=createPeerConnection(observer)
        mediaConstraints= MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        localVideoSource=peerConnectionFactory.createVideoSource(false)
        localAudioSource=peerConnectionFactory.createAudioSource(mediaConstraintsAudio)
    }

    // initialize peer connection section
    private fun initPeerConnectionFactory(){
        var options=PeerConnectionFactory.InitializationOptions.builder(context).setFieldTrials("WebRTC-H264HighProfile/Enabled/").setEnableInternalTracer(true).createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }


    private fun createPeerConnectionFactory():PeerConnectionFactory{
        val options=PeerConnectionFactory.Options()
        options.let {
            it.disableEncryption=false
            it.disableNetworkMonitor=false

        }

        return PeerConnectionFactory.builder().setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext,true,true)).setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext)).setOptions(options).createPeerConnectionFactory()
    }
    private fun createPeerConnection(observer:Observer):PeerConnection{
        return peerConnectionFactory.createPeerConnection(iceServer,observer)!!
    }

    // initialized ui like surface view renderers

    private fun initSurfaceViewRendered(viewRenderer:SurfaceViewRenderer){
        viewRenderer.setEnableHardwareScaler(true)
        viewRenderer.setMirror(true)
        viewRenderer.setZOrderMediaOverlay(true)
        viewRenderer.init(eglBaseContext,null)

    }

    fun initLocalSurfaceView(view:SurfaceViewRenderer){
        initSurfaceViewRendered(view)
        startLocalVideoStreaming(view)
    }

    private fun startLocalVideoStreaming(view: SurfaceViewRenderer) {

        Log.e("check"," WebRtcClient - start Local Video Streaming - current thread name is  ${Thread.currentThread().name}")
        val helper=SurfaceTextureHelper.create(Thread.currentThread().name,eglBaseContext)
        videoCapturer=getVideoCapturer()

        videoCapturer.initialize(helper,context,localVideoSource.capturerObserver)
        videoCapturer.startCapture(720,480,24)

        localVideoTrack=peerConnectionFactory.createVideoTrack(
            "${localTrackId}_video",localVideoSource
        )
        localVideoTrack.addSink(view)

        localAudioTrack=peerConnectionFactory.createAudioTrack(
            "${localTrackId}_audio",localAudioSource
        )

        localStream=peerConnectionFactory.createLocalMediaStream(localStreamId)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection.addStream(localStream)
    }

    private fun getVideoCapturer(): CameraVideoCapturer {
        val enumerator=Camera2Enumerator(context)
        val deviceNames=enumerator.deviceNames
        for(device in deviceNames){
            if(enumerator.isFrontFacing(device)){
                return enumerator.createCapturer(device,null)
            }
            if(enumerator.isBackFacing(device))
            {
                return enumerator.createCapturer(device,null)
            }
        }
        throw IllegalStateException("front facing camera not found")
    }

    fun initRemoteSurfaceView(view:SurfaceViewRenderer){
        initSurfaceViewRendered(view)
    }


    //negotiation section like call and answer

    fun call(){
        Log.e("check"," WebRtcClient - Call")
        try {
           peerConnection.createOffer(object :MySdpObserver(){
               override fun onCreateSuccess(p0: SessionDescription?) {
                   super.onCreateSuccess(p0)
                   Log.e("check"," WebRtcClient - Call - Create Offer")
                   peerConnection.setLocalDescription(object :MySdpObserver(){
                       override fun onSetSuccess() {
                           super.onSetSuccess()
                           Log.e("check"," WebRtcClient - Call - Create Offer - set Local desc")
                           // its time to transfer this sdp to other peer
                           if(listener!=null){
                               listener.onTransferDataToOtherPeer(DataModel(
                                   GiveOne.getTarget()!!,
                                   GiveOne.getUserName()!!,p0?.description, Constant.DataTypeModel.Offer),
                                   GiveOne.getRoomId()!!)
                           }else{
                               Log.e("check"," WebRtcClient - Call - Create Offer - set Local desc - error")
                           }
                       }
                   },p0)
               }
           },mediaConstraints)
        }
        catch (e:Exception){
            e.printStackTrace()
            Log.e("check"," WebRtcClient - Call - error")
        }
    }
    fun answer(){
        Log.e("check"," WebRtcClient - Call - Create Answer")
        try {
           peerConnection.createAnswer(object :MySdpObserver(){
               override fun onCreateSuccess(p0: SessionDescription?) {
                   super.onCreateSuccess(p0)
                   Log.e("check"," WebRtcClient - Call - Create Answer")
                   peerConnection.setLocalDescription(object :MySdpObserver(){
                       override fun onSetSuccess() {
                           super.onSetSuccess()
                           Log.e("check"," WebRtcClient - Call - Create Answer - set Local desc")
                           // its time to transfer this sdp to other peer
                           if(listener!=null){
                               listener.onTransferDataToOtherPeer(DataModel(GiveOne.getTarget()!!,username,p0?.description,
                                   Constant.DataTypeModel.Answer), GiveOne.getRoomId()!!)
                           }else{
                               Log.e("check"," WebRtcClient - Call - Create Answer - set Local desc - error")
                           }
                       }
                   },p0)
               }
           },mediaConstraints)
        }
        catch (e:Exception){
            e.printStackTrace()
            Log.e("check"," WebRtcClient - Call - Create Answer - error - ${e.message}")
        }
    }


    fun onRemoteSessionReceived(sessionDescription: SessionDescription)
    {
        Log.e("check"," WebRtcClient - onRemoteSessionReceived - ${sessionDescription.description} - ${sessionDescription.type}")
        peerConnection.setRemoteDescription(MySdpObserver(),sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate){
        Log.e("check"," WebRtcClient - addIceCandidate - $iceCandidate ")
        peerConnection.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(iceCandidate: IceCandidate,target:String,roomId: String){
        Log.e("check"," WebRtcClient - addIceCandidate - $iceCandidate  - target is $target")
        addIceCandidate(iceCandidate)

        if(listener!=null){

            listener.onTransferDataToOtherPeer(
                DataModel(target,username,gson.toJson(iceCandidate), Constant.DataTypeModel.IceCandidate),roomId
            )
        }
    }

    fun switchCamera(){
        videoCapturer.switchCamera(null)
    }
    fun toggleVideo(isVideoOn:Boolean){
        localVideoTrack.setEnabled(isVideoOn)
    }
    fun toggleAudio(isAudioOn:Boolean){
        if (isAudioOn){
            localStream.removeTrack(localAudioTrack)
        }else{
            localStream.addTrack(localAudioTrack)
        }
    }

    fun closeConnection(){
        Log.e("check"," WebRtcClient - closeConnection  ")
        try {
            localVideoTrack.dispose()
            localAudioTrack.dispose()
            videoCapturer.dispose()
            peerConnection.close()


        }
        catch (e:Exception){
            Log.e("check"," WebRtcClient - closeConnection - error - ${e.message} ")
            e.printStackTrace()
        }
    }

    fun updateUserName(){
        this.username= GiveOne.getUserName()!!
    }

    interface Listener{
        fun onTransferDataToOtherPeer(model:DataModel,roomId:String)
    }
}