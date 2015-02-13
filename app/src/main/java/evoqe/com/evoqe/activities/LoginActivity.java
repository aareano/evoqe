package evoqe.com.evoqe.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Locale;

import evoqe.com.evoqe.R;
import evoqe.com.evoqe.utilities.ConnectionDetector;

/**
 * @author Aaron Bowen 1/23/14
 */
public class LoginActivity extends Activity{
    Button btn_LoginIn = null;
    Button btn_SignUp = null;
    Button btn_ForgetPass = null;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;

    // flag for Internet connection status
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initializing Parse SDK - happens in EvoqeApplication class
        // Calling ParseAnalytics to see Analytics of our app
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        btn_LoginIn = (Button) findViewById(R.id.btn_login);
        btn_SignUp = (Button) findViewById(R.id.btn_signup);
        btn_ForgetPass = (Button) findViewById(R.id.btn_ForgetPass);
        mUserNameEditText = (EditText) findViewById(R.id.username);
        mUserNameEditText.setText("areano"); // TODO
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mPasswordEditText.setText("hhhhhh");


        btn_LoginIn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {
                    // Internet Connection is Present
                    // make HTTP requests
                    attemptLogin();
                } else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    showAlertDialog(LoginActivity.this, "No Internet Connection",
                            "You don't have internet connection.", false);
                }

            }
        });

        btn_SignUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent in =  new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(in);
            }
        });
        
        btn_ForgetPass.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                Intent in =  new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(in);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_forgot_password:
            forgotPassword();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void forgotPassword(){
        /* 
        FragmentManager fm = getSupportFragmentManager();
         ForgotPasswordDialogFragment forgotPasswordDialog = new ForgotPasswordDialogFragment();
         forgotPasswordDialog.show(fm, null);
         */
    }

    public void attemptLogin() {

        clearErrors();

        // Store values at the time of the login attempt.
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (password.length() < 4) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView =mPasswordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_field_required));
            focusView = mUserNameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            login(username.toLowerCase(Locale.getDefault()), password);
        }
    }

    private void login(String lowerCase, String password) {
        /*
         * Logs in a user with a username and password. 
         * On success, this saves the session to disk, so you can retrieve the 
         * currently logged in user using ParseUser.getCurrentUser().
         */
        ParseUser.logInInBackground(lowerCase, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                
                if (e == null) {
                    ParseUser me = ParseUser.getCurrentUser();
                    Log.i("Login activity", "current user: " + me.getString("fullName") 
                            + " - " + me.getString("email")); // just make a note
                    loginSuccessful();
                } else {
                    loginUnSuccessful();
                }
            }
        });

    }

    protected void loginSuccessful() {
        
        Intent in =  new Intent(LoginActivity.this, MainActivity.class);
        startActivity(in);
    }
    protected void loginUnSuccessful() {
        
        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
        showAlertDialog(LoginActivity.this,"Login", "Username or Password is invalid.", false);
    }

    private void clearErrors(){
        mUserNameEditText.setError(null);
        mPasswordEditText.setError(null);
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon 
        alertDialog.setIcon(R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", 
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
