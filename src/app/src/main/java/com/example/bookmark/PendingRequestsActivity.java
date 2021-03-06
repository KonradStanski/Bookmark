package com.example.bookmark;

import android.content.Context;
import android.os.Bundle;

import com.example.bookmark.abstracts.ListingBooksActivity;
import com.example.bookmark.models.Book;
import com.example.bookmark.server.StorageServiceProvider;
import com.example.bookmark.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity shows a user a list of books that they have pending requests
 * for (books that they have requested that have a status of either REQUESTED
 * or ACCEPTED - they can select a book which takes them to the
 * BorrowerBookDetailsActivity or the BorrowerBookDetailsActivity,
 * respectively where they can see the books details.
 *
 * @author Ryan Kortbeek.
 */
public class PendingRequestsActivity extends ListingBooksActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Returns the title that is to be used for this activity.
     *
     * @return String
     */
    @Override
    protected String getActivityTitle() {
        return "Pending Requests";
    }

    /**
     * Gets all books from the firestore database that are requested by the
     * current user and sets the values of visibleBooks and relevantBooks
     * accordingly.
     */
    @Override
    protected void getRelevantBooks() {
        StorageServiceProvider.getStorageService().retrieveBooksByRequester(
            user,
            books -> {
                List<Book> relevantBooks = new ArrayList<>();
                for (Book book : books) {
                    if ((book.getStatus() == Book.Status.REQUESTED) ||
                        (book.getStatus() == Book.Status.ACCEPTED)) {
                        relevantBooks.add(book);
                    }
                }
                updateBookList(relevantBooks);
            }, e -> {
                DialogUtil.showErrorDialog(this, e);
            }
        );
    }

    /**
     * Returns the context that is used for the starting point of the
     * intent that is created when a Book in the visibleBooksListView is
     * clicked.
     *
     * @return Context
     */
    @Override
    protected Context getPackageContext() {
        return PendingRequestsActivity.this;
    }

    /**
     * Returns the class that is used for the destination of the
     * intent that is created when a Book in the visibleBooksListView is
     * clicked.
     *
     * @return Class<?>
     */
    @Override
    protected Class<?> getIntentDestination() {
        return BorrowerBookDetailsActivity.class;
    }
}
