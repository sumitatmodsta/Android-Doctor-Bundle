package com.modastadoc.doctors.docconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.VolleyCallback;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.StorageUtil;
import com.modastadoc.doctors.common.utils.UtilityMethods;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.docconnect.activity.FullImageActivity;
import com.modastadoc.doctors.docconnect.activity.FullWebViewActivity;
import com.modastadoc.doctors.docconnect.activity.VideoPlayActivity;
import com.modastadoc.doctors.docconnect.adapter.AttachmentHorizontalAdapter;
import com.modastadoc.doctors.docconnect.model.AttachmentModel;
import com.modastadoc.doctors.docconnect.model.ServerAttachmentModel;
import com.modastadoc.doctors.docconnect.widgets.AttachmentLayout;
import com.modastadoc.doctors.network.GetApi;
import com.modastadoc.doctors.network.MultipartRequest;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.network.PostApiSimple;
import com.modastadoc.doctors.widget.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class TopicDetailActivity extends AppCompatActivity implements VolleyCallback
{
    public static int COMMENT_ATTCH_DELETE = 0;
    public static int TOPIC_ATTCH_DELETE   = 0;

    int CAMERA_PIC_REQUEST = 1888;
    int CHOOSE_FILE = 145;

    String TAG = TopicDetailActivity.class.getSimpleName();
    String topic_id = "";
    String read_only = "0";
    String group_id = "";
    String can_edit = "";
    Context mContext;
    AlertDialog alertDialog;

    TextView tv_zero_state;
    TextView tv_topic_title;
    TextView tv_topic_author;
    TextView tv_topic_modified;
    TextView tv_topic_content;
    HtmlTextView reference_detail_content;

    //Button bt_comments;

    String forum_id = "0";

    ArrayList<CommentModel> commentModels = new ArrayList<>();
    CommentsAdapter adapter;
    RecyclerView rv_comments;
    RecyclerView rv_attachments;
    TextView tv_comments;
    EditText et_post_reply;
    Button bt_post_reply;
    FrameLayout fl_comments_holder;
    TextView tv_zero_state_comment;
    TextView tv_comment_title_count;
    ScrollView sv_content;
    LinearLayout ll_comment_section;
    LinearLayout ll_attachment_holder;
    LinearLayout ll_upload_error_msg;
    Button bt_try_again;
    Button bt_cancel_upload;
    FloatingActionsMenu fam_comment_section;
    FloatingActionButton fab_view_comment_section;
    FloatingActionButton fab_edit_topic;
    FloatingActionButton fab_subscribe_topic;

    RelativeLayout rl_holder;

    TextView tv_add_attachments;
    //TextView tv_attachments_name;
    Uri attachment_uri = null;
    String attachment_name = "";
    Uri uriSavedImage;
    File croppedFile;
    ArrayList<AttachmentModel> attachmentModels = new ArrayList<>();
    int attachment_index_to_upload;
    String parent_upload_id = "";

    /*************************/
    String twoHyphens = "--";
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String mimeType = "multipart/form-data;boundary=" + boundary;

    /*************************/


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        mContext = this;

        /************************/
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            if (bundle.containsKey("t_id")) ;
            {
                topic_id = bundle.getString("t_id");
            }

            if (bundle.containsKey("read_only"))
            {
                //value is 1 : 0
                read_only = bundle.getString("read_only");
            }

            if (bundle.containsKey("g_id"))
            {
                group_id = bundle.getString("g_id");
            }

            if (bundle.containsKey("can_edit"))
            {
                can_edit = bundle.getString("can_edit");
            }
        }

        /************************/

        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Topic Details");
            getSupportActionBar().setElevation(0);
        }
        catch (Exception e)
        {
            Log.e(TAG, "e. " + e.toString());
        }

        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);
        tv_topic_author = (TextView) findViewById(R.id.tv_topic_author);
        tv_topic_modified = (TextView) findViewById(R.id.tv_topic_modified);
        tv_topic_content = (TextView) findViewById(R.id.tv_topic_content);
        tv_topic_title = (TextView) findViewById(R.id.tv_topic_title);
        //bt_comments = (Button) findViewById(R.id.bt_comments);
        fl_comments_holder = (FrameLayout) findViewById(R.id.fl_comments_holder);
        rv_comments = (RecyclerView) findViewById(R.id.rv_comments);
        et_post_reply = (EditText) findViewById(R.id.et_reply_post);
        bt_post_reply = (Button) findViewById(R.id.bt_reply_post);
        tv_zero_state_comment = (TextView) findViewById(R.id.tv_zero_state_comment);
        tv_comment_title_count = (TextView) findViewById(R.id.tv_comment_title_count);
        sv_content = (ScrollView) findViewById(R.id.sv_content);
        ll_comment_section = (LinearLayout) findViewById(R.id.ll_comment_section);
        rv_attachments = (RecyclerView) findViewById(R.id.rv_attachments);
        tv_add_attachments = (TextView) findViewById(R.id.tv_add_attachments);
        tv_add_attachments.setPaintFlags(tv_add_attachments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        ll_attachment_holder = (LinearLayout) findViewById(R.id.ll_attachment_holder);
        bt_try_again = (Button) findViewById(R.id.bt_try_again);
        ll_upload_error_msg = (LinearLayout) findViewById(R.id.ll_upload_error_msg);
        bt_cancel_upload = (Button) findViewById(R.id.bt_cancel_upload);
        rl_holder = (RelativeLayout) findViewById(R.id.rl_holder);
        fab_view_comment_section = (FloatingActionButton) findViewById(R.id.fab_view_comment_section);
        fab_edit_topic = (FloatingActionButton) findViewById(R.id.fab_edit_topic);
        fam_comment_section = (FloatingActionsMenu) findViewById(R.id.fam_comment_section);
        reference_detail_content = (HtmlTextView) findViewById(R.id.reference_detail_content);
        fab_subscribe_topic = (FloatingActionButton) findViewById(R.id.fab_subscribe_topic);


        Log.e(TAG, "t_id : " + topic_id);
        Log.e(TAG, "read : " + read_only);


        // dont check if all-topics
        if (!read_only.equalsIgnoreCase("1"))
        {
            checkSubscription(topic_id);
        }


        if (can_edit.equalsIgnoreCase("1"))
        {
            fab_edit_topic.setVisibility(View.VISIBLE);
        }
        else
        {
            fab_edit_topic.setVisibility(View.GONE);
            Log.e(TAG, "Can not edit topic!");
        }

        bt_post_reply.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String comment_txt = et_post_reply.getText().toString();
                if (comment_txt.length() > 0)
                {
                    commentOnArticle(comment_txt);
                }
            }
        });


        fab_edit_topic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog(mContext);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.edit_topic_layout);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.show();

                dialog.getWindow().setAttributes(lp);

                final EditText et_edit_topic_title   = (EditText) dialog.findViewById(R.id.et_edit_topic_name);
                final EditText et_edit_topic_content = (EditText) dialog.findViewById(R.id.et_edit_topic_content);
                Button bt_edit_topic = (Button) dialog.findViewById(R.id.bt_edit_topic);
                bt_edit_topic.setText("Edit Topic");

                bt_edit_topic.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        String user_cookie    = LocalDataManager.getInstance().get(PreferenceConstants.cookie);
                        String edited_title   = et_edit_topic_title.getText().toString();
                        String edited_content = et_edit_topic_content.getText().toString();

                        if( edited_content.length() <= 0 )
                        {
                            return;
                        }

                        if( edited_title.length() <= 0 )
                        {
                            return;
                        }

                        Log.e( TAG , "edited_title   : " + edited_title );
                        Log.e( TAG , "edited_content : " + edited_content );

                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put(  "forum_id" , forum_id   );
                        params.put(  "content"  , edited_content );
                        params.put(  "title"    , edited_title );
                        params.put(  "cookie"   , user_cookie);
                        params.put(  "topic_id" , topic_id );

                        String url = ServerConstants.CREATE_NEW_TOPIC;
                        createNewTopic(url, params);

                        dialog.dismiss();
                    }

                });
            }
        });

        fab_view_comment_section.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fl_comments_holder.setVisibility(View.VISIBLE);
                fam_comment_section.setVisibility(View.GONE);
                if (read_only.equalsIgnoreCase("1"))
                {
                    ll_comment_section.setVisibility(View.GONE);
                }

                getAllComments();
            }
        });

        fab_subscribe_topic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "subscribe to the topic");
                String current_subscription_status = fab_subscribe_topic.getTitle();
                if (current_subscription_status.equalsIgnoreCase("Unsubscribe"))
                {
                    unSubscribeTopic(topic_id);
                } else
                {
                    subscribeTopic(topic_id);
                }

            }
        });


        tv_add_attachments.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                final Dialog dialog = new Dialog(mContext);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.gallery_camera_layout);

                dialog.show();

                Button bt_gallery = (Button) dialog.findViewById(R.id.bt_gallery);
                Button bt_camera = (Button) dialog.findViewById(R.id.bt_camera);

                bt_gallery.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        startActivityForResult(intent, CHOOSE_FILE);
                    }
                });

                bt_camera.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();

                        int randomPIN = (int) (Math.random() * 9000) + 1000;
                        String file_index = String.valueOf(randomPIN);
                        String new_image_captured = "Image_" + file_index + ".jpg";
                        attachment_name = new_image_captured;
                        File dir = Environment.getExternalStorageDirectory();
                        String targetFilename = dir.getAbsolutePath() + "/" + "Modasta" + "/" + new_image_captured;

                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        uriSavedImage = Uri.fromFile(new File(targetFilename));
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                        startActivityForResult(camera, CAMERA_PIC_REQUEST);
                    }
                });

            }
        });

        bt_try_again.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog("Loading...");
                uploadFilesOneByOne(parent_upload_id);
            }
        });

        bt_cancel_upload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                attachmentModels.clear();
                ll_attachment_holder.removeAllViews();
                et_post_reply.setText("");
                getAllComments();
                ll_upload_error_msg.setVisibility(View.GONE);
            }
        });

        showDialog("Loading...");
        getTopicDetails();

    }

    private void createNewTopic(String URL, final HashMap<String, String> params)
    {
        showDialog("Editing...");

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                Log.e(TAG, "createNewTopic : " + response);
                getTopicDetails();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText( mContext , "Network error. Please try again.", Toast.LENGTH_LONG).show();
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if( COMMENT_ATTCH_DELETE == 1)
        {
            COMMENT_ATTCH_DELETE = 0;
            getAllComments();
        }

        if( TOPIC_ATTCH_DELETE == 1 )
        {
            getTopicDetails();
        }
    }

    private void setThisAttachmentAsUploaded(int index)
    {
        try
        {
            AttachmentLayout view = (AttachmentLayout) ll_attachment_holder.getChildAt(index);
            view.setAttachmentAsUploaded();
        } catch (Exception e)
        {
            Log.e( TAG , "Ex setThisAttachmentAsUploaded " + e.toString());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.e( TAG ,  "onActivityResult");
        Log.e( TAG , "requestCode" + requestCode);
        Log.e( TAG , "requestCode" + requestCode);
        Log.e( TAG , "resultCode"  + resultCode);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_FILE && data != null && data.getData() != null)
        {
            final Uri uri = data.getData();
            String uriString = uri.toString();
            String displayName = "";
            long file_size = 0;
            /*******************************************/
            try
            {
                if (uriString.startsWith("content://"))
                {
                    Cursor cursor = null;
                    try
                    {
                        cursor = this.getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst())
                        {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                            file_size = ( size / 1024 ) / 1024 ;
                        }
                    } finally
                    {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://"))
                {
                    File myFile = new File(uriString);
                    displayName = myFile.getName();
                }
            } catch (Exception e)
            {

                Log.e(TAG, "E : " + e.toString());
            }


            if (displayName.length() <= 0)
            {
                Toast.makeText(mContext, "Some thing went wrong :(", Toast.LENGTH_LONG).show();
                return;
            }

            if( file_size > 8 )
            {
                Toast.makeText( mContext , "File upload limit is 8 MB", Toast.LENGTH_LONG).show();
                return;
            }

            /*****************
             * uploading file
             ****************/
            attachment_uri = uri;
            attachment_name = displayName;
            //tv_attachments_name.setText("" + displayName);
            //tv_attachments_name.setVisibility(View.VISIBLE);
            //tv_add_attachments.setVisibility(View.GONE);
            /****************/

            /*************************************************
             * Multiple upload thing
             *************************************************/
            final AttachmentLayout valueTV = new AttachmentLayout(mContext);
            valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            valueTV.setAttachmentName(attachment_name);
            ll_attachment_holder.addView(valueTV);


            valueTV.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AttachmentLayout layout = (AttachmentLayout) v;
                    String attachment_name = layout.getAttachmentName();
                    Log.e(TAG, "Attachment Name : " + attachment_name);
                    for (int i = 0; i < attachmentModels.size(); i++)
                    {
                        if (attachmentModels.get(i).getDisplayName().equalsIgnoreCase(attachment_name))
                        {
                            Log.e(TAG, "remove");
                            attachmentModels.remove(i);
                            break;
                        }
                    }

                    ((LinearLayout) v.getParent()).removeView(v);
                }
            });

            attachmentModels.add(new AttachmentModel(uri, displayName));
            /*************************************************/
        } else if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            try
            {
                final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.crop_image_layout);
                dialog.setTitle("Upload");
                dialog.show();

                final CropImageView cropImageView = (CropImageView) dialog.findViewById(R.id.iv_cropimageview);
                final LinearLayout ll_crop_optios = (LinearLayout) dialog.findViewById(R.id.ll_crop_options);
                final LinearLayout ll_upload_options = (LinearLayout) dialog.findViewById(R.id.ll_upload_options);
                final ImageView iv_crop = (ImageView) dialog.findViewById(R.id.iv_crop);
                final ImageView iv_delete = (ImageView) dialog.findViewById(R.id.iv_delete);
                final ImageView iv_crop_ok = (ImageView) dialog.findViewById(R.id.iv_crop_ok);
                final ImageView iv_crop_cance = (ImageView) dialog.findViewById(R.id.iv_crop_cancel);
                final Button bt_cancel = (Button) dialog.findViewById(R.id.bt_cancel);

                cropImageView.setCropEnabled(false);

                cropImageView.setFrameColor(getResources().getColor(R.color.colorPrimary));
                cropImageView.setHandleColor(getResources().getColor(R.color.fab_color));
                cropImageView.setGuideColor(getResources().getColor(R.color.colorPrimary));

                cropImageView.setFrameStrokeWeightInDp(1);
                cropImageView.setGuideStrokeWeightInDp(1);
                cropImageView.setHandleSizeInDp(8);
                cropImageView.setTouchPaddingInDp(16);


                Bitmap bitmap = decodeSampledBitmapFromFile(uriSavedImage.getPath(), 100, 100);

//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriSavedImage);
                cropImageView.setImageBitmap(bitmap);
                cropImageView.invalidate();

                Button bt_upload = (Button) dialog.findViewById(R.id.bt_upload);
                bt_upload.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                        //String filename = "" + new Date().getTime() + ".jpg";
                        //String filename1 = "" + new Date().getTime() + ".png";

                        if (croppedFile != null)
                        {
                            /**************************************
                             * Cropped imageview. Not supported now
                             *************************************/

                            //Uri uri = Uri.fromFile(croppedFile);
                            //askFileNameBeforeUploading(filename1, uri);

                            /*************************************/
                        } else if (uriSavedImage != null)
                        {
                            //askFileNameBeforeUploading(filename, uriSavedImage);
                            /*****************
                             * uploading file
                             ****************/
                            attachment_uri = uriSavedImage;
                            //tv_attachments_name.setText("" + attachment_name );
                            //tv_attachments_name.setVisibility(View.VISIBLE);
                            //tv_add_attachments.setVisibility(View.GONE);
                            /****************/

                            /*************************************************
                             * Multiple upload thing
                             *************************************************/
                            final AttachmentLayout valueTV = new AttachmentLayout(mContext);
                            valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            valueTV.setAttachmentName(attachment_name);
                            ll_attachment_holder.addView(valueTV);


                            valueTV.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    AttachmentLayout layout = (AttachmentLayout) v;
                                    String attachment_name = layout.getAttachmentName();
                                    Log.e(TAG, "Attachment Name : " + attachment_name);
                                    for (int i = 0; i < attachmentModels.size(); i++)
                                    {
                                        if (attachmentModels.get(i).getDisplayName().equalsIgnoreCase(attachment_name))
                                        {
                                            Log.e(TAG, "remove");
                                            attachmentModels.remove(i);
                                            break;
                                        }
                                    }

                                    ((LinearLayout) v.getParent()).removeView(v);
                                }
                            });

                            attachmentModels.add(new AttachmentModel(attachment_uri, attachment_name));
                            /*************************************************/
                        } else
                        {
                            Toast.makeText(mContext, "Sorry. Image upload failed.", Toast.LENGTH_LONG).show();
                        }

                    }
                });

                bt_cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });

                iv_crop.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        cropImageView.setCropEnabled(true);
                        ll_crop_optios.setVisibility(View.VISIBLE);
                        ll_upload_options.setVisibility(View.GONE);
                    }
                });

                iv_crop_ok.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        final Toast d = Toast.makeText(mContext, "Cropping image...", Toast.LENGTH_LONG);
                        d.show();
                        cropImageView.setCropEnabled(false);
                        ll_crop_optios.setVisibility(View.GONE);
                        ll_upload_options.setVisibility(View.VISIBLE);

                        AsyncTask.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String path = StorageUtil.saveToCacheFile(cropImageView.getCroppedBitmap());
                                Log.e(TAG, "Image saved at : " + path);
                                croppedFile = new File(path);

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (croppedFile.isFile())
                                        {
                                            d.cancel();
                                            Toast.makeText(mContext, "Done!", Toast.LENGTH_LONG).show();
                                            Bitmap myBitmap = BitmapFactory.decodeFile(croppedFile.getAbsolutePath());
                                            cropImageView.setImageBitmap(myBitmap);
                                        } else
                                        {
                                            d.cancel();
                                            Toast.makeText(mContext, "Image cropping failed.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });


                    }
                });

                iv_crop_cance.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ll_crop_optios.setVisibility(View.GONE);
                        ll_upload_options.setVisibility(View.VISIBLE);
                        cropImageView.setCropEnabled(false);
                    }
                });

            } catch (Exception e)
            {
                Toast.makeText(this, "Picture Not taken " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Log.e(TAG, "Oops :(");
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void attachmentRequest()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, CHOOSE_FILE);
    }

    private void getAllComments()
    {
        showDialog("Loading");
        RequestQueue volleyQueue = Volley.newRequestQueue(mContext);

        /***************************************
         *
         *
         * New Api for editable comment
         *
         ***************************************/
//        String URL = ServerConstants.getTopicComments(topic_id);
//        Log.e(TAG, "getAllComments : " + URL);
//
//        GetApi api = new GetApi(mContext, new Response.ErrorListener()
//        {
//            @Override
//            public void onErrorResponse(VolleyError error)
//            {
//                Log.e("errorVolley", String.valueOf(error));
//                dismissDialog();
//            }
//        }, URL, 167);
//        volleyQueue.add( api );
        /***************************************/


        /***************************************
         *
         *
         * New Api for editable comment
         *
         ***************************************/
        String user_cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("topic_id", topic_id);
        params.put("cookie"  , user_cookie);

        Log.e(TAG, "User cookie : " + user_cookie);

        String url = ServerConstants.COMMENTS_EDITABLE;
        Log.e(TAG, "URL : " + url);

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                Log.e(TAG, "getAllComments Response: " + response);
                try
                {
                    commentModels.clear();
                    JSONObject mainObj = new JSONObject(response);
                    JSONArray mainArr = mainObj.getJSONArray("data");
                    for (int i = 0; i < mainArr.length(); i++)
                    {
                        JSONObject commentObj = mainArr.getJSONObject(i);

                        String comment = commentObj.getString("post_content");
                        String commentId = commentObj.getString("ID");

                        String authorName = "";
                        try
                        {
                            JSONObject authorObj = commentObj.getJSONObject("post_author");
                            authorName = authorObj.getString("user_nicename");

                            if (authorName.length() <= 0)
                            {
                                authorName = authorObj.getString("display_name");
                            }

                        } catch (Exception e)
                        {
                            Log.e(TAG, "e " + e.toString());
                        }

                        //authorName = "";
                        String postedDate = commentObj.getString("post_modified");

                        ArrayList<ServerAttachmentModel> imageList = new ArrayList<>();
                        ArrayList<String> videoList = new ArrayList<>();

                        String attchment_obj = commentObj.getString("attachmentURL");
                        try
                        {
                            JSONArray attch_arry = new JSONArray(attchment_obj);
                            for (int j = 0; j < attch_arry.length(); j++)
                            {
                                JSONObject attchOBJ = attch_arry.getJSONObject(j);
                                String img    = attchOBJ.getString("IMG");
                                String att_id = attchOBJ.getString("ID");

                                imageList.add(new ServerAttachmentModel(att_id,img) );
                            }
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG, "" + e.toString());
                        }

                        boolean isEditable = false;
                        String flag = commentObj.getString("flag");
                        if (flag.equalsIgnoreCase("true"))
                        {
                            isEditable = true;
                        }

                        /**********************************
                         *
                         * comments not editable if read-only
                         *
                         *********************************/
                        if( read_only.equalsIgnoreCase("1") )
                        {
                            isEditable = false;
                        }

                        CommentModel model = new CommentModel(commentId, comment, authorName, postedDate, imageList, videoList, isEditable);
                        commentModels.add(model);
                    }

                    final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                    rv_comments.setLayoutManager(mLayoutManager);

                    adapter = new CommentsAdapter(commentModels, mContext);
                    rv_comments.setAdapter(adapter);

                    adapter.editCommentListener(new CommentsAdapter.AttachmentClickListener()
                    {
                        @Override
                        public void onItemClick(int position, View v)
                        {

                            final CommentModel model = commentModels.get(position);
                            String model_id = model.getId();


                            Log.e(TAG, "Comment : " + commentModels.get(position).getComment());


                            final Dialog dialog = new Dialog(mContext);
                            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.edit_comment_layout);
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            lp.copyFrom(dialog.getWindow().getAttributes());
                            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            dialog.show();
                            dialog.getWindow().setAttributes(lp);


//                        final Dialog dialog = new Dialog(mContext);
//                        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//                        dialog.setContentView(R.layout.edit_comment_layout);
//                        dialog.show();

                            final EditText html_comment = (EditText) dialog.findViewById(R.id.html_comment);
                            Button bt_edit_comment = (Button) dialog.findViewById(R.id.bt_edit_comment);

                            String content = html_comment.getText().toString();

                            /************************************
                             *
                             * Edit comment here.
                             *
                             ************************************/
                            if (content.contains("<style>"))
                            {
                                try
                                {
                                    String style_end_index = content.substring(content.lastIndexOf("</style>") + 8, content.length());
                                    html_comment.setText(style_end_index);
                                    Log.e(TAG, "Text : " + style_end_index);
                                }
                                catch (Exception e)
                                {
                                    Log.e(TAG, "HTML error : " + e.toString());
                                    html_comment.setText(content);
                                    Log.e(TAG, "Text : " + content);
                                }
                            }
                            else
                            {
                                html_comment.setText(content);
                                Log.e(TAG, "Text123 : " + content);
                            }
                            /******************************************/

                            bt_edit_comment.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    dialog.dismiss();

                                    String edited_string = html_comment.getText().toString();
                                    if( edited_string.length() <= 0 )
                                    {
                                        return;
                                    }

                                    Log.e(TAG, "edited comment : " + edited_string);

                                    String url = ServerConstants.POST_TOPIC_COMMENT;
                                    Log.e(TAG, "this comment url : " + url);

                                    String user_cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

                                    Log.e(TAG, "Topic ID    : " + topic_id);
                                    Log.e(TAG, "forum_id ID : " + forum_id);

                                    HashMap<String, String> params = new HashMap<String, String>();
                                    params.put("forum_id" , forum_id );
                                    params.put("topic_id" , topic_id );
                                    params.put("content"  , edited_string );
                                    params.put("cookie"   , user_cookie );
                                    params.put("reply_id" , model.getId() );

                                    showDialog("Editing...");

                                    PostApiSimple postApiSimple = new PostApiSimple(new Response.Listener<String>()
                                    {
                                        @Override
                                        public void onResponse(String response)
                                        {
                                            dismissDialog();
                                            Log.e(TAG, "Edit Comment Response : " + response);

                                            try
                                            {
                                                JSONObject responseObj = new JSONObject(response);
                                                String reply_id = responseObj.getString("reply_id");

                                                parent_upload_id = reply_id;

                                                if (attachmentModels.size() > 0)
                                                {
                                                    attachment_index_to_upload = 0;
                                                    uploadFilesOneByOne(reply_id);
                                                } else
                                                {
                                                    Log.e(TAG, "No attachments added.");
                                                    et_post_reply.setText("");
                                                    getAllComments();
                                                }
                                            } catch (Exception e)
                                            {
                                                Log.e(TAG, " Network error!. " + e.toString());
                                                Toast.makeText(mContext, "Network Error!", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }, new Response.ErrorListener()
                                    {
                                        @Override
                                        public void onErrorResponse(VolleyError error)
                                        {
                                            Log.e(TAG, "Error Response : " + error.toString());
                                            dismissDialog();
                                        }
                                    }, params, url);

                                    RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
                                    volleyQueue.add(postApiSimple);
                                    postApiSimple.setRetryPolicy(new DefaultRetryPolicy(
                                            5000,
                                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


                                }
                            });
                        }
                    });
                    /***********************************/

                    if (commentModels.size() <= 0)
                    {
                        tv_zero_state_comment.setVisibility(View.VISIBLE);
                    } else
                    {
                        tv_zero_state_comment.setVisibility(View.GONE);
                    }

                    //bt_comments.setText("Comments : " + commentModels.size());
                    tv_comment_title_count.setText("Comments : " + commentModels.size());

                }
                catch (Exception e)
                {
                    Log.e(TAG, "Ex : " + e.toString());
                    tv_zero_state_comment.setText("Network Error");
                    tv_zero_state_comment.setVisibility(View.VISIBLE);
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText(mContext, "Network error. Please try again.", Toast.LENGTH_LONG).show();
            }

        }, params, url);

        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        volleyQueue.add(postApi);
        /***************************************/
    }

    private void setZeroState()
    {
        tv_zero_state.setVisibility(View.VISIBLE);
        tv_zero_state.setText("No details found.");
        sv_content.setVisibility(View.GONE);
        tv_topic_title.setVisibility(View.GONE);
        tv_topic_author.setVisibility(View.GONE);
        tv_topic_modified.setVisibility(View.GONE);
        //bt_comments.setVisibility(View.GONE);
        fam_comment_section.setVisibility(View.GONE);
    }

    private void commentOnArticle(String comment_txt)
    {
        String url = ServerConstants.POST_TOPIC_COMMENT;

        String user_cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        Log.e(TAG, "Topic ID    : " + topic_id);
        Log.e(TAG, "forum_id ID : " + forum_id);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("forum_id" , forum_id );
        params.put("topic_id" , topic_id );
        params.put("content"  , comment_txt );
        params.put("cookie"   , user_cookie );
        params.put("reply_id" , "0" );

        showDialog( "Loading..." );

        PostApiSimple postApiSimple = new PostApiSimple(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                Log.e(TAG, "Comment Response : " + response);

                try
                {
                    JSONObject responseObj = new JSONObject(response);
                    String reply_id = responseObj.getString("reply_id");

                    parent_upload_id = reply_id;

                    if (attachmentModels.size() > 0)
                    {
                        attachment_index_to_upload = 0;
                        uploadFilesOneByOne(reply_id);
                    } else
                    {
                        Log.e(TAG, "No attachments added.");
                        et_post_reply.setText("");
                        getAllComments();
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, " Network error!. " + e.toString());
                    Toast.makeText(mContext, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Error Response : " + error.toString());
                dismissDialog();
            }
        }, params, url);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApiSimple);
        postApiSimple.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public void uploadFilesOneByOne(String reply_id)
    {
        try
        {
            if (attachment_index_to_upload < attachmentModels.size())
            {
                AttachmentModel model = attachmentModels.get(attachment_index_to_upload);
                uploadFileUsingVolley(reply_id, model.getDisplayName(), model.getImageUri());
            } else
            {
                Log.e(TAG, "Finished upload all images.");
                ll_attachment_holder.removeAllViews();
                attachmentModels.clear();

                getAllComments();

                /*******************************
                 * Reset Comment here view.
                 *******************************/
                et_post_reply.setText("");
                tv_add_attachments.setVisibility(View.VISIBLE);
                //tv_attachments_name.setVisibility(View.GONE);
                //attachment_uri = null;
                //attachment_name = "";
                /*******************************/
            }
        } catch (Exception e)
        {
            Log.e(TAG, "Ex . " + e.toString());
        }
    }

    private void showDialog(String message)
    {

        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(mContext);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void dismissDialog()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
    }

    public void uploadFileUsingVolley(final String comment_id, final String attachment_name, final Uri attachment_uri)
    {
        if (attachment_name.length() <= 0 || attachment_uri == null)
        {
            getAllComments();
            return;
        }

        byte[] multipartBody = null;

        byte[] fileData1 = null;
        try
        {
            fileData1 = getBytes(mContext, attachment_uri);
        } catch (Exception e)
        {

            Log.e(TAG, "Ex : " + e.toString());
            return;
        }


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try
        {
            String token = LocalDataManager.getInstance().get(PreferenceConstants.Token);
            buildPart(dos, fileData1, attachment_name, comment_id, token);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            multipartBody = bos.toByteArray();
        } catch (IOException e)
        {

            Log.e(TAG, "E. " + e.toString());
            return;
        }


        showDialog("Loading...");


        String uploadServerUri = ServerConstants.UPLOAD_DOCS;

        MultipartRequest multipartRequest = new MultipartRequest(uploadServerUri, null, mimeType, multipartBody, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "Upload Response : " + response);
                dismissDialog();

                try
                {
                    JSONObject obj = new JSONObject(response);
                    String msg = obj.getString("msg");
                    if (msg.contains("Success"))
                    {
                        ll_upload_error_msg.setVisibility(View.GONE);
                        setThisAttachmentAsUploaded(attachment_index_to_upload);
                        attachment_index_to_upload++;
                        uploadFilesOneByOne(comment_id);
                    } else
                    {
                        ll_upload_error_msg.setVisibility(View.VISIBLE);
                        Toast.makeText(mContext, "Network Error. Upload failed", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e)
                {
                    ll_upload_error_msg.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Ex . " + e.toString());
                    Toast.makeText(mContext, "Network Error. Upload failed", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                ll_upload_error_msg.setVisibility(View.VISIBLE);
                dismissDialog();
                Toast.makeText(mContext, "Network Error. Upload failed", Toast.LENGTH_LONG).show();
            }
        });


        RequestQueue volleyQueue = Volley.newRequestQueue(mContext);
        volleyQueue.add(multipartRequest);
    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName12, String parentid, String token) throws IOException
    {
        String fileName = fileName12 + "::" + parentid + "::" + token;
        Log.e(TAG, "Uploaded FileName : " + fileName);
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0)
        {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    private void getTopicDetails()
    {
        String URL = ServerConstants.getTopicDetails(topic_id);
        Log.e(TAG, "Topic detail : " + URL);
        RequestQueue volleyQueue = Volley.newRequestQueue(mContext);
        GetApi api = new GetApi(this, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, String.valueOf(error));
                dismissDialog();
                setZeroState();
            }
        }, URL, 1);
        volleyQueue.add(api);
    }

    public static Intent createIntent(Context context, String topic_id, String read_only, String group_id, String can_edit)
    {
        Intent intent = new Intent(context, TopicDetailActivity.class);
        intent.putExtra("t_id", topic_id);
        intent.putExtra("read_only", read_only);
        intent.putExtra("g_id", group_id);
        intent.putExtra("can_edit", can_edit);
        return intent;
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException
    {

        byte[] bytesResult = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try
        {
            int len;
            while ((len = inputStream.read(buffer)) != -1)
            {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } finally
        {
            // close the stream
            try
            {
                byteBuffer.close();
            } catch (IOException ignored)
            { /* do nothing */ }

        }
        return bytesResult;
    }

    public static byte[] getBytes(Context context, Uri uri) throws IOException
    {
        InputStream iStream = context.getContentResolver().openInputStream(uri);
        try
        {
            return getBytes(iStream);
        } finally
        {
            try
            {

                iStream.close();
            } catch (Exception ignored)
            {
                Log.e("uploadFile", "" + ignored.toString());
            }
        }
    }

    @Override
    public void volleyResponse(int response_code, String response)
    {
        dismissDialog();
        Log.e(TAG, "response_code : " + response_code);
        Log.e(TAG, "Response      : " + response);

        /***********************
         *
         * GET ALL COMMENTS
         *
         **********************/
        if (response_code == 167)
        {

            /*******************************
             *
             * This api not used.
             * This response is not used.
             *
             ******************************/
            try
            {
                commentModels.clear();
                JSONObject mainObj = new JSONObject(response);
                JSONArray mainArr = mainObj.getJSONArray("data");
                for (int i = 0; i < mainArr.length(); i++)
                {
                    JSONObject commentObj = mainArr.getJSONObject(i);

                    String comment = commentObj.getString("content");
                    String commentId = commentObj.getString("ID");

                    String authorName = "";
                    try
                    {
                        JSONObject authorObj = commentObj.getJSONObject("author");
                        authorName = authorObj.getString("first_name");

                        if (authorName.length() <= 0)
                        {
                            authorName = authorObj.getString("username");
                        }

                    } catch (Exception e)
                    {
                        Log.e(TAG, "e " + e.toString());
                    }

                    //authorName = "";
                    String postedDate = commentObj.getString("modified");

                    ArrayList<ServerAttachmentModel> imageList = new ArrayList<>();
                    ArrayList<String> videoList = new ArrayList<>();

                    String attchment_obj = commentObj.getString("attachmentURL");
                    try
                    {
                        JSONArray attch_arry = new JSONArray(attchment_obj);
                        for (int j = 0; j < attch_arry.length(); j++)
                        {
                            JSONObject attchOBJ = attch_arry.getJSONObject(j);
                            String img    = attchOBJ.getString("IMG");
                            String att_id = attchOBJ.getString("ID");
                            imageList.add( new ServerAttachmentModel( att_id , img ));
                        }
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "" + e.toString());
                    }

                    //todo change this
                    boolean isEditable = true;
//                    String flag = commentObj.getString("flag");
//                    if (flag.equalsIgnoreCase("true"))
//                    {
//                        isEditable = true;
//                    }

                    CommentModel model = new CommentModel(commentId, comment, authorName, postedDate, imageList, videoList, isEditable);
                    commentModels.add(model);
                }

                final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                rv_comments.setLayoutManager(mLayoutManager);

                adapter = new CommentsAdapter(commentModels, mContext);
                rv_comments.setAdapter(adapter);
                adapter.editCommentListener(new CommentsAdapter.AttachmentClickListener()
                {
                    @Override
                    public void onItemClick(int position, View v)
                    {

                        final CommentModel model = commentModels.get(position);
                        final String model_id = model.getId();


                        Log.e(TAG, "Comment : " + commentModels.get(position).getComment());


                        final Dialog dialog = new Dialog(mContext);
                        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.edit_comment_layout);
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialog.show();
                        dialog.getWindow().setAttributes(lp);

                        final EditText html_comment = (EditText) dialog.findViewById(R.id.html_comment);
                        Button bt_edit_comment = (Button) dialog.findViewById(R.id.bt_edit_comment);

                        String content = html_comment.getText().toString();

                        /************************************
                         *
                         * Edit comment here.
                         *
                         ************************************/
                        if (content.contains("<style>"))
                        {
                            try
                            {
                                String style_end_index = content.substring(content.lastIndexOf("</style>") + 8, content.length());
                                html_comment.setText(style_end_index);
                                Log.e(TAG, "Text : " + style_end_index);
                            } catch (Exception e)
                            {
                                Log.e(TAG, "HTML error : " + e.toString());
                                html_comment.setText(content);
                                Log.e(TAG, "Text : " + content);
                            }
                        } else
                        {
                            html_comment.setText(content);
                            Log.e(TAG, "Text123 : " + content);
                        }
                        /******************************************/

                        bt_edit_comment.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                String edited_string = html_comment.getText().toString();
                                Log.e(TAG, "edited comment : " + edited_string);

                                String url = ServerConstants.POST_TOPIC_COMMENT;
                                Log.e(TAG, "this comment url : " + url);

                                String user_cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

                                Log.e(TAG, "Topic ID    : " + topic_id);
                                Log.e(TAG, "forum_id ID : " + forum_id);

                                HashMap<String, String> params = new HashMap<String, String>();
                                params.put("forum_id" , forum_id );
                                params.put("topic_id" , topic_id );
                                params.put("content"  , edited_string );
                                params.put("cookie"   , user_cookie );
                                params.put("reply_id" , model_id );

                                showDialog("Loading...");

                                PostApiSimple postApiSimple = new PostApiSimple(new Response.Listener<String>()
                                {
                                    @Override
                                    public void onResponse(String response)
                                    {
                                        dismissDialog();
                                        Log.e(TAG, "Edit Comment Response : " + response);

                                        try
                                        {
                                            JSONObject responseObj = new JSONObject(response);
                                            String reply_id = responseObj.getString("reply_id");

                                            parent_upload_id = reply_id;

                                            if (attachmentModels.size() > 0)
                                            {
                                                attachment_index_to_upload = 0;
                                                uploadFilesOneByOne(reply_id);
                                            }
                                            else
                                            {
                                                Log.e(TAG, "No attachments added.");
                                                et_post_reply.setText("");
                                                getAllComments();
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            Log.e(TAG, " Network error!. " + e.toString());
                                            Toast.makeText(mContext, "Network Error!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Response.ErrorListener()
                                {
                                    @Override
                                    public void onErrorResponse(VolleyError error)
                                    {
                                        Log.e(TAG, "Error Response : " + error.toString());
                                        dismissDialog();
                                    }

                                }, params, url);

                                RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
                                volleyQueue.add(postApiSimple);
                                postApiSimple.setRetryPolicy(new DefaultRetryPolicy(
                                        5000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            }
                        });
                    }
                });
                /***********************************/

                if (commentModels.size() <= 0)
                {
                    tv_zero_state_comment.setVisibility(View.VISIBLE);
                } else
                {
                    tv_zero_state_comment.setVisibility(View.GONE);
                }

                //bt_comments.setText("Comments : " + commentModels.size());
                tv_comment_title_count.setText("Comments : " + commentModels.size());

            } catch (Exception e)
            {
                Log.e(TAG, "Ex : " + e.toString());
                tv_zero_state_comment.setText("Network Error");
                tv_zero_state_comment.setVisibility(View.VISIBLE);
            }
        } else
        {
            /*************************
             *
             * topic detail api
             *
             *************************/
            try
            {
                JSONObject mainObj = new JSONObject(response);
                String title = mainObj.getString("title");
                String author_name = "";
                try
                {
                    JSONObject authorObj = mainObj.getJSONObject("author");
                    author_name = authorObj.getString("first_name");
                    Log.e(TAG, "Before : " + author_name);
                    if (author_name.length() <= 0)
                    {
                        author_name = authorObj.getString("username");
                    }
                    Log.e(TAG, "After : " + author_name);
                } catch (Exception e)
                {
                    Log.e(TAG, "Ex : " + e.toString());
                    author_name = "";
                }


                String modified = mainObj.getString("modified");
                String content = mainObj.getString("content");
                String commentCount = mainObj.getString("replies");


                tv_topic_title.setText(Html.fromHtml( title ) );
                Log.e(TAG, "Final : " + author_name.length());
                author_name.trim();
                if (author_name.length() > 0)
                {
                    tv_topic_author.setText("By " + author_name);
                } else
                {
                    tv_topic_author.setText("");
                }


                tv_topic_modified.setText(modified);
                try
                {
                    String json_txt = UtilityMethods.convertToLocalTime(modified);
                    JSONObject obj = new JSONObject(json_txt);
                    String time = obj.getString("TIME");
                    String date = obj.getString("DATE_DETAIL");
                    tv_topic_modified.setText("on " + date + " " + time);
                } catch (Exception e)
                {
                    Log.e(TAG, "Ex : " + e.toString());
                    tv_topic_modified.setText("");
                }


                if (content.contains("<style>"))
                {
                    int s = content.lastIndexOf("</style>");
                    String ss = content.substring(s + 1, content.length());
                    tv_topic_content.setText(ss);

                    try
                    {
                        String style_end_index = content.substring(content.lastIndexOf("</style>") + 8, content.length());
                        reference_detail_content.setHtml(style_end_index);
                    } catch (Exception e)
                    {
                        Log.e(TAG, "HTML error : " + e.toString());
                        reference_detail_content.setHtml(content);
                    }
                } else
                {
                    tv_topic_content.setText(Html.fromHtml(content));
                    reference_detail_content.setHtml(content);
                }

                JSONObject parentObj = mainObj.getJSONObject("parent");
                forum_id = parentObj.getString("ID");

                //bt_comments.setText("Comments - " + commentCount);
                tv_comment_title_count.setText("Comments - " + commentCount);

                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                rv_attachments.setLayoutManager(layoutManager);

                /*************************
                 *
                 * attachment url
                 *
                 *************************/
                try
                {
                    String attachments_obj = mainObj.getString("attachmentURL");

                    JSONArray attchArray = new JSONArray(attachments_obj);
                    final ArrayList<String> mList = new ArrayList<>();


                    Log.e( TAG , "Attch json len : " + attchArray.length() );

                    for (int i = 0; i < attchArray.length(); i++)
                    {
                        JSONObject obj = attchArray.getJSONObject(i);
                        String image_url           = obj.getString("IMG");
                        final String attachment_id = obj.getString("ID");

                        String extension = "";

                        int index = image_url.lastIndexOf('.');
                        if (index > 0)
                        {
                            extension = image_url.substring(index + 1);
                        }

                        if (extension.length() > 0)
                        {
                            mList.add(image_url);
                        }

                        Log.e(TAG, "Attachment Size : " + mList.size());
                        if (mList.size() == 0)
                        {
                            rv_attachments.setVisibility(View.GONE);
                        } else
                        {
                            rv_attachments.setVisibility(View.VISIBLE);
                        }


                        AttachmentHorizontalAdapter adapter = new AttachmentHorizontalAdapter(mList, mContext);
                        rv_attachments.setAdapter(adapter);

                        adapter.setOnItemClickListener(new AttachmentHorizontalAdapter.OptionClickListener()
                        {
                            @Override
                            public void onItemClick(int positions, View v)
                            {

                                String icon_url = mList.get(positions);
                                String extension = "";

                                int index = icon_url.lastIndexOf('.');
                                if (index > 0)
                                {
                                    extension = icon_url.substring(index + 1);
                                }

                                if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg"))
                                {
                                    Intent full_image_intent = FullImageActivity.createIntent(
                                            mContext,
                                            mList.get(positions) ,
                                            can_edit ,
                                            topic_id ,
                                            attachment_id ,
                                            FullImageActivity.TOPIC_DETAIL_SOURCE);
                                    startActivity( full_image_intent );
                                }
                                else if (extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("m3u8"))
                                {
                                    //todo video functionality attachment not there :(
                                    startActivity(VideoPlayActivity.createIntent(mContext, mList.get(positions)));
                                }
                                else if (extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("doc"))
                                {
                                    startActivity(FullWebViewActivity.createIntent(
                                            mContext,
                                            mList.get(positions),
                                            topic_id ,
                                            attachment_id,
                                            can_edit ,
                                            FullWebViewActivity.TOPIC_DETAIL_SOURCE));
                                }
                            }
                        });

                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Attachment Ex : " + e.toString());
                    rv_attachments.setVisibility(View.GONE);
                }
                /*************************/

            } catch (Exception e)
            {
                Log.e(TAG, "Ex - " + e.toString());
                tv_zero_state.setVisibility(View.VISIBLE);
                sv_content.setVisibility(View.GONE);
                tv_topic_title.setVisibility(View.GONE);
                tv_topic_author.setVisibility(View.GONE);
                tv_topic_modified.setVisibility(View.GONE);
                //bt_comments.setVisibility(View.GONE);
                fam_comment_section.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                if (fl_comments_holder.getVisibility() == View.VISIBLE)
                {
                    fl_comments_holder.setVisibility(View.GONE);
                    //bt_comments.setVisibility(View.VISIBLE);
                    fam_comment_section.setVisibility(View.VISIBLE);
                } else
                {
                    super.onBackPressed();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {

        if (fl_comments_holder.getVisibility() == View.VISIBLE)
        {
            fl_comments_holder.setVisibility(View.GONE);
            //bt_comments.setVisibility(View.VISIBLE);
            fam_comment_section.setVisibility(View.VISIBLE);
        } else
        {
            super.onBackPressed();
        }

    }

    private void checkSubscription(String topid_ID)
    {
        Log.e(TAG, "checkSubscription : " + topid_ID);

        String cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        String URL = ServerConstants.CHECK_SUBSCRIBE_TOPIC;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put( "topic_id", topid_ID );
        params.put( "cookie"  , cookie );

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "checkSubscription : " + response);
                try
                {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("subscribed");
                    if (value.equalsIgnoreCase("false"))
                    {
                        showSubscriptionAlert();
                        //fab_subscribe_topic.setIcon(R.drawable.icon_subscribe);
                        fab_subscribe_topic.setVisibility(View.VISIBLE);
                    } else
                    {
                        fab_subscribe_topic.setTitle("Unsubscribe");
                        //fab_subscribe_topic.setIcon(R.drawable.icon_unsubscribe);
                        fab_subscribe_topic.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, "Ex . " + e.toString());
                }
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "checkSubscription ex. " + error.toString());
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void showSubscriptionAlert()
    {
        Snackbar snackbar = Snackbar
                .make(rl_holder, "You are not subscribed.", Snackbar.LENGTH_LONG)
                .setAction("Subscribe", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        subscribeTopic(topic_id);
                    }
                });

        //snackbar.show();
    }

    private void unSubscribeTopic(String topid_ID)
    {
        Log.e(TAG, "subscribeTopic : " + topid_ID);

        String cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        String URL = ServerConstants.SUBSCRIBE_TOPIC;

        Log.e(TAG, "topid_ID : " + topid_ID);
        Log.e(TAG, "cookie : " + cookie);
        Log.e(TAG, "group_id : " + group_id);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("topic_id" , topid_ID);
        params.put("cookie"   , cookie);
        params.put("group_id" , group_id);
        params.put("action"   , "bbp_unsubscribe"); // bbp_unsubscribe
        params.put("nonce"    , "");

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "subscribeTopic : " + response);
                //{"action":"bbp_subscribe","status":true}
                try
                {
                    JSONObject subscribeObj = new JSONObject(response);
                    String action = subscribeObj.getString("status");
                    if (action.equalsIgnoreCase("true"))
                    {
                        Toast.makeText(mContext, "Un-Subscription success!", Toast.LENGTH_LONG).show();
                        fab_subscribe_topic.setVisibility(View.VISIBLE);
                        fab_subscribe_topic.setTitle("Subscribe");
                        //fab_subscribe_topic.setIcon(R.drawable.icon_subscribe);
                    } else
                    {
                        Toast.makeText(mContext, "Un-Subscription failed.", Toast.LENGTH_LONG).show();
                        fab_subscribe_topic.setVisibility(View.VISIBLE);
                        fab_subscribe_topic.setTitle("Unsubscribe");
                        //fab_subscribe_topic.setIcon(R.drawable.icon_unsubscribe);
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, "subscribeTopic ex. " + e.toString());
                    Toast.makeText(mContext, "Subscription failed.", Toast.LENGTH_LONG).show();
                    fab_subscribe_topic.setTitle("Unsubscribe");
                    //fab_subscribe_topic.setIcon(R.drawable.icon_unsubscribe);
                }
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "subscribeTopic ex. " + error.toString());
                Toast.makeText(mContext, "Network Error", Toast.LENGTH_LONG).show();
                fab_subscribe_topic.setVisibility(View.VISIBLE);
                fab_subscribe_topic.setTitle("Unsubscribe");
                //fab_subscribe_topic.setIcon(R.drawable.icon_unsubscribe);
            }

        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void subscribeTopic(String topid_ID)
    {
        Log.e(TAG, "subscribeTopic : " + topid_ID);

        String cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        String URL = ServerConstants.SUBSCRIBE_TOPIC;

        Log.e(TAG, "topid_ID : " + topid_ID);
        Log.e(TAG, "cookie : " + cookie);
        Log.e(TAG, "group_id : " + group_id);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("topic_id", topid_ID);
        params.put("cookie", cookie);
        params.put("group_id", group_id);
        params.put("action", "bbp_subscribe"); // bbp_unsubscribe
        params.put("nonce", "");

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "subscribeTopic : " + response);
                //{"action":"bbp_subscribe","status":true}
                try
                {
                    JSONObject subscribeObj = new JSONObject(response);
                    String action = subscribeObj.getString("status");
                    if (action.equalsIgnoreCase("true"))
                    {
                        Toast.makeText(mContext, "Subscription success!", Toast.LENGTH_LONG).show();
                        fab_subscribe_topic.setTitle("Unsubscribe");
                        //fab_subscribe_topic.setIcon(R.drawable.icon_unsubscribe);
                    } else
                    {
                        Toast.makeText(mContext, "Subscription failed.", Toast.LENGTH_LONG).show();
                        fab_subscribe_topic.setVisibility(View.VISIBLE);
                        fab_subscribe_topic.setTitle("Subscribe");
                        //fab_subscribe_topic.setIcon(R.drawable.icon_subscribe);
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, "subscribeTopic ex. " + e.toString());
                    Toast.makeText(mContext, "Subscription failed.", Toast.LENGTH_LONG).show();
                    fab_subscribe_topic.setTitle("Subscribe");
                    //fab_subscribe_topic.setIcon(R.drawable.icon_subscribe);
                }
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "subscribeTopic ex. " + error.toString());
                Toast.makeText(mContext, "Network Error", Toast.LENGTH_LONG).show();
                fab_subscribe_topic.setVisibility(View.VISIBLE);
                fab_subscribe_topic.setTitle("Subscribe");
                //fab_subscribe_topic.setIcon(R.drawable.icon_subscribe);
            }

        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
