package com.snv.simpleMessage

import android.os.Bundle
import android.provider.Settings.*
import android.util.Log
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.snv.musicplayerapp.R
import com.snv.musicplayerapp.databinding.ActivityMainBinding
import com.snv.simpleMessage.adapters.messageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var messageAdapter: messageAdapter
    private lateinit var listOfOhersMessages: MutableList<messageClass>
    private lateinit var listOfMyMessages: MutableList<messageClass>
    private lateinit var binding: ActivityMainBinding
    private lateinit var deviceUniqueId: String
    private val model: MainViewModel by viewModels()
    private lateinit var recyclerMessages: RecyclerView

    var maxMessagesToShow = 100

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerMessages = binding.recyclerMessagesView

        listOfOhersMessages = mutableListOf()
        listOfMyMessages = mutableListOf()

        deviceUniqueId =
            Secure.getString(contentResolver, Secure.ANDROID_ID).toString()

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        binding.searchBtn.setOnClickListener {
            sendClick()

            binding.editText1.text.clear()
        }


        val messageListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("UPDATER", "We've got an update.. retrieving data..")
                messagesUpdate()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("UPDATER", "loadPost:onCancelled", databaseError.toException())
            }
        }
        Firebase.database.getReference("messages").addValueEventListener(messageListener)



    }

    fun messagesUpdate() {
        GlobalScope.launch(Dispatchers.IO) {
            loadMessages()

        }
        setMessagesRecycler()
    }

    fun sendClick() {
        val MessageTextBox = findViewById<EditText>(R.id.editText1)
        val usernameBox = findViewById<EditText>(R.id.personNameTextbox)

        val inputMessage = MessageTextBox.text.toString()
        val currentUserUsername = usernameBox.text.toString()

        GlobalScope.launch(Dispatchers.IO) {
            sendMessage(inputMessage, currentUserUsername)

            Log.d("SENDER", inputMessage)
        }

    }


    suspend fun getCount(): Int {

        val dbref = Firebase.database.getReference("count")

        Log.d("COUNTER ", "Getting messages count")

        return ((dbref.get().await()).value as Long).toInt()
    }



    private suspend fun getMessage(searchText: String): DataSnapshot {

        val fb = Firebase.database.getReference("messages")

        return fb.child(searchText).get().await() as DataSnapshot
    }


    suspend fun sendMessage(message: String, userName: String) {
        var count = getCount()

        var messagesRef = Firebase.database.getReference("messages")
        raiseMessagesCount(count)
        messagesRef.child(count.toString()).setValue(mapOf("text" to message, "username" to userName, "userid" to deviceUniqueId ))
    }


    fun raiseMessagesCount(messagesCount: Int) {
        val reference = Firebase.database.getReference("count")
        reference.setValue(messagesCount + 1)
    }


    suspend fun loadMessages() {

        var toLoadMessages by Delegates.notNull<Int>()

        listOfOhersMessages.clear()

        var count = getCount()

        if (count - maxMessagesToShow <= 0) toLoadMessages = 0
        else toLoadMessages = count - maxMessagesToShow

        var recievedMessage: DataSnapshot
        var messageUserName: String
        var messageText: String

        for (i in (count) downTo (toLoadMessages + 1)) {
            Log.d("LOADER", String.format("Added message with id - %S", i - 1))

            recievedMessage = getMessage((i - 1).toString())

            messageUserName = recievedMessage.child("username").value as String
            messageText = recievedMessage.child("text").value as String

            if (recievedMessage.child("userid").value.toString() == deviceUniqueId
            ) {
                Log.d(
                    "LOADER", String.format(
                        "This is two equals ids: %s and %s",
                        recievedMessage.child("userid").value.toString(),
                        deviceUniqueId
                    )
                )

                    listOfOhersMessages.add(0, messageClass(i, messageText))


            } else {
                    listOfOhersMessages.add(0, messageClass(i, messageText, messageUserName))
                Log.d(
                    "LOADER",
                    String.format(
                        "This is two not equals ids: %s and %s",
                        recievedMessage.child("userid").value.toString(),
                        deviceUniqueId
                    )
                )
            }
        }
        notifyChange()
    }

    private fun notifyChange() {
        GlobalScope.launch (Dispatchers.Main) {

            recyclerMessages.adapter?.notifyDataSetChanged()
        }
    }

    private fun setMessagesRecycler() {

        val layoutManger: RecyclerView.LayoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val llm = LinearLayoutManager(this)
        llm.stackFromEnd = true     // items gravity sticks to bottom
        llm.reverseLayout = false   // item list sorting (new messages start from the bottom)


        val recyclerMessagesView = findViewById<RecyclerView>(R.id.recyclerMessagesView)
        recyclerMessagesView.layoutManager = layoutManger


        binding.recyclerMessagesView.layoutManager = llm
        messageAdapter = messageAdapter(this, listOfOhersMessages)
        recyclerMessages.setAdapter(messageAdapter)

        notifyChange()

    }
}

