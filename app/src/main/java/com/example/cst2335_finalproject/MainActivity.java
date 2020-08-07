package com.example.cst2335_finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button deezerButton = findViewById(R.id.deezerButton);
        Button soccerButton = findViewById(R.id.soccerButton);
        Button lyricsButton = findViewById(R.id.songButton);
        Button geoButton = findViewById(R.id.geoButton);

        deezerButton.setOnClickListener(btn -> {
            Intent goToDeezer = new Intent(MainActivity.this, DeezerActivity.class);
            startActivity(goToDeezer);
        });

        soccerButton.setOnClickListener(btn -> {
            Intent goToDeezer = new Intent(MainActivity.this, DeezerActivity.class);
            startActivity(goToDeezer);
        });

        lyricsButton.setOnClickListener(btn -> {
            Intent goToDeezer = new Intent(MainActivity.this, DeezerActivity.class);
            startActivity(goToDeezer);
        });

        geoButton.setOnClickListener(btn -> {
            Intent goToDeezer = new Intent(MainActivity.this, DeezerActivity.class);
            startActivity(goToDeezer);
        });

    }
}
