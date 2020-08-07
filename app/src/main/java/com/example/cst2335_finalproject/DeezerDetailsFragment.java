package com.example.cst2335_finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.cst2335_finalproject.DeezerActivity.Song;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeezerDetailsFragment extends Fragment {

    DeezerDB db;
    private Bundle dataFromActivity;
    ArrayList<Song> tracklist = new ArrayList<>();
    Bitmap bm;
    ArrayList<Bitmap> albumsCovers;
    private long id;
    int isSend;
    private AppCompatActivity parentActivity;
    TrackListAdapter adapter;



    public DeezerDetailsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        albumsCovers = new ArrayList<>();
        db = new DeezerDB(getContext());
        db.getWritableDatabase();
        tracklist = db.getAll();

        dataFromActivity = getArguments();
        albumsCovers = dataFromActivity.getParcelableArrayList("covers");

        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_deezer_details, container, false);

        //show the message
        TextView message = (TextView)result.findViewById(R.id.fragmessage);
        message.setText(getString(R.string.deezer_favstitle));

        //favsView
        ListView favsView = (ListView)result.findViewById(R.id.favView);
        favsView.setAdapter(adapter = new TrackListAdapter());

        favsView.setOnItemClickListener((p, b, pos, id) -> {
            Intent nextActivity = new Intent(getActivity(), SelectedSong.class);

            Bundle dataToPass = new Bundle();
            dataToPass.putSerializable("song", tracklist.get(pos));
            dataToPass.putParcelable("artwork", albumsCovers.get(pos));
            nextActivity.putExtras(dataToPass);
            startActivity(nextActivity);

        });

        favsView.setOnItemLongClickListener( (p, b, pos, id) -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            Song song = tracklist.get(pos);

            alertDialogBuilder.setTitle(R.string.deezer_herearedetails)

                    //What is the message:
                    .setMessage(getString(R.string.deezer_selectedrow) + " " + pos + "\n\n" +
                            getString(R.string.deezer_songtitle) + " " + song.getSongTitle() + "\n\n" +
                            getString(R.string.deezer_songduration) + " " + song.getDuration() + "\n\n" +
                            getString(R.string.deezer_albumtitle) + " " + song.getAlbumTitle() + "\n\n" +
                            getString(R.string.deezer_albumcover) + " " + song.getAlbumCover() //generate albumView xml to display this cover actually
                    )

                    .setNegativeButton(getString(R.string.deezer_deletebutton), (click, arg) -> {
                        db.deleteSong(song);
                        albumsCovers.remove(pos);
                        tracklist.remove(pos);
                        //tracklist = db.getAll();
                        adapter.notifyDataSetChanged();


                    })

                    //Show the dialog
                    .create().show();

            return true;
        });

        // button hides the favourites fragment
        Button hideButton = (Button)result.findViewById(R.id.hideButton);
        hideButton.setOnClickListener(btn -> {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            albumsCovers.clear();
            getActivity().finish();
        });

        return result;
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
        //last week we returned (long) position. Now we return the object's database id that we get from line 71
        public long getItemId(int position)
        {
            return  tracklist.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Song song = (Song) getItem(position);
            //Bitmap cover = albumsCovers.get(position);

            LayoutInflater inflater = getLayoutInflater();
            View newView = inflater.inflate(R.layout.searchedsong, parent, false);

            TextView songInfo = newView.findViewById(R.id.songDetails);
            songInfo.setText(song.getSongTitle());

            ImageView coverInfo = newView.findViewById((R.id.albumImage));


            return newView;
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //context will either be FragmentExample for a tablet, or EmptyActivity for phone
        parentActivity = (AppCompatActivity)context;
    }

}
