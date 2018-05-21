package com.modastadoc.doctors.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.fragment.ChatFragment;
import com.modastadoc.doctors.fragment.DetailFragment;
import com.modastadoc.doctors.model.Appointment;

public class PatientDetailActivity extends FragmentActivity {

    private static final String[] CONTENT = new String[] { "Chat", "Patient Details", /*"Patient uploaded files",*/ "Summary"};
    private Appointment apmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        LiveActivity.isChatVisible = true;

        apmt = getIntent().getParcelableExtra("appointment");
        boolean summary = getIntent().getBooleanExtra("open_summary", false);

        FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);

        /*TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);*/

        if(summary)
            pager.setCurrentItem(2);
    }

    class GoogleMusicAdapter extends FragmentPagerAdapter {
        public GoogleMusicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return ChatFragment.getInstance(apmt);
            else if(position == 1)
                return DetailFragment.getInstance(apmt);
            /*else if(position == 2)
                return UploadedFileFragment.getInstance(apmt);*/
            else
                return DetailFragment.getInstance(apmt);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveActivity.isChatVisible = false;
    }
}


