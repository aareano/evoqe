package evoqe.com.evoqe.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import evoqe.com.evoqe.R;

/**
 * @author Aaron Bowen on 2/3/2015.
 */
public class SplashActivity extends Activity {

    // Splash screen timer
    private int SPLASH_TIME_OUT_LONG = 4000;
    private int SPLASH_TIME_OUT_SHORT = 2500;
    private int SPLASH_TIME_OUT;

    private String PREF_FIRST_TIME_APP_OPENED = "first_time_app_opened";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstTime = sp.getBoolean(PREF_FIRST_TIME_APP_OPENED, false);
        // because we're here, this means the app has now been opened
        sp.edit().putBoolean(PREF_FIRST_TIME_APP_OPENED, false).commit();

        if (isFirstTime) {
            SPLASH_TIME_OUT = SPLASH_TIME_OUT_LONG;
        } else {
            SPLASH_TIME_OUT = SPLASH_TIME_OUT_SHORT;
        }

        new Handler().postDelayed(new Runnable() {
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}