package com.example.heartbeatclassifier;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Snema pravi WAV (PCM 16 kHz mono, 16-bit) v datoteko.
 * Uporabljaj ga samo, če je v Activity že odobren RECORD_AUDIO permission.
 */
public class WavRecorder {
    public static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final Context ctx;
    private final String outPath;

    private AudioRecord recorder;
    private Thread thread;
    private volatile boolean isRecording = false;

    public WavRecorder(Context ctx, String outPath) {
        this.ctx = ctx.getApplicationContext();
        this.outPath = outPath;
    }

    public void start() throws IOException, SecurityException {
        // Permission guard – če ni odobren, vržemo jasno izjemo
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("RECORD_AUDIO permission not granted");
        }

        int minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (minBuf <= 0) minBuf = SAMPLE_RATE; // fallback

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBuf
        );
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IOException("AudioRecord init failed");
        }

        isRecording = true;
        recorder.startRecording();

        final int bufferSize = Math.max(minBuf, 4096);
        thread = new Thread(() -> writePcmToWav(bufferSize), "WavWriter");
        thread.start();
    }

    public void stop() throws IOException {
        isRecording = false;
        if (recorder != null) {
            try { recorder.stop(); } catch (Exception ignored) {}
            recorder.release();
            recorder = null;
        }
        if (thread != null) {
            try { thread.join(); } catch (InterruptedException ignored) {}
            thread = null;
        }
    }

    private void writePcmToWav(int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        File outFile = new File(outPath);
        int totalBytes = 0;

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            // rezerviraj 44 bajtov za WAV header
            fos.write(new byte[44]);

            while (isRecording && recorder != null) {
                int n = recorder.read(buffer, 0, buffer.length);
                if (n > 0) {
                    fos.write(buffer, 0, n);
                    totalBytes += n;
                }
            }

            // dopiši WAV header
            writeWavHeader(outFile, totalBytes);
        } catch (IOException e) {
            // raje logiraj v Logcat kot printStackTrace
            android.util.Log.e("WavRecorder", "writePcmToWav error", e);
        }
    }

    private void writeWavHeader(File file, int pcmBytes) throws IOException {
        int channels = 1;
        int byteRate = SAMPLE_RATE * channels * 2; // 16-bit = 2 bajta na vzorec
        int dataSize = pcmBytes;
        int chunkSize = 36 + dataSize;

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(0);
            raf.writeBytes("RIFF");
            raf.write(intLE(chunkSize));
            raf.writeBytes("WAVE");
            raf.writeBytes("fmt ");
            raf.write(intLE(16));                 // subchunk1 size
            raf.write(shortLE((short) 1));        // PCM
            raf.write(shortLE((short) channels)); // mono
            raf.write(intLE(SAMPLE_RATE));
            raf.write(intLE(byteRate));
            raf.write(shortLE((short) (channels * 2))); // block align
            raf.write(shortLE((short) 16));       // bits per sample
            raf.writeBytes("data");
            raf.write(intLE(dataSize));
        }
    }

    private byte[] intLE(int v) {
        return new byte[]{(byte) (v), (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24)};
    }

    private byte[] shortLE(short v) {
        return new byte[]{(byte) (v), (byte) (v >> 8)};
    }
}

