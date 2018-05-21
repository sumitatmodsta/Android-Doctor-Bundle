package com.modastadoc.doctors.fragment;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.AboutUs;
import com.modastadoc.doctors.activity.ContactUs;
import com.modastadoc.doctors.activity.LoginActivity;
import com.modastadoc.doctors.adapter.MoreListAdapter;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.interfaces.ItemClickListener;
import com.modastadoc.doctors.database.LocalDataManager;

import java.util.ArrayList;

/**
 * Created by vivek.c on 21/09/16.
 */
public class MoreFragment extends Fragment implements ItemClickListener{
    private static final String ARG_SECTION_NUMBER = "section_number";
    RecyclerView mRecyclerView;
    MoreListAdapter mAdapter;

    public MoreFragment() {}

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_more, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        accountFeatures();
        return rootView;
    }

    private void accountFeatures() {
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        ArrayList<String> listFeatures = new ArrayList<>();
        listFeatures.add("About Us");
        listFeatures.add("Contact Us");
        listFeatures.add("Refer a Colleague");
        listFeatures.add("Rate this App");
        listFeatures.add("Logout");
        mAdapter = new MoreListAdapter(listFeatures, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void logOut() {
        LocalDataManager.getInstance().set(PreferenceConstants.Name, "");
        LocalDataManager.getInstance().set(PreferenceConstants.Email, "");
        LocalDataManager.getInstance().set(PreferenceConstants.Token, "");
        LocalDataManager.getInstance().set(PreferenceConstants.DOCID, "");
        gotoLoginScreen();
    }

    private void gotoLoginScreen() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getContext().startActivity(intent);
        getActivity().finish();
    }

    public Intent getShareIntent() {
        String playStoreLink = "https://play.google.com/store/apps/details?id=com.modastadoc.android";
        String sharedText = "Download this app for answering the queries.\n\n" + playStoreLink;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharedText);
        return shareIntent;
    }

    private void logOutAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Do you want to logout?");
        alertDialog.setIcon(R.drawable.logout);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        logOut();
                    }
                }
        );
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        alertDialog.show();
    }

    @Override
    public void onItemClicked(int position) {
        if (position == 0) {
            Intent intent = new Intent(getContext(), AboutUs.class);
            getContext().startActivity(intent);
        } else if (position == 1) {
            Intent intent = new Intent(getContext(), ContactUs.class);
            getContext().startActivity(intent);
        } else if (position == 2) {
            startActivity(Intent.createChooser(getShareIntent(), getResources().getString(R.string.send_to)));
        } else if (position == 3) {
            String packageName = "com.modastadoc.doctors";
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
            }
        } else if (position == 4) {
            logOutAlert();
        }
    }
}
