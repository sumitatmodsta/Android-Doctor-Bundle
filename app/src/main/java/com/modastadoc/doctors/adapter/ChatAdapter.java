package com.modastadoc.doctors.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.model.Chat;

import java.util.List;

/**
 * Created by kunasi on 13/08/17.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> chatList;
    public static final int ITEM_TYPE_LEFT = 1;
    public static final int ITEM_TYPE_RIGHT = 2;

    public ChatAdapter(List<Chat> list) {
        chatList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == ITEM_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_left, parent, false);
            return new LeftViewHolder(view);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_right, parent, false);
            return new RightViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int type = getItemViewType(position);
        if(type == ITEM_TYPE_LEFT) {
            ((LeftViewHolder)holder).bind(chatList.get(position));
        }else {
            ((RightViewHolder)holder).bind(chatList.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chatList.get(position).getType() == 1)
            return ITEM_TYPE_LEFT;
        return ITEM_TYPE_RIGHT;
    }

    @Override
    public int getItemCount() {
        if(chatList != null && chatList.size() > 0)
            return chatList.size();
        return 0;
    }

    private class LeftViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        LeftViewHolder(View view) {
            super(view);

            message = (TextView) view.findViewById(R.id.message);
        }

        void bind(Chat chat) {
            message.setText(chat.getMessage());
        }
    }

    private class RightViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        RightViewHolder(View view) {
            super(view);

            message = (TextView) view.findViewById(R.id.message);
        }

        void bind(Chat chat) {
            message.setText(chat.getMessage());
        }
    }

    public void refresh(List<Chat> list) {
        chatList = list;
        notifyDataSetChanged();
    }
}
