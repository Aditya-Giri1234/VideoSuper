package com.example.videosuper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.example.videosuper.R
import com.example.videosuper.model.AdapterUsers
import com.example.videosuper.util.GiveOne
import com.example.videosuper.model.Preference
import com.example.videosuper.repository.MainRepository

class UserAdapter(var context: Context, var list:ArrayList<AdapterUsers>) :RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    var pref=Preference(context)
    var firebaseClient= GiveOne.getInstanceOfFire()
    private  var mainRepository: MainRepository= GiveOne.getInstanceOfRepo()
    class ViewHolder(view: View) :RecyclerView.ViewHolder(view){
        val tvUser=view.findViewById<TextView>(R.id.tvUser)
        val btnStartCall=view.findViewById<TextView>(R.id.btnStartCall)
        val view=view.findViewById<View>(R.id.view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.sample_user,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvUser.text = list[position].username
        val position = position
        holder.view.isGone = position==list.size-1
        holder.btnStartCall.setOnClickListener {
            GiveOne.setRoomId(pref.getRoomId())
            GiveOne.setTarget(list[position].username)
            GiveOne.setTargetId(list[position].usernameId)
            mainRepository.sendCallRequest()


        }
    }


    fun filter(tempList:ArrayList<AdapterUsers>){
       list=tempList
        notifyDataSetChanged()
    }

}