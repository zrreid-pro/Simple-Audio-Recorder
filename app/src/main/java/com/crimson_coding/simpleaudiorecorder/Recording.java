package com.crimson_coding.simpleaudiorecorder;

import android.net.Uri;
import android.util.Log;

public class Recording {
    private String name;
    private String duration;
    private long min;
    private int sec;
    private String dateOfRecording;
    private String fileSuffix = ".3gp";
    private Uri uri;

    public Recording(String name, String duration, String recordingDate, Uri uri) {
        this.name = name.substring(0,name.indexOf("."));
        Log.i("DEBUG_RECORDING_ITEM", this.name);


        /*long millis = Long.getLong(duration);
        if(millis >= 6000) {
            min = (millis/1000)/60;
            Log.i("DEBUG_RECORDING_ITEM", String.valueOf(min));
        }
        else {
            min = 0;
            Log.i("DEBUG_RECORDING_ITEM", String.valueOf(min));
        }
        sec = (int) (millis/1000)%60;
        Log.i("DEBUG_RECORDING_ITEM", String.valueOf(sec));
        this.duration = min + ":" + sec;*/
        this.duration = duration;
        Log.i("DEBUG_RECORDING_ITEM", this.duration);

        /*SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        Date date = new Date(Long.valueOf(recordingDate));
        this.dateOfRecording = sdf.format(date);*/
        //this.dateOfRecording = "(October 20, 2019)";
        this.dateOfRecording = recordingDate;
        Log.i("DEBUG_RECORDING_ITEM", this.dateOfRecording);
        /*try {
            Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).parse(recordingDate);
            dateOfRecording =
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        Log.i("DEBUG_RECORDING_ITEM", name + " : " + duration + " : " + dateOfRecording);
        this.uri = uri;

    }

    /*public Recording(String length) {
        this.length = length;

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        dateOfRecording = sdf.format(now);
    }*/

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getFileName() { return name + fileSuffix; }

    public String getDuration() { return duration; }

    public void setDuration(String duration) { this.duration = duration; }

    public String getDateOfRecording() { return dateOfRecording; }

    public void setDateOfRecording(String dateOfRecording) { this.dateOfRecording = dateOfRecording; }

    public Uri getUri() { return uri; }
}