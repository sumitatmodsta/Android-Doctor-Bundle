package com.modastadoc.doctors.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.modastadoc.doctors.docconnect.GroupListFragment;
import com.modastadoc.doctors.fragment.HomeFragment;
import com.modastadoc.doctors.fragment.MoreFragment;
import com.modastadoc.doctors.fragment.NewHomeFragment;
import com.modastadoc.doctors.fragment.ProfileFragment;
import com.modastadoc.doctors.fragment.NotificationFragment;

/**
 * Created by vijay.hiremath on 15/09/16.
 */
public class TabAdapter extends FragmentPagerAdapter
{
    public TabAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                //return HomeFragment.newInstance(position + 1);
                return new NewHomeFragment();

            case 1:
                return ProfileFragment.newInstance();

            /*case 2:
                return GroupListFragment.newInstance();*/

            case 2:
                return MoreFragment.newInstance();

            default:
                //return HomeFragment.newInstance(position + 1);
                return new NewHomeFragment();
        }
    }

    @Override
    public int getCount()
    {

        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Home";
            case 1:
                return "Account";
            /*case 2:
                return "Doc-Connect";*/
            case 2:
                return "More";
        }
        return "Home";
    }
}
