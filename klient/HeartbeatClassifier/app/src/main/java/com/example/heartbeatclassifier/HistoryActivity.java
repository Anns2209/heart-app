package com.example.heartbeatclassifier;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private HistoryAdapter adapter;
    private final List<Measurement> data = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView rv = findViewById(R.id.recyclerHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(data);
        rv.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        ApiService api = RetrofitClient.getApiService();
        api.getHistory().enqueue(new Callback<List<Measurement>>() {
            @Override public void onResponse(Call<List<Measurement>> call, Response<List<Measurement>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    data.clear();
                    data.addAll(res.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(HistoryActivity.this, "Napaka: " + res.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Measurement>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Napaka povezave: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
