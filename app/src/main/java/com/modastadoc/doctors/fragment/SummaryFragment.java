package com.modastadoc.doctors.fragment;


import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.Live2Activity;
import com.modastadoc.doctors.adapter.FilterAdapter;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.interfaces.IFragmentlistener;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.common.utils.StorageUtil;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.LabTest;
import com.modastadoc.doctors.network.ImageUpload;
import com.modastadoc.doctors.network.ImageUploadResponse;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.view.ContactsCompletionView;
import com.tokenautocomplete.TokenCompleteTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SummaryFragment extends Fragment implements TokenCompleteTextView.TokenListener<LabTest>,
        ImageUploadResponse, View.OnClickListener {

    private static final String TAG = "SummaryFragment";

    private static final int REQUEST_CHOOSER = 1234;
    private static final int REQUEST_CAMERA = 2345;

    private FilterAdapter filterAdapter;
    private ContactsCompletionView autoCompleteTextView;

    private Button submit, add, upload;
    private EditText diagnosis, advice, other;
    private TextView fileName;

    private TextView otherTestLabel;

    private ArrayList<String> mOtherLabTestList;

    private static Appointment mAppointment;
    private static IFragmentlistener mListener;

    private String picturePath;
    private Uri mImageCaptureUri;

    private Live2Activity activity;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "mReceiver");
            showSubmit(intent.getStringExtra("title"));
        }
    };

    public static SummaryFragment getInstance(Appointment appointment, IFragmentlistener listener) {
        mAppointment = appointment;
        mListener = listener;
        return new SummaryFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        activity = (Live2Activity) getActivity();

        activity.setIsSummaryVisible(true);

        getActivity().registerReceiver(mReceiver, new IntentFilter("can_show_summary"));

        add = (Button) view.findViewById(R.id.add);
        submit = (Button) view.findViewById(R.id.submit);
        upload = (Button) view.findViewById(R.id.upload);
        diagnosis = (EditText) view.findViewById(R.id.diagnosis);
        advice = (EditText) view.findViewById(R.id.advice);
        other = (EditText) view.findViewById(R.id.other);
        otherTestLabel = (TextView) view.findViewById(R.id.otehr_tset_add_label);
        fileName = (TextView) view.findViewById(R.id.file_name);

        if (mOtherLabTestList == null) {
            mOtherLabTestList = new ArrayList<>();
        }

        setValues();

        autoCompleteTextView = (ContactsCompletionView) view.findViewById(R.id.autocomplete_textview);

        //Initializing and attaching adapter for AutocompleteTextView
        filterAdapter = new FilterAdapter(this.getActivity(), R.layout.item_contact, activity.getLabTest());
        autoCompleteTextView.setAdapter(filterAdapter);

        //Set the listener that will be notified of changes in the Tokenlist
        autoCompleteTextView.setTokenListener(this);

        //Set the action to be taken when a Token is clicked
        autoCompleteTextView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);

        for(LabTest test : activity.getSelectedLabTest())
            autoCompleteTextView.addObject(test);

        /*
        *  OnClick Listener
        * */
        submit.setOnClickListener(this);
        add.setOnClickListener(this);
        upload.setOnClickListener(this);

        return view;
    }

    private void setValues() {
        if(LocalDataManager.getInstance().get("can_show_summary", false)) {
            add.setVisibility(View.GONE);
            submit.setText(getActivity().getResources().getString(R.string.text_submit));
        }else {
            add.setVisibility(View.VISIBLE);
            submit.setText(getActivity().getResources().getString(R.string.text_show_to_patient));
        }
        String diag = activity.getDiagnosis();
        String adv = activity.getAdvice();
        String oth = activity.getOther();
        if(diag != null && !diag.isEmpty()) {
            diagnosis.setText(diag);
        }
        if(adv != null && !adv.isEmpty()) {
            advice.setText(adv);
        }
        if(oth != null && !oth.isEmpty()) {
            other.setText(oth);
        }

        if(activity.getIsDocumentUploaded()) {
            setFileName(activity.getDocumentPath());
        }
    }

    private void setFileName(String name) {
        try {
            fileName.setText(name.substring(name.lastIndexOf("/")+1));
        }catch (Exception e) {
            fileName.setText(name);
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                AppCoreUtil.hideKeyboard(activity);
                if(submit.getText().toString().equalsIgnoreCase(activity.getResources().getString(
                        R.string.text_submit))) {
                    String diag = diagnosis.getText().toString().trim();
                    String adv = advice.getText().toString().trim();
                    if(diag.isEmpty() || adv.isEmpty()) {
                        if(diag.isEmpty()) {
                            diagnosis.setError(activity.getString(R.string.error_field_required));
                        }
                        if(adv.isEmpty()) {
                            advice.setError(activity.getString(R.string.error_field_required));
                        }
                    }else {
                        submitClicked();
                    }
                }else {
                    postData(2);
                }
                break;
            case R.id.add:
                AppCoreUtil.hideKeyboard(activity);
                addClicked();
                break;
            case R.id.upload:
                AppCoreUtil.hideKeyboard(activity);
                handleUpload();
                break;
            default:
                break;
        }
    }

    private void handleUpload(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectOption();
        }else {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 4 && permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectOption();
        }else {
            Toast.makeText(getContext(), "Please grant permission",Toast.LENGTH_SHORT).show();
        }
    }

    private void addClicked() {
        postData(0);
    }

    private void showClicked() {
        try {
            if (activity.getSession() != null && activity.getConnection() != null) {
                activity.getSession().sendSignal("summary", "SUMMARY", activity.getConnection());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitClicked() {
        postData(1);
    }

    private void selectOption() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.gallery_camera_layout);

        dialog.show();

        android.widget.Button bt_gallery = (android.widget.Button) dialog.findViewById(R.id.bt_gallery);
        android.widget.Button bt_camera = (android.widget.Button) dialog.findViewById(R.id.bt_camera);

        bt_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                uploadFile();
            }
        });

        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                uploadThroughCamera();
            }
        });
    }
/*

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Select Option");
            builder.setMessage("Note: Supported file types are jpeg, jpg, png, pdf only and maximum size is 25 MB only");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                    if(item == 0) {
                        uploadThroughCamera();
                    }else {
                        uploadFile();
                    }
                }
            });
            builder.show();*/

    private void uploadThroughCamera() {
        activity.unPublish();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mImageCaptureUri = Uri.fromFile(StorageUtil.getTempFile());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        cameraIntent.putExtra("return-data", false);

        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private void uploadFile() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        final String[] ACCEPT_MIME_TYPES = {
                "application/pdf",
                "image/*"
        };
        // The MIME data type filter
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPT_MIME_TYPES);
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent in = Intent.createChooser(intent, "Select a file");
        startActivityForResult(in, REQUEST_CHOOSER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == -1) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    picturePath = StorageUtil.getFilePath(this.getActivity(), uri);
                    Log.i(TAG, "path -- "+picturePath);
                    uploadToServer();
                }
                break;
            case REQUEST_CAMERA:
                activity.publish();
                picturePath = mImageCaptureUri.getPath();
                uploadToServer();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadToServer() {
        ImageUpload mUpload = new ImageUpload(this, picturePath, mAppointment.orderID);
        //mUpload.uploadResponse = this;
        AppDialogUtil.showCustomDialog(getActivity());
        mUpload.execute();
    }

    private void postData(final int status) {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(activity);
            String URL = ServerConstants.SUMMARY_ADD_AND_SUBMIT;

            String medical = "";
            List<LabTest> tests = activity.getSelectedLabTest();
            /*int size = tests.size();
            String[] med = new String[size];

            for(int i=0; i<size; i++) {
                med[i] = tests.get(i).id;
            }*/

            for(LabTest test : tests) {
                medical += test.id+",";
            }
            if(medical.length() > 0) {
                medical = medical.substring(0, medical.length() - 1);
            }

            Log.i(TAG, "Order id - "+ mAppointment.orderID+"");
            Log.i(TAG, "medical_test - "+ medical);

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+mAppointment.orderID);
            params.put("orderId", mAppointment.orderID);
            params.put("diagnosis", diagnosis.getText().toString());
            params.put("medical_advice", advice.getText().toString());
            params.put("status", status+"");

            params.put("upload_pris", activity.getIsDocumentUploaded() ? "Y" : "N");
            params.put("medical_test", medical);

            String label = mOtherLabTestList.toString();
            label = label.replace("[","");
            label = label.replace("]","");
            params.put("test_name", label);

            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "submitClicked --> onResponse --> " + response);
                    if(response != null) {
                        if(response.equalsIgnoreCase("1")) {
                            if(status == 1) {
                                mListener.onSummaryCompleted();
                            }else {
                                if(status == 2)
                                    showClicked();
                                save();
                            }
                        }else {
                            AppDialogUtil.showAlert(activity, R.string.error_title, R.string.error_something_wrong_message);
                        }
                    }else {
                        AppDialogUtil.showAlert(activity, R.string.error_title, R.string.error_something_wrong_message);
                    }
                    //buildLabTests(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AppDialogUtil.dismissCustomDialog();
                    //AppCoreUtil.showToast(Live2Activity.this, R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(activity);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(activity, getString(R.string.error_no_network_message));
        }
    }

    private void showSubmit(String title) {
        submit.setText(title);
        if(title.equalsIgnoreCase(activity.getResources().getString(
                R.string.text_submit))) {
            add.setVisibility(View.GONE);
        }else {
            add.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTokenAdded(LabTest test) {
        if (test.id.equals("-1")) {
            mOtherLabTestList.add(test.name);
        } else {
            activity.addLabTest(test);
        }
    }

    @Override
    public void onTokenRemoved(LabTest test) {
        if (test.id.equals("-1")) {
            mOtherLabTestList.remove(test.name);
        } else {
            activity.removeLabTest(test);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.setIsSummaryVisible(false);
        getActivity().unregisterReceiver(mReceiver);
        //save();
    }

    private void save() {
        try {
            String diag = diagnosis.getText().toString();
            String adv = advice.getText().toString();
            String otherTest = mOtherLabTestList.toString();
            otherTest = otherTest.replace("[","");
            otherTest = otherTest.replace("]","");
            activity.setSummaryInfo(diag, adv, otherTest);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(String response) {
        AppDialogUtil.dismissCustomDialog();
        try {
            if(response != null && response.length() > 0) {
                JSONObject o = new JSONObject(response);
                if(o.optString("msg").equalsIgnoreCase("success")) {
                    setFileName(picturePath);
                    activity.setIsDocumentUploaded(true);
                    activity.setDocumentPath(picturePath);
                }else {
                    error();
                }
            }else {
                error();
            }
        }catch (Exception e) {
            e.printStackTrace();
            error();
        }
    }

    private void error() {
        AppCoreUtil.showToast(getActivity(), "File upload failed");
    }
}