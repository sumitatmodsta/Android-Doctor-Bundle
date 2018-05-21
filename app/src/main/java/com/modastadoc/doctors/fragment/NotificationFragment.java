package com.modastadoc.doctors.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.modastadoc.doctors.R;

/**
 * Created by vijay.hiremath on 15/09/16.
 */
public class NotificationFragment extends Fragment
{
    public NotificationFragment()
    {
    }


    public static NotificationFragment newInstance()
    {
        NotificationFragment fragment = new NotificationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        return rootView;
    }
}
