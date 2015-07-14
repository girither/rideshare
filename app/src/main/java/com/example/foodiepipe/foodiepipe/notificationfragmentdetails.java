package com.example.foodiepipe.foodiepipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foodpipe.android.helper.ConnectionDetector;
import com.foodpipe.android.helper.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class notificationfragmentdetails extends ActionBarActivity implements View.OnClickListener {

    private getindividualnotificationdetailsetask mynotificationTask = null;
    JSONParser jsonParser = new JSONParser();
    TextView ridefromheader_source,ridefromheader_destination, todayortomorrowheader, timeofday, rideownernamevalue, rideowneremailvalue, rideownerphonevalue;
    getindividualnotificationdetailsetask individualnotificationstask;
    ProgressBar bar;
    Button acceptrequest, rejectrequest;
    LinearLayout detailform;
    CustomerAdapter customerlistadapter;
    private ProgressDialog pDialog;
    GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationfragmentdetails);
        SharedPreferenceManager.setApplicationContext(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ridefromheader_source = (TextView) findViewById(R.id.rideFromTextHeader_source);
        ridefromheader_destination = (TextView)findViewById(R.id.rideFromTextHeader_destination);
        todayortomorrowheader = (TextView) findViewById(R.id.rideday);
        timeofday = (TextView) findViewById(R.id.ridetime);
        rideownernamevalue = (TextView) findViewById(R.id.rideownernamevalue);
        rideowneremailvalue = (TextView) findViewById(R.id.rideowneremailvalue);
        rideownerphonevalue = (TextView) findViewById(R.id.rideownerphonenumbervalue);
        bar = (ProgressBar) findViewById(R.id.individualnotification_progress);
        detailform = (LinearLayout) findViewById(R.id.requestdatashow);
        mGridView = (GridView) findViewById(android.R.id.list);
        acceptrequest = (Button) findViewById(R.id.accept_request);
        rejectrequest = (Button) findViewById(R.id.reject_request);
        acceptrequest.setOnClickListener(this);
        rejectrequest.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        String requestId = (!extras.getString("requestId").isEmpty())?extras.getString("requestId"):SharedPreferenceManager.getPreference("currentrequestId");
        SharedPreferenceManager.setPreference("currentrequestId",requestId);
        new getindividualnotificationdetailsetask(requestId).execute();
    }

    public void onClick(View view) {

        ConnectionDetector cd = new ConnectionDetector(notificationfragmentdetails.this.getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            Toast.makeText(notificationfragmentdetails.this,
                    "Internet Connection Error Please connect to working Internet connection", Toast.LENGTH_LONG).show();
            // stop executing code by return
            return;
        }
        switch(view.getId()) {
            case R.id.accept_request:
                new acceptorrejecttojoinridetask(SharedPreferenceManager.getPreference("currentrequestId"),"accept").execute();
                break;
            case R.id.reject_request:
                new acceptorrejecttojoinridetask(SharedPreferenceManager.getPreference("currentrequestId"),"reject").execute();
                break;
        }

    }

    private class CustomerAdapter extends BaseAdapter {
        private List<customer> mSamples;
        public CustomerAdapter(List<customer> myDataset) {
            mSamples = myDataset;
        }

        @Override
        public int getCount() {
            return mSamples.size();
        }

        @Override
        public Object getItem(int position) {
            return mSamples.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mSamples.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            String todayortomorrow;
            if (convertView == null) {
                convertView = notificationfragmentdetails.this.getLayoutInflater().inflate(R.layout.customer_detail_list,
                        container, false);
            }


            ((TextView) convertView.findViewById(R.id.rideowneremailvalue)).setText(mSamples.get(position).getCustomerEmail());
            ((TextView) convertView.findViewById(R.id.rideownernamevalue)).setText(mSamples.get(position).getCustomerName());
            ((TextView) convertView.findViewById(R.id.rideownerphonenumbervalue)).setText(mSamples.get(position).getCustomerPhoneNumber());
            final String latlongposition = mSamples.get(position).getLatLong();
            ((Button)convertView.findViewById(R.id.see_pickup_point)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StringBuilder latlongbuilder = new StringBuilder();
                    latlongbuilder.append("geo:").append(latlongposition).append("?q=").append(latlongposition).append("(Pickuppoint)");
                    Uri gmmIntentUri = Uri.parse(latlongbuilder.toString());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            });

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notificationfragmentdetails, menu);
        return true;
    }


    public class getindividualnotificationdetailsetask extends AsyncTask<Void, Void,notificationdata > {

        private final String mrequestId;



        getindividualnotificationdetailsetask(String requestId) {
            mrequestId = requestId;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setVisibility(View.VISIBLE);

        }
        @Override
        protected notificationdata doInBackground(Void... param) {
            notificationdata info = null;

            try {
                JSONObject params = new JSONObject();
                params.put("requestId", mrequestId);

                // getting JSON string from URL
                String json = jsonParser.makeHttpRequest("http://radiant-peak-3095.herokuapp.com/getRequestDetails", "POST",
                        params);



                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    JSONObject ride = jObj.getJSONObject("requesterRide");
                    JSONObject customerindividualdata = jObj.getJSONObject("requesterCustomerProfile");
                    List<customer> customerlistdata = new ArrayList<customer>();
                    customer customeradapterdata = new customer(customerindividualdata.getString("name"),customerindividualdata.getString("email"),ride.getString("phoneNumber"),ride.getString("latlong"),customerindividualdata.getString("profileId"));
                    customerlistdata.add(customeradapterdata);
                    info = new notificationdata(ride.getString("source"), ride.getString("destination"), ride.getString("date"),customerlistdata,ride.getString("rideId"));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return info;
        }

        @Override
        protected void onPostExecute(final notificationdata notificationdataobject) {
            individualnotificationstask = null;
            bar.setVisibility(View.GONE);
            detailform.setVisibility((notificationdataobject!=null)?View.VISIBLE:View.GONE);
            if(notificationdataobject != null ){
                ridefromheader_source.setText(notificationdataobject.getSource());
                ridefromheader_destination.setText(notificationdataobject.getDestination());
                String dateOfRides = notificationdataobject.getDate().split("T")[0];
                String timeofrides = notificationdataobject.getDate().split("T")[1];
                timeofrides = timeofrides.split(".000Z")[0];
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate = sdf.format(cal.getTime());
                Calendar nextdaycal = Calendar.getInstance();
                nextdaycal.add(Calendar.DATE, 1);
                String todayortomorrow;
                SimpleDateFormat sdftomorrow = new SimpleDateFormat("yyyy-MM-dd");
                String tomorrowDate = sdftomorrow.format(nextdaycal.getTime());
                if(currentDate.equals(dateOfRides))
                {
                    todayortomorrow = "Today        ";
                }
                else if(tomorrowDate.equals(dateOfRides))
                {
                    todayortomorrow = "Tomorrow     ";
                }
                else
                {
                    todayortomorrow = dateOfRides;
                }
                if(!(todayortomorrow.trim().equals("Today") || todayortomorrow.trim().equals("Tomorrow")))
                {
                    acceptrequest.setVisibility(View.GONE);
                    rejectrequest.setVisibility(View.GONE);
                }
                todayortomorrowheader.setText(todayortomorrow);
                timeofday.setText(timeofrides);
                customerlistadapter = new CustomerAdapter(notificationdataobject.getCustomerlistdata());
                mGridView.setAdapter(customerlistadapter);
            }
        }

        @Override
        protected void onCancelled() {
            individualnotificationstask = null;
        }
    }
    public class acceptorrejecttojoinridetask extends AsyncTask<Void, Void, Boolean > {


        private final String mrequestId;
        private final String mstatus;


        acceptorrejecttojoinridetask(String requestId,String status) {
            mrequestId= requestId;
            mstatus=status;
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(notificationfragmentdetails.this);
            pDialog.setMessage("Sending reponse...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... param) {
            ridedata info = null;

            try {
                JSONObject params = new JSONObject();
                params.put("requestId", mrequestId);
                params.put("status",mstatus);
                String json = jsonParser.makeHttpRequest("http://radiant-peak-3095.herokuapp.com/acceptOrRejectTheRequestToJoin", "POST",
                        params);



                JSONObject jObj = new JSONObject(json);
                if(jObj != null) {
                    String message = jObj.getString("success");
                    return true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            pDialog.dismiss();
            if (success) {
                finish();
            } else {
                Toast.makeText(notificationfragmentdetails.this,
                        "Something went wrong while sending request. Please try again", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {

        }
    }
}
