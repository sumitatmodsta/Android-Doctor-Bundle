package com.modastadoc.doctors.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.LanguageAdapter;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.interfaces.IDialogListener;
import com.modastadoc.doctors.common.interfaces.ISelectionListener;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.common.utils.PermissionUtil;
import com.modastadoc.doctors.common.utils.StorageUtil;
import com.modastadoc.doctors.cropimage.CropActivity;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.model.Language;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CircularImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener,
        ISelectionListener, PermissionUtil.Callbacks, IDialogListener {

    private static final String TAG = "ProfileActivity";

    /*private static final int REQUEST_C0DE_IMAGE_PICK = 1;
    private static final int REQUEST_CODE_IMAGE_PICK_ABOVE_18 = 2;
    public static final int REQUEST_C0DE_IMAGE_CROP = 3;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 4;*/

    private static final int REQUEST_CHOOSER = 1234;
    private static final int REQUEST_CAMERA = 2345;
    private static final int REQUEST_CROP = 3456;

    private static final int REQUEST_CODE_PERMISSION_GALLERY = 123;
    private static final int REQUEST_CODE_PERMISSION_CAMERA = 234;

    private static final String[] LANGUAGE = {"Indian Languages", "Bengali", "English", "Gujrati",
            "Hindi", "Kannada", "Malayalam", "Marathi", "Punjabi", "Tamil", "Telugu", "Urdu",
            "International Languages", "Arabic", "French", "German", "Japanese", "Mandarin",
            "Portuguese", "Russian", "Spanish"};

    private TextView /*headerName, headerQualification,*/ name, guardian, date, month, year, registration
            , qualification, speciality, paymentMode, currency, pancard, selectedLang;
    private EditText city, subSpeciality, experience, bankAccount, bankName, bankBranch, ifsc, description;
    private RadioButton male, female, saving, current;
    //private ImageView coverPic;
    private CircularImageView pic;

    private Spinner language;

    private LanguageAdapter mAdapter;

    private String picturePath;
    private Uri mImageCaptureUri;
    private String finalPicturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();

        getProfile();
    }

    private void initializeViews() {
        //coverPic = (ImageView) findViewById(R.id.cover_pic);
        pic = (CircularImageView) findViewById(R.id.pic);
        //headerName = (TextView) findViewById(R.id.header_name);
        //headerQualification = (TextView) findViewById(R.id.header_qualification);
        name = (TextView) findViewById(R.id.name);
        guardian = (TextView) findViewById(R.id.guardian_name);

        date = (TextView) findViewById(R.id.date);
        month = (TextView) findViewById(R.id.month);
        year = (TextView) findViewById(R.id.year);
        registration = (TextView) findViewById(R.id.reg_no);
        qualification = (TextView) findViewById(R.id.qualification);
        speciality = (TextView) findViewById(R.id.speciality);
        paymentMode = (TextView) findViewById(R.id.payment_mode);
        currency = (TextView) findViewById(R.id.currency);
        pancard = (TextView) findViewById(R.id.pancard);
        selectedLang = (TextView) findViewById(R.id.selected_lang);

        language = (Spinner) findViewById(R.id.spinner_language);

        city = (EditText) findViewById(R.id.city);
        subSpeciality = (EditText) findViewById(R.id.sub_speciality);
        experience = (EditText) findViewById(R.id.experience);
        bankAccount = (EditText) findViewById(R.id.bank_account_no);
        bankName = (EditText) findViewById(R.id.bank_name);
        bankBranch = (EditText) findViewById(R.id.bank_branch_name);
        ifsc = (EditText) findViewById(R.id.ifsc);
        description = (EditText) findViewById(R.id.description);

        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);
        saving = (RadioButton) findViewById(R.id.saving);
        current = (RadioButton) findViewById(R.id.current);

        String url = LocalDataManager.getInstance().get(PreferenceConstants.PIC);
        /*Glide.with(this)
                .load(LocalDataManager.getInstance().get(PreferenceConstants.PIC))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.doctor)
                .into(coverPic);*/
        /*Glide.with(this)
                .load(LocalDataManager.getInstance().get(PreferenceConstants.PIC))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.doctor)
                .into(pic);*/



        mAdapter = new LanguageAdapter(this, selectedLang);
        language.setAdapter(mAdapter);
        selectedLang.setOnClickListener(this);
        pic.setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                save();
                break;
            case R.id.selected_lang:
                language.performClick();
                break;
            case R.id.pic:
                String[] options = {"Camera", "Gallery"};
                AppDialogUtil.showDialog(this, options, this);
                break;
            default:
                break;
        }
    }

    private void getProfile() {
        AppDialogUtil.showCustomDialog(this, true);
        HashMap<String, String> params = new HashMap<>();

        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                AppCoreUtil.log(TAG, "getProfile response --> "+ response);
                AppDialogUtil.dismissCustomDialog();
                buildProfile(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppDialogUtil.dismissCustomDialog();
                AppCoreUtil.showToast(ProfileActivity.this,
                        "Something went wrong ,Please check your Internet connection");
            }
        }, params, ServerConstants.GET_PROFILE);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void buildProfile(String response) {
        try {
            JSONObject o = new JSONObject(response);
            if(o.length() > 0) {
                String n = o.optString("fullname");
                String q = o.optString("qualification");
                String g = o.optString("guardian_name");
                String gen = o.optString("gender");
                String dob = o.optString("dob");
                String c = o.optString("city_of_residence");
                String reg = o.optString("reg_no");
                String s = o.optString("specialties");
                String ss = o.optString("sub_speciality");
                String e = o.optString("years_exp");
                String l = o.optString("lang_known");
                //String p = o.optString("payment_mode");
                //String cu = o.optString("currency");
                String pc = o.optString("pan_card_no");
                String b = o.optString("bank_acc");
                String at = o.optString("bank_acc_type");
                String bb = o.optString("bank_branch_name");
                String bn = o.optString("bank_name");
                String i = o.optString("ifsc_code");
                String d = o.optString("doc_desc");
                String url = o.optString("image");

                //headerName.setText(n);
                //headerQualification.setText(q);
                name.setText(n);
                guardian.setText(g);

                if(gen.equalsIgnoreCase("male")) {
                    male.setChecked(true);
                }else {
                    female.setChecked(true);
                }

                try {
                    String[] dt = dob.split("-");
                    if(dt.length == 3) {
                        date.setText(dt[0]);
                        month.setText(getMonth(dt[1]));
                        year.setText(dt[2]);
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }

                city.setText(c);
                registration.setText(reg);
                qualification.setText(q);
                speciality.setText(s);
                subSpeciality.setText(ss);
                experience.setText(e);
                paymentMode.setText("Electronic");
                currency.setText("INR");
                pancard.setText(pc);

                bankAccount.setText(b);
                if(at.equalsIgnoreCase("saving")) {
                    saving.setChecked(true);
                }else {
                    current.setChecked(true);
                }
                bankBranch.setText(bb);
                bankName.setText(bn);
                ifsc.setText(i);
                description.setText(d);

                selectedLang.setText(l);
                Glide.with(this)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .fitCenter()
                        .error(R.drawable.doctor)
                        .into(pic);

                langList(l);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMonth(String month) {
        String res = "";
        try {
            int m = Integer.parseInt(month);
            switch (m) {
                case 1:
                    res = "January";
                    break;
                case 2:
                    res = "February";
                    break;
                case 3:
                    res = "March";
                    break;
                case 4:
                    res = "April";
                    break;
                case 5:
                    res = "May";
                    break;
                case 6:
                    res = "June";
                    break;
                case 7:
                    res = "July";
                    break;
                case 8:
                    res = "August";
                    break;
                case 9:
                    res = "September";
                    break;
                case 10:
                    res = "October";
                    break;
                case 11:
                    res = "November";
                    break;
                case 12:
                    res = "December";
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();
            res = month;
        }

        return res;
    }

    private void langList(String lang) {
        ArrayList<Language> list = new ArrayList<>();
        if(lang != null && !lang.isEmpty()) {
            String[] l = lang.split(",");
            for(String s : LANGUAGE) {
                list.add(new Language(s, contains(l, s)));
            }
        }else {
            for(String s : LANGUAGE) {
                list.add(new Language(s));
            }
        }

        mAdapter.refresh(list);
    }

    private boolean contains(String[] lang, String l) {
        try {
            for(String s : lang) {
                if(s.equals(l)) {
                    return true;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void save() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppCoreUtil.hideKeyboard(this);
            AppDialogUtil.showCustomDialog(this);
            String type = "";
            if(saving.isChecked()) {
                type = "saving";
            }
            if(current.isChecked()) {
                type = "current";
            }

            Log.i(TAG, "selected lang -- " + mAdapter.getSelected());

            HashMap<String, String> params = new HashMap<>();
            params.put("city_of_residence", city.getText().toString().trim());
            params.put("multi_lang", mAdapter.getSelected());
            params.put("bank_acc", bankAccount.getText().toString().trim());
            params.put("bank_acc_type", type);
            params.put("bank_name", bankName.getText().toString().trim());
            params.put("bank_branch_name", bankBranch.getText().toString().trim());
            params.put("ifsc_code", ifsc.getText().toString().trim());
            params.put("years_exp", experience.getText().toString().trim());
            params.put("doc_desc", description.getText().toString().trim());
            params.put("sub_speciality", subSpeciality.getText().toString().trim());

            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    AppCoreUtil.log(TAG, "save response --> "+ response);
                    AppDialogUtil.dismissCustomDialog();
                    buildSave(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.log(TAG, "save --> error --> " + error.getMessage());
                    AppDialogUtil.showError(ProfileActivity.this);
                }
            }, params, ServerConstants.UPDATE_PROFILE);

            RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
            volleyQueue.add(postApi);
        }else {
            AppDialogUtil.showInternetError(this);
        }
    }

    private void buildSave(String response) {
        try {
            if(response.equalsIgnoreCase("Success")) {
                AppCoreUtil.showToast(this, "Your profile updated successfully.");
            }else {
                AppDialogUtil.showError(this);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {
                    final Uri uri = data.getData();
                    picturePath = StorageUtil.getFilePath(this, uri);
                    Log.i(TAG, "path -- "+picturePath);
                      cropImage(uri);
                }
                break;
            case REQUEST_CAMERA:
                picturePath = mImageCaptureUri.getPath();
                cropImage(mImageCaptureUri);
                break;
            case REQUEST_CROP:
                if(resultCode == RESULT_OK) {
                    picturePath = data.getStringExtra("CROP_PATH");
                    AppCoreUtil.log(TAG, "Image Crop Path", picturePath);
                    //profileImage.setImageBitmap(StorageUtil.loadFromProfileFile());
                    uploadToServer();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadToServer() {
        //mUpload.uploadResponse = this;
        AppDialogUtil.showCustomDialog(ProfileActivity.this);
        UploadProfilePic uploadProfilePic = new UploadProfilePic();
        uploadProfilePic.execute();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mImageCaptureUri = Uri.fromFile(StorageUtil.getTempFile());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        cameraIntent.putExtra("return-data", false);

        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    /*Open Gallery*/
    private void openGallery() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent in = Intent.createChooser(intent, "Select a file");
        startActivityForResult(in, REQUEST_CHOOSER);
    }

    private void cropImage(Uri selectedImage) {
        Intent crop_intent = new Intent(this, CropActivity.class);
        crop_intent.putExtra("uri", selectedImage);
        crop_intent.putExtra("scale", true);
        crop_intent.putExtra("return-data", true);
        crop_intent.putExtra("aspectX", 1);
        crop_intent.putExtra("aspectY", 1);
        startActivityForResult(crop_intent, REQUEST_CROP);
    }

    @Override
    public void onSelected(int position) {
        if (position == 0) {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            if(PermissionUtil.hasPermission(perms)) {
                openCamera();
            }else {
                PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSION_CAMERA,
                        PermissionUtil.getNecessaryPermissions(perms));
            }

        } else {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if(PermissionUtil.hasPermission(perms)) {
                openGallery();
            }else {
                PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSION_GALLERY,
                        PermissionUtil.getNecessaryPermissions(perms));
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            openCamera();
        }else if(requestCode == REQUEST_CODE_PERMISSION_GALLERY) {
            openGallery();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            AppCoreUtil.showToast(this, "Permission required for accessing camera.");
        }else if(requestCode == REQUEST_CODE_PERMISSION_GALLERY) {
            AppCoreUtil.showToast(this, "Permission required for accessing storage.");
        }
    }

    @Override
    public void onPermissionsPermanentlyDenied(int requestCode, List<String> perms) {
        if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            AppDialogUtil.showDialog(this, R.string.app_name, PermissionUtil.getCameraPermanentlyDeniedMessage(perms),
                    R.string.alert_settings_positive_title, R.string.alert_settings_negative_title, this);
        }else if(requestCode == REQUEST_CODE_PERMISSION_GALLERY) {
            AppDialogUtil.showDialog(this, R.string.app_name, PermissionUtil.getGalleryPermanentlyDeniedMessage(perms),
                    R.string.alert_settings_positive_title, R.string.alert_settings_negative_title, this);
        }
    }

    @Override
    public void onPositiveButtonClicked() {
        AppCoreUtil.openSettingsScreen(this);
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        if(!AppCoreUtil.isEmpty(finalPicturePath))
            returnIntent.putExtra("pictire_path", finalPicturePath);

        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //deleteTempFile();
    }

    private void deleteTempFile() {
        try {
            if(mImageCaptureUri != null) {
                File f = new File(mImageCaptureUri.getPath());
                if(f.exists())
                    f.delete();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class UploadProfilePic extends AsyncTask{

        private final String uploadURL = "https://dashboard.modasta.com/api/v1/patient/profileupload";
        private final String picParamKey = "file";

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(uploadURL);

                httpPost.setHeader("Authorization", LocalDataManager.getInstance().get(PreferenceConstants.Token));

                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                //String cookie = AccountHelper.getUserCookie();
                File file = new File(picturePath);
                entity.addPart(picParamKey, new FileBody(file, "application/octet"));

                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost, localContext);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));


                return reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(Object o) {
            String response = o.toString();
            Log.d("onPostExecute", response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                    final String url = jsonObject.optString("image");
                    LocalDataManager.getInstance().set(PreferenceConstants.PIC, url);
                    finalPicturePath = picturePath;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Glide.clear(coverPic);
                            Glide.clear(pic);
                            Bitmap bitmap = BitmapFactory.decodeFile(finalPicturePath);
                            if(bitmap != null) {
                                //coverPic.setImageBitmap(bitmap);
                                pic.setImageBitmap(bitmap);
                            }
                            /*Glide.with(ProfileActivity.this)
                                    .load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .fitCenter()
                                    .error(R.drawable.doctor)
                                    .into(coverPic);
                            Glide.with(ProfileActivity.this)
                                    .load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .fitCenter()
                                    .error(R.drawable.doctor)
                                    .into(pic);*/
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppDialogUtil.dismissCustomDialog();
        }
    }
}
