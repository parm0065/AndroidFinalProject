package com.example.cst2335_finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Uses the Deezer api to search and list the top 50 song list of a searched artist
 */
public class DeezerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<Song> tracklist = new ArrayList<>();
    ArrayList<Song> favourites = new ArrayList<>();
    TrackListAdapter adapter;
    ImageView albumCoverView;
    //SongQuery songQuery = new SongQuery();
    EditText searchField;
    ProgressBar progressBar;
    String songListUrl;
    TextView trackListTitle;
    Bitmap albumCover = null;
    List<Bitmap> albumsCovers = new ArrayList<>();
    List<Bitmap> favSongArt = new ArrayList<>();
    private SharedPreferences prefs;
    private String savedSearchString;
    SQLiteDatabase db;
    String artistName;
    String coverLink;
    Bitmap bm;
    public static final long serialVersionUID = 1L;
    public static final String ARTIST = "ARTIST";
    public static final String SONG = "SONG";
    public static final String ITEM_ID = "ID";
    public static final String DURATION = "DURATION";
    public static final String ALBUM = "ALBUM";
    public static final String COVER = "COVER";

    DeezerDetailsFragment aFragment;
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deezer);

        albumCoverView = null;
        prefs = getSharedPreferences("FileName", Context.MODE_PRIVATE);
        savedSearchString = prefs.getString("ReserveName", null);
        trackListTitle = findViewById(R.id.artistName);
        progressBar = findViewById(R.id.deezerProgressBar);
        searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);
        Button favsButton = findViewById(R.id.favsButton);
        SongQuery songQuery = new SongQuery();
        searchField.setText(savedSearchString);
        //loadDataFromDatabase();

        //This gets the toolbar from the layout:
        Toolbar tBar = (Toolbar)findViewById(R.id.toolbar);

        //This loads the toolbar, which calls onCreateOptionsMenu below:
        setSupportActionBar(tBar);

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.deezeropen, R.string.deezerclose);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListView songListView = findViewById(R.id.songListWindow);
        songListView.setAdapter(adapter = new TrackListAdapter());

        songListView.setOnItemClickListener((p, b, pos, id) -> {
            Intent nextActivity = new Intent(DeezerActivity.this, SelectedSong.class);

            Bundle dataToPass = new Bundle();
            dataToPass.putSerializable("song", tracklist.get(pos));
            dataToPass.putParcelable("artwork", albumsCovers.get(pos));
            nextActivity.putExtras(dataToPass);
            startActivity(nextActivity);
            //nextActivity.putExtra("song", tracklist.get(pos));
            //startActivity (nextActivity);
        });

        /**
         * long click listener for when user presses on a song from ListView, displays Alert
         * with details of the selected song
         */
        songListView.setOnItemLongClickListener( (p, b, pos, id) -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            Song song = tracklist.get(pos);

            alertDialogBuilder.setTitle(R.string.deezer_herearedetails)

                    //What is the message:
                    .setMessage(getString(R.string.deezer_selectedrow) + " " + pos + "\n\n" +
                        getString(R.string.deezer_songtitle) + " " + song.getSongTitle() + "\n\n" +
                            getString(R.string.deezer_songduration) + " " + song.getDuration() + "\n\n" +
                            getString(R.string.deezer_albumtitle) + " " + song.getAlbumTitle() + "\n\n" +
                            getString(R.string.deezer_albumcover) + " " + song.getAlbumCover() //generate albumView xml to display this cover actually
                    )

                    //positive button to favourite the song which then put song in DB and opens fragment with the favourties db view
                    .setPositiveButton(getString(R.string.deezer_addtofavs), (click, arg) -> {
                        ContentValues newRowValues = new ContentValues();
                        newRowValues.put(DeezerDB.ARTIST, artistName);
                        newRowValues.put(DeezerDB.SONG, song.getSongTitle());
                        newRowValues.put(DeezerDB.DURATION, song.getDuration());
                        newRowValues.put(DeezerDB.ALBUM, song.getAlbumTitle());
                        newRowValues.put(DeezerDB.COVER, song.getAlbumCover());

                        db.insert(DeezerDB.TABLE_NAME, null, newRowValues);
                        favSongArt.add(albumsCovers.get(pos));
                        loadDataFromDatabase(); //should now update the tracklist and favourite lists properly

                        adapter.notifyDataSetChanged();


                    })

                    //Show the dialog
                    .create().show();

            return true;
        });

        /**
         * temporary snack bar showing "coming soon" when user clicks search button
         */
//        Snackbar comingSoon = Snackbar.make(findViewById(R.id.searchButton),
//                "search functionality coming soon",
//                Snackbar.LENGTH_LONG);

        searchButton.setOnClickListener(btn -> {
            //comingSoon.show();
            artistName = searchField.getText().toString();
            tracklist.clear();
            new SongQuery().execute("https://api.deezer.com/search/artist/?q=" + searchField.getText().toString().replace(" ", "") + "&output=xml");
            saveSharedPrefs(searchField.getText().toString());
        });

        favsButton.setOnClickListener((btn -> {
            Intent nextActivity = new Intent(DeezerActivity.this, DeezerEmptyActivity.class);
            Bundle dataToPass = new Bundle();
            dataToPass.putSerializable("tracklist", favourites);

            nextActivity.putExtras(dataToPass);
            startActivity (nextActivity);
        }));
    }

    @Override
    protected void onPause(){
        super.onPause();
        savedSearchString = searchField.getText().toString();
        saveSharedPrefs(searchField.getText().toString());
    }

    @Override
    protected void onStop(){
        super.onStop();
        savedSearchString = searchField.getText().toString();
        saveSharedPrefs(searchField.getText().toString());
    }

    @Override
    protected void onResume(){
        super.onResume();
        searchField.setText(savedSearchString);
        loadDataFromDatabase();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        searchField.setText(savedSearchString);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deezer_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message = null;
        //Look at your menu XML file. Put a case for every id in that file:
        switch(item.getItemId())
        {
            //what to do when the menu item is selected:
            case R.id.aboutProject:
                message = getString(R.string.deezer_credits);
                break;
            case R.id.geoChoice1:
            Intent goToGeo = new Intent(DeezerActivity.this, DeezerActivity.class);
            startActivity(goToGeo);
            break;
            case R.id.lyricsChoice2:
                Intent goToLyrics = new Intent(DeezerActivity.this, DeezerActivity.class);
                startActivity(goToLyrics);
                break;
            case R.id.soccerChoice3:
                Intent goToSoccer = new Intent(DeezerActivity.this, DeezerActivity.class);
                startActivity(goToSoccer);
                break;

        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        return true;
    }

    protected void deleteFaveSong(Song song)
    {
        db.delete(DeezerDB.TABLE_NAME, DeezerDB.SONG + "= ?", new String[] {song.getSongTitle()});
    }

    private void loadDataFromDatabase() {
        DeezerDB dbOpener = new DeezerDB(this);
        db = dbOpener.getWritableDatabase(); //This calls onCreate() if you've never built the table before, or onUpgrade if the version here is newer


        // We want to get all of the columns. Look at MyOpener.java for the definitions:
        String [] columns = {DeezerDB.COL_ID, DeezerDB.ARTIST, DeezerDB.SONG, DeezerDB.DURATION, DeezerDB.ALBUM, DeezerDB.COVER};
        //query all the results from the database:
        Cursor results = db.query(false, DeezerDB.TABLE_NAME, columns, null, null, null, null, null, null);

        //Now the results object has rows of results that match the query.
        //find the column indices:
        int artistColumnIndex = results.getColumnIndex(DeezerDB.ARTIST);
        int songColumnIndex = results.getColumnIndex(DeezerDB.SONG);
        int durationColumnIndex = results.getColumnIndex(DeezerDB.DURATION);
        int albumColIndex = results.getColumnIndex(DeezerDB.ALBUM);
        int coverColIndex = results.getColumnIndex(DeezerDB.COVER);
        int idColIndex = results.getColumnIndex(DeezerDB.COL_ID);

        //clearing the favourites list so the list doesn't double, triple etc...
        if(favourites.size() > 0)
            favourites.clear();

        //iterate over the results, return true if there is a next item:
        while(results.moveToNext())
        {
            String artist = results.getString(artistColumnIndex);
            String song = results.getString(songColumnIndex);
            String duration = results.getString(durationColumnIndex);
            String album = results.getString(albumColIndex);
            String coverLink = results.getString(coverColIndex);
            long id = results.getLong(idColIndex);

            //add the songs to the array list:
            favourites.add(new Song(artist, song, duration, album, coverLink));

        }

        //At this point, the contactsList array has loaded every row from the cursor.
        printCursor(results, db.getVersion());

    }

    protected void printCursor (Cursor c, int version) {
        Log.v("Cursor Object", String.valueOf(db.getVersion()));
        Log.v("Cursor number of cols", String.valueOf(c.getColumnCount()));
        Log.v("Cursor col names", Arrays.toString(c.getColumnNames()));
        Log.v("Cursor number of rows", String.valueOf(c.getCount()));
        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(c));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String message = null;

        switch(item.getItemId()) {
            case R.id.deezer_instructions:
                message = getString(R.string.deezer_deezerclickmsg);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getString(R.string.deezer_instructionstitle))

                        //What is the message:
                        .setMessage(getString(R.string.deezer_instructiondetails))

                        //Show the dialog
                        .create().show();
                break;
            case R.id.about_deezer_api:
                message = getString(R.string.deezer_deezerapiclickmsg);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.deezer.com/guidelines"));
                startActivity(browserIntent);
                break;
            case R.id.deezer_donate:
                EditText donate = new EditText(this); //TODO maybe set it to deezerDonateText
                donate.setFilters(new InputFilter[] {
                DigitsKeyListener.getInstance(false, true),
                });
                donate.setKeyListener(DigitsKeyListener.getInstance());

                message = getString(R.string.deezer_deezerdonateclickmsg);
                AlertDialog.Builder donateDialogBuilder = new AlertDialog.Builder(this);
                donateDialogBuilder.setTitle(getString(R.string.deezer_donate))
                        .setMessage(getString(R.string.deezer_donateamountmsg))
                        .setView(donate)
                        .setPositiveButton(getString(R.string.deezer_thankyoumsg), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Snackbar thanksMsg = Snackbar.make(findViewById(R.id.searchButton),
                                        getString(R.string.deezer_thankyoumsg),
                                        Snackbar.LENGTH_LONG);
                                thanksMsg.show();
                            }
                        })
                        .setNegativeButton(getString(R.string.deezer_cancelbutton), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create().show();
                break;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        Toast.makeText(this, "NavigationDrawer: " + message, Toast.LENGTH_LONG).show();
        return false;
    }

    private class TrackListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tracklist.size();
        }

        @Override
        public Object getItem(int position) {
            return tracklist.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return  tracklist.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Song song = (Song) getItem(position);
            Bitmap cover = albumsCovers.get(position);
            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.searchedsong, parent, false);

            TextView songInfo = newView.findViewById(R.id.songDetails);
            songInfo.setText(song.getSongTitle());

            ImageView coverInfo = newView.findViewById((R.id.albumImage));
            coverInfo.setImageBitmap(cover);

            return newView;
        }
    }


    /**
     * parser that extracts songlist given a specified artist
     */
    private class SongQuery extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String ... args) {

            try {
                //create a URL object with server address from args
                URL url = new URL(args[0]);

                //open the connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //wait for the data
                runOnUiThread(new Runnable() {
                      @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.deezer_waitmsg),
                                Toast.LENGTH_LONG).show();
                    }
                });
                publishProgress(20);
                InputStream response = urlConnection.getInputStream();

                //pulling xml
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(response, "UTF-8");

                //the parse is currently at START_DOCUMENT
                int eventType = xpp.getEventType();
                Boolean artistFound = false;

                while(artistFound.equals(false)) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("tracklist")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.TEXT) {
                                songListUrl = xpp.getText();
                                artistFound = true;
                            }
                        }
                    }
                    eventType = xpp.next();
                }
                publishProgress(40);

                URL iconUrl = new URL(songListUrl);
                HttpURLConnection songListConnection = (HttpURLConnection) iconUrl.openConnection();
                songListConnection.connect();

                InputStream songListResponse = songListConnection.getInputStream();

                publishProgress(60);

                //JSON reading:
                //Build the entire string response:
                BufferedReader reader = new BufferedReader(new InputStreamReader(songListResponse, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                String result = sb.toString(); //result is the whole string


                // convert string to JSON: Look at slide 27:
                JSONObject songListReport = new JSONObject(result);

                JSONArray songList = songListReport.getJSONArray("data");

                for(int i = 0; i < songList.length(); i++) {
                    JSONObject foundSong = songList.getJSONObject(i);

                    //Getting the nested album details
                    JSONObject foundSongAlbumDetails = foundSong.getJSONObject("album");
                    JSONObject foundSongArtistDetails = foundSong.getJSONObject("artist");

                    //Artist Details
                    //int artistID = foundSongArtistDetails.getInt("id");
                    String artistName = foundSongArtistDetails.getString("name");

                    //Album Details
                    //int albumID = foundSongAlbumDetails.getInt("id");
                    String albumTitle = foundSongAlbumDetails.getString("title");

                    //Album Cover
                    coverLink = foundSongAlbumDetails.getString("cover_small");
                    bm = findAlbumCover(coverLink);
                    albumsCovers.add(bm);

                    //Song Details
                    String songTitle = foundSong.getString("title");
                    int durationInSeconds = foundSong.getInt("duration");
                    int minute = durationInSeconds / 60;
                    int second = durationInSeconds % 60;
                    String duration = minute + "min " + second + "sec";

                    publishProgress(80);

                    Song song = new Song(artistName, songTitle, duration, albumTitle, coverLink);
                    tracklist.add(song);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackListTitle.setText(getString(R.string.deezer_resultstitle) + " " + artistName);
//
                        }

                    });
                }

            } catch (Exception e) {
                Log.e("parsing error", e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

            }
            //songQuery.cancel(true);
            return null;
        }

        public void onProgressUpdate(Integer ...value)
        {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(value[0]);

        }

        public void onPostExecute(String fromDoInBackground)
        {
            searchField.setText(artistName);
            progressBar.setVisibility(View.INVISIBLE);
            trackListTitle.setVisibility(View.VISIBLE);

        }
    }

    public Bitmap findAlbumCover(String coverLink) {
        Bitmap artwork = null;
        try {
            URL albumCoverURL = new URL(coverLink);
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

            FileInputStream fis = null;
            try {
                fis = openFileInput(albumCover + ".png");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            artwork = BitmapFactory.decodeStream(fis);

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }

        return artwork;
    }

    /**
     * song object which stores all the relevant information that makes up a song
     */
    protected static class Song implements Serializable {
        String artist;
        String title;
        long id;
        String duration;
        String albumTitle;
        String coverLink;

        protected Song(String artist, String title, String duration, String albumTitle, String coverLink) {
            this.artist = artist;
            this.title = title;
            this.duration = duration;
            this.albumTitle = albumTitle;
            this.coverLink = coverLink;
        }

        protected String getArtist() { return artist; }

        protected String getSongTitle() {
            return title;
        }

        protected String getDuration() {
            return duration;
        }

        protected String getAlbumTitle() {
            return albumTitle;
        }

        protected String getAlbumCover() { return coverLink; }

        public void setId(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

    }

    private void saveSharedPrefs(String stringToSave) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ReserveName", stringToSave);
        editor.commit();

    }

}
