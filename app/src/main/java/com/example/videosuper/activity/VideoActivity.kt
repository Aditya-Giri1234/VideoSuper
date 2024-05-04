package com.example.videosuper.activity

import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isGone
import com.example.videosuper.R
import com.example.videosuper.databinding.ActivityVideoBinding
import com.example.videosuper.firebase.FirebaseClient
import com.example.videosuper.util.GiveOne
import com.example.videosuper.model.Preference
import com.example.videosuper.repository.MainRepository
import com.example.videosuper.callBack.SuccessCallback
import com.example.videosuper.model.Message
import com.example.videosuper.util.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoActivity : AppCompatActivity(),MainRepository.Listener {
    lateinit var binding:ActivityVideoBinding
    private lateinit var mainRepository: MainRepository
    lateinit var firebaseClient: FirebaseClient
    var isMute: Boolean = false
    var isSpeakerOn = true
    var isVideoOn = true

    private lateinit var audioManager: AudioManager
    lateinit var pref: Preference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainRepository = GiveOne.getInstanceOfRepo()
        pref = Preference(this)
        firebaseClient= GiveOne.getInstanceOfFire()

        mainRepository.initLocalView(binding.localView)
        mainRepository.initRemoteView(binding.remoteView)
        mainRepository.listener = this@VideoActivity




        mainRepository.subscribeSelfEvent(object : SuccessCallback {
            override fun onSuccess(message: Message?) {
                // For Receiver Side
                if(message?.callStatus== Constant.CallStatus.CreateRoom){
//                    mainRepository.startCall()
                    mainRepository.subscribeForLatestEvent(object :SuccessCallback{
                        override fun onSuccess(message: Message?) {
                            binding.remoteViewLoading.isGone=true
                        }

                    })
                }
                // For Sender Side
                if(message?.callStatus== Constant.CallStatus.JoinRoom){
                    mainRepository.startCall()
                    mainRepository.subscribeForLatestEvent(object :SuccessCallback{
                        override fun onSuccess(message: Message?) {
                            binding.remoteViewLoading.isGone=true
                        }

                    })
                }
                if(message?.callStatus== Constant.CallStatus.Disconnect){
                    firebaseClient.dbRef.child("Rooms").child(GiveOne.getRoomId()!!).child(GiveOne.getUserName()!!).child(FirebaseClient.LATEST_EVENT_FIELD_NAME).removeEventListener(
                        GiveOne.latestListener!!)
                    mainRepository.endCall()
                }
            }

        })




        binding.cameraSwitch.setOnClickListener {
            mainRepository.switchCamera()
        }

        binding.muteMic.setOnClickListener {
            isMute = !isMute
            if (isMute) {
                binding.muteMic.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
                binding.muteMic.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            mainRepository.toggleAudio(isMute)

        }
        binding.speakerOn.setOnClickListener {

            isSpeakerOn = !isSpeakerOn
            audioManager.isSpeakerphoneOn = isSpeakerOn
            if (isSpeakerOn) {
                binding.speakerOn.setImageResource(R.drawable.ic_baseline_speaker_up_24)
            } else {
                binding.speakerOn.setImageResource(R.drawable.ic_baseline_volume_off_24)
            }
        }


        binding.disableCamera.setOnClickListener {
            isVideoOn = !isVideoOn
            if (isVideoOn) {
                binding.disableCamera.setImageResource(R.drawable.ic_baseline_videocam_24)

            } else {
                binding.disableCamera.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            }
            mainRepository.toggleVideo(isVideoOn)

        }

        binding.endCall.setOnClickListener {
            firebaseClient.dbRef.child("Rooms").child(GiveOne.getRoomId()!!).child(GiveOne.getUserName()!!).child(FirebaseClient.LATEST_EVENT_FIELD_NAME).removeEventListener(
                GiveOne.latestListener!!)

            firebaseClient.sendDisconnect()
            mainRepository.endCall()
        }

    }

    override fun onStart() {
        super.onStart()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

    }




    override fun webRtcConnected() {
        Log.e("check", "VideoActivity - webRtcConnected ")
        CoroutineScope(Dispatchers.Main).launch {
            Log.e("check", "CallActivity - webRtcConnected - Coroutine")
            mainRepository.toggleAudio(isMute)
            mainRepository.toggleVideo(isVideoOn)
            audioManager.isSpeakerphoneOn = isSpeakerOn
        }
    }

    override fun webRtcClosed() {
        Log.e("check", "VideoActivity - webRtcClosed")


        firebaseClient.dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("status").setValue(
            Constant.UserStatus.Free.name).addOnSuccessListener{
            firebaseClient.dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("message").removeValue().addOnSuccessListener{
                Log.e("check","VideoActivity - onDestroy ")
                finish()
            }
        }

    }
}