package com.modastadoc.doctors.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.App;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.Live2Activity;
import com.modastadoc.doctors.activity.LiveActivity;
import com.modastadoc.doctors.activity.PatientInfoActivity;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.interfaces.IApmtDialogListener;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CircularImageView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kunasi on 16/08/17.
 */

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.MyViewHolder> {
    private Context mContext;
    private List<Appointment> mAppointments;
    private IApmtDialogListener mListener;
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, time, call, cancel, delay, viewSummary, details, orderID, delayedBy;
        ImageView status;
        CircularImageView pic;
        LinearLayout bottom;

        MyViewHolder(View view) {
            super(view);
            pic = (CircularImageView) view.findViewById(R.id.pic);
            status = (ImageView) view.findViewById(R.id.status);
            name = (TextView) view.findViewById(R.id.name);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            details = (TextView) view.findViewById(R.id.details);
            bottom = (LinearLayout) view.findViewById(R.id.bottom) ;
            call = (TextView) view.findViewById(R.id.call);
            cancel = (TextView) view.findViewById(R.id.cancel);
            delay = (TextView) view.findViewById(R.id.delay);
            viewSummary = (TextView) view.findViewById(R.id.view_summary);
            orderID = (TextView) view.findViewById(R.id.order_id);
            delayedBy = (TextView) view.findViewById(R.id.delayed_by);
        }

        void bind(final Appointment appointment) {
            name.setText(appointment.name);
            date.setText(appointment.date);
            time.setText(appointment.time);
            orderID.setText(appointment.orderID);

            switch (appointment.status) {
                case AppConstants.CONFIRMED:
                    bottom.setVisibility(View.VISIBLE);
                    delayedBy.setVisibility(View.GONE);
                    call.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    delay.setVisibility(View.VISIBLE);
                    viewSummary.setVisibility(View.GONE);
                    status.setBackgroundResource(R.drawable.ic_confirmed);
                    break;
                case AppConstants.DELAYED:
                    bottom.setVisibility(View.VISIBLE);
                    delayedBy.setVisibility(View.VISIBLE);
                    call.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    delay.setVisibility(View.VISIBLE);
                    viewSummary.setVisibility(View.GONE);
                    delayedBy.setText("Delayed By "+appointment.delayedBy+" Minutes");
                    status.setBackgroundResource(R.drawable.ic_delayed);
                    break;
                case AppConstants.COMPLETED:
                    bottom.setVisibility(View.VISIBLE);
                    call.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    delay.setVisibility(View.GONE);
                    viewSummary.setVisibility(View.VISIBLE);
                    delayedBy.setVisibility(View.GONE);
                    status.setBackgroundResource(R.drawable.ic_completed);
                    break;
                default:
                    bottom.setVisibility(View.GONE);
                    delayedBy.setVisibility(View.GONE);
                    status.setBackgroundResource(R.drawable.ic_cancelled);
                    break;
            }

            Glide.with(mContext)
                    .load(appointment.picUrl)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .error(R.drawable.patient)
                    .into(pic);

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onCallClicked(appointment);
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onCancelClicked(appointment);
                }
            });

            delay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onDelayClicked(appointment);
                }
            });

            viewSummary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSummaryClicked(appointment);
                }
            });

            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onViewDetailsClicked(appointment);
                }
            });
        }
    }


    public AppointmentAdapter(Context context, IApmtDialogListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_appointment, parent, false);

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