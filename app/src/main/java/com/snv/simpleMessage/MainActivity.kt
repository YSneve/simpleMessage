package com.snv.simpleMessage

import android.content.Intent
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
import com.snv.simpleMessage.databinding.ActivityMainBinding
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


    private lateinit var recyclerMessages: RecyclerView
    private var startUpLoadComplete = false
    private var sentByCurrentUser = false
    private var listOfIds =  mutableMapOf<String, String>()

    private lateinit var myName: String

//    private var myMediaplayer = MediaPlayer.create(this, R.raw.new_message_sound)

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
            sentByCurrentUser = true
            binding.editText1.text.clear()
        }

        binding.settingsButton.setOnClickListener {

            val myIntent = Intent(this, settings_activity::class.java)
            startActivity(myIntent)
        }

        GlobalScope.launch(Dispatchers.IO) {
            myName = getMyName()
        }

        val nameChangeListner = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                GlobalScope.launch(Dispatchers.IO) {
                    myName = getMyName()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("UPDATER", "loadPost:onCancelled", databaseError.toException())
            }
        }
        Firebase.database.getReference("name").child(deviceUniqueId)
            .addValueEventListener(nameChangeListner)

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

    suspend fun getName(idToFind: String):String{

        if (listOfIds.containsKey(idToFind))
            return listOfIds[idToFind]!!

        val namesRef = Firebase.database.getReference("names")
        listOfIds.put(idToFind, namesRef.child(idToFind).get().await().value.toString())

        return listOfIds[idToFind]!!
    }
    suspend fun getMyName(): String {
        val namesRef = Firebase.database.getReference("names")
        var retName: String?
        try {
            retName = (namesRef.child(deviceUniqueId).get().await()).value?.toString()
            return retName!!

        } catch (e:java.lang.Exception) {
            namesRef.child(deviceUniqueId).setValue("User")
            return "User"
        }
    }


    fun messagesUpdate() {

        if (startUpLoadComplete == false) {

            GlobalScope.launch(Dispatchers.IO) {
                startupLoadMessages()
                startUpLoadComplete = true
            }
            setMessagesRecycler()
        } else {

            GlobalScope.launch(Dispatchers.IO) {
                updateLoadMessage()
//                    myMediaplayer.start()
            }
        }


    }

    fun sendClick() {
        val MessageTextBox = findViewById<EditText>(R.id.editText1)
//        val usernameBox = findViewById<EditText>(R.id.personNameTextbox)

        val inputMessage = MessageTextBox.text.toString()
        val currentUserUsername = "test"//usernameBox.text.toString()

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


    private suspend fun getMessage(messageId: String): DataSnapshot {

        val fb = Firebase.database.getReference("messages")

        return fb.child(messageId).get().await() as DataSnapshot
    }


    suspend fun sendMessage(message: String, userName: String) {
        var count = getCount()

        var messagesRef = Firebase.database.getReference("messages")
        raiseMessagesCount(count)
        messagesRef.child(count.toString())
            .setValue(mapOf("text" to message, "userid" to deviceUniqueId))
    }


    fun raiseMessagesCount(messagesCount: Int) {
        val reference = Firebase.database.getReference("count")
        reference.setValue(messagesCount + 1)
    }

    suspend fun updateLoadMessage() {
        var count = getCount()

        listOfOhersMessages.add(loadMessages(count))

        notifyChange()
    }

    suspend fun startupLoadMessages() {
        var toLoadMessages by Delegates.notNull<Int>()

        var count = getCount()

        if (count - maxMessagesToShow <= 0) toLoadMessages = 0
        else toLoadMessages = count - maxMessagesToShow

        for (i in (count) downTo (toLoadMessages + 1))
            listOfOhersMessages.add(0, loadMessages(i))

        notifyChange()
    }

    suspend fun loadMessages(dbId: Int): messageClass {

        var recievedMessage: DataSnapshot
        var messageUserName: String
        var messageText: String


        Log.d("LOADER", String.format("Added message with id - %S", dbId - 1))

        recievedMessage = getMessage((dbId - 1).toString())


        messageText = recievedMessage.child("text").value as String

        if (recievedMessage.child("userid").value.toString() == deviceUniqueId
        ) {

            return messageClass(dbId, messageText)
        } else {
            val name = getName(recievedMessage.child("userid").value.toString())
            messageUserName = name
            return messageClass(dbId, messageText, messageUserName)
        }
    }

    private fun notifyChange() {
        GlobalScope.launch(Dispatchers.Main) {

            recyclerMessages.adapter?.notifyDataSetChanged()
            if (sentByCurrentUser) {

                recyclerMessages.scrollToPosition(listOfOhersMessages.count() - 1)
                sentByCurrentUser = false
            } else {
                Log.d("SCROLLSTATE", recyclerMessages.scrollState.toString())
                if (recyclerMessages.scrollState < 3)
                    recyclerMessages.scrollToPosition(listOfOhersMessages.count() - 1)
            }
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

