package com.guyson.smartmirror;

import android.app.Activity;
import android.view.Gravity;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/*
* Logout before running instrumented tests
* */

@RunWith(AndroidJUnit4.class)
public class UserInterfaceTesting {

    private static final String EMAIL = "jennifer@email.com";
    private static final String PASSWORD = "password";
    private static final String FIRST_NAME = "Jen";
    private static final String LAST_NAME = "Lopez";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testLogin() {
        login();
        logout();
    }

    @Test
    public void testDirectToRegister() {
        directToRegister();
    }

    @Test
    public void testDirectToLogin() {
        directToRegister();
        onView(withId(R.id.tv_login)).perform(click());
        String activityName = MainActivity.class.getName();
        intended(hasComponent(hasClassName(activityName)));
    }

    @Test
    public void testLoginWithEmptyEmail() {
        // Enter username and password.
        onView(withId(R.id.input_password)).perform(typeText(PASSWORD), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.login_button)).perform(click());

        onView(withText("Please enter valid data!")).inRoot(withDecorView(not(mainActivityActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginWithEmptyPassword() {
        // Enter username and password.
        onView(withId(R.id.input_email)).perform(typeText(EMAIL), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.login_button)).perform(click());

        onView(withText("Please enter valid data!")).inRoot(withDecorView(not(mainActivityActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisterWithEmptyInput() {
        directToRegister();
        onView(withId(R.id.register_button)).perform(click());
        onView(withText("Please enter valid data!")).inRoot(withDecorView(not(mainActivityActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisterWithInvalidEmail() {
        directToRegister();

        //Enter input
        onView(withId(R.id.input_email)).perform(typeText("guyson"), closeSoftKeyboard());
        onView(withId(R.id.input_password)).perform(typeText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.input_confirmPassword)).perform(typeText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.input_firstName)).perform(typeText(FIRST_NAME), closeSoftKeyboard());
        onView(withId(R.id.input_lastName)).perform(typeText(LAST_NAME), closeSoftKeyboard());

        onView(withId(R.id.register_button)).perform(click());
        onView(withText("Please enter valid email!")).inRoot(withDecorView(not(mainActivityActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisterWithDifferentPasswords() {
        directToRegister();

        //Enter input
        onView(withId(R.id.input_email)).perform(typeText(EMAIL), closeSoftKeyboard());
        onView(withId(R.id.input_password)).perform(typeText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.input_confirmPassword)).perform(typeText("pass"), closeSoftKeyboard());
        onView(withId(R.id.input_firstName)).perform(typeText(FIRST_NAME), closeSoftKeyboard());
        onView(withId(R.id.input_lastName)).perform(typeText(LAST_NAME), closeSoftKeyboard());

        onView(withId(R.id.register_button)).perform(click());
        onView(withText("Passwords don't match!")).inRoot(withDecorView(not(mainActivityActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testViewLocationInformation() {
        login();

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.location_nav));

        // Directed activity
        String activityName = LocationActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);

        logout();
    }

    @Test
    public void testViewNewsInterests() {
        login();

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.news_nav));

        // Directed activity
        String activityName = NewsActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);

        logout();
    }

    @Test
    public void testViewHappyFeed() {
        login();

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.happy_nav));

        // Directed activity
        String activityName = TwitterActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);

        logout();
    }

    @Test
    public void testViewSadFeed() {
        login();

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.sad_nav));

        // Directed activity
        String activityName = TwitterActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);

        logout();
    }

    @Test
    public void testViewNeutralFeed() {
        login();

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.neutral_nav));

        // Directed activity
        String activityName = TwitterActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);

        logout();
    }

    @Test
    public void testDirectToFaceRecognitionSetup() {
        login();

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.setup_fr));

        // Directed activity
        String activityName = FacialRecognitionActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);

        logout();
    }

    private void login() {
        // Enter username and password.
        onView(withId(R.id.input_email)).perform(typeText(EMAIL), closeSoftKeyboard());
        onView(withId(R.id.input_password)).perform(typeText(PASSWORD), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.login_button)).perform(click());

        // Directed activity
        String activityName = UserActivity.class.getName();

        WaitActivityIsResumedIdlingResource resource = new WaitActivityIsResumedIdlingResource(activityName);

        Espresso.registerIdlingResources(resource);
        intended(hasComponent(hasClassName(activityName)));
        Espresso.unregisterIdlingResources(resource);
    }

    private void logout() {
        // Open Drawer
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
                .perform(DrawerActions.open()); // Open Drawer

        // Click on logout button
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.logout));
    }

    private void directToRegister() {
        onView(withId(R.id.tv_register)).perform(click());
        String activityName = RegisterActivity.class.getName();
        intended(hasComponent(hasClassName(activityName)));
    }

    //Handle Idling resources where waiting for REST API response
    private static class WaitActivityIsResumedIdlingResource implements IdlingResource {
        private final ActivityLifecycleMonitor instance;
        private final String activityToWaitClassName;
        private volatile ResourceCallback resourceCallback;
        boolean resumed = false;
        public WaitActivityIsResumedIdlingResource(String activityToWaitClassName) {
            instance = ActivityLifecycleMonitorRegistry.getInstance();
            this.activityToWaitClassName = activityToWaitClassName;
        }

        @Override
        public String getName() {
            return this.getClass().getName();
        }

        @Override
        public boolean isIdleNow() {
            resumed = isActivityLaunched();
            if(resumed && resourceCallback != null) {
                resourceCallback.onTransitionToIdle();
            }

            return resumed;
        }

        private boolean isActivityLaunched() {
            Collection<Activity> activitiesInStage = instance.getActivitiesInStage(Stage.RESUMED);
            for (Activity activity : activitiesInStage) {
                if(activity.getClass().getName().equals(activityToWaitClassName)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }
}
