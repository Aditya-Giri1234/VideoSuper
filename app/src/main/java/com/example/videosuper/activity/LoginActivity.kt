package com.example.videosuper.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.videosuper.callBack.SuccessCallback
import com.example.videosuper.databinding.ActivityMainBinding
import com.example.videosuper.util.GiveOne
import com.example.videosuper.model.Preference
import com.example.videosuper.repository.MainRepository
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX

class LoginActivity : AppCompatActivity() {

    val TAG="LoginActivity"

    lateinit var binding: ActivityMainBinding
    lateinit var mainRepository: MainRepository
    lateinit var pref: Preference
    var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = Preference(this)
        mainRepository = GiveOne.getInstanceOfRepo()

        Log.e("check", "$TAG - onCreate - mainRepository reference id is  $mainRepository")
        binding.enterBtn.setOnClickListener {
            if (binding.username.text.toString() == "") {
                binding.username.setError("Please Enter username")
                return@setOnClickListener
            }
            pref.setUsername(binding.username.text.toString())
            pref.setTime()
            GiveOne.setUserName(pref.getUsername().toString())

            it.isClickable = false
            PermissionX.init(this@LoginActivity).permissions(
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ).request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    mainRepository.login(
                        pref.getUsername()!!,
                        pref.getTime()!!,
                        applicationContext,
                        object : SuccessCallback {

                            override fun onSuccess(message: com.example.videosuper.model.Message?) {

                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        DialActivity::class.java
                                    )
                                )
                                finish()

                            }

                        })
                }

            }


        }


        if (pref.getUsername() != null && pref.getUsername() != "") {
            GiveOne.setUserName(pref.getUsername().toString())
            mainRepository.initWeb(this@LoginActivity, pref.getUsername()!!)
            startActivity(Intent(this@LoginActivity, DialActivity::class.java))
            finish()
        }

    }
}



