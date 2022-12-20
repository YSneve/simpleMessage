package com.snv.simpleMessage

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.snv.musicplayerapp.R
import com.snv.musicplayerapp.databinding.ActivitySettingsBinding

class settings_activity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding
    lateinit var deviceUniqueId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setContentView(binding.root)

        deviceUniqueId =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID).toString()


        binding.getBackButton.setOnClickListener{
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
        }

        binding.saveNameButton.setOnClickListener {
            Firebase.database.getReference("names").child(deviceUniqueId).setValue(binding.editTextTextPersonName.text.toString())

        }

        getMyName()
    }
    fun getMyName()
    {
        val namesRef = Firebase.database.getReference("names")

        namesRef.child(deviceUniqueId).get().addOnSuccessListener() {

            if (it.exists()){

               binding.editTextTextPersonName.setText(it.value.toString())
            }
            else{

                namesRef.child(deviceUniqueId).setValue("User")
                binding.editTextTextPersonName.setText("User")
            }
        }
    }
}