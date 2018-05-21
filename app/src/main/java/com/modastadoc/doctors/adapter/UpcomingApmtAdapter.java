package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.PatientInfoActivity;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.widget.CircularImageView;

import java.util.List;

/**
 * Created by kunasi on 16/08/17.
 */

public class UpcomingApmtAdapter extends RecyclerView.Adapter<UpcomingApmtAdapter.MyViewHolder> {
    private Context mContext;

    private List<Appointment> mAppointments;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, details, orderID, delayedBy;
        CircularImageView pic;
        MyViewHolder(View view) {
            super(view);
            pic = (CircularImageView) view.findViewById(R.id.pic);
            name = (TextView) view.findViewById(R.id.name);
            date = (TextView) view.findViewById(R.id.datetime);
            details = (TextView) view.findViewById(R.id.details);
            orderID = (TextView) view.findViewById(R.id.order_id);
            delayedBy = (TextView) view.findViewById(R.id.delayed_by);
        }

        void bind(final Appointment appointment) {
            name.setText(appointment.name);
            date.setText(appointment.date + "   " + appointment.time);
            orderID.setText(appointment.orderID);
            if(appointment.status.equalsIgnoreCase(AppConstants.DELAYED)) {
                delayedBy.setVisibility(View.VISIBLE);
                delayedBy.setText("Delayed By "+appointment.delayedBy+" Minutes");
            }else {
                delayedBy.setVisibility(View.GONE);
            }

            Glide.with(mContext)
                    .load(appointment.picUrl)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .error(R.drawable.patient)
                    .into(pic);

            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent in = new Intent(mContext, PatientInfoActivity.class);
                    in.putExtra("appointment", appointment);
                    mContext.startActivity(in);
                }
            });
        }
    }


    public UpcomingApmtAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_upcoming_appointment, parent, false);

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
}
