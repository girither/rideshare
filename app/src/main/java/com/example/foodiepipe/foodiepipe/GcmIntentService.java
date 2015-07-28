package com.example.foodiepipe.foodiepipe;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.foodpipe.android.helper.JSONParser;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gbm on 6/30/15.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    JSONParser jsonParser = new JSONParser();

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "inside the onHandleIntent method");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            Log.i(TAG,"inside the extras.isEmpty clause");
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(TAG,"inside the first if clause");
                //sendNotification(extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(TAG,"inside the first else if clause");
               // sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                Log.i(TAG,"inside the second else if clause");
                for (int i = 0; i < 5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        Gcmbroadcastreciever.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle msg) {
        String data ="";
        if(msg != null) {
           data = msg.getString("NotificationType");
        }
            if(data.equals("StartRideDistanceTravelled")){
                new startridewithdistancetask(SharedPreferenceManager.getPreference("started_jrride"),SharedPreferenceManager.getPreference("locationstringdata")).execute((Void) null);
                SharedPreferenceManager.setPreference("locationstringdata","");
            }
            else if(data.equals("EndRideDistanceTravelled")){
                new endridewithdistancetask(SharedPreferenceManager.getPreference("started_jrride"),SharedPreferenceManager.getPreference("locationstringdata")).execute((Void) null);
                SharedPreferenceManager.setPreference("locationstringdata","");
            }
            else if(data.equals("requestToJoinTheRide")){
            String requestID = msg.getString("requestId");
                int count = SharedPreferenceManager.getIntPreference("notificationcount");
                SharedPreferenceManager.setPreference("notificationcount", count+1);
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent getnotificationdetails = new Intent(this,notificationfragmentdetails.class);
            getnotificationdetails.putExtra("requestId", requestID);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,getnotificationdetails, PendingIntent.FLAG_CANCEL_CURRENT);
            String source = msg.getString("source");
            String destination =msg.getString("destination");
            String customername = msg.getString("requesterCustomerName");
            String phonenumber = msg.getString("requesterCustomerPhoneNumber");
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Request To Join Ride From " + customername)
                            .setContentText("source :" + source + " destination :" + destination)
                            .setStyle(new NotificationCompat.InboxStyle()
                                    .addLine("source :" + source)
                                    .addLine("destination :" + destination)
                                    .setBigContentTitle("Request To Join Ride From " + customername)
                                    .setSummaryText("Phone Number :" + phonenumber));


            mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);

            Log.i(TAG,"in the penultimate line");
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
        else if(data.equals("acceptedByTheOwner")){
                String ownerrideid = msg.getString("ownerrideid");
                mNotificationManager = (NotificationManager)
                        this.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent getridedetails = new Intent(this,showupcomingridedetails.class);
                getridedetails.putExtra("rideId",ownerrideid);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,getridedetails, PendingIntent.FLAG_CANCEL_CURRENT);
                String emailId = msg.getString("ownerCustomerEmail");
                String customername = msg.getString("ownerCustomerName");
                String phonenumber = msg.getString("ownerPhoneNumber");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Ride Request accepted by " + customername)
                                .setContentText("email Id :" + emailId)
                                .setStyle(new NotificationCompat.InboxStyle()
                                        .addLine("email Id :" + emailId)
                                        .setBigContentTitle("Ride Request accepted by " + customername)
                                        .setSummaryText("Phone Number :"+phonenumber));


                mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);
                Log.i(TAG,"in the penultimate line");
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
            else if(data.equals("YouAreTheNewOwner") || data.equals("OwnerHasExitedTheRide") ||data.equals("FellowRiderHasExited")){
                String ownerrideid = msg.getString("jrId");
                mNotificationManager = (NotificationManager)
                        this.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent getridedetails = new Intent(this,showupcomingridedetails.class);
                getridedetails.putExtra("rideId",ownerrideid);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, getridedetails, PendingIntent.FLAG_CANCEL_CURRENT);
                String message = msg.getString("message");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(message)
                                .setStyle(new NotificationCompat.InboxStyle()
                                        .setBigContentTitle(message));


                mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);
                Log.i(TAG,"in the penultimate line");
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }
        else if(data.equals("rejectedByTheOwner")){
                String ownerrideid = msg.getString("ownerrideid");
                mNotificationManager = (NotificationManager)
                        this.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent getridedetails = new Intent(this,searchshowinduvidualrides.class);
                getridedetails.putExtra("rideId",ownerrideid);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,getridedetails, PendingIntent.FLAG_CANCEL_CURRENT);
                String customername = msg.getString("ownerCustomerName");
                String emailId = msg.getString("ownerCustomerEmail");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Ride Request rejected by " + customername)
                                .setContentText("email Id :" + emailId)
                                .setStyle(new NotificationCompat.InboxStyle()
                                        .addLine("email Id :" + emailId)
                                        .setBigContentTitle("Ride Request rejected by " + customername));


                mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);
                Log.i(TAG,"in the penultimate line");
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
      }

    public class startridewithdistancetask extends AsyncTask<Void, Void,String > {

        private final String mjrId;
        private final String mdistanceTravelled;


        startridewithdistancetask(String jRideId,String distancetravelled) {

            mjrId = jRideId;
            mdistanceTravelled = distancetravelled;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... param) {
            String data = null;

            try {
                JSONObject params = new JSONObject();
                params.put("jrId", mjrId);
                params.put("latLngString",mdistanceTravelled);

                String json = jsonParser.makeHttpRequest(mainurl.geturl() +"startRideDistanceTravelled", "POST",
                        params);



                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    data = jObj.getString("success");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(final String success) {
            if(success != null){

            }
        }

        @Override
        protected void onCancelled() {

        }
    }
    public class endridewithdistancetask extends AsyncTask<Void, Void,String > {

        private final String mjrId;
        private final String mdistanceTravelled;


        endridewithdistancetask(String jRideId,String distancetravelled) {

            mjrId = jRideId;
            mdistanceTravelled = distancetravelled;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... param) {
            String data = null;

            try {
                JSONObject params = new JSONObject();
                params.put("jrId", mjrId);
                params.put("latLngString",mdistanceTravelled);

                // getting JSON string from URL
                String json = jsonParser.makeHttpRequest(mainurl.geturl() +"endRideDistanceTravelled", "POST",
                        params);



                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    data = jObj.getString("success");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(final String success) {
            if(success != null){

            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}