package com.example.dialer;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Class for working with audio recording and playing
 */
public class RecordWorker {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private final String recordDirectoryName = Environment.getExternalStorageDirectory() + "/dialer";
    private final String recordFileName = recordDirectoryName + "/record.mp3";

    /**
     * Start record
     */
    public void recordStart() {
        // not start record if it's already recording
        if (mediaRecorder != null)
            return;

        try {
            releaseRecorder();

            // check if directory exists
            File directory = new File(recordDirectoryName);
            if (!directory.exists()) {
                directory.mkdir();
            }

            File outFile = new File(recordFileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(recordFileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop record
     */
    public void recordStop() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Play recorded audio
     */
    public void playStart() {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(recordFileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop playing recorded audio
     */
    public void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * Clear recorder
     */
    public void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**
     * Clear player
     */
    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
