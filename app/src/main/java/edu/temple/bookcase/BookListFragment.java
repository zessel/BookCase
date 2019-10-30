package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment {

    // Instead of having this be GetBookInterface I made it Context
    Context parent;
    public final static String TITLE_KEY = "titles";
    String[] titles;

    public BookListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GetBookInterface)
            parent = context;
        else
            throw new RuntimeException("Didn't implement BookListFragment's interface");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null)
            titles = bundle.getStringArray(TITLE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ListView listView = (ListView) inflater.inflate(R.layout.fragment_book_list, container, false);


        // It's context so I can do these next two lines, but I'm going to refactor this fragment to be less specific
        //final String[] titles = parent.getResources().getStringArray(R.array.titles_list);
        listView.setAdapter(new ArrayAdapter(parent,android.R.layout.simple_list_item_1, titles));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((GetBookInterface) BookListFragment.this.parent).bookSelected(titles[position]); //this needs casting
            }
        });
        return listView;
    }

    public static BookListFragment newInstance(String[] titles)
    {
        BookListFragment bookListFragment = new BookListFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(TITLE_KEY, titles);
        bookListFragment.setArguments(bundle);
        return bookListFragment;
    }

    interface GetBookInterface
    {
        void bookSelected (String bookTitle);
    }

}
