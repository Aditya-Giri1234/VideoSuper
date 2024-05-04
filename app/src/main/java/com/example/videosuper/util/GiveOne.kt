package com.example.videosuper.util

import com.example.videosuper.adapter.UserAdapter
import com.example.videosuper.firebase.FirebaseClient
import com.example.videosuper.model.AdapterUsers
import com.example.videosuper.repository.MainRepository
import com.google.firebase.database.ValueEventListener

object GiveOne {
        private var INSTANCE_Main:MainRepository?=null
        private var INSTANCE_Fire:FirebaseClient?=null
        private var roomId:String?=null
        private var username:String?=null
        private var target:String?=null
        private var targetId:String?=null
        private var userNameId:String?=null
    public var latestListener:ValueEventListener?=null




   public  var adapter:UserAdapter?=null
    public var list=ArrayList<AdapterUsers>()



        fun getInstanceOfRepo():MainRepository{
            if(INSTANCE_Main ==null){
                INSTANCE_Main = MainRepository()
            }
            return INSTANCE_Main!!
        }
    fun getInstanceOfFire():FirebaseClient{
            if(INSTANCE_Fire ==null){
                INSTANCE_Fire = FirebaseClient()
            }
            return INSTANCE_Fire!!
        }

    fun setUserName(username:String){
            GiveOne.username =username
    }
    fun setTarget(target:String?){
            GiveOne.target =target
    }
    fun setTargetId(targetId:String?){
            GiveOne.targetId =targetId
    }
    fun setUserNameId(userNameId:String){
            GiveOne.userNameId =userNameId
    }
    fun setRoomId(roomId:String?){
        GiveOne.roomId =roomId
    }

    fun getUserName():String?{
        return username
    }

    fun getTarget():String?{
        return target
    }

    fun getTargetId():String?{
        return targetId
    }
    fun getUserNameId():String?{
        return userNameId
    }

    fun getRoomId():String?{
        return roomId
    }
}