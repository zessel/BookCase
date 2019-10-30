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

    String title;
    TextView textView;
    public final static String TITLE_KEY = "title";
    public BookDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null)
            title = bundle.getString(TITLE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_details, container, false);

        textView= view.findViewById(R.id.textView);
        textView.setText(title);
        return view;
    }

    public void changeTitle(String title)
    {
        textView.setText(title);
    }

    public static BookDetailsFragment newInstance(String title)
    {
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bookDetailsFragment.setArguments(bundle);
        return bookDetailsFragment;
    }

}
