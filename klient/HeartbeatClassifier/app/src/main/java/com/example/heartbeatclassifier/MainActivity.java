package com.example.heartbeatclassifier;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    private static final String DOCTOR_EMAIL = "ana.gjorceska@gmail.com"; // <-- zamenjaj


    private WavRecorder wavRecorder;

    private static final int PICK_WAV_FILE = 1;

    private TextView resultTextView;
    private Button selectButton;

    private ImageView resultIcon;

    private ImageView spectrogramImage;

    private static final int REQUEST_MICROPHONE = 200;
    private MediaRecorder recorder;
    private String recordedFilePath;
    private Button recordButton;

    private Button btnHistory;
    private boolean isRecording = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.resultTextView);
        selectButton = findViewById(R.id.selectButton);
        recordButton = findViewById(R.id.recordButton);
        Button btnHistory = findViewById(R.id.btnHistory);

        btnHistory.setOnClickListener(v -> {
            startActivity(new android.content.Intent(MainActivity.this, HistoryActivity.class));
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
                } else {
                    toggleRecording();
                }
            }
        });


        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_WAV_FILE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_WAV_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            uploadFile(fileUri);
        }
    }

    private void uploadFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();

            String fileName = getFileName(fileUri);  // npr. test.wav

            // Ustvari telo zahteve z "multipart/form-data"
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("audio/wav"),
                    fileBytes
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            ApiService apiService = RetrofitClient.getApiService();
            Call<ResponseBody> call = apiService.uploadFile(body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String result = response.body().string();

                            JSONObject jsonObject = new JSONObject(result);
                            String label = jsonObject.getString("heartbeat_status");
                            String spectrogramBase64 = jsonObject.getString("spectrogram");

                            TextView resultTextView = findViewById(R.id.resultTextView);
                            ImageView resultIcon = findViewById(R.id.resultIcon);
                            ImageView spectrogramImage = findViewById(R.id.spectrogramImage); // Dodaj v layout

                            // Nastavi tekst in ikono
                            if (label.contains("abnormal")) {
                                resultIcon.setImageResource(R.drawable.ic_warning);
                                resultTextView.setText("Utrip je ABNORMALEN!");
                                resultTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                resultIcon.setVisibility(View.VISIBLE);
                            } else if (label.contains("normal")) {
                                resultTextView.setText("Utrip je normalen.");
                                resultTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                resultIcon.setImageResource(R.drawable.ic_heart);
                                resultIcon.setVisibility(View.VISIBLE);
                            } else {
                                resultTextView.setText("Ni bilo mogoče prepoznati rezultata.");
                                resultTextView.setTextColor(getResources().getColor(android.R.color.black));
                                resultIcon.setVisibility(View.GONE);
                            }

                            double pNormal = 0.0, pAbnormal = 0.0;
                            try {
                                org.json.JSONObject probs = jsonObject.getJSONObject("probabilities");
                                pNormal = probs.optDouble("normal", 0.0);
                                pAbnormal = probs.optDouble("abnormal", 0.0);
                            } catch (Exception ignored) {}


                            if (label.toLowerCase().contains("abnormal")) {
                                String maybeFilename = getFileName(fileUri);
                                String subject = "Opozorilo: Abnormalen srčni utrip";
                                String body = buildEmailBody(label, pNormal, pAbnormal, maybeFilename);

                                composeEmail(DOCTOR_EMAIL, subject, body);
                            }



                            // 🎨 Dekodiraj in prikaži sliko
                            byte[] decodedBytes = Base64.decode(spectrogramBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            spectrogramImage.setImageBitmap(bitmap);
                            spectrogramImage.setVisibility(View.VISIBLE);

                        } else {
                            resultTextView.setText("Napaka: " + response.code());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resultTextView.setText("Napaka pri branju odgovora");
                    }
                }



                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    resultTextView.setText("Napaka pri povezavi: " + t.getMessage());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Napaka pri branju datoteke", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRecording() {
        if (!isRecording) {
            // 1) permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
                return;
            }

            // 2) start
            recordedFilePath = getExternalCacheDir().getAbsolutePath() + "/recorded_audio.wav";
            wavRecorder = new WavRecorder(this, recordedFilePath);
            try {
                wavRecorder.start();
                isRecording = true;
                recordButton.setText("Ustavi");
            } catch (SecurityException se) {
                se.printStackTrace();
                Toast.makeText(this, "Ni dovoljenja za mikrofon", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Napaka pri zagonu snemanja", Toast.LENGTH_SHORT).show();
            }

        } else {
            // 3) stop & upload
            try { if (wavRecorder != null) wavRecorder.stop(); } catch (IOException ignored) {}
            isRecording = false;
            recordButton.setText("Snemaj");
            uploadFile(Uri.fromFile(new File(recordedFilePath))); // pošlje pravi WAV
        }
    }


    private String getFileName(Uri uri) {
        String result = "file.wav";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (index >= 0) {
                result = cursor.getString(index);
            }
            cursor.close();
        }
        return result;
    }

    private void composeEmail(String to, String subject, String body) {
        // mailto:doctor@example.com?subject=...&body=...
        android.net.Uri uri = android.net.Uri.parse(
                "mailto:" + android.net.Uri.encode(to) +
                        "?subject=" + android.net.Uri.encode(subject) +
                        "&body=" + android.net.Uri.encode(body)
        );

        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO, uri);
        try {
            startActivity(android.content.Intent.createChooser(intent, "Pošlji e-pošto…"));
        } catch (android.content.ActivityNotFoundException e) {
            android.widget.Toast.makeText(this, "Ni nameščenega e-poštnega odjemalca.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }


    private String buildEmailBody(String label, double pNormal, double pAbnormal, @Nullable String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append("Pozdravljeni,\n\n");
        sb.append("Aplikacija je zaznala ABNORMALEN srčni utrip.\n\n");
        sb.append("Čas: ").append(new java.util.Date().toString()).append("\n");
        if (filename != null) sb.append("Datoteka: ").append(filename).append("\n");
        sb.append(String.format(java.util.Locale.US, "Verjetnosti: normal=%.3f, abnormal=%.3f\n\n", pNormal, pAbnormal));
        sb.append("Lep pozdrav,\n");
        sb.append("HeartbeatClassifier");
        return sb.toString();
    }

}
