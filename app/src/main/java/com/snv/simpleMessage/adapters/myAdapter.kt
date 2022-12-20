//package com.snv.musicplayerapp
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//
//class CustomAdapter(private val context: Context) :
//    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
//
//    private var messagesList: List<String?>? = null
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val myImageView: ImageView = itemView.findViewById(R.id.messageBackgroundView)
//    }
//
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.msgs, viewGroup, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//        val messageToShow = messagesList?.get(position)
//        val imageView = viewHolder.myImageView
//
//
//    }
//
//    override fun getItemCount(): Int {
//        if (messagesList != null) {
//            return messagesList!!.size
//        }
//        return -1
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun updateContent(messages: List<String?>) {
//        messagesList = messages
//        notifyDataSetChanged()
//    }
//}
