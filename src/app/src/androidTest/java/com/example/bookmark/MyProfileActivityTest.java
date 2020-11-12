package com.example.bookmark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Context.MODE_PRIVATE;


/**
 * Perform intent testing on MyProfileActivity
 * <p>
 * Outstanding Issues/TODOs
 * Find out how to mock the database being accessed so that I'm not making any calls to
 * the database unnecessarily
 *
 * @author Konrad Staniszewski.
 */
public class MyProfileActivityTest {
    private Solo solo;
    Intent intent;
    SharedPreferences.Editor sharedPrefEditor;

    @Rule
    public ActivityTestRule<MyProfileActivity> rule =
        new ActivityTestRule<>(MyProfileActivity.class, true, false);

    /**
     * Runs before all tests and create the solo instance.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("LOGGED_IN_USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_NAME", "john.smith42").commit();

        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Gets activity
     *
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        rule.launchActivity(new Intent());
        solo.assertCurrentActivity("WRONG ACTIVITY", MyProfileActivity.class);
    }

    /**
     * Test clicking the edit profile button
     */
    @Test
    public void editProfile() {
        rule.launchActivity(new Intent());
        View editProfileBtn = rule.getActivity().findViewById(R.id.menu_edit_edit_btn);
        solo.clickOnView(editProfileBtn);
        solo.assertCurrentActivity("WRONG ACTIVITY", EditProfileActivity.class);
    }

    /**
     * Close the activity after each test
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
