package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPagerFragment extends Fragment {

    public static final String BOOKS_KEY = "books";
    ViewPager viewPager;
    String[] titles;
    Context parent;
    ArrayList<Book> books;
    ArrayList<BookDetailsFragment> fragments;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parent = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragments = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null)
            books = bundle.getParcelableArrayList(BOOKS_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        for (int i = 0; i < books.size(); i++)
        {
            fragments.add(BookDetailsFragment.newInstance(new Book(1, "A", "B", 2, 3, "C")));
        }
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyFragmentAdapter(getFragmentManager()));

        return view;
    }

    public static ViewPagerFragment newInstance(ArrayList<Book> books)
    {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BOOKS_KEY, books);
        viewPagerFragment.setArguments(bundle);
        return viewPagerFragment;
    }

    class MyFragmentAdapter extends FragmentStatePagerAdapter
    {

        public MyFragmentAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}
