package com.example.videosuper.activity

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videosuper.R
import com.example.videosuper.adapter.UserAdapter
import com.example.videosuper.databinding.ActivityDialBinding
import com.example.videosuper.firebase.FirebaseClient
import com.example.videosuper.model.AdapterUsers
import com.example.videosuper.util.GiveOne
import com.example.videosuper.model.Preference
import com.example.videosuper.repository.MainRepository
import com.example.videosuper.callBack.SuccessCallback
import com.example.videosuper.model.Message
import com.example.videosuper.util.Constant
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class DialActivity : AppCompatActivity() {
    lateinit var binding: ActivityDialBinding
    private lateinit var mainRepository: MainRepository
    lateinit var firebaseClient: FirebaseClient
    var list = ArrayList<AdapterUsers>()
    var listener:ValueEventListener?=null
     var sheet:BottomSheetDialog?=null


    lateinit var pref: Preference
    var gson= Gson()
    lateinit var connecting:MediaPlayer
    lateinit var callComming:MediaPlayer




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainRepository = GiveOne.getInstanceOfRepo()
        pref = Preference(this)
        GiveOne.setUserNameId(pref.getTime()!!)
        connecting=MediaPlayer.create(this,R.raw.ringing)
        callComming=MediaPlayer.create(this,R.raw.incomming)

        binding.textView.text = "Your username is ${pref.getUsername()}"

        Log.e("check", "CallActivity - onCreate - mainRepository reference id is  $mainRepository")
        firebaseClient = GiveOne.getInstanceOfFire()



        firebaseClient.dbRef.child("Users").child(pref.getTime()!!).child("status").setValue(
            Constant.UserStatus.Free.name)

        // set adapter and show user
        mainRepository.getUsers()







        // get all user
        mainRepository.subscribeSelfEvent(object :SuccessCallback{
            override fun onSuccess(message: Message?) {
                //Sender Side
                if(message?.callStatus== Constant.CallStatus.CreateCall){
                   playConnecting()

                    Log.e("media"," connecting is playing ${connecting.isPlaying}")
                    binding.connectingLayout.isGone=false
                    binding.whoToCallLayout.isGone=true
                    if(sheet!=null){
                        sheet!!.dismiss()
                        sheet=null
                    }
                    binding.cancelCall.setOnClickListener{
                        connecting.pause()
                        firebaseClient.resetMe()
                        firebaseClient.sendCancelCall()
                        binding.whoToCallLayout.isGone=false
                        binding.connectingLayout.isGone=true
                    }
                }
                //Receiver Side
                if(message?.callStatus== Constant.CallStatus.RequestCall){
                    playCallComing()
                    GiveOne.setTarget(message?.sender)
                    GiveOne.setTargetId(message?.senderId)
                    binding.incomingCallLayout.isGone=false
                    binding.incomingNameTV.text="${message?.sender} is calling "

                    binding.acceptButton.setOnClickListener{
                        callComming.pause()
                        binding.incomingCallLayout.isGone=true
                        firebaseClient.acceptCall()
                        startActivity(Intent(this@DialActivity,VideoActivity::class.java))
                    }
                    binding.rejectButton.setOnClickListener{
                        callComming.pause()
                        binding.incomingCallLayout.isGone=true
                        firebaseClient.sendRejectCall(Constant.UserStatus.RejectByUser)
                        firebaseClient.resetMe()
                    }

                }
                //Sender side
                if(message?.callStatus== Constant.CallStatus.AcceptCall){
                    connecting.pause()
                    binding.connectingLayout.isGone=true
                    startActivity(Intent(this@DialActivity,VideoActivity::class.java))
                }
                //For Receiver Use
                if(message?.callStatus== Constant.CallStatus.CancelCall){
                    binding.incomingCallLayout.isGone=true
                    // stop ringtone here
                    callComming.pause()
                }
                //Sender Side
                if(message?.callStatus== Constant.CallStatus.RejectCall){
                    connecting.pause()
                    binding.connectingLayout.isGone=true
                    if(message?.rejectReason== Constant.UserStatus.Busy){
                        Toast.makeText(this@DialActivity,"${GiveOne.getTarget()} is busy on other call !",Toast.LENGTH_SHORT).show()
                    }
                    if(message?.rejectReason== Constant.UserStatus.NotAvailable){
                        Toast.makeText(this@DialActivity,"${GiveOne.getTarget()} is not available !",Toast.LENGTH_SHORT).show()
                    }
                    if(message?.rejectReason== Constant.UserStatus.RejectByUser){

                            Toast.makeText(this@DialActivity,"${GiveOne.getTarget()} is rejected your call !",Toast.LENGTH_SHORT).show()



                    }

                    GiveOne.setTarget(null)
                    GiveOne.setTargetId(null)
                    binding.whoToCallLayout.isGone=false


                }

            }

        })





        binding.callBtn.setOnClickListener {
            if(binding.targetUserNameEt.text.isEmpty()){
                binding.targetUserNameEt.error = "Please enter username !"
                return@setOnClickListener
            }
            if (mainRepository.isTargetPresent(binding.targetUserNameEt.text.toString().trim())){
                GiveOne.setRoomId(pref.getRoomId())
                GiveOne.setTarget(binding.targetUserNameEt.text.toString().trim())

                mainRepository.sendCallRequest()

            }
            else{
                Toast.makeText(this@DialActivity,"${binding.targetUserNameEt.text} is can't find !",Toast.LENGTH_SHORT).show()
            }



        }












        binding.btnChange.setOnClickListener {
            val popupMenu = PopupMenu(this@DialActivity, binding.btnChange)

            popupMenu.menuInflater.inflate(R.menu.pop_up_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.pmChangeUserName -> {
                        changeUserName()
                    }

                    R.id.pmSearchUser -> {
                        openSearchView()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }


    override fun onResume() {
        binding.whoToCallLayout.isGone=false
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopConnecting()
      stopCallComing()
        firebaseClient.dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("status").setValue(
            Constant.UserStatus.NotAvailable.name).addOnSuccessListener{
            firebaseClient.dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("message").removeValue().addOnSuccessListener{
                Log.e("check","CallActivity - onDestroy ")
            }
        }
    }

    private fun openSearchView(){
        sheet = BottomSheetDialog(this@DialActivity)
        val inflator = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflator.inflate(R.layout.bottom_sheet_user, null)
        sheet!!.setContentView(view)
        sheet!!.setCancelable(false)
        sheet!!.show()

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val rvUsers = view.findViewById<RecyclerView>(R.id.rvUsers)
        val bsConstraint = view.findViewById<ConstraintLayout>(R.id.bsContraint)
        val cancel = view.findViewById<ImageView>(R.id.imageView)

        cancel.setOnClickListener{

            sheet!!.dismiss()
            sheet=null
        }

        bsConstraint.setOnClickListener{
            hideKeyboard()
        }
        val temp=ArrayList<AdapterUsers>()
        temp.addAll(GiveOne.list)
        val adapter = UserAdapter(this, temp)
        GiveOne.adapter=adapter

        Log.e("other" , "${GiveOne.list}")

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.setHasFixedSize(true)
        rvUsers.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               filter(newText.toString())
                return true
            }

        })

    }

    private fun changeUserName(){
        val sheet = BottomSheetDialog(this@DialActivity)
        val inflator = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflator.inflate(R.layout.sample_change_username, null)
        sheet.setContentView(view)
        sheet.show()
        var tvUserName = view.findViewById<TextView>(R.id.tvCurrentUsername)
        var etChange = view.findViewById<EditText>(R.id.etChangeusername)
        var btnChange = view.findViewById<Button>(R.id.btnSampleChange)
        var btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvUserName.text = "Your username is ${pref.getUsername()}"
        btnChange.setOnClickListener {
            if (etChange.text.toString() == "") {
                etChange.setError("Please Enter New username")
                return@setOnClickListener
            }
            firebaseClient.dbRef.child("Users").child(pref.getTime()!!).child("username")
                .setValue(etChange.text.toString()).addOnSuccessListener {
                    pref.setUsername(etChange.text.toString())
                    GiveOne.setUserName(pref.getUsername()!!)
                    CoroutineScope(Dispatchers.Main).launch {
                        async { mainRepository.webRtcClient?.updateUserName() }.await()
                        binding.textView.text = "Your username is ${pref.getUsername()}"
                        sheet.dismiss()

                    }

                }
        }
        btnCancel.setOnClickListener {
            sheet.dismiss()
        }
    }
    private fun hideKeyboard(){


        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.currentFocus?.windowToken, 0)

    }

    fun filter(text:String){
        var tempList=ArrayList<AdapterUsers>()

        for(user in GiveOne.list){
            if(user.username.contains(text)){
                tempList.add(user)
            }
        }

        if(tempList.isEmpty()){
            GiveOne.adapter?.filter(tempList)
            Toast.makeText(this@DialActivity,"$text can't find !",Toast.LENGTH_SHORT).show()
        }
        else{
            GiveOne.adapter?.filter(tempList)
        }
    }

    fun playConnecting(){
        if(!connecting.isPlaying){

                connecting.isLooping=true
                connecting.start()


        }

    }
    fun stopConnecting(){
        if(connecting.isPlaying){
            connecting.stop()
            connecting.release()
        }

    }
    fun playCallComing(){
        if(!callComming.isPlaying){
                callComming.isLooping=true
                callComming.start()
        }

    }
    fun stopCallComing(){
        if(callComming.isPlaying){
            callComming.stop()
            callComming.release()
        }
    }



//    private fun changeFile(){
//        CoroutineScope(Dispatchers.IO).launch {
//            val dir= File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/VideoSuper")
//            if(!dir.exists())
//                dir.mkdir()
//            val file= File("${dir.absolutePath}/user.json")
//               file.delete()
//            file.createNewFile()
//            file.writeText(gson.toJson(AdapterUsers(pref.getUsername()!!,pref.getTime()!!)))
//        }
//    }
}
