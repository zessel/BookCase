package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.temple.audiobookplayer.AudiobookService;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

    Context parent;
    String title;
    int progress;
    int duration;
    SeekBar seekBar;
    TextView textView;
    Button pausePlayButton;
    boolean paused;
    boolean stopped;

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlayerFragmentInterface)
            parent = context;
        else
            throw new RuntimeException("Didn't implement PlayerFragment's interface");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        seekBar = view.findViewById(R.id.seekBar);
        textView = view.findViewById(R.id.playerTitleTextView);
        pausePlayButton = view.findViewById(R.id.pauseplay);
        //title = "";
        //progress = 0;
        stopped = true;
        seekBar.setProgress(progress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                paused = true;
                ((PlayerFragmentInterface) parent).playPauseClicked();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((PlayerFragmentInterface) parent).userMovedSeekBar(seekBar.getProgress());
                ((PlayerFragmentInterface) parent).playPauseClicked();
                paused = false;
            }
        });

        ((Button) view.findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PlayerFragmentInterface) parent).stopClicked();
                stopped = true;
            }
        });

        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PlayerFragmentInterface) parent).playPauseClicked();
                if (!paused){
                    pausePlayButton.setBackgroundResource(R.drawable.ic_play_circle_outline_black_24dp);
                    paused = true;
                }
                else if (paused){
                    pausePlayButton.setBackgroundResource(R.drawable.ic_pause_circle_outline_black_24dp);
                    paused = false;
                }
            }
        });
        ((PlayerFragmentInterface)parent).fragmentCreated();
        return view;
    }

 /*   Handler progressHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
            if (!paused) {
                progress = bookProgress.getProgress();
                Log.d("PLAYING ", "" + progress);
            }
            seekBar.setProgress(progress);
            return true;}
    });
*/
    public void updatePlayer (String title){
        this.title = title;
        textView.setText("Now Playing - " + title);
        paused = false;
        stopped = false;
    }

    public void updateSeekBar (int progress){
        seekBar.setProgress(progress);
    }

    interface PlayerFragmentInterface{
        void userMovedSeekBar(int progress);
        void playPauseClicked();
        void stopClicked();
        void fragmentCreated();
    }
}
