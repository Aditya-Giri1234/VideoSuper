package com.example.videosuper.repository

import android.content.Context
import android.util.Log
import com.example.videosuper.firebase.FirebaseClient
import com.example.videosuper.model.DataModel
import com.example.videosuper.util.Constant.DataTypeModel
import com.example.videosuper.util.GiveOne
import com.example.videosuper.callBack.NewEventCallBack
import com.example.videosuper.callBack.StatusCallback
import com.example.videosuper.callBack.SuccessCallback
import com.example.videosuper.util.Constant.CallStatus
import com.example.videosuper.model.Message
import com.example.videosuper.util.Constant.UserStatus
import com.example.videosuper.webRtc.MyPeerConnection
import com.example.videosuper.webRtc.WebRtcClient
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class MainRepository:WebRtcClient.Listener {
    var firebaseClient:FirebaseClient?=null

    lateinit var remoteView:SurfaceViewRenderer
     var gson:Gson
    lateinit var listener:Listener
    var webRtcClient:WebRtcClient?=null





    init {
        firebaseClient= GiveOne.getInstanceOfFire()
        gson=Gson()

    }




    fun login(username:String,time:String, context: Context, callback:SuccessCallback){
        firebaseClient?.login(username,time, object :SuccessCallback {
            override fun onSuccess(message: Message?) {

                Log.e("check","main repo - Login - username - $username")
                webRtcClient= WebRtcClient(context,object :MyPeerConnection(){
                    override fun onAddStream(mediaStream: MediaStream?) {
                        super.onAddStream(mediaStream)
                        Log.e("check","main repo - login - webRtc initialize constructor - onAddStream - $mediaStream")
                        try{
                            mediaStream?.videoTracks?.get(0)?.addSink(remoteView)
                        }
                        catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                        super.onConnectionChange(newState)
                        Log.e("check","main repo - login - webRtc initialize constructor - onConnectionChange - $newState")
                        if(newState==PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
                            listener.webRtcConnected()

                        }
                        if(newState==PeerConnection.PeerConnectionState.CLOSED||newState==PeerConnection.PeerConnectionState.DISCONNECTED){
                            if(listener!=null){
                                listener.webRtcClosed()
                            }
                        }
                    }

                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        Log.e("check","main repo - login - webRtc initialize constructor - onIceCandidate - $p0 - ${GiveOne.getTarget()} ")
                        webRtcClient?.sendIceCandidate(p0!!, GiveOne.getTarget()!!, GiveOne.getRoomId()!!)
                    }
                },username)
                webRtcClient?.listener=this@MainRepository
                callback.onSuccess()
            }

        })

    }

    fun initLocalView(view:SurfaceViewRenderer){
        Log.e("check","main repo - initLocalView - ${view.id}")
        webRtcClient?.initLocalSurfaceView(view)
    }
    fun initRemoteView(view:SurfaceViewRenderer){
        Log.e("check","main repo - initRemoteView - ${view.id}")
        webRtcClient?.initRemoteSurfaceView(view)
        remoteView=view
    }

    fun startCall(){
        Log.e("check","main repo - startCall - sender is ${GiveOne.getUserName()} -target is ${GiveOne.getTarget()}")
        webRtcClient?.call()
    }
    fun switchCamera(){
        webRtcClient?.switchCamera()
    }
    fun toggleVideo(isVideoOn:Boolean){
        webRtcClient?.toggleVideo(isVideoOn)
    }
    fun toggleAudio(isAudioOn:Boolean){
        webRtcClient?.toggleAudio(isAudioOn)
    }
    fun endCall(){
        webRtcClient?.closeConnection()
    }
    fun sendCallRequest(){
        Log.e("check","main repo - sendCallRequest - sender is ${GiveOne.getUserName()} -target is ${GiveOne.getTarget()}")
        firebaseClient?.sendMessageToOtherUser(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,null,CallStatus.RequestCall,null))
    }


    fun subscribeForLatestEvent(successCallback: SuccessCallback){
        Log.e("check","main repo - subscribe latest event - webRtc is null ${webRtcClient==null} ")
        firebaseClient?.observerInComingEvent(object :NewEventCallBack{
            override fun onNewEventReceived(dataModel: DataModel,roomId1: String) {
                when(dataModel.type){
                    // For Receiver Use
                    DataTypeModel.Offer ->{
                        Log.e("check","main repo - subscribe latest event- firebase observer in coming event - Offer - $dataModel")
                        webRtcClient?.onRemoteSessionReceived(SessionDescription(SessionDescription.Type.OFFER,dataModel.data))
                        webRtcClient?.answer()
                    }
                    //For Sender Use
                    DataTypeModel.Answer->{
                        Log.e("check","main repo - subscribe latest event - firebase observer in coming event - Answer - $dataModel")

                        webRtcClient?.onRemoteSessionReceived(SessionDescription(SessionDescription.Type.ANSWER,dataModel.data))
                    }
                    // For Both Use
                    DataTypeModel.IceCandidate->{
                        try{
                            Log.e("check","main repo - subscribe latest event - firebase observer in coming event - IceCandidate - $dataModel")
                            val iceCandidate=gson.fromJson(dataModel.data,IceCandidate::class.java)
                            webRtcClient?.addIceCandidate(iceCandidate)
                            successCallback.onSuccess(null)


                        }
                        catch(e:Exception){
                            e.printStackTrace()
                        }
                    }

                }
            }

        })
    }

    override fun onTransferDataToOtherPeer(model: DataModel,roomId: String) {
        Log.e("check","main repo - onTransfer - $model")
            firebaseClient?.dataTransfer(model,roomId)
    }


    fun initWeb(context:Context,username: String){
        if(webRtcClient==null){
            webRtcClient= WebRtcClient(context,object :MyPeerConnection(){
                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.e("check","main repo - init - webRtc initialize constructor - onAddStream - $p0")
                    try{
                        p0?.videoTracks?.get(0)?.addSink(remoteView)
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                }


                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)
                    Log.e("check","main repo - init - webRtc initialize constructor - onConnectionChange - $newState")
                    if(newState==PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
                        listener.webRtcConnected()

                    }
                    if(newState==PeerConnection.PeerConnectionState.CLOSED||newState==PeerConnection.PeerConnectionState.DISCONNECTED){
                        if(listener!=null){
                            listener.webRtcClosed()
                        }
                    }
                }

                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    Log.e("check","main repo - inti - webRtc initialize constructor - onIceCandidate - $p0 - ${GiveOne.getTarget()} ")
                    webRtcClient?.sendIceCandidate(p0!!, GiveOne.getTarget()!!, GiveOne.getRoomId()!!)
                }
            },username)
            webRtcClient?.listener=this@MainRepository
        }
    }
    fun isTargetPresent(target:String):Boolean{
        return firebaseClient?.isTargetPresent(target)!!
    }

    fun getUsers() {
        firebaseClient?.getUsers()
    }

    fun subscribeSelfEvent(successCallback: SuccessCallback) {
        firebaseClient?.subscribeSelfEvent(object:SuccessCallback{


            override fun onSuccess(message: Message?) {
                if(message!=null){
                    when(message.callStatus){

                        //For Sender Side
                        CallStatus.CreateCall->{
                            successCallback.onSuccess(message)
                        }
                        //Receiver Side
                        CallStatus.RequestCall->{
                            firebaseClient?.checkMyStatus(object :StatusCallback{
                                override fun onFree() {
                                    successCallback.onSuccess(message)

                                }

                                override fun onBusy() {
                                    firebaseClient?.sendRejectCall(UserStatus.Busy)
                                }

                                override fun onNotAvailable() {
                                    firebaseClient?.sendRejectCall(UserStatus.NotAvailable)
                                }

                            })
                        }
                        //For Receiver Side

                        CallStatus.CancelCall->{
                            firebaseClient?.resetMe()
                            successCallback.onSuccess(message)
                        }

                        //Sender side
                        CallStatus.AcceptCall->{
                               firebaseClient?.createRoomIdAndSend()
                            successCallback.onSuccess(message)
                        }
                        //Receiver Side
                        CallStatus.SendRoomId->{
                                GiveOne.setRoomId(message.roomId)
                            firebaseClient?.acceptRoomId()
                        }

                        //Sender Side
                        CallStatus.AcceptRoomId->{
                            firebaseClient?.createRoom()

                        }
                        // Receiver Side
                        CallStatus.CreateRoom->{
                            firebaseClient?.joinRoom()
                            successCallback.onSuccess(message)
                        }
                        // For Sender Side
                        CallStatus.JoinRoom->{
                            successCallback.onSuccess(message)
                        }
                        //For Sender Side
                        CallStatus.RejectCall->{
                            firebaseClient?.resetMe()
                     successCallback.onSuccess(message)

                        }
                        CallStatus.Disconnect->{

                                successCallback.onSuccess(message)
                        }

                    }
                }


        }
        })
    }


    interface Listener{
        fun webRtcConnected()
        fun webRtcClosed()
    }

}