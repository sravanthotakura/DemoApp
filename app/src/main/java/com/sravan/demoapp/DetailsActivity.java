package com.sravan.demoapp;

import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.Header;
import org.json.*;
import com.loopj.android.http.*;
import com.sravan.demoapp.ConnectionDetector.ConnectionDetector;
import com.sravan.demoapp.DatabaseHandler.DatabaseHandler;

import java.io.IOException;
import java.sql.SQLException;


public class DetailsActivity extends ActionBarActivity {

    private ListView projectDetails;

    private String[] projectID;
    private String[] projectName;
    private String[] projectLocation;

    private JSONArray projectListArr;
    private JSONObject projectListObj;

    private DatabaseHandler dbHandler;

    private static String URL = "http://54.254.240.217:8080/app-task/projects/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        /*
        * Copies the database into system and opens the connection to database
        * */
        startDB();

        /*
        * Initializes the list view and declares the OnItem Event listener so that when user clicks on listview
        * the required action is performed
        * */
        projectDetails = (ListView) findViewById(R.id.projectDetails);
        projectDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent projectDetailsIntent = new Intent(DetailsActivity.this, ProjectDetails.class);
                projectDetailsIntent.putExtra("pID", projectID[i]);
                projectDetailsIntent.putExtra("pName", projectName[i]);
                projectDetailsIntent.putExtra("pLoc", projectLocation[i]);
                startActivity(projectDetailsIntent);
            }
        });

        /*
        * Used to check whether the mobile is connected to internet
        * Returns true if yes else no
        * */

        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if(isInternetPresent) {
            getProjectsList();
        } else {
            getProjectsListFromLocalDB();
        }

    }

    /*
    * Copies the database into system and opens the connection to database
    * */
    private void startDB() {
        dbHandler = new DatabaseHandler(this);
        try {
            dbHandler.createDataBase();
            dbHandler.openDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    * If device is connected to internet this method downloads the list from the specified URL
    * */

    private void getProjectsList() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                displayProjectList(new String(bytes), true);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                displayErrorMessage("Error connecting to server");
            }
        });
    }

    /*
    * If the device is not connected to internet this method retrieves the data from local database
    * */

    private void getProjectsListFromLocalDB() {
        displayProjectList(dbHandler.getProjectsList().toString(), false);
    }

    /*
    * This method is used to display the list of projects in listview that are downloaded or retrieved
    * */

    private void displayProjectList(String output, boolean connection) {
        try {
            projectListArr = new JSONArray(output);

            projectID           = new String[projectListArr.length()];
            projectName         = new String[projectListArr.length()];
            projectLocation     = new String[projectListArr.length()];

            for(int i=0; i<projectListArr.length(); i++) {
                projectListObj = projectListArr.getJSONObject(i);
                projectID[i] = projectListObj.getString("id");
                projectName[i] = projectListObj.getString("projectName");
                projectLocation[i] = projectListObj.getString("lat")+"/"+projectListObj.getString("lon");
            }

            ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, projectName);
            projectDetails.setAdapter(arrayAdapter);

            /*
            * If connection is present the data downloaded from URL is synced to local db so that the data is
            * available offline
            * */
            if(connection) {
                try {
                    long status = dbHandler.addProjectsList(output);
                    if(status >= 1) {
                        displayErrorMessage("Synced to local database");
                    } else {
                        displayErrorMessage("Error syncing to local database");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    * This method is used to display the toast messages in the application.
    * */

    private void displayErrorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
