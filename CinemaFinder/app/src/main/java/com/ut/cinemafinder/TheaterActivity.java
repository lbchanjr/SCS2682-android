package com.ut.cinemafinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class TheaterActivity extends AppCompatActivity {
    private static final String TAG = "TheaterActivity";
    ArrayList<Theater> theaters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entered Theater Activity from intent");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater);

        ArrayList<String> theaters_str = getIntent().getStringArrayListExtra("theaters_in_string");
        if(theaters_str == null) {
            return;
        }
        for(int i = 0; i < theaters_str.size(); i = i + 4) {
            String[] latlng = theaters_str.get(i+3).split(",");
            String lat_str = latlng[0].substring(latlng[0].indexOf('(')+1);
            String long_str = latlng[1].substring(0, latlng[1].indexOf(')'));
            LatLng coords = new LatLng(Double.parseDouble(lat_str), Double.parseDouble(long_str));
            theaters.add(new Theater(theaters_str.get(i), theaters_str.get(i+1), theaters_str.get(i+2), coords));
        }

        CustomAdapter customListAdapter = new CustomAdapter(this, theaters);
        ListView customListView = findViewById(R.id.custom_list_id);
        customListView.setAdapter(customListAdapter);

        customListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(TheaterActivity.this, "Sending link to external browser...", Toast.LENGTH_LONG).show();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(theaters.get(position).url));
                        startActivity(browserIntent);
                    }
                }
        );
    }
}
