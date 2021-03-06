package com.splitrides;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.splitrides.android.helper.ConnectionDetector;
import com.splitrides.android.helper.JSONParser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.splitrides.android.helper.ConnectionDetector;
import com.splitrides.android.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks,
        View.OnClickListener,
        OnShowcaseEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,facebookloginFragment.OnGooglePlusButtonClicked,signupage.OnSignUpsButtonClicked, notificationfragment.OnDataChangedListener
{

    private static final int LOGINPAGE = 0;
    private static final int SIGNUPPAGE = 1;
    static final int  SWITCH_SEARCH_RIDES = 23;
    private static final int RC_SIGN_IN = 0;
    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final String SAVED_PROGRESS = "sign_in_progress";
    private String personName="";
    private String email="";
    private String gender="";
    private String birthDay = "";
    private String educationHistory = "";
    private String workHistory = "";
    int clickeditem =0;
    private String id ="";
    private String name="";
    private UserLoginTask mAuthTask = null;
    private Retrieveauthtokengoogle mTokenTask = null;
    private ProgressDialog pDialog;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mEmailView_signup;
    private EditText mPasswordView_signup;
    private EditText mNameView_signup;
    private static final int FRAGMENT_COUNT = SIGNUPPAGE+1;
    private static final String TAG = "RetrieveAccessToken";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private Toolbar toolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private boolean isResumed = false;
    private boolean mIsSignedIn = false;
    private GoogleApiClient mGoogleApiClient;
    private int mSignInError;
    private int mSignInProgress;
    private PendingIntent mSignInIntent;
    private CharSequence mTitle;
    private boolean mIntentInProgress;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    JSONParser jsonParser = new JSONParser();
    private static final int REQ_SIGN_IN_REQUIRED = 55664;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    MenuItem hidemenuitem;
    String SENDER_ID = "544261498132";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean mSignInClicked;
    CallbackManager callbackManager;
    GoogleCloudMessaging gcm;
    AccessTokenTracker accessTokenTracker;
    ShowcaseView sv;
    //AtomicInteger msgId = new AtomicInteger();

    private String googleProfilePicUrl = "";

    String regid;

    private ProgressDialog progressLayout = null;

    private void showProgressLayout(String title, String message) {
        if(progressLayout == null) {
            progressLayout = new ProgressDialog(this);
        }

        progressLayout.setTitle(title);
        progressLayout.setCancelable(false);
        progressLayout.setMessage(message);
        progressLayout.show();
    }

    private void dismissProgressDialog() {
        if(progressLayout != null && progressLayout.isShowing()) {
            progressLayout.dismiss();
            progressLayout = null;
        }
    }

    public void onClick(View view) {
        GridView mGridView = (GridView)findViewById(R.id.listgridview);
        View targetView;
        ViewTarget target;
        int margin,marginbottom;
        switch(clickeditem){
            case 0:
                if(mGridView != null && mGridView.getChildAt(0) != null ) {
                    targetView = mGridView.getChildAt(0);
                    target = new ViewTarget(targetView);
                    sv.setShowcase(target, true);
                    sv.setContentText("Tell us where you are going, when you are going,we will find companions traveling in the same direction.");
                    sv.setContentTitle("Post Ride:");
                    RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    marginbottom = ((Number) (getResources().getDisplayMetrics().density * 50)).intValue();
                    margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
                    lps.setMargins(0, 0, margin, marginbottom);
                    sv.setButtonPosition(lps);
                }
                else{
                    sv.hide();
                }
                break;
            case 1:
                if(mGridView != null && mGridView.getChildAt(1) != null ) {
                    targetView = mGridView.getChildAt(1);
                    target = new ViewTarget(targetView);
                    sv.setShowcase(target, true);
                    sv.setContentText("Search companions for the rides you posted,we show other travelers with similar ride as the one you posted");
                    sv.setContentTitle("Search your ride:");
                    RelativeLayout.LayoutParams lpssearch = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpssearch.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lpssearch.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    marginbottom = ((Number) (getResources().getDisplayMetrics().density * 50)).intValue();
                    margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
                    lpssearch.setMargins(0, 0, margin, marginbottom);
                    sv.setButtonPosition(lpssearch);
                    sv.setShouldCentreText(false);
                }
                else{
                    sv.hide();
                }
                break;
            case 2:
                if(mGridView != null && mGridView.getChildAt(2) != null ) {
                    targetView = mGridView.getChildAt(2);
                    target = new ViewTarget(targetView);
                    sv.setShowcase(target, true);
                    sv.setContentText("Click here to see the rides that you have scheduled and are yet to happen");
                    sv.setContentTitle("Upcoming rides");
                    sv.setButtonText("OK Gotit");
                    RelativeLayout.LayoutParams lpsupcoming = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpsupcoming.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lpsupcoming.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    marginbottom = ((Number) (getResources().getDisplayMetrics().density * 50)).intValue();
                    margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
                    lpsupcoming.setMargins(0, 0, margin, marginbottom);
                    sv.setButtonPosition(lpsupcoming);
                    sv.setShouldCentreText(true);
                }
                else{
                    sv.hide();
                }
                break;
            case 3:
                sv.hide();
                SharedPreferenceManager.setPreference("showhomepageftu",true);
                break;

        }
        clickeditem++;
    }


   @Override
   public void onsinuppageButtonClick() {
       attemptSignup();
   }

   @Override
   public void onButtonClick()
   {
       ConnectionDetector cd = new ConnectionDetector(getApplicationContext());


       // Check if Internet present
       if (!cd.isConnectingToInternet()) {
           // Internet Connection is not present
           Toast.makeText(MainActivity.this,
                   "Internet Connection Error Please connect to working Internet connection", Toast.LENGTH_LONG).show();
           // stop executing code by return
           return;
       }
       showProgressLayout("", "Logging in...");
       mSignInProgress = STATE_SIGN_IN;
       mGoogleApiClient.connect();
   }

    @Override
    public void onDataChanged(int number){

        try {

            homepageX homepagexfragment = (homepageX)
                    getSupportFragmentManager().findFragmentById(R.id.containernavigation);


            if (homepagexfragment != null) {
                // If article frag is available, we're in two-pane layout...

                // Call a method in the ArticleFragment to update its content
                homepagexfragment.onDataChanged(number);
            }
        }
        catch(java.lang.ClassCastException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    public static boolean isFacebookAppInstalled(Context p_context)
    {
        Intent m_shareIntent;
        PackageManager m_packageManager;
        List<ResolveInfo> m_activityList;
        boolean m_isAppInstall = false;
        try
        {
            m_shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            m_shareIntent.setType("text/plain");
            m_packageManager = p_context.getApplicationContext().getPackageManager();
            m_activityList = m_packageManager.queryIntentActivities(m_shareIntent, 0);
            for (final ResolveInfo m_app : m_activityList)
            {
                if ((m_app.activityInfo.name).contains("com.facebook.katana"))
                {
                    m_isAppInstall = true;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            m_packageManager = null;
            m_activityList = null;
        }
        return m_isAppInstall;
    }


    @Override
    public void onFacebookLoginButtonClicked()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email", "user_birthday", "user_education_history", "user_work_history"));
            showProgressLayout("", "Logging in...");
        }
        else{
            if (accessTokenTracker != null) {
                accessTokenTracker.stopTracking();
            }
            LoginManager.getInstance().logOut();
        }
    }

    @Override
    public void onLinkedInLoginButtonClicked() {
        /*LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                LISession linkedInSession = LISessionManager.getInstance(getApplicationContext()).getSession();
                com.linkedin.platform.AccessToken accessTkn = linkedInSession.getAccessToken();
                saveUserLinkedInProfileDetail();
                Toast.makeText(getApplicationContext(), "User successfully logged in", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthError(LIAuthError error) {
                Log.e("Linkedin", "SignIn Failure");
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }, true);*/
    }

    /*
    * This method has been used for saving linkedin profile data
    * */
    private void saveUserLinkedInProfileDetail() {
        String url = "https://api.linkedin.com/v1/people/~";

        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                JSONObject jsonObject = apiResponse.getResponseDataAsJson();
                try {
                    gender = null;
                    email = null;
                    id = jsonObject.getString("id");
                    name = jsonObject.getString("firstName") + " " + jsonObject.getString("lastName");
                    /*if (mAuthTask == null && (!email.isEmpty()) && (!name.isEmpty()) && (!id.isEmpty()) && (!gender.isEmpty())) {*/
                    if (mAuthTask == null && (!name.isEmpty()) && (!id.isEmpty())) {
                        SharedPreferenceManager.setPreference("id", id);
                        mAuthTask = new UserLoginTask(email, "", name, "linkedin", id, LISessionManager.getInstance(getApplicationContext()).getSession().getAccessToken().toString(), gender, SharedPreferenceManager.getPreference("registrationid"), "", "", "");
                        mAuthTask.execute((Void) null);
                    }
                } catch (JSONException je) {
                    Log.e("LinkedIn Error occured", "JSON has missing parameters");
                }

            }

            @Override
            public void onApiError(LIApiError liApiError) {
                Log.e("LinkedIn Profile Error", liApiError.getApiErrorResponse().getMessage());
            }
        });
    }

    /*
    * This method has been used for setting scope for linkedin app
    * */
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.RW_COMPANY_ADMIN, Scope.R_EMAILADDRESS, Scope.W_SHARE);
    }

   @Override
   public void onsignupButtonClicked(){

       showFragment(SIGNUPPAGE, true);
   }

    @Override
    public void onLoginButtonClicked(){
       attemptLogin();
    }
    public void attemptSignup() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView_signup.setError(null);
        mPasswordView_signup.setError(null);
        mNameView_signup.setError(null);

        // Store values at the time of the login attempt.
        String Email = mEmailView_signup.getText().toString();
        String Password = mPasswordView_signup.getText().toString();
        String Name = mNameView_signup.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(Password)) {
            mPasswordView_signup.setError(getString(R.string.error_field_required));
            focusView = mPasswordView_signup;
            cancel = true;
        } else if (Password.length() < 4) {
            mPasswordView_signup.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView_signup;
            cancel = true;
        }
        if (TextUtils.isEmpty(Name)) {
            mNameView_signup.setError(getString(R.string.error_field_required));
            focusView = mNameView_signup;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(Email)) {
            mEmailView_signup.setError(getString(R.string.error_field_required));
            focusView = mEmailView_signup;
            cancel = true;
        } else if (!Email.contains("@")) {
            mEmailView_signup.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView_signup;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.


            mAuthTask = new UserLoginTask(Email,Password,Name,"local","","","male",SharedPreferenceManager.getPreference("registrationid"), "", "", "");
            mAuthTask.execute((Void) null);
        }
    }
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String Email = mEmailView.getText().toString();
        String Password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(Password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (Password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(Email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Email.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.


            mAuthTask = new UserLoginTask(Email,Password,"","local","","","",getRegistrationId(getApplicationContext()), "", "", "");
            mAuthTask.execute((Void) null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferenceManager.setApplicationContext(getApplicationContext());
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        accessTokenTracker = new AccessTokenTracker() {
                            @Override
                            protected void onCurrentAccessTokenChanged(
                                    AccessToken oldAccessToken,
                                    final AccessToken currentAccessToken) {
                                this.stopTracking();
                                if(currentAccessToken != null) {
                                    GraphRequest request = GraphRequest.newMeRequest(
                                            currentAccessToken.getCurrentAccessToken(),
                                            new GraphRequest.GraphJSONObjectCallback() {
                                                @Override
                                                public void onCompleted(
                                                        JSONObject jsonObject,
                                                        GraphResponse response) {
                                                    try {
                                                        email = jsonObject.getString("email");
                                                        gender = jsonObject.getString("gender");
                                                        id = jsonObject.getString("id");
                                                        name = jsonObject.getString("name");
                                                        birthDay = jsonObject.has("birthday") ? jsonObject.getString("birthday") : "";
                                                        workHistory = findLatestWorkHistoryFromFB(jsonObject);
                                                        educationHistory = findLatestEducationFromFB(jsonObject);
                                                        if (mAuthTask == null && (!email.isEmpty()) && (!name.isEmpty()) && (!id.isEmpty()) && (!gender.isEmpty())) {
                                                            SharedPreferenceManager.setPreference("id", id);
                                                            dismissProgressDialog();
                                                            mAuthTask = new UserLoginTask(email, "", name, "facebook", id, currentAccessToken.getCurrentAccessToken().getToken(), gender, SharedPreferenceManager.getPreference("registrationid"), birthDay, educationHistory, workHistory);
                                                            mAuthTask.execute((Void) null);
                                                        }
                                                    } catch (JSONException exe) {

                                                    }
                                                }
                                            });
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "id,name,link,email,gender,birthday,education,work");
                                    request.setParameters(parameters);
                                    request.executeAsync();
                                }
                            }
                        };
                        accessTokenTracker.startTracking();
                    }



                    @Override
                    public void onCancel() {
                        dismissProgressDialog();
                        if (accessTokenTracker != null) {
                            accessTokenTracker.stopTracking();
                        }
                        LoginManager.getInstance().logOut();

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        dismissProgressDialog();
                        if (accessTokenTracker != null) {
                            accessTokenTracker.stopTracking();
                        }
                        LoginManager.getInstance().logOut();
                    }
                });
        mGoogleApiClient = buildGoogleApiClient();
        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState
                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        fragments[LOGINPAGE] = fm.findFragmentById(R.id.loginpage);
        fragments[SIGNUPPAGE] = fm.findFragmentById(R.id.signuppage);
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commitAllowingStateLoss();
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mEmailView_signup = (EditText) findViewById(R.id.enter_email_signup);

        mPasswordView_signup = (EditText) findViewById(R.id.enter_password_signup);
        mNameView_signup = (EditText) findViewById(R.id.enter_name_signup);
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onDataChanged(SharedPreferenceManager.getIntPreference("notificationcount"));
            }
        };
        ConnectionDetector cd = new ConnectionDetector(this.getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            Toast.makeText(this,
                    "Internet Connection Error Please connect to working Internet connection", Toast.LENGTH_LONG).show();
            // stop executing code by return
            return;
        }
        new getallnotificationtask().execute((Void) null);

    }

    /*
    * This method has been used for getting user education history from Facebook
    * */
    private String findLatestEducationFromFB(JSONObject fbProfileObject) throws JSONException{
        String userEducation = "";
        JSONArray fbEducationArrayObject = fbProfileObject.has("education") ? fbProfileObject.getJSONArray("education") : null;

        if(fbEducationArrayObject != null) {
            Integer fbEducationCount = fbEducationArrayObject.length();
            JSONObject educationObj = null, tempEduObj = null;
            Integer latestGraduationYear = null, tempEduYear = null;

            try {
                for(int i = 0; i < fbEducationCount; i++) {
                    tempEduObj = (JSONObject)fbEducationArrayObject.get(i);
                    tempEduYear = tempEduObj.has("year") ? ((JSONObject) tempEduObj.get("year")).getInt("name") : null;
                    if(educationObj != null) {
                        if(latestGraduationYear != null && tempEduYear != null && latestGraduationYear < tempEduYear) {
                            latestGraduationYear = tempEduYear;
                            educationObj = tempEduObj;
                        }
                    } else {
                        educationObj = tempEduObj;
                        latestGraduationYear = tempEduYear;
                    }
                }

                if(educationObj != null) {
                    userEducation = ((JSONObject)educationObj.get("school")).getString("name");
                    if(latestGraduationYear != null) {
                        userEducation = userEducation + ", " + latestGraduationYear.toString();
                    }
                }
            } catch (JSONException je) {
                Log.e("Error occured", "Reading FB details of user");
                userEducation = "";
            }
        }

        return  userEducation;
    }

    /*
    * This method has been used for getting user work details from Facebook
    * */
    private String findLatestWorkHistoryFromFB(JSONObject fbProfileObject) throws JSONException {
        String latestWorkHistory = "";
        JSONArray fbWorkArrayObject = fbProfileObject.has("work") ? fbProfileObject.getJSONArray("work") : null;

        if(fbWorkArrayObject != null) {
            Integer fbWorkHistoryCount = fbWorkArrayObject.length();
            JSONObject workObj = null, tempWorkObj = null;
            String latestWorkStartDate = null, tempWorkDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            try {
                for(int i = 0; i < fbWorkHistoryCount; i++) {
                    tempWorkObj = (JSONObject)fbWorkArrayObject.get(i);
                    tempWorkDate = tempWorkObj.has("start_date") ? tempWorkObj.getString("start_date") : null;
                    if(workObj != null) {
                        if(latestWorkStartDate!= null && tempWorkDate != null && (sdf.parse(latestWorkStartDate)).before(sdf.parse(tempWorkDate))) {
                            latestWorkStartDate = tempWorkDate;
                            workObj = tempWorkObj;
                        }
                    } else {
                        workObj = tempWorkObj;
                        latestWorkStartDate = tempWorkDate;
                    }
                }
                if(workObj != null) {
                    if(workObj.has("position")) {
                        latestWorkHistory = latestWorkHistory + ((JSONObject)workObj.get("position")).getString("name") + " at ";
                    }
                    latestWorkHistory = latestWorkHistory + ((JSONObject)workObj.get("employer")).getString("name");

                    if(latestWorkStartDate != null) {
                        latestWorkHistory = latestWorkHistory + " since " + latestWorkStartDate;
                    }
                }
            } catch (JSONException je) {
                je.printStackTrace();
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
        }

        return  latestWorkHistory;
    }

    public class getallnotificationtask extends AsyncTask<Void, Void, List<notificationdata>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected List<notificationdata> doInBackground(Void... param) {
            List<notificationdata> notificationdataArray = new ArrayList<notificationdata>();
            try {
                JSONObject params = new JSONObject();

                // getting JSON string from URL
                String json = jsonParser.makeHttpRequest(mainurl.geturl() +"getRequests", "POST",
                        params);



                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    JSONArray notification = jObj.getJSONArray("requests");
                    for(int i=0; i<notification.length(); i++){
                        JSONObject notificationJSONObject = notification.getJSONObject(i);
                        notificationdata info = new notificationdata(notificationJSONObject.getString("requesterSource"),notificationJSONObject.getString("requesterDestination"),notificationJSONObject.getString("requesterDate"),notificationJSONObject.getString("requestId"));
                        notificationdataArray.add(info);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return notificationdataArray;
        }

        @Override
        protected void onPostExecute(final List<notificationdata> notificationdataArray) {
            if(!notificationdataArray.isEmpty()){
                onDataChanged(notificationdataArray.size());
                SharedPreferenceManager.setPreference("notificationcount",notificationdataArray.size());
            }
            else
            {
                onDataChanged(0);
                SharedPreferenceManager.setPreference("notificationcount",0);
            }
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        SharedPreferenceManager.setPreference("registrationid",regId);
        SharedPreferenceManager.setPreference("appversion",appVersion);
    }

    private String getRegistrationId(Context context) {

        String registrationId = SharedPreferenceManager.getPreference("registrationid");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = SharedPreferenceManager.getIntPreference("appversion");
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    storeRegistrationId(getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }


    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN);


        return builder.build();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        //Log.d("position to", Integer.toString(position));
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {

            case 0:

                fragmentManager.beginTransaction()
                    .replace(R.id.containernavigation, new homepageX())
                    .commitAllowingStateLoss();
                break;
            case 1:

                fragmentManager.beginTransaction()
                        .replace(R.id.containernavigation, new myridedetail())
                        .commitAllowingStateLoss();
                break;
            case 2:

                fragmentManager.beginTransaction()
                        .replace(R.id.containernavigation, new ratecard())
                        .commitAllowingStateLoss();
                break;
            case 3:

                fragmentManager.beginTransaction()
                        .replace(R.id.containernavigation, new completedrides())
                        .commitAllowingStateLoss();
                break;
            case 4:
                fragmentManager.beginTransaction().replace(R.id.containernavigation,new wallet())
                        .commitAllowingStateLoss();
                break;
            case 5:
                fragmentManager.beginTransaction().replace(R.id.containernavigation,new selfdrive())
                        .commitAllowingStateLoss();
                break;

        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("onconnected", "connected");
        mTitle = getTitle();
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            if(currentPerson.getGender() == 0)
            {
               gender = "male";
            }
            else if(currentPerson.getGender() == 1)
            {
                gender = "female";
            }
            else
            {
                gender = "other";
            }
            email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            googleProfilePicUrl = currentPerson.getImage().getUrl();
        }
        mSignInProgress = STATE_DEFAULT;
        dismissProgressDialog();
        mTokenTask = new Retrieveauthtokengoogle(Plus.AccountApi.getAccountName(mGoogleApiClient), personName, email,gender, googleProfilePicUrl);
        mTokenTask.execute((Void) null);


    }

       @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.


        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // An API requested for GoogleApiClient is not available. The device's current
            // configuration might not be supported with the requested API or a required component
            // may not be installed, such as the Android Wear application. You may need to use a
            // second GoogleApiClient to manage the application's optional APIs.

        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_SIGN_IN) {
                // STATE_SIGN_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                resolveSignInError();
            }
        }

    }
    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        hidemenuitem = menu.findItem(R.id.action_signout);
        if(SharedPreferenceManager.getBooleanPreference("mIsSignedIn"))
        {
            hidemenuitem.setVisible(true);
        }
        else {
            hidemenuitem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_signout) {
            singoutfromapp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
    }
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferenceManager.setApplicationContext(getApplicationContext());
        checkPlayServices();
        isResumed = true;
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("requesttojoinedridenotification"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = STATE_DEFAULT;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
        }
        if (requestCode == REQ_SIGN_IN_REQUIRED && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
           // new Retrieveauthtokengoogle(email, personName, email,gender, googleProfilePicUrl).execute((Void) null);
        }
        if(requestCode == SWITCH_SEARCH_RIDES){
            if (resultCode == Activity.RESULT_OK) {
                Intent startsearchrides = new Intent(this,Searchyourrides.class);
                startActivity(startsearchrides);
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isResumed = false;
        super.onPause();
    }
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mIsSignedIn = SharedPreferenceManager.getBooleanPreference("mIsSignedIn");
        if ( mIsSignedIn ) {
            // if the session is already open,
            // try to show the selection fragment
           /* if(hidemenuitem != null) {
                hidemenuitem.setVisible(true);
            }*/
            invalidateOptionsMenu();
            OnLoginAuthenticated();
            ConnectionDetector cd = new ConnectionDetector(getApplicationContext());


            // Check if Internet present
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                Toast.makeText(MainActivity.this,
                        "Internet Connection Error Please connect to working Internet connection", Toast.LENGTH_LONG).show();
                // stop executing code by return
                return;
            }
        } else {
            // otherwise present the splash screen
            // and ask the person to login.
           // if (getSupportFragmentManager().getBackStackEntryCount() > 0){
             //   showFragment(SIGNUPPAGE, false);
           // }
           // else{
            showFragment(LOGINPAGE, false);
            //}

        }
    }
    public void ftububble(){
        if(!SharedPreferenceManager.getBooleanPreference("showhomepageftu")) {
            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            int marginbottom = ((Number) (getResources().getDisplayMetrics().density * 50)).intValue();
            int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
            lps.setMargins(0, 0, margin, marginbottom);
            ViewTarget target = new ViewTarget(R.id.tool_bar, this);
            sv = new ShowcaseView.Builder(this, true)
                    .setTarget(Target.NONE)
                    .setContentTitle(R.string.showcase_main_title)
                    .setContentText(R.string.showcase_main_message)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setOnClickListener(this)
                    .build();
            sv.setButtonPosition(lps);
            sv.setShouldCentreText(true);
        }
    }
    public void OnLoginAuthenticated() {
        showFragment(FRAGMENT_COUNT, false);
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        String profileImg = SharedPreferenceManager.getPreference("profileImage");
        mNavigationDrawerFragment.setUserDetails(SharedPreferenceManager.getPreference("name"),SharedPreferenceManager.getPreference("email"), (profileImg != null && profileImg.trim().length() > 0) ? profileImg : SharedPreferenceManager.getPreference("id"), SharedPreferenceManager.getPreference("profile"));
    }
    public void singoutfromapp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do You want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showFragment(LOGINPAGE, false);
                        //hidemenuitem.setVisible(false);
                        invalidateOptionsMenu();
                        SharedPreferenceManager.setPreference("mIsSignedIn", false);
                        String profile = SharedPreferenceManager.getPreference("profile");

                        if (profile.equals("facebook")) {
                            if (accessTokenTracker != null) {
                                accessTokenTracker.stopTracking();
                            }
                            LoginManager.getInstance().logOut();
                        } else if (profile.equals("google")) {
                            mGoogleApiClient.disconnect();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mName;
        private final String mProfile;
        private final String mProfileId;
        private final String accessToken;
        private final String mgender;
        private final String mgcmid;
        private final String mBirthDate;
        private final String mEducationDetails;
        private final String mWorkDetails;

        UserLoginTask(String email, String password, String name,
                      String profile, String profileId, String accesstoken,
                      String gender, String gcmid, String birthday,
                      String educationDetails, String workDetails) {
            mEmail = email;
            mPassword = password;
            mName = name;
            mProfile = profile;
            mProfileId = profileId;
            accessToken = accesstoken;
            mgender = gender;
            mgcmid = gcmid;
            mBirthDate = birthday;
            mEducationDetails = educationDetails;
            mWorkDetails = workDetails;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Attempting Login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... param) {
            // Building Parameters
            /*List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id, song id as GET parameters
            params.add(new BasicNameValuePair("name", mName));
            params.add(new BasicNameValuePair("email", mEmail));
            params.add(new BasicNameValuePair("password", mPassword));
            params.add(new BasicNameValuePair("profile", mProfile));*/
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            try {
            JSONObject params = new JSONObject();
            params.put("name", mName);
            params.put("email", mEmail);
            params.put("profile", mProfile);
                params.put("password", mPassword);
                params.put("token", accessToken);
                params.put("gender",mgender);
                params.put("gcmId",mgcmid);
                params.put("profileId",mProfileId);
                params.put("birthday", sdf.parse(mBirthDate));
                params.put("educationHistory", mEducationDetails);
                params.put("workHistory", mWorkDetails);



            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(mainurl.geturl() +"signuplogin", "POST",
                    params);

            // Check your log cat for JSON reponse
            //Log.d("Single Track JSON: ", json);


                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    String auth_token = jObj.getString("token");
                    String data = jObj.getString("data");
                    JSONObject jcustomerObj = new JSONObject(data);
                    String name = jcustomerObj.getString("name");
                    String customerNumber = jcustomerObj.getString("customerNumber");
                    String email = jcustomerObj.getString("email");
                    SharedPreferenceManager.setPreference("auth_token",auth_token);
                    SharedPreferenceManager.setPreference("name",name);
                    SharedPreferenceManager.setPreference("customerNumber",customerNumber);
                    SharedPreferenceManager.setPreference("email",email);
                    SharedPreferenceManager.setPreference("profile",mProfile);
                    SharedPreferenceManager.setPreference("profileImage", mProfileId);
                    return true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            return false;
    }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            pDialog.dismiss();

            if (success) {
                SharedPreferenceManager.setPreference("mIsSignedIn",true);
                ftububble();
                invalidateOptionsMenu();
                OnLoginAuthenticated();

            } else {
                if(mProfile.equals("facebook")||mProfile.equals("google") || mProfile.equals("linkedin"))
                {
                    if (mProfile.equals("facebook"))
                    {
                        if (accessTokenTracker != null) {
                            accessTokenTracker.stopTracking();
                        }
                        LoginManager.getInstance().logOut();
                    }
                    if (mProfile.equals("google"))
                    {
                        mGoogleApiClient.disconnect();
                    }
                    if(mProfile.equals("linked")) {
                        LISessionManager.getInstance(getApplicationContext()).clearSession();
                    }
                    Toast.makeText(MainActivity.this,
                            "failed", Toast.LENGTH_LONG).show();
                }
                else if (mName.isEmpty()) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
                else {
                    mPasswordView_signup.setError(getString(R.string.error_incorrect_password));
                    mPasswordView_signup.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
    public class Retrieveauthtokengoogle extends AsyncTask<Void, Void,String> {

        private final String maccountName;
        private final String mEmail;
        private final String mName;
        private final String mGender;
        private final String mProfileImageUrl;


        Retrieveauthtokengoogle(String accountName,String name,String email,String gender, String profileImageUrl) {
            maccountName = accountName;
            mEmail = email;
            mName = name;
            mGender = gender;
            mProfileImageUrl = profileImageUrl;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Fetching authtoken...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(Void... param) {
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), maccountName, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(final String result) {
            mTokenTask = null;
            pDialog.dismiss();
            if(result!=null)
            {
                mAuthTask = new UserLoginTask(mEmail,"",mName,"google",(mProfileImageUrl != null && mProfileImageUrl.length() > 0) ? mProfileImageUrl : "",result,mGender,SharedPreferenceManager.getPreference("registrationid"), "", "", "");
                mAuthTask.execute((Void) null);
            }
            else
            {
                mGoogleApiClient.disconnect();
            }
        }
    }
}
