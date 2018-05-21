package com.modastadoc.doctors.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.Live2Activity;
import com.modastadoc.doctors.adapter.ChatAdapter;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView send;
    private EditText message;
    private LinearLayout mBottom;
    private ChatAdapter mAdapter;
    private static Appointment appointment;
    private Live2Activity activity;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

    public static ChatFragment getInstance(Appointment apmt) {
        appointment = apmt;
        return new ChatFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        message = (EditText) view.findViewById(R.id.message);
        send = (ImageView) view.findViewById(R.id.send);
        mBottom = (LinearLayout) view.findViewById(R.id.bottom);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        getActivity().registerReceiver(mReceiver, new IntentFilter("chat"));

        activity = (Live2Activity) getActivity();

        mAdapter = new ChatAdapter(activity.getChatList());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        if(activity.getIsTimeOut()) {
            mBottom.setVisibility(View.GONE);
        }

        return view;
    }

    private void sendMessage() {
        try {
            if (!activity.getIsTimeOut()) {
                String msg = message.getText().toString().trim();
                if (msg.length() > 0) {
                    message.setText("");
                    if (activity.getSession() != null && activity.getConnection() != null) {
                        activity.getSession().sendSignal("msg", msg, activity.getConnection());
                        activity.addChat(new Chat(2, msg));
                        refresh();
                    } else {
                        AppCoreUtil.showToast(activity, "Session is disconnected.");
                    }
                }
            } else {
                mBottom.setVisibility(View.GONE);
                AppCoreUtil.showToast(activity, "Session is disconnected. Please submit the summary");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refresh() {
        mAdapter.refresh(activity.getChatList());
        recyclerView.scrollToPosition(activity.getChatList().size()-1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }
}
