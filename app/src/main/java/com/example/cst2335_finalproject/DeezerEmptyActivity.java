package com.example.cst2335_finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;

import com.example.cst2335_finalproject.DeezerActivity.Song;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DeezerEmptyActivity extends AppCompatActivity {

    Bitmap albumCover;
    ArrayList<Bitmap> albumCovers;
    ArrayList<Song> tracklist;
    Bitmap artwork;
    DeezerDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_deezer);

        tracklist = new ArrayList<>();
        db = new DeezerDB(this);
        db.getWritableDatabase();
        tracklist = db.getAll();
        albumCovers = new ArrayList<>();

        CoverQuery coverQuery = new CoverQuery();
        coverQuery.execute();

        Bundle dataToPass = new Bundle();
        tracklist = (ArrayList<Song>) getIntent().getSerializableExtra("tracklist");

        dataToPass.putParcelableArrayList("covers", albumCovers);
        DeezerDetailsFragment aFragment = new DeezerDetailsFragment();
        aFragment.setArguments(dataToPass);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLocation, aFragment)
                . commit();

    }

    private class CoverQuery extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... args) {

            for (int i = 0; i < tracklist.size(); i++) {
                try {
                    URL albumCoverURL = new URL(tracklist.get(i).getAlbumCover());
                    HttpURLConnection albumCoverConnection = (HttpURLConnection) albumCoverURL.openConnection();
                    albumCoverConnection.connect();
                    int responseCode = albumCoverConnection.getResponseCode();

                    if (responseCode == 200) {
                        albumCover = BitmapFactory.decodeStream(albumCoverConnection.getInputStream());
                        FileOutputStream outputStream = openFileOutput(albumCover + ".png", Context.MODE_PRIVATE);
                        albumCover.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    }

                    String imageFilename = albumCover + ".png";
                    FileInputStream fis = null;
                    try {
                        fis = openFileInput(albumCover + ".png");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    artwork = BitmapFactory.decodeStream(fis);
                    albumCovers.add(artwork);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        albumCovers.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracklist = db.getAll();
    }
}
