package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
        editText = findViewById(R.id.searchText);
        button = findViewById(R.id.button);
        bookSearch();
    }

    Handler bookResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
            JSONArray responseArray;
            MainActivity.this.books = books = new ArrayList<>();

            if (fragment instanceof ViewPagerFragment) {
                responseArray = ((ViewPagerFragment) fragment).getBooksAsJSON();
            }
            else if (fragment instanceof BookListFragment){
                responseArray = ((BookListFragment) fragment).getBooksAsJSON();
            }
            else {
                responseArray = (JSONArray) msg.obj;
            }
            Log.d("HANDLER-Retrieved books", "" + responseArray.toString());
            booksArrayLength = responseArray.length();
            titles = new String[booksArrayLength];
            try {
                for (int i = 0; i < booksArrayLength; i++) {
                    if (responseArray.getJSONObject(i).has("coverURL")) {
                        Log.d("CHECKING", ""+ responseArray.getJSONObject(i).getString("coverURL"));
                    }
                    books.add(new Book(responseArray.getJSONObject(i)));
                    Log.d("HANDLER", "" + responseArray.getJSONObject(i).toString());
                    titles[i] = responseArray.getJSONObject(i).getString("title");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            viewPagerFragment = ViewPagerFragment.newInstance(books);
            bookListFragment = BookListFragment.newInstance(books);
            bookDetailsFragment = BookDetailsFragment.newInstance(new Book(0,"","",0,0,""));

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
/*                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
                if (fragment instanceof ViewPagerFragment) {
                    JSONArray bookArray = ((ViewPagerFragment) fragment).getBooksAsJSON();
                    Message msg = Message.obtain();
                    msg.obj = bookArray;
                    bookResponseHandler.sendMessage(msg);
                    Log.d("SentFromViewFrag", "" + bookArray.toString());
                }
                else if (fragment instanceof BookListFragment){
                    JSONArray bookArray = ((BookListFragment) fragment).getBooksAsJSON();
                    Message msg = Message.obtain();
                    msg.obj = bookArray;
                    bookResponseHandler.sendMessage(msg);
                    Log.d("SentFromListFrag", "" + bookArray.toString());
                }
                else {*/
                    search("");
//                }


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
