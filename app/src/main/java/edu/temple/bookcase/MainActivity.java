package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BookListFragment.GetBookInterface{

    ViewPagerFragment viewPagerFragment;
    BookListFragment bookListFragment;
    BookDetailsFragment bookDetailsFragment;
    String[] titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    public void bookSelected(String bookTitle) {
        bookDetailsFragment.changeTitle(bookTitle);
    }

}
