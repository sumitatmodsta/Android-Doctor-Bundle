package com.modastadoc.doctors.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.ConsultationFeeActivity;
import com.modastadoc.doctors.activity.DoctorSummaryActivity;
import com.modastadoc.doctors.activity.ProfileActivity;
import com.modastadoc.doctors.adapter.AccountListAdapter;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.interfaces.ItemClickListener;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.widget.CircularImageView;

import java.util.ArrayList;

/**
 * Created by vivek on 21/09/16.
 */
public class ProfileFragment extends Fragment implements ItemClickListener {
    String TAG = ProfileFragment.class.getSimpleName();

    private static final int REQUEST_CODE_PROFILE_EDIT = 321;

    CardView cardViewProfile;
    RecyclerView mRecyclerView;
    AccountListAdapter mAccountAdapter;
    CircularImageView profileView;
    TextView txtViewUsername;
    RelativeLayout relativeLayoutMain;
    LinearLayout ll_button_link;
    Button bt_boarding;

    public ProfileFragment() {}

    private BroadcastReceiver profileListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent ) {
            updatePic();
        }
    };

    private void updatePic() {
        Glide.clear(profileView);
        Glide.with(this)
                .load(LocalDataManager.getInstance().get(PreferenceConstants.PIC))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.doctor)
                .into(profileView);
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        cardViewProfile = (CardView) rootView.findViewById(R.id.card);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        profileView = (CircularImageView) rootView.findViewById(R.id.profile_image);
        relativeLayoutMain = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutMain);
        ll_button_link = (LinearLayout) rootView.findViewById(R.id.ll_button_link);
        bt_boarding = (Button) rootView.findViewById(R.id.bt_boarding);
        txtViewUsername = (TextView) rootView.findViewById(R.id.name);
        String name = LocalDataManager.getInstance().get(PreferenceConstants.Name);

        txtViewUsername.setText(name);
        accountFeatures();

        /*******************************
         *
         * Permission required.
         *
         *******************************/
        String user_type = LocalDataManager.getInstance().get(PreferenceConstants.USER_TYPE);
        Log.e(TAG, "USER_TYPE ==> " + user_type);
        if( user_type.equalsIgnoreCase(PreferenceConstants.ONLY_IMAKA) ) {
            mRecyclerView.setVisibility(View.GONE);
            ll_button_link.setVisibility(View.VISIBLE);
        }

        bt_boarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.modasta.com/register-to-join-the-modasta-as-physiciandoctor/"));
                startActivity(browserIntent);
            }
        });

        cardViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), ProfileActivity.class), REQUEST_CODE_PROFILE_EDIT);
            }
        });

        Glide.with(this)
                .load(LocalDataManager.getInstance().get(PreferenceConstants.PIC))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.doctor)
                .into(profileView);

        /*******************************/

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(profileListener,
                    new IntentFilter("profile_refresh"));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(profileListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_PROFILE_EDIT) {
            try {
                if(data.hasExtra("pictire_path")) {
                    String path = data.getStringExtra("pictire_path");
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if(bitmap != null) {
                        Glide.clear(profileView);
                        profileView.setImageBitmap(bitmap);
                    }else {
                        Glide.with(this)
                                .load(LocalDataManager.getInstance().get(PreferenceConstants.PIC))
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .fitCenter()
                                .error(R.drawable.doctor)
                                .into(profileView);
                    }
                }else {
                    Glide.with(this)
                            .load(LocalDataManager.getInstance().get(PreferenceConstants.PIC))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .fitCenter()
                            .error(R.drawable.doctor)
                            .into(profileView);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void accountFeatures() {
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        ArrayList<String> listFeatures = new ArrayList<>();
        //listFeatures.add("Accepted Queries");
        //listFeatures.add("Suggestions Given By Inhouse");
        //listFeatures.add("Closed Queries");
        listFeatures.add("Profile");
        listFeatures.add("Reports");
        listFeatures.add("Consultation Fee");

        mAccountAdapter = new AccountListAdapter(listFeatures, this);
        mRecyclerView.setAdapter(mAccountAdapter);
    }

    @Override
    public void onItemClicked(int position) {
        if(position == 0) {
            startActivityForResult(new Intent(getActivity(), ProfileActivity.class), REQUEST_CODE_PROFILE_EDIT);
        } else if (position == 1) {
            Intent intent = new Intent(getActivity(), DoctorSummaryActivity.class);
            getActivity().startActivity(intent);
        } else if (position == 2) {
            Intent intent = new Intent(getActivity(), ConsultationFeeActivity.class);
            getActivity().startActivity(intent);
        }
    }
}