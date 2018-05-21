package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.ViewSummaryActivity;
import com.modastadoc.doctors.model.Appointment;

import java.util.List;

/**
 * Created by kunasi on 04/09/17.
 */

public class PreviousApmtAdapter extends RecyclerView.Adapter<PreviousApmtAdapter.MyViewHolder> {
    private Context mContext;

    private List<Appointment> mAppointments;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView message, viewFile;
        MyViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.name);
            viewFile = (TextView) view.findViewById(R.id.view);
        }

        void bind(final Appointment appointment) {
            message.setText(appointment.name + " on "+ appointment.date);

            viewFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewAppointment(appointment);
                }
            });
        }
    }


    public PreviousApmtAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_previous_appointment, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(mAppointments.get(position));
    }

    @Override
    public int getItemCount() {
        if(mAppointments != null && mAppointments.size() > 0)
            return mAppointments.size();
        return 0;
    }

    public void refresh(List<Appointment> list) {
        mAppointments = list;
        notifyDataSetChanged();
    }

    private void viewAppointment(Appointment appointment) {
        Intent in = new Intent(mContext, ViewSummaryActivity.class);
        in.putExtra("order_id", appointment.orderID);
        mContext.startActivity(in);
    }
 }
