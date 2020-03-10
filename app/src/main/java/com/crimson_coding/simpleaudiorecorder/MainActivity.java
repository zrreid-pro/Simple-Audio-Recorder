package com.crimson_coding.simpleaudiorecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private enum ACTIVITY_STATE { STANDBY, RECORDING, SAVING }
    private enum LARGE_BUTTON_STATE { INVISIBLE, RECORD_BUTTON, STOP_BUTTON }
    private TextView dialogTextView;
    private Chronometer chronometer;
    private boolean isRunning;
    private long recordingLength;
    private ImageButton largeButton;
    private TextView recordingTextView;
    private Button archiveButton;
    private ImageButton saveButton;
    private ImageButton cancelButton;
    private File soundBites;
    private String fileSuffix = ".3gp";
    private File audioFile = null;
    private MediaRecorder mediaRecorder;

    private final int MEDIUM = android.R.style.TextAppearance_Medium;
    private final int LARGE = android.R.style.TextAppearance_Large;

    private String slash = File.separator;


    private ACTIVITY_STATE CURRENT_ACTIVITY_STATE;
    private LARGE_BUTTON_STATE CURRENT_LARGE_BUTTON_STATE;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialogTextView = findViewById(R.id.dialogTextView);
        chronometer = findViewById(R.id.chronometer);
        isRunning = false;
        largeButton = findViewById(R.id.largeButtonHolder);
        recordingTextView = findViewById(R.id.recordingTextView);
        archiveButton = findViewById(R.id.archiveButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);


        //ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        //ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        //ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        if(!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        CURRENT_ACTIVITY_STATE = ACTIVITY_STATE.STANDBY;
        //Standby: 0, Recording: 1, Saving: 2
        CURRENT_LARGE_BUTTON_STATE = LARGE_BUTTON_STATE.RECORD_BUTTON;
        //Invisible: 0, Record Button: 1, Stop Button: 2

        toggleCurrentState(CURRENT_ACTIVITY_STATE);

    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if(context != null && permissions != null) {
            for(String permission : permissions) {
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startRecording() {
        try {
            soundBites = new File(Environment.getExternalStorageDirectory() + slash + "Sound Bites");
            Log.i("DEBUG_MAIN", "CREATED FILE");
            //Log.i("DEBUG1", "CREATED SOUND BITES FILE");
            //Log.i("DEBUG2", String.valueOf(!soundBites.exists()));
            //Log.i("DEBUG3", String.valueOf(!soundBites.isDirectory()));
            if(!soundBites.exists() || !soundBites.isDirectory()) { soundBites.mkdir(); Log.i("DEBUG_MAIN", "DIRECTORY CREATED"); }
            Log.i("DEBUG_MAIN", soundBites.getAbsolutePath());
            String[] fileList = soundBites.list();
            Log.i("DEBUG_MAIN", "FILE LIST FOUND");
            //Log.i("DEBUG5", fileList[0]);
            Log.i("DEBUG_MAIN", "FILE LIST LENGTH: "+fileList.length);
            int num = fileList.length + 1;
            Log.i("DEBUG_MAIN", "FILE LIST MEASURED");
            //Log.i("DEBUG6", "" + num);
            String tempFileName = "Recording #" + num;
            dialogTextView.setHint(tempFileName);
            tempFileName += fileSuffix;
            Log.i("DEBUG_MAIN", "FILE NAME CREATED");
            audioFile = new File(soundBites.getAbsolutePath() + slash + tempFileName);
            Log.i("DEBUG_MAIN", "AUDIO FILE CREATED");

            //Toast.makeText(this, audioFile.getName(), Toast.LENGTH_SHORT).show();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            Log.i("DEBUG_MAIN", "MIC SET");
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(384000);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            Log.i("DEBUG_MAIN", "OUTPUT FILE SET");



            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.i("DEBUG_MAIN", "RECORDING STARTED");

            //Sets the current state to recording
            CURRENT_ACTIVITY_STATE = ACTIVITY_STATE.RECORDING;
            //Sets the state of the large button to the stop button
            CURRENT_LARGE_BUTTON_STATE = LARGE_BUTTON_STATE.STOP_BUTTON;
            //Changes the current state to the new desired state
            toggleCurrentState(CURRENT_ACTIVITY_STATE);
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, "RECORDING FAILED", Toast.LENGTH_SHORT).show();
        }
    }

    private File changeFileName(File oldFile) {
        File dir = new File(Environment.getExternalStorageDirectory() + slash + "Sound Bites");
        String newName = dialogTextView.getText().toString() + fileSuffix;
        File newFile = new File(dir, newName);
        oldFile.renameTo(newFile);
        //Toast.makeText(this, "RENAMED FILE TO: " + newName, Toast.LENGTH_SHORT).show();
        return newFile;
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        stopChronometer();
        //Sets the next state of the large button to invisible
        CURRENT_LARGE_BUTTON_STATE = LARGE_BUTTON_STATE.INVISIBLE;
        //Sets the next state of the activity to saving
        CURRENT_ACTIVITY_STATE = ACTIVITY_STATE.SAVING;
        //Sets the state of the activity to the new desired state
        toggleCurrentState(CURRENT_ACTIVITY_STATE);
    }

    private void discardFile(File trash) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + slash + "Sound Bites");
            File trashFile = new File(dir, trash.getName());
            if(trashFile.exists()) {
                trashFile.delete();
            }
        }
        catch (Exception e) {
            //Toast.makeText(this, "SORRY", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToMediaLibrary(File audio) {
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, audio.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current/1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DURATION, (int) recordingLength);
        values.put(MediaStore.Audio.Media.DATA, audio.getAbsolutePath());
        ContentResolver resolver = getContentResolver();

        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = resolver.insert(base, values);

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));

    }

    private void toggleCurrentState(ACTIVITY_STATE nextState) {
        if(nextState == ACTIVITY_STATE.STANDBY) {
            dialogTextView.setVisibility(View.INVISIBLE);
            dialogTextView.setText("");
            chronometer.setVisibility(View.INVISIBLE);
            recordingTextView.setText("Start Recording?");
            recordingTextView.setTextAppearance(LARGE);

            //Sets large button to start button
            toggleLargeButton(CURRENT_LARGE_BUTTON_STATE);
            //Sets archive button to visible
            toggleArchiveButton(CURRENT_ACTIVITY_STATE);
            //Turns off saving buttons
            toggleSavingButtons(CURRENT_ACTIVITY_STATE);

        }
        else if(nextState == ACTIVITY_STATE.RECORDING) {
            //Makes temporary recording name visible
            dialogTextView.setVisibility(View.VISIBLE);
            //Makes chronometer visible
            chronometer.setVisibility(View.VISIBLE);

            recordingTextView.setText("Recording...");
            recordingTextView.setTextAppearance(MEDIUM);

            //Starts chronometer
            startChronometer();
            //Sets the large button to stop button
            toggleLargeButton(CURRENT_LARGE_BUTTON_STATE);
            //Hides archive button
            toggleArchiveButton(CURRENT_ACTIVITY_STATE);
        }
        else if(nextState == ACTIVITY_STATE.SAVING) {
            recordingTextView.setText("Save Recording?");
            recordingTextView.setTextAppearance(LARGE);
            //Sets the state of the large button to the new desired state
            toggleLargeButton(CURRENT_LARGE_BUTTON_STATE);
            toggleSavingButtons(CURRENT_ACTIVITY_STATE);

        }
    }

    private void toggleLargeButton(LARGE_BUTTON_STATE nextState) {
        if(nextState == LARGE_BUTTON_STATE.RECORD_BUTTON) {
            largeButton.setImageResource(R.drawable.record_button);
            largeButton.setVisibility(View.VISIBLE);
            largeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Start Recording
                    //change state values
                    //call toggleLargeButton again with new state value
                    startRecording();

                }
            });


        }
        else if(nextState == LARGE_BUTTON_STATE.STOP_BUTTON) {
            largeButton.setImageResource(R.drawable.stop_button);
            largeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Stop recording
                    //change state values
                    //call toggleLargeButton again with new state value
                    //Stops chronometer
                    stopRecording();
                }
            });

        }
        else if (nextState == LARGE_BUTTON_STATE.INVISIBLE){
            //Remove listeners and hide large button
            largeButton.setVisibility(View.INVISIBLE);
            largeButton.setOnClickListener(null);
        }
    }

    private void toggleArchiveButton(ACTIVITY_STATE nextState) {
        if(nextState == ACTIVITY_STATE.STANDBY) {
            archiveButton.setVisibility(View.VISIBLE);
            archiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Start archive activity
                    //Toast.makeText(MainActivity.this, "ARCHIVE OPENED", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, RecordingArchiveActivity.class);
                    startActivity(intent);
                }
            });
        }
        else {
            archiveButton.setVisibility(View.INVISIBLE);
            archiveButton.setOnClickListener(null);
        }
    }

    private void toggleSavingButtons(ACTIVITY_STATE nextState) {
        if(nextState == ACTIVITY_STATE.SAVING) {
            dialogTextView.setCursorVisible(true);
            dialogTextView.setFocusable(true);
            dialogTextView.setInputType(InputType.TYPE_CLASS_TEXT);
            //Try to automatically bring up keyboard
            saveButton.setVisibility(View.VISIBLE);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //save recording
                    resetChronometer();
                    if(!dialogTextView.getText().toString().equals("")) {
                        audioFile = changeFileName(audioFile);
                        //addToMediaLibrary(audioFile);
                    }
                    else {
                        //Toast.makeText(MainActivity.this, "DEFAULT NAME", Toast.LENGTH_SHORT).show();
                    }
                    addToMediaLibrary(audioFile);
                    Snackbar.make(v, "Audio File Saved", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();


                    //Toast.makeText(MainActivity.this, "RECORDING SAVED", Toast.LENGTH_SHORT).show();
                    CURRENT_ACTIVITY_STATE = ACTIVITY_STATE.STANDBY;
                    CURRENT_LARGE_BUTTON_STATE = LARGE_BUTTON_STATE.RECORD_BUTTON;
                    toggleCurrentState(CURRENT_ACTIVITY_STATE);
                }
            });
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //don't save recording
                    resetChronometer();
                    discardFile(audioFile);
                    //Toast.makeText(MainActivity.this, "RECORDING DISCARDED", Toast.LENGTH_SHORT).show();
                    Snackbar.make(v, "Audio File Discarded", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    CURRENT_ACTIVITY_STATE = ACTIVITY_STATE.STANDBY;
                    CURRENT_LARGE_BUTTON_STATE = LARGE_BUTTON_STATE.RECORD_BUTTON;
                    toggleCurrentState(CURRENT_ACTIVITY_STATE);
                }
            });
        }
        else {
            saveButton.setVisibility(View.INVISIBLE);
            saveButton.setOnClickListener(null);
            cancelButton.setVisibility(View.INVISIBLE);
            cancelButton.setOnClickListener(null);
        }
    }

    private void startChronometer() {
        if(!isRunning) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            isRunning = true;
        }

    }

    private void stopChronometer() {
        if(isRunning) {
            chronometer.stop();
            recordingLength = SystemClock.elapsedRealtime() - chronometer.getBase();
            isRunning = false;
        }

    }

    private void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        recordingLength = 0;
    }
}
