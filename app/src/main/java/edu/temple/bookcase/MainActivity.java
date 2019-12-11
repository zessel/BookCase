package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Set;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.GetBookInterface,
        BookDetailsFragment.BookDetailsFragmentInterface, PlayerFragment.PlayerFragmentInterface {

    final String BINDER_KEY = "binder";
    final String CURRENTBOOK_KEY = "current book";
    final String ISFILE_KEY = "isfile";
    final String SEARCH_KEY = "search";
    ViewPagerFragment viewPagerFragment;
    BookListFragment bookListFragment;
    BookDetailsFragment bookDetailsFragment;
    PlayerFragment playerFragment;
    String[] titles;
    ArrayList<Book> books;
    int booksArrayLength;
    Button button;
    EditText editText;
    Fragment fragment;
    FrameLayout player;
    AudiobookService.MediaControlBinder binder;
    ServiceConnection serviceConnection;
    Intent intent;
    boolean connected;
    int progress;
    String playingTitle;
    File stateData;
    String searchTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        searchTerm = sharedPref.getString(SEARCH_KEY, "");

        player = findViewById(R.id.playframe);
        player.setVisibility(View.GONE);

        intent = new Intent(this, AudiobookService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service){
                connected = true;
                binder = (AudiobookService.MediaControlBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0){
                connected = false;
                binder = null;
            }
        };

        if (savedInstanceState != null) {
            binder = (AudiobookService.MediaControlBinder) savedInstanceState.getBinder(BINDER_KEY);
            binder.setProgressHandler(progressHandler);
            progress = savedInstanceState.getInt("progress");
            playingTitle = savedInstanceState.getString("now playing");
/*
            playerFragment.progress = savedInstanceState.getInt("progress");
            playerFragment.stopped = savedInstanceState.getBoolean("connected");
            playerFragment.paused = savedInstanceState.getBoolean("paused");
            binder.setProgressHandler(playerFragment.progressHandler);

 //           Log.d("Hash original", "" + savedInstanceState.getInt("int"));
 //           Log.d("Hash new", "" + playerFragment.hashCode());

 */
        }
        else {
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        fragment = getSupportFragmentManager().findFragmentById(R.id.playframe);
        if (!(fragment instanceof PlayerFragment)){
            playerFragment = new PlayerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.playframe, playerFragment).commit();
        }
        else {
            playerFragment = (PlayerFragment) fragment;
            if (!playerFragment.stopped) {
                player.setVisibility(View.VISIBLE);
            }
            binder.setProgressHandler(progressHandler);
            //    Log.d("TESTING", " " +playerFragment.progress + " " + playerFragment.textView.toString());
        }
        fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
        editText = findViewById(R.id.searchText);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTerm = editText.getText().toString();
                Thread bookSearch = new Thread(){
                    @Override
                    public void run(){
                        bookSearchResponseHandler.sendMessage(search(searchTerm));
                    }
                };bookSearch.start();
            }
        });
        initialBookSearch();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stateData = new File (getFilesDir().toString(), "StateData");
        if (!stateData.exists()) {
            try {
                stateData.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SEARCH_KEY, searchTerm);

        if (binder != null && binder.isPlaying()) {
            editor.putInt(playerFragment.title, progress);
        }
        if (binder != null && !binder.isPlaying()) {
            if (connected == true)
                unbindService(serviceConnection);
            stopService(intent);
        }

        editor.apply();
        editor.commit();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBinder(BINDER_KEY, binder);
        savedInstanceState.putInt("int", playerFragment.hashCode());
        savedInstanceState.putInt("progress", playerFragment.progress);
        savedInstanceState.putString("now playing", playerFragment.title);
        savedInstanceState.putBoolean("connected", playerFragment.stopped);
        savedInstanceState.putBoolean("paused", playerFragment.paused);
        super.onSaveInstanceState(savedInstanceState);
    }

    Handler bookResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
            JSONArray responseArray;
            MainActivity.this.books = books = new ArrayList<>();

            if (fragment instanceof ViewPagerFragment) {
                MainActivity.this.books = books = ((ViewPagerFragment) fragment).getBooksAsArrayList();
            }
            else if (fragment instanceof BookListFragment){
                MainActivity.this.books = books = ((BookListFragment) fragment).getBooksAsArrayList();
            }
            else {
                responseArray = (JSONArray) msg.obj;

                Log.d("HANDLER-Retrieved books", "" + responseArray.toString());
                booksArrayLength = responseArray.length();
                titles = new String[booksArrayLength];
                try {
                    for (int i = 0; i < booksArrayLength; i++) {
                        if (responseArray.getJSONObject(i).has("coverURL")) {
                            Log.d("CHECKING", "" + responseArray.getJSONObject(i).getString("coverURL"));
                        }
                        books.add(new Book(responseArray.getJSONObject(i)));
                        Log.d("HANDLER", "" + responseArray.getJSONObject(i).toString());
                        titles[i] = responseArray.getJSONObject(i).getString("title");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            MainActivity.this.viewPagerFragment = ViewPagerFragment.newInstance(books);
            MainActivity.this.bookListFragment = BookListFragment.newInstance(books);
            MainActivity.this.bookDetailsFragment = BookDetailsFragment.newInstance(new Book(0,"","",0,0,""));

            if (findViewById(R.id.frame2) == null) {
                if (fragment instanceof BookListFragment) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                getSupportFragmentManager().beginTransaction().add(R.id.frame1, MainActivity.this.viewPagerFragment).commit();
            } else {
                if (fragment instanceof ViewPagerFragment) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                fragment = getSupportFragmentManager().findFragmentById(R.id.frame2);
                if (fragment != null){
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                getSupportFragmentManager().beginTransaction().add(R.id.frame1, MainActivity.this.bookListFragment).commit();
                getSupportFragmentManager().beginTransaction().add(R.id.frame2, MainActivity.this.bookDetailsFragment).commit();
            }
            return false;
            }
        });

    private void initialBookSearch()
    {
        Thread t = new Thread(){
            @Override
            public void run(){

                    bookResponseHandler.sendMessage(search(searchTerm));
            }
        };
        t.start();
    }

    Handler bookSearchResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            JSONArray responseArray;
            responseArray = (JSONArray) msg.obj;
            booksArrayLength = responseArray.length();
            titles = new String[booksArrayLength];
            books.clear();
            try {
                for (int i = 0; i < booksArrayLength; i++) {
                    if (responseArray.getJSONObject(i).has("coverURL")) {
                    }
                    books.add(new Book(responseArray.getJSONObject(i)));
                    titles[i] = responseArray.getJSONObject(i).getString("title");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
            Log.d("BLAHBLAH", "" + fragment.toString());
            if (fragment instanceof BookListFragment)
                ((BookListFragment)fragment).updateBooks(books);
            if (fragment instanceof ViewPagerFragment)
                ((ViewPagerFragment)fragment).updateBooks(books);
            return false;
        }
    });
            @Override
    public void bookSelected(Book book) {
                MainActivity.this.bookDetailsFragment.changeBook(book);
    }

    private Message search(String searchTerm) {
        URL fullBookListURL;
        try {
            fullBookListURL = new URL(getResources().getString(R.string.bookSearchAPI)+ searchTerm);

            BufferedReader reader = new BufferedReader(new InputStreamReader(fullBookListURL.openStream()));

            String response = "", tmpResponse;

            tmpResponse = reader.readLine();
            while (tmpResponse != null) {
                response = response + tmpResponse;
                tmpResponse = reader.readLine();
            }

            JSONArray bookArray = new JSONArray(response);
            Message msg = Message.obtain();
            msg.obj = bookArray;
            Log.d("Thread running and sending message", " ABCD  " + bookArray.toString());
            return msg;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    Handler progressHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
            if (!playerFragment.paused) {
                playerFragment.progress = bookProgress.getProgress();
                progress = bookProgress.getProgress();
                playerFragment.updateSeekBar(bookProgress.getProgress());
                Log.d("PLAYING ", "" + bookProgress.getProgress());
            }
            return true;}
    });

    @Override
    public void playButtonClicked(Book book) {

        String filePath = getExternalFilesDir(null).toString() +
                getResources().getString(R.string.audioBookDir) + "/" + book.getTitle() + ".mp3";
        filePath = filePath.replace(" ", "");
        File audioFile = new File(filePath);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (binder.isPlaying()) {
            editor.putInt(playerFragment.title, progress);
            editor.apply();
        }

        progress = sharedPref.getInt(book.getTitle(), 0);
        Log.d("PROGRESS VALUE play", book.getTitle() + " " + progress);

        if (audioFile.exists()) {
            binder.play(audioFile, progress);
            editor.putString(CURRENTBOOK_KEY, book.getTitle());
            editor.putInt(ISFILE_KEY, 1);
        }
        else {
            binder.play(book.getId(), progress);
            editor.putString(CURRENTBOOK_KEY, String.valueOf(book.getId()));
            editor.putInt(ISFILE_KEY, 0);
        }
        editor.apply();
        editor.commit();
        player.setVisibility(View.VISIBLE);
        playerFragment.updatePlayer(book.getTitle());
        playerFragment.seekBar.setMax(book.getDuration());
        binder.setProgressHandler(progressHandler);
    }

    @Override
    public void downloadButtonClicked(Book book) {
        final int id = book.getId();
        final String title = book.getTitle();
        Thread t = new Thread(){
            @Override
            public void run(){
                downloadHandler.sendMessage(download(id, title));
            }
        };
        t.start();
    }

    private Message download(int id, String title) {
        Log.d("downloadASDF", "made it to download");
        URL downloadURL;

        String filePath = getExternalFilesDir(null).toString() +
                getResources().getString(R.string.audioBookDir) + "/" + title + ".mp3";
        filePath = filePath.replace(" ", "");
        Log.d("filePath", filePath);
        File outputFile = new File(filePath);
        outputFile.mkdirs();

        try {
            downloadURL = new URL(getResources().getString(R.string.bookDownload)+ id);
            URLConnection connection = downloadURL.openConnection();
            int connectionLength = connection.getContentLength();
            Log.d("downloadASDF", downloadURL.toString());
            Log.d("downloadASDF", "opened connection " + connectionLength);


            DataInputStream inputStream = new DataInputStream(downloadURL.openStream());

            byte[] buffer = new byte[connectionLength];
            inputStream.readFully(buffer);
            inputStream.close();

            Log.d("downloadASDF", "a" + outputFile.getAbsolutePath());

            if (!outputFile.exists())
                outputFile.createNewFile();
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile));
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.d("Malformed URL", "qwert");
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("IOException", "werty");
            return null;
        }

        Message msg = Message.obtain();
        msg.what = 1;

        return msg;
    }

    Handler downloadHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d("handlerASDF", "made it to handler");
            if (msg == null) {
                //TODO worry about this later
            }
            else {
                MainActivity.this.bookDetailsFragment.downloaded = true;
//                MainActivity.this.bookDetailsFragment.refreshDownload();
            }
            return true;}
    });

    @Override
    public void userMovedSeekBar(int progress) {
        if (connected)
        binder.seekTo(progress);
    }

    @Override
    public void playPauseClicked() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(playerFragment.title, progress);
        Log.d("PROGRESS VALUE pause", playerFragment.title + " " + progress);

        editor.apply();
        editor.commit();
        binder.pause();
        Log.d("PROGRESS VALUE post pause", playerFragment.title + " " + progress);

    }

    @Override
    public void stopClicked() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(playerFragment.title, 0);
        editor.apply();
        editor.commit();
        binder.stop();
        player.setVisibility(View.GONE);

    }

    @Override
    public void fragmentCreated() {
        if (progress > 0) {
            playerFragment.updatePlayer(playingTitle);
            playerFragment.progress = progress;
        }
    }
}
