package com.crimson_coding.simpleaudiorecorder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordingArchiveActivity extends AppCompatActivity implements TrashFilesDialog.DialogListener {

    private File soundBites;
    private String slash = File.separator;
    private String name;
    private String duration;
    private String dateOfRecording;
    private ListView listView;
    private ArrayList<Recording> recordingList;
    private MyRecordingAdapter adapter;
    private ImageButton trashButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_archive);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        recordingList = new ArrayList<Recording>();
        listView = findViewById(R.id.listView_archive);
        trashButton = findViewById(R.id.trashButton);




        try {
            soundBites = new File(Environment.getExternalStorageDirectory() + slash + "Sound Bites");
            File[] biteList = soundBites.listFiles();

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for(int i=0; i<biteList.length; i++) {
                Uri uri = MediaStore.Audio.Media.getContentUri(biteList[i].getName());
                mmr.setDataSource(biteList[i].getPath());
                name = biteList[i].getName();
                Log.i("DEBUG_ARCHIVE", name);
                duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long d = Long.valueOf(duration);
                long min = (d/1000)/60;
                int sec = (int)((d/1000)&60);
                String nd = min + ":" + sec;
                Log.i("DEBUG_ARCHIVE", duration);
                //dateOfRecording = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
                Date date = new Date(biteList[i].lastModified());
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                String ndate = sdf.format(date);
                Log.i("DEBUG_ARCHIVE", ndate);
                recordingList.add(new Recording(name, nd, ndate, uri));
                Log.i("DEBUG_ARCHIVE", "CREATED");
            }
        }
        catch (Exception e){ Log.i("DEBUG_ARCHIVE", "OOPS"); }

        adapter = new MyRecordingAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(RecordingArchiveActivity.this, "Clicked: " + position, Toast.LENGTH_SHORT).show();
                Recording current = recordingList.get(position);
                File dir = new File(Environment.getExternalStorageDirectory() + slash + "Sound Bites");
                //File audioFile = new File(soundBites, current.getFileName());

                File path = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                File audioFile = new File(dir, current.getFileName());
                Log.i("DEBUG_ARCHIVE", audioFile.getPath());
                Uri audioUri = Uri.fromFile(audioFile);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    audioUri = FileProvider.getUriForFile(RecordingArchiveActivity.this, BuildConfig.APPLICATION_ID + ".provider", audioFile);
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                //intent.setDataAndType(Uri.parse(recordingList.get(position).getUri().toString()), "audio/*");
                intent.setDataAndType(audioUri, "audio/*");
                startActivity(intent);
            }
        });

        trashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();

            }
        });
    }

    @Override
    public void dialogSignal() {
        trashFiles();
    }

    public void trashFiles() {
        File[] biteList = soundBites.listFiles();
        for(int i=0; i<biteList.length; i++) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory() + slash + "Sound Bites");
                File trashFile = new File(dir, biteList[i].getName());
                if(trashFile.exists()) {
                    trashFile.delete();
                }
            }
            catch (Exception e) {
                //Toast.makeText(this, "SORRY", Toast.LENGTH_SHORT).show();
            }
        }
        adapter.notifyDataSetChanged();
        finish();
        startActivity(getIntent());

    }

    public void openDialog() {
        TrashFilesDialog dialog = new TrashFilesDialog();
        dialog.show(getSupportFragmentManager(), "trash files dialog");
    }

    private class MyRecordingAdapter extends ArrayAdapter<Recording> {
        public MyRecordingAdapter() {
            super(RecordingArchiveActivity.this, R.layout.recording_item_layout, recordingList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.recording_item_layout, parent, false);
            }
            Recording current = recordingList.get(position);
            TextView titleTextView = itemView.findViewById(R.id.titleTextView);
            TextView durationTextView = itemView.findViewById(R.id.durationTextView);
            TextView dateOfRecordingTextView = itemView.findViewById(R.id.dateOfRecordingTextView);
            titleTextView.setText(current.getName());
            durationTextView.setText(current.getDuration());
            dateOfRecordingTextView.setText(current.getDateOfRecording());
            return itemView;
        }
    }
}