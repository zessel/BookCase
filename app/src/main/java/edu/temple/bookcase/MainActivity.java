package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BookListFragment.GetBookInterface{

    ViewPagerFragment viewPagerFragment;
    BookListFragment bookListFragment;
    BookDetailsFragment bookDetailsFragment;
    ArrayList<String> titles;
    ArrayList<Book> books;
    int booksArrayLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        books = new ArrayList<>();
        initialBookSearch();

        Log.d("RESPONSEARRAYSIZE", "   " + booksArrayLength);

        //titles = getResources().getStringArray(R.array.titles_list);

        Log.d("in on start ", "books " + books.size());
        while (titles == null);
        Log.d("in on start", " titles " + titles.length);
        viewPagerFragment = ViewPagerFragment.newInstance(books);
        bookListFragment = BookListFragment.newInstance(titles);
        bookDetailsFragment = new BookDetailsFragment();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
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

    }

    Handler bookResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d("in handler", "  ZYX ");


            JSONArray responseArray = (JSONArray) msg.obj;
            booksArrayLength = responseArray.length();
            titles = new String[booksArrayLength];
            Log.d("titlesinhandler", "   " + titles.length);

            try {
                for (int i = 0; i < booksArrayLength; i++) {
                    books.add(new Book(responseArray.getJSONObject(i)));
                    titles[i] = responseArray.getJSONObject(i).getString("title");
                    Log.d("TESTING", " " + titles[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    });

    private void initialBookSearch()
    {
        Thread t = new Thread(){
            @Override
            public void run(){

                URL fullBookListURL;
                try {

                    fullBookListURL = new URL(getResources().getString(R.string.fullBookListAPI));
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
                    Log.d("Thread running and sending message", " ABCD  ");

                    bookResponseHandler.sendMessage(msg);
                } catch (Exception e){
                    Log.d("this borked", "borking");
                    e.printStackTrace();
                }
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



}
