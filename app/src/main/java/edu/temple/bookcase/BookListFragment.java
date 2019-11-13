package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment {

    // Instead of having this be GetBookInterface I made it Context
    Context parent;
    public final static String BOOKS_KEY = "books";
    ArrayList<Book> books;
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
        if (bundle != null) {
            books = bundle.getParcelableArrayList(BOOKS_KEY);
            titles = new String[books.size()];
            for (int i = 0; i < books.size(); i++) {
                titles[i] = books.get(i).getTitle();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ListView listView = (ListView) inflater.inflate(R.layout.fragment_book_list, container, false);

        listView.setAdapter(new ArrayAdapter(parent,android.R.layout.simple_list_item_1, titles));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((GetBookInterface) BookListFragment.this.parent).bookSelected(titles[position]); //this needs casting
            }
        });
        return listView;
    }

    public ArrayList<Book> getBooksAsArrayList(){
        return books;
    }
/*    public JSONArray getBooksAsJSON() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < books.size(); i++){
            jsonArray.put(books.get(i).toJSON());
        }
        Log.d("SentFromListFrag", "" + jsonArray.toString());
        return jsonArray;
    }
*/
    public static BookListFragment newInstance(ArrayList<Book> books)
    {
        BookListFragment bookListFragment = new BookListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BOOKS_KEY, books);
        bookListFragment.setArguments(bundle);
        return bookListFragment;
    }

    interface GetBookInterface
    {
        void bookSelected (String bookTitle);
    }



}
