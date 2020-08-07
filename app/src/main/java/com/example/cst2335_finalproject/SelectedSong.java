package com.example.cst2335_finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.cst2335_finalproject.DeezerActivity.Song;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SelectedSong extends AppCompatActivity {

    Bitmap albumCover = null;
    Bitmap artwork;
    String coverLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selected_song);


        Song song = (Song) getIntent().getSerializableExtra("song");
        artwork = (Bitmap) getIntent().getExtras().get("artwork");

        ImageView albumCover = findViewById(R.id.selected_song_cover);
        albumCover.setImageBitmap(artwork);

        TextView artistName = findViewById(R.id.selected_artist);
        artistName.setText(song.getArtist());

        TextView songName = findViewById(R.id.selected_song_title);
        songName.setText(song.getSongTitle());

        TextView songDuration = findViewById(R.id.selected_duration);
        songDuration.setText(song.getDuration());

        TextView songAlbum = findViewById(R.id.selected_album);
        songAlbum.setText(song.getAlbumTitle());

    }
}
