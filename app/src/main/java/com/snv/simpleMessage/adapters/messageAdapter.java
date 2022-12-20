package com.snv.simpleMessage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snv.simpleMessage.R;
import com.snv.simpleMessage.messageClass;

import java.util.List;

public class messageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<messageClass> listOfMessages;

    public messageAdapter(Context context, List<messageClass> messagesList) {
        this.context = context;
        this.listOfMessages = messagesList;
    }

    public int getItemViewType(int position) {
        if (listOfMessages.get(position).getUserName() == null)
            return 1;
        else return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View myMessageItems = LayoutInflater.from(context).inflate(R.layout.my_msgs, parent, false);
            return new myMessageAdapterViewHolder(myMessageItems);
        } else {
            View othersMessageItems = LayoutInflater.from(context).inflate(R.layout.others_msgs, parent, false);
            return new othersMessageAdapterViewHolder(othersMessageItems);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case 0:
                othersMessageAdapterViewHolder otherHolder = (othersMessageAdapterViewHolder) holder;
                otherHolder.otherUserMessageText.setText(listOfMessages.get(position).getMessageText());
                otherHolder.sentMessageUserName.setText(listOfMessages.get(position).getUserName());


                break;
            case 1:
                myMessageAdapterViewHolder myHolder = (myMessageAdapterViewHolder) holder;
                myHolder.messageText.setText(listOfMessages.get(position).getMessageText());
                myHolder.sentByUser.setText("You");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listOfMessages.size();
    }

    public static final class othersMessageAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView otherUserMessageText;
        TextView sentMessageUserName;
        ImageView backgroundView;

        public othersMessageAdapterViewHolder(@NonNull View othersItemView) {
            super(othersItemView);

            otherUserMessageText = itemView.findViewById(R.id.othersMessageText);
            sentMessageUserName = itemView.findViewById(R.id.othersMessageUserName);

        }



    }

    public static final class myMessageAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView sentByUser;

        public myMessageAdapterViewHolder(@NonNull View myItemView) {
            super(myItemView);

            messageText = itemView.findViewById(R.id.myMessageText);
            sentByUser = itemView.findViewById(R.id.myMessageUserName);
//            ResizeByText(myItemView);
        }


    }

}

