package com.example.foodiepipe.foodiepipe;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.foodpipe.android.helper.ConnectionDetector;
import com.foodpipe.android.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gbm on 7/1/15.
 */
public class searchyourridesfragment extends SwipeRefreshListFragment {

    private static final String LOG_TAG = searchyourridesfragment.class.getSimpleName();
    JSONParser jsonParser = new JSONParser();
    private static final int LIST_ITEM_COUNT = 20;
    private getmyridetask mMyrideTask = null;
    ListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ridedata rideobj = (ridedata)getListAdapter().getItem(position);
        if(rideobj.getNoresults() == null)
        {
            Intent searchrides = new Intent(getActivity(),Searchridessourcedestination.class);
            searchrides.putExtra("timeChoice", rideobj.getTodayortomorrow().toLowerCase().trim());
            SharedPreferenceManager.setPreference("myrideId", rideobj.getRideId());
            startActivity(searchrides);
        }
        getActivity().overridePendingTransition(R.animator.activity_in, R.animator.activity_out);
    }

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConnectionDetector cd = new ConnectionDetector(getActivity().getApplicationContext());
        if (!cd.isConnectingToInternet()) {
            List<ridedata> noresultsarray = new ArrayList<ridedata>();
            ridedata info = new ridedata(null,null,null);
            info.setNoresults("No Internet Available Currently");
            noresultsarray.add(info);
            adapter = new noresultsadapter(getActivity(),noresultsarray);

            // Set the adapter between the ListView and its backing data.
            setListAdapter(adapter);

        }
        else {
            mMyrideTask = new getmyridetask();
            mMyrideTask.execute();
        }
        // BEGIN_INCLUDE (setup_refreshlistener)
        /**
         * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });
        // END_INCLUDE (setup_refreshlistener)
    }
    // END_INCLUDE (setup_views)



    // BEGIN_INCLUDE (setup_refresh_menu_listener)
    /**
     * Respond to the user's selection of the Refresh action item. Start the SwipeRefreshLayout
     * progress bar, then initiate the background task that refreshes the content.
     *
     * <p>A color scheme menu item used for demonstrating the use of SwipeRefreshLayout's color
     * scheme functionality. This kind of menu item should not be incorporated into your app,
     * it just to demonstrate the use of color. Instead you should choose a color scheme based
     * off of your application's branding.
     */

    // END_INCLUDE (setup_refresh_menu_listener)

    // BEGIN_INCLUDE (initiate_refresh)
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");
        ConnectionDetector cd = new ConnectionDetector(getActivity().getApplicationContext());
        if (!cd.isConnectingToInternet()) {
            List<ridedata> noresultsarray = new ArrayList<ridedata>();
            ridedata info = new ridedata(null,null,null);
            info.setNoresults("No Internet Available Currently");
            noresultsarray.add(info);
            adapter = new noresultsadapter(getActivity(),noresultsarray);

            // Set the adapter between the ListView and its backing data.
            setListAdapter(adapter);

        }
        else {
            mMyrideTask = new getmyridetask();
            mMyrideTask.execute();
        }

    }
    public class getmyridetask extends AsyncTask<Void, Void, List<ridedata>> {


        @Override
        protected List<ridedata> doInBackground(Void... param) {
            List<ridedata> ridedataArray = new ArrayList<ridedata>();
            // Building Parameters
            /*List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id, song id as GET parameters
            params.add(new BasicNameValuePair("name", mName));
            params.add(new BasicNameValuePair("email", mEmail));
            params.add(new BasicNameValuePair("password", mPassword));
            params.add(new BasicNameValuePair("profile", mProfile));*/
            try {
                JSONObject params = new JSONObject();

                // getting JSON string from URL
                String json = jsonParser.makeHttpRequest("http://radiant-peak-3095.herokuapp.com/getMyRides", "POST",
                        params);



                JSONObject jObj = new JSONObject(json);
                if(jObj != null){
                    JSONArray rides = jObj.getJSONArray("rides");
                    for(int i=0; i<rides.length(); i++){
                        JSONObject rideindividualdata = rides.getJSONObject(i);
                        ridedata info = new ridedata(rideindividualdata.getString("source"),rideindividualdata.getString("destination"),rideindividualdata.getString("date"),rideindividualdata.getString("rideId"),"ride",rideindividualdata.getString("customerNumber"));
                        ridedataArray.add(info);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ridedataArray;
        }

        @Override
        protected void onPostExecute(final List<ridedata> ridedataArray) {
            mMyrideTask = null;

            super.onPostExecute(ridedataArray);
            setRefreshing(false);
            if(!ridedataArray.isEmpty()){
                adapter = new customridedataadapter(getActivity(),ridedataArray);

                // Set the adapter between the ListView and its backing data.
                setListAdapter(adapter);
            }
            else
            {
                List<ridedata> noresultsarray = new ArrayList<ridedata>();
                ridedata info = new ridedata(null,null,null);
                info.setNoresults("No Results Available Currently");
                noresultsarray.add(info);
                adapter = new noresultsadapter_searchrides(getActivity(),noresultsarray);

                // Set the adapter between the ListView and its backing data.
                setListAdapter(adapter);
            }
        }

        @Override
        protected void onCancelled() {
            mMyrideTask = null;
        }
    }

}