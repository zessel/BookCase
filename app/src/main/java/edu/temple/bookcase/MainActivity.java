package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BookListFragment.GetBookInterface{

    ViewPagerFragment viewPagerFragment;
    BookListFragment bookListFragment;
    BookDetailsFragment bookDetailsFragment;
    String[] titles;
    ArrayList<Book> books;
    int booksArrayLength;
    Button button;
    EditText editText;
    String searchTerm;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
        editText = findViewById(R.id.searchText);
        bookSearch();
    }

    Handler bookResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
        MainActivity.this.books = books = new ArrayList<>();
        JSONArray responseArray = (JSONArray) msg.obj;
        booksArrayLength = responseArray.length();
        titles = new String[booksArrayLength];

        try {
            for (int i = 0; i < booksArrayLength; i++) {
                books.add(new Book(responseArray.getJSONObject(i)));
                titles[i] = responseArray.getJSONObject(i).getString("title");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        viewPagerFragment = ViewPagerFragment.newInstance(books);
        bookListFragment = BookListFragment.newInstance(titles);
        if (bookDetailsFragment == null)
        {
            bookDetailsFragment = BookDetailsFragment.newInstance(books.get(0));
        }

        if (findViewById(R.id.frame2) == null) {
            if (fragment instanceof BookListFragment) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.frame1, viewPagerFragment).commit();
        } else {
            if (fragment instanceof ViewPagerFragment) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.frame1, bookListFragment).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.frame2, bookDetailsFragment).commit();
        }
        return false;
        }
    });

    private void bookSearch()
    {
        Thread t = new Thread(){
            @Override
            public void run(){

                button = findViewById(R.id.button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchTerm = editText.getText().toString();
                        search(searchTerm);
                        Log.d("QWERTY", "button clicked");
                    }
                });
                search("");


/*
            URL fullBookListURL;
            try {
                fullBookListURL = new URL(getResources().getString(R.string.bookSearchAPI)+ searchTerm);
                Log.d("Thread running before stream opened", " IJK  ");

                BufferedReader reader = new BufferedReader(new InputStreamReader(fullBookListURL.openStream()));
                Log.d("Thread running stream opened", " EFGH  ");

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

                bookResponseHandler.sendMessage(msg);
            } catch (Exception e){
                Log.d("this borked", "borking");
                e.printStackTrace();
            }*/
            }
        };
        t.start();
    }

    @Override
    public void bookSelected(String bookTitle) {
        for (int i = 0; i < booksArrayLength; i++) {
            if (books.get(i).getTitle() == bookTitle)
                bookDetailsFragment.changeBook(books.get(i));
        }
    }

    private void search(String searchTerm) {
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
            bookResponseHandler.sendMessage(msg);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
