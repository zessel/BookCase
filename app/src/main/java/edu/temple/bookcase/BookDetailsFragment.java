package edu.temple.bookcase;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    Book book;
    TextView titleView;
    TextView authorView;
    TextView durationView;
    TextView publishedView;


    public final static String BOOK_KEY = "title";
    public BookDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null)
            book = bundle.getParcelable(BOOK_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_details, container, false);

        titleView = view.findViewById(R.id.titleView);
        titleView.setText(book.getTitle());
        authorView = view.findViewById(R.id.authorView);
        authorView.setText(book.getAuthor());
        durationView = view.findViewById(R.id.durationView);
        durationView.setText(book.getDuration());
        publishedView = view.findViewById(R.id.publishedView);
        publishedView.setText(book.getPublished());

        return view;
    }

    public void changeBook(Book book)
    {
        this.book = book;
        titleView.setText(book.getTitle());
        authorView.setText(book.getAuthor());
        durationView.setText(book.getDuration());
        publishedView.setText(book.getPublished());
    }

    public static BookDetailsFragment newInstance(Book book)
    {
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BOOK_KEY, book);
        bookDetailsFragment.setArguments(bundle);
        return bookDetailsFragment;
    }
}
