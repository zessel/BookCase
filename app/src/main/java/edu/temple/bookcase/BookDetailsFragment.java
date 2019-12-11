package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    Context parent;
    Book book;
    TextView titleView;
    TextView authorView;
    TextView durationView;
    TextView publishedView;
    ImageView coverView;
    Button downloadButton;
    boolean downloaded;
    String filePath;
    File audioFile;


    public final static String BOOK_KEY = "title";
    public BookDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookDetailsFragmentInterface)
            parent = context;
        else
            throw new RuntimeException("Didn't implement BookDetailsFragment's interface");
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

        filePath = parent.getExternalFilesDir(null).toString() +
                getResources().getString(R.string.audioBookDir) + "/" + book.getTitle() + ".mp3";
        filePath = filePath.replace(" ", "");
        audioFile = new File(filePath);

        titleView = view.findViewById(R.id.titleView);
        titleView.setText(book.getTitle());
        authorView = view.findViewById(R.id.authorView);
        authorView.setText(book.getAuthor());
        durationView = view.findViewById(R.id.durationView);
        durationView.setText(String.valueOf(book.getDuration()));
        publishedView = view.findViewById(R.id.publishedView);
        publishedView.setText(String.valueOf(book.getPublished()));
        coverView = view.findViewById(R.id.imageView);
        if (!book.getCoverURL().isEmpty())
            Picasso.get().load(book.getCoverURL()).into(coverView);

        ((Button) view.findViewById(R.id.playbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BookDetailsFragmentInterface) BookDetailsFragment.this.parent).playButtonClicked(book);
            }
        });

        downloadButton = view.findViewById(R.id.downloadButton);
        refreshDownload();
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!audioFile.exists()) {
                    ((BookDetailsFragmentInterface) BookDetailsFragment.this.parent).downloadButtonClicked(book);
                    refreshDownload();
                    downloadButton.setBackgroundResource(R.drawable.ic_delete_forever_black_24dp);
                }
                else {
                    audioFile.delete();
                    refreshDownload();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDownload();
    }

    public void refreshDownload()
    {
        if (audioFile.exists()) {
            downloadButton.setBackgroundResource(R.drawable.ic_delete_forever_black_24dp);
            downloaded = true;
        }
        else {
            downloadButton.setBackgroundResource((R.drawable.ic_cloud_download_black_24dp));
            downloaded = false;
        }
    }

    public void changeBook(Book book)
    {
        this.book = book;
        titleView.setText(book.getTitle());
        authorView.setText(book.getAuthor());
        durationView.setText(String.valueOf(book.getDuration()));
        publishedView.setText(String.valueOf(book.getPublished()));
        Picasso.get().load(book.getCoverURL()).into(coverView);
    }

    public static BookDetailsFragment newInstance(Book book)
    {
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BOOK_KEY, book);
        bookDetailsFragment.setArguments(bundle);
        return bookDetailsFragment;
    }

    interface BookDetailsFragmentInterface
    {
        void playButtonClicked(Book book);
        void downloadButtonClicked(Book book);
    }
}
