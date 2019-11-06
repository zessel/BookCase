package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        books = new ArrayList<>();
        initialBookSearch();

        titles = getResources().getStringArray(R.array.titles_list);

        viewPagerFragment = ViewPagerFragment.newInstance(titles);
        bookListFragment = BookListFragment.newInstance(titles);
        bookDetailsFragment = new BookDetailsFragment();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame1);
        if (findViewById(R.id.frame2) == null)
        {
            if (fragment instanceof BookListFragment)
            {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.frame1, viewPagerFragment).commit();
        }
        else
        {
            if (fragment instanceof ViewPagerFragment)
            {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.frame1, bookListFragment).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.frame2, bookDetailsFragment).commit();
        }

    }

    Handler bookResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            JSONArray responseArray = (JSONArray) msg.obj;

            try {
                for (int i = 0; i < responseArray.length(); i++)
                    books.add(new Book(responseArray.getJSONObject(i)));
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

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    fullBookListURL.openStream()));

                    String response = "", tmpResponse;

                    tmpResponse = reader.readLine();
                    while (tmpResponse != null) {
                        response = response + tmpResponse;
                        tmpResponse = reader.readLine();
                    }

                    JSONArray bookArray = new JSONArray(response);
                    Message msg = Message.obtain();
                    msg.obj = bookArray;
                    bookResponseHandler.sendMessage(msg);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    @Override
    public void bookSelected(String bookTitle) {
        bookDetailsFragment.changeTitle(bookTitle);
    }



}
