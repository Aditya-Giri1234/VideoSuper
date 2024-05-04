package com.example.videosuper.firebase

import android.util.Log
import com.example.videosuper.model.AdapterUsers
import com.example.videosuper.model.DataModel
import com.example.videosuper.util.GiveOne
import com.example.videosuper.model.Message
import com.example.videosuper.util.Constant.CallStatus
import com.example.videosuper.model.Users
import com.example.videosuper.callBack.NewEventCallBack
import com.example.videosuper.callBack.StatusCallback
import com.example.videosuper.callBack.SuccessCallback
import com.example.videosuper.util.Constant.UserStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class FirebaseClient {
    val gson= Gson();
    val dbRef=FirebaseDatabase.getInstance().reference
    var listener:ValueEventListener?=null



    companion object{
        const val LATEST_EVENT_FIELD_NAME="latest_event"
    }

    // For Both use
    fun login(userName:String,time:String, callback:SuccessCallback){
        dbRef.child("Users").child(time).setValue(Users(userName)).addOnCompleteListener() {
            Log.e("other" , "data  entered  in firebase !")
            callback.onSuccess(null)
        }.addOnFailureListener{
            Log.e("other ","data can't entered !")
        }
    }

    //For Sender Use
    fun sendMessageToOtherUser(message: Message){


        dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,null,CallStatus.CreateCall,null))).addOnSuccessListener {
            dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("status").setValue(UserStatus.Busy.name).addOnSuccessListener {
                dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(message)).addOnSuccessListener {
                    Log.e("check","FirebaseClient -sendMessageToOtherUser - request call to ${message.target} ")
                }
            }
        }


        }


    // For Both Use
    fun dataTransfer(dataModel: DataModel,roomId: String){
                    dbRef.child("Rooms").child(roomId).child(dataModel.target).child(LATEST_EVENT_FIELD_NAME).setValue(gson.toJson(dataModel)).addOnSuccessListener {
                        Log.e("check"," FirebaseClient - dataTransfer - sender is ${dataModel.sender} - target is ${dataModel.target} - dataModel is $dataModel - type is ${dataModel.type}")
                    }

    }

    //For Both Use
    fun observerInComingEvent(callback: NewEventCallBack){
        Log.e("check"," FirebaseClient - observerInComingEvent - username - ${GiveOne.getUserName()} ")

        dbRef.child("Rooms").child(GiveOne.getRoomId()!!).child(GiveOne.getUserName()!!).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("check"," FirebaseClient - observerInComingEvent - onDataChange - dataModel is ${snapshot.value}")
                GiveOne.latestListener=this
                if(snapshot.value!=null) {
                    try {
                        val data = snapshot.value.toString()
                        val dataModel = gson.fromJson(data, DataModel::class.java)
                        callback.onNewEventReceived(dataModel, GiveOne.getRoomId()!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(
                            "check",
                            " FirebaseClient - observerInComingEvent - onDataChange - error -${e.message}"
                        )
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("check"," FirebaseClient - observerInComingEvent -onCancelled  - error -${error.message}")
            }

        })
    }

    // For Both Use
    fun isTargetPresent(target:String):Boolean{
            var find=false

                    for(user in GiveOne.list){
                        if(user.username.equals(target,false)){
                            GiveOne.setTargetId(user.usernameId)
                            Log.e("other","Target Present")
                            find=true
                            break
                        }
                    }


            Log.e("other"," target is present $find")
             return  find

    }

    //For Both Use
    fun getUsers() {

        dbRef.child("Users").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                GiveOne.list.clear()
                for(user in snapshot.children ){
                    if(!user.key.equals(GiveOne.getUserNameId())){
                        try {
                            GiveOne.list.add(AdapterUsers(user.getValue(Users::class.java)?.username!!,user.key!!))
                        }
                        catch (e:Exception){
                            Log.e("error"," in getUsers - ${e.message}")
                        }
                    }

                }
                Log.e("other","${GiveOne.list}")
                if(GiveOne.adapter!=null)
                GiveOne.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // For Both Use
    fun subscribeSelfEvent( successCallback: SuccessCallback) {
            dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("message").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value!=null){
                        successCallback.onSuccess(gson.fromJson(snapshot.value.toString(),Message::class.java))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    // For Receiver Use
    fun checkMyStatus(statusCallback: StatusCallback) {

        dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("status").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value!=null){
                    val userStatus=gson.fromJson(snapshot.value.toString(),UserStatus::class.java)
                   if(userStatus==UserStatus.Free){
                       statusCallback.onFree()
                   }
                    if(userStatus==UserStatus.Busy){
                        statusCallback.onBusy()
                    }
                    if(userStatus==UserStatus.NotAvailable){
                        statusCallback.onNotAvailable()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    // For Receiver Use
    fun acceptCall() {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,null,CallStatus.AcceptCall
        ,null))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - acceptCall - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.AcceptCall}")
        }
    }

   // For Receiver Use
    fun sendRejectCall(reason: UserStatus) {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,null,CallStatus.RejectCall
            ,reason))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - acceptCall - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.RejectCall} - Reason is ${reason.name} ")
        }
    }

    // For Sender Use
    fun createRoomIdAndSend() {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,
            GiveOne.getRoomId(),CallStatus.SendRoomId
            ,null))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - acceptCall - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.SendRoomId} - roomId is ${GiveOne.getRoomId()} ")
        }
    }

    // For Receiver Use
    fun acceptRoomId() {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,
            GiveOne.getRoomId(),CallStatus.AcceptRoomId
            ,null))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - acceptCall - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.AcceptRoomId} - roomId is ${GiveOne.getRoomId()} ")
        }
    }

    // For Sender use
    fun createRoom() {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,
            GiveOne.getRoomId(),CallStatus.CreateRoom
            ,null))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - acceptCall - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.CreateRoom} - roomId is ${GiveOne.getRoomId()} ")
        }

        dbRef.child("Rooms").child(GiveOne.getRoomId()!!).child(GiveOne.getUserName()!!).setValue("").addOnSuccessListener {
            Log.e("check","FirebaseClient - createRoom - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - roomId is ${GiveOne.getRoomId()}")
        }
    }

    //For Receiver Use
    fun joinRoom() {
        dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("status").setValue(UserStatus.Busy.name).addOnSuccessListener {
            dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
                GiveOne.getUserName()!!,
                GiveOne.getTarget()!!,
                GiveOne.getUserNameId()!!,
                GiveOne.getRoomId(),CallStatus.JoinRoom
                ,null))).addOnSuccessListener {
                Log.e("check"," FirebaseClient - join Room - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.JoinRoom} - roomId is ${GiveOne.getRoomId()} ")
            }
        }

    }

    fun sendDisconnect() {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,
            GiveOne.getRoomId(),CallStatus.Disconnect
            ,null))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - join Room - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.JoinRoom} - roomId is ${GiveOne.getRoomId()} ")
        }
    }

    fun resetMe() {
        dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("status").setValue(UserStatus.Free.name).addOnSuccessListener {
            dbRef.child("Users").child(GiveOne.getUserNameId()!!).child("message").removeValue()
        }
    }

    fun sendCancelCall() {
        dbRef.child("Users").child(GiveOne.getTargetId()!!).child("message").setValue(gson.toJson(Message(
            GiveOne.getUserName()!!,
            GiveOne.getTarget()!!,
            GiveOne.getUserNameId()!!,null,CallStatus.CancelCall
            ,null))).addOnSuccessListener {
            Log.e("check"," FirebaseClient - join Cancel Call - sender is ${GiveOne.getUserName()} - target is ${GiveOne.getTarget()} - callStatus is ${CallStatus.CancelCall}  ")
        }
    }


}