package com.modastadoc.doctors.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.network.MultipartRequest;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AudioQueryActivity extends AppCompatActivity
{

    /*************************/
    String twoHyphens = "--";
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String mimeType = "multipart/form-data;boundary=" + boundary;
    /*************************/

    String TAG = AudioQueryActivity.class.getSimpleName();

    CustomProgressDialog customDialog;

    private Button bt_forward, b2, b3, b4;
    private ImageView iv;
    private MediaPlayer mediaPlayerParent;

    Button bt_reply_audio;

    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();
    ;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx1, tx2, tx3;

    Button bt_record_button;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    Random random = new Random();
    MediaPlayer mediaPlayer;

    CountDownTimer t;
    Button bt_play;
    Button bt_record;
    Button bt_reload;
    Button bt_stop;
    Button bt_upload_audio_file;
    TextView tv_timer_task;
    ImageView iv_close_audio_popup;
    int cnt;

    Uri uriAudioFile;

    public static int oneTimeOnly = 0;
    String audio_link = "";

    Dialog recorddialog;
    String uploadedAudioFileId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_query);

        bt_forward = (Button) findViewById(R.id.bt_forward);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);
        b4 = (Button) findViewById(R.id.button4);
        iv = (ImageView) findViewById(R.id.imageView);

        tx1 = (TextView) findViewById(R.id.textView2);
        tx2 = (TextView) findViewById(R.id.textView3);
        tx3 = (TextView) findViewById(R.id.textView4);

        tx3.setText("");

        bt_reply_audio = (Button) findViewById(R.id.bt_reply_audio);

        //mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayerParent = new MediaPlayer();

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        b2.setEnabled(false);


        /************************/
        audio_link = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            if (bundle.containsKey("audio_link")) ;
            {
                audio_link = bundle.getString("audio_link");
            }
        }

        Log.e( TAG , "Audio Link : " + audio_link ) ;

        if( audio_link .length() <= 0 )
        {
            Toast.makeText(AudioQueryActivity.this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
            finish();
        }

        /************************/

        b3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    mediaPlayerParent.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayerParent.setDataSource( audio_link );
                    mediaPlayerParent.prepare();
                    mediaPlayerParent.start();
                }
                catch (Exception e)
                {
                    Log.e( TAG , "Here : " + e.toString());
                }

                Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
                //mediaPlayerParent.start();

                finalTime = mediaPlayerParent.getDuration();
                startTime = mediaPlayerParent.getCurrentPosition();

                if (oneTimeOnly == 0)
                {
                    seekbar.setMax((int) finalTime);
                    oneTimeOnly = 1;
                }

                tx2.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                                finalTime)))
                );

                tx1.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                                startTime)))
                );

                seekbar.setProgress((int) startTime);
                myHandler.postDelayed(UpdateSongTime, 100);
                b2.setEnabled(true);
                b3.setEnabled(false);

//                mediaPlayerParent.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
//                {
//                    @Override
//                    public void onCompletion(MediaPlayer mp)
//                    {
//                        b3.setEnabled( false );
//                        b2.setEnabled( true  );
//                    }
//                });
            }
        });

        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
                mediaPlayerParent.pause();
                b2.setEnabled(false);
                b3.setEnabled(true);
            }
        });

        bt_forward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int temp = (int) startTime;

                if ((temp + forwardTime) <= finalTime)
                {
                    startTime = startTime + forwardTime;
                    mediaPlayerParent.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped forward 5 seconds", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
                }
            }
        });


        b4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int temp = (int) startTime;

                if ((temp - backwardTime) > 0)
                {
                    startTime = startTime - backwardTime;
                    mediaPlayerParent.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped backward 5 seconds", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
                }
            }
        });


        bt_reply_audio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showRecordAudioFilePopup();
            }
        });
    }

    public void MediaRecorderReady()
    {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void showRecordAudioFilePopup()
    {
        MediaRecorderReady();

        recorddialog = new Dialog(AudioQueryActivity.this);
        recorddialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        recorddialog.setContentView(R.layout.record_audio_layout);
        recorddialog.setCancelable(false);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(recorddialog.getWindow().getAttributes());
        lp.width   = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height  = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        recorddialog.getWindow().setAttributes(lp);

        recorddialog.show();

        bt_play    = (Button) recorddialog.findViewById(R.id.bt_play);
        bt_record  = (Button) recorddialog.findViewById(R.id.bt_record);
        bt_reload  = (Button) recorddialog.findViewById(R.id.bt_reload);
        bt_stop    = (Button) recorddialog.findViewById(R.id.bt_stop);
        bt_upload_audio_file  = (Button) recorddialog.findViewById(R.id.bt_upload_audio_file);
        iv_close_audio_popup  = (ImageView) recorddialog.findViewById(R.id.iv_close_audio_popup);
        //visualizer = (Visualizer) dialog.findViewById(R.id.visualizer);

        tv_timer_task = (TextView) recorddialog.findViewById(R.id.tv_timer_task);
        tv_timer_task.setText("00:00:00");

        /*********************
         *
         * Zero state.
         *
         ********************/
        bt_record.setVisibility( View.VISIBLE);
        bt_play.setVisibility( View.INVISIBLE );
        bt_reload.setVisibility( View.INVISIBLE );
        bt_stop.setVisibility(View.INVISIBLE);
        /********************/

        bt_upload_audio_file.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AudioSavePathInDevice.length() > 0 && uriAudioFile != null)
                {
                    uploadFileUsingVolley( AudioSavePathInDevice , uriAudioFile );
                }
                else
                {
                    Toast.makeText(AudioQueryActivity.this, "Failed!", Toast.LENGTH_LONG).show();
                }

            }
        });

        iv_close_audio_popup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recorddialog.dismiss();
            }
        });

        bt_record.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bt_stop.setVisibility(View.VISIBLE);
                startTimer();

                AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CreateRandomAudioFileName(5) + "AudioRecording.3gp";

                MediaRecorderReady();

                try
                {
                    mediaRecorder.prepare();
                    Log.e(TAG, "prepare");
                    mediaRecorder.start();
                    Log.e(TAG, "start");
                    //visualizer.startListening();
                    //Log.e(TAG, "visualizer start listening");
                }
                catch (IllegalStateException e)
                {
                    Log.e(TAG, "IllegalStateException : " + e.toString());
                }
                catch (IOException e)
                {
                    Log.e(TAG, "IOException : " + e.toString());
                }

                Toast.makeText(AudioQueryActivity.this, "Recording started", Toast.LENGTH_LONG).show();
            }

        });

        bt_stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bt_play.setVisibility(View.VISIBLE);
                bt_reload.setVisibility(View.VISIBLE);

                uriAudioFile = Uri.fromFile(new File(AudioSavePathInDevice));


                cnt = 0;
                stopTimer();

                mediaRecorder.stop();
                //visualizer.stopListening();
                Toast.makeText(AudioQueryActivity.this, "Recording Completed", Toast.LENGTH_LONG).show();
            }
        });

        bt_play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mediaPlayer = new MediaPlayer();

                try
                {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                }
                catch (IOException e)
                {
                    Log.e( TAG , "IOException : " + e.toString() ) ;
                }

                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        bt_play.setEnabled( true );
                    }
                });
                Toast.makeText(AudioQueryActivity.this, "Recording Playing", Toast.LENGTH_LONG).show();
            }
        });



        bt_reload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bt_record.setVisibility(View.VISIBLE);

                bt_play.setVisibility(View.INVISIBLE);
                bt_reload.setVisibility( View.INVISIBLE);
                bt_stop.setVisibility(View.INVISIBLE);

                System.out.println( new File(AudioSavePathInDevice).getAbsoluteFile().delete()  );

                // check if AudioSavePathInDevice has any value delete if exist!

                // reset textview value
                tv_timer_task.setText("00:00:00");
                t.cancel();
                // mediaRecorder reset
                mediaRecorder.reset();
            }
        });
    }

    public String CreateRandomAudioFileName(int string)
    {
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string )
        {
            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt( RandomAudioFileName.length() ) ) );
            i++ ;
        }
        return stringBuilder.toString();
    }

    private void stopTimer()
    {
        t.cancel();
        tv_timer_task.setText("00:00");
    }

    private void startTimer()
    {
        cnt = 0 ;
        t = new CountDownTimer( Long.MAX_VALUE , 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                cnt++;
                String time = new Integer(cnt).toString();

                long millis = cnt;
                int seconds = (int) (millis / 60);
                int minutes = seconds / 60;
                seconds     = seconds % 60;

                tv_timer_task.setText(String.format("%d:%02d:%02d", minutes, seconds,millis));
            }

            @Override
            public void onFinish() {            }
        };

        t.start();
    }


    private Runnable UpdateSongTime = new Runnable()
    {
        public void run()
        {
            try
            {
                startTime = mediaPlayer.getCurrentPosition();
                tx1.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                );
                seekbar.setProgress((int) startTime);
                myHandler.postDelayed(this, 100);
            } catch (Exception e)
            {
                Log.e(TAG, e.toString());
            }
        }
    };

    public static Intent createIntent(Context context)
    {
        Intent intent = new Intent(context, AudioQueryActivity.class);
        return intent;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public static Intent createIntent( Context context , String link )
    {
        Intent intent = new Intent( context , AudioQueryActivity.class);
        intent.putExtra("audio_link", "" + link);
        return intent;
    }

    public void uploadFileUsingVolley(String file_name, Uri uri)
    {
        Context context = this;
        byte[] multipartBody = null;

        byte[] fileData1 = null;
        try
        {
            fileData1 = getBytes( AudioQueryActivity.this , uri );
        }
        catch (Exception e)
        {
            Log.e(TAG, "Ex : " + e.toString());
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try
        {
            buildPart(dos, fileData1, file_name);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            multipartBody = bos.toByteArray();
        }
        catch (IOException e)
        {
            Log.e(TAG, "E. " + e.toString());
            return;
        }

        showCustomDialog();
        String upLoadServerUrl = ServerConstants.UPLOAD_AUDIO;

        Log.e( TAG , "Upload URL : " + upLoadServerUrl );

        MultipartRequest multipartRequest = new MultipartRequest(upLoadServerUrl, null, mimeType, multipartBody, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e( TAG , "Response : " + response );

                dismissCustomDialog();
                try
                {
                    if( recorddialog != null ) recorddialog.dismiss();
                    JSONObject responseObj = new JSONObject( response );
                    String status          = responseObj.getString("status");
                    if( status.equalsIgnoreCase("201") )
                    {
                        Toast.makeText(AudioQueryActivity.this ,"Audio File uploaded!" , Toast.LENGTH_LONG).show();
                        String upload_id =  responseObj.getString("upload_id");
                        uploadedAudioFileId = upload_id;

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra( "uploadedAudioFileId" , uploadedAudioFileId );
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(AudioQueryActivity.this ,"Audio File failed!" , Toast.LENGTH_LONG).show();
                    }
                }
                catch( Exception  e )
                {
                    Toast.makeText(AudioQueryActivity.this ,"Audio File failed!" , Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissCustomDialog();
                Toast.makeText( AudioQueryActivity.this , "Error : " + error.toString() , Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue volleyQueue = Volley.newRequestQueue(AudioQueryActivity.this);
        volleyQueue.add(multipartRequest);
    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName) throws IOException
    {
        String doc_id = LocalDataManager.getInstance().get(PreferenceConstants.DOCID);

        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"audio_qry\"; filename=\"" + fileName + "::" + doc_id + "\"" + lineEnd);
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

    private void showCustomDialog()
    {
        customDialog = new CustomProgressDialog(this);
        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void dismissCustomDialog()
    {
        if (customDialog != null)
        {
            customDialog.stopLoading();
            customDialog.dismiss();
        }
    }
}
