package com.example.bookmark.util;


import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.widget.Toolbar;

import com.example.bookmark.BorrowedActivity;
import com.example.bookmark.ExploreActivity;
import com.example.bookmark.MainActivity;
import com.example.bookmark.MyBooksActivity;
import com.example.bookmark.MyProfileActivity;
import com.example.bookmark.PendingRequestsActivity;
import com.example.bookmark.R;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

/**
 * This class implement a navigation drawer that is passed to
 * NavigationDrawerActivity to be used in all navigation drawer activities
 *
 * @author Konrad Staniszewski.
 */
public class DrawerProvider {

    private static final PrimaryDrawerItem myBooksItem =
        new PrimaryDrawerItem().withName("My Books").withIcon(R.drawable.ic_baseline_menu_book_24).withIdentifier(1);
    private static final PrimaryDrawerItem borrowedItem =
        new PrimaryDrawerItem().withName("Borrowed").withIcon(R.drawable.ic_baseline_bookmarks_24).withIdentifier(2);
    private static final PrimaryDrawerItem pendingItem =
        new PrimaryDrawerItem().withName("Pending Requests").withIcon(R.drawable.ic_baseline_compare_arrows_24).withIdentifier(3);
    private static final PrimaryDrawerItem exploreItem =
        new PrimaryDrawerItem().withName("Explore").withIcon(R.drawable.ic_baseline_explore_24).withIdentifier(4);
    private static final PrimaryDrawerItem myProfileItem =
        new PrimaryDrawerItem().withName("My Profile").withIcon(R.drawable.ic_baseline_person_24).withIdentifier(5);
    private static final PrimaryDrawerItem logOutItem =
        new PrimaryDrawerItem().withName("Log Out").withIcon(R.drawable.ic_baseline_exit_to_app_24).withIdentifier(6);

    public static Drawer getDrawer(final Activity activity, Toolbar toolbar) {
        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(activity);
        drawerBuilder.withToolbar(toolbar);
        drawerBuilder.withHeader(R.layout.navigation_drawer_header);
        drawerBuilder.withHeaderPadding(true);
        drawerBuilder.withHeader(R.layout.navigation_drawer_header);
        drawerBuilder.addDrawerItems(
            myBooksItem,
            borrowedItem,
            pendingItem,
            exploreItem,
            myProfileItem,
            logOutItem
        );
        drawerBuilder.withOnDrawerItemClickListener((view, position, drawerItem) -> {
            if (drawerItem != null) {
                Intent intent = null;
                if (position == 1 && !(activity instanceof MyBooksActivity)) {
                    intent = new Intent(activity, MyBooksActivity.class);
                } else if (position == 2 && !(activity instanceof BorrowedActivity)) {
                    intent = new Intent(activity, BorrowedActivity.class);
                } else if (position == 3 && !(activity instanceof PendingRequestsActivity)) {
                    intent = new Intent(activity, PendingRequestsActivity.class);
                } else if (position == 4 && !(activity instanceof ExploreActivity)) {
                    intent = new Intent(activity, ExploreActivity.class);
                } else if (position == 5 && !(activity instanceof MyProfileActivity)) {
                    intent = new Intent(activity, MyProfileActivity.class);
                } else if (position == 6) {
                    intent = new Intent(activity, MainActivity.class);
                }
                if (intent != null) {
                    // send the identifier so that the next one can figure out what was selected
                    intent.putExtra("SELECTED_IDENTIFIER", drawerItem.getIdentifier());
                    activity.startActivity(intent);
                }
            }
            return false;
        });
        Drawer result = drawerBuilder.build();
//        result.setSelection(0); // this just removes the selection all together
        return result;
    }
}
