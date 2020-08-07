package com.example.cst2335_finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.cst2335_finalproject.DeezerActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class DeezerDB extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "FavouritesDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "FAVSONGS";
    public final static String ARTIST = "ARTIST";
    public final static String DURATION = "DURATION";
    public final static String SONG = "SONG";
    public final static String COVER = "COVER";
    public final static String ALBUM = "ALBUM";
    public final String[] columns = {COL_ID, ARTIST, SONG, DURATION, ALBUM, COVER};

    public final static String COL_ID = "_id";


    public DeezerDB(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }


    //This function gets called if no database file exists.
    //Look on your device in the /data/data/package-name/database directory.
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ARTIST + " text,"
                + SONG + " text,"
                + DURATION + " text,"
                + ALBUM + " text,"
                + COVER + " text);");  // add or remove columns
    }

    public ArrayList<Song> getAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Song> favsList = new ArrayList<>();
        Cursor c = db.query(TABLE_NAME, columns, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            favsList.add(new Song(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)));
            c.moveToNext();
        }
        return favsList;
    }

    public void addSong(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);

    }

    public boolean deleteSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete(TABLE_NAME, SONG + "=" + song.getSongTitle(), null);
        if (db.delete(TABLE_NAME, String.format("%s like \"%s\"", SONG, song.getSongTitle()),null) > 0) {
            return true;
        }
        return false;
    }


    //this function gets called if the database version on your device is lower than VERSION_NUM
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        //Create the new table:
        onCreate(db);
    }

    //this function gets called if the database version on your device is higher than VERSION_NUM
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        //Create the new table:
        onCreate(db);
    }
}
