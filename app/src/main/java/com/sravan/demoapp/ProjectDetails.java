package com.sravan.demoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.loopj.android.http.Base64;
import com.sravan.demoapp.ConnectionDetector.ConnectionDetector;
import com.sravan.demoapp.DatabaseHandler.DatabaseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Objects;


public class ProjectDetails extends ActionBarActivity {

    private EditText projectName;
    private EditText projectDesc;
    private EditText addressLine1;
    private EditText addressLine2;
    private EditText city;
    private EditText builderDesc;

    private ImageView builderLogo;
    private ImageView imgObj;

    private Button mapBtn;

    private JSONObject projectDetailsObj;
    private JSONArray  projectDetailsArr;

    private DatabaseHandler dbHandler;

    private Boolean connection = true;

    private static String URL = "http://54.254.240.217:8080/app-task/projects/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        /*
        * Copies the database into system and opens the connection to database
        * */
        startDB();

        projectName     = (EditText) findViewById(R.id.projectNameEditView);
        projectName.setText(getIntent().getExtras().getString("pName"));

        addressLine1    = (EditText) findViewById(R.id.addressEditText1);
        addressLine2    = (EditText) findViewById(R.id.addressEditText2);
        city            = (EditText) findViewById(R.id.cityEditView);
        projectDesc     = (EditText) findViewById(R.id.projectDescEditView);
        builderDesc     = (EditText) findViewById(R.id.builderDescEditView);

        builderLogo     = (ImageView) findViewById(R.id.builderLogoImageView);

        /*
        * This event listener loads the map activity and displays the location on map using the specified lat long
        * */
        mapBtn          = (Button) findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(ProjectDetails.this, MainActivity.class);
                mapIntent.putExtra("pName", getIntent().getExtras().getString("pName"));
                mapIntent.putExtra("pID", getIntent().getExtras().getString("pID"));
                mapIntent.putExtra("pLoc", getIntent().getExtras().getString("pLoc"));
                mapIntent.putExtra("pDetails", "Address Line1: "+addressLine1.getText().toString()+"\n Address Line2: "+addressLine2.getText().toString()+"\n City: "+city.getText().toString()+ "Project Description: "+projectDesc.getText().toString());
                startActivity(mapIntent);
            }
        });

        /*
        * Used to check whether the mobile is connected to internet
        * Returns true if yes else no
        * */
        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if(isInternetPresent) {
            getProjectDescription(getIntent().getExtras().getString("pID"));
        } else {
            getProjectDescriptionFromLocalDB(getIntent().getExtras().getString("pID"));
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
    private void getProjectDescription(String id) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL + id, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                displayProjectDetails(new String(bytes), true);
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
    private void getProjectDescriptionFromLocalDB(String pID) {
        displayProjectDetails(dbHandler.getProjectListDetails(pID).toString(), false);
    }

    /*
    * This method is used to display the project details that are downloaded or retrieved and set them to specified fields
    * */
    private void displayProjectDetails(String output, boolean connection) {
        try {
            projectDetailsObj = new JSONObject(output);
            addressLine1.setText(projectDetailsObj.getString("addressLine1"));
            addressLine2.setText(projectDetailsObj.getString("addressLine2"));
            city.setText(projectDetailsObj.getString("city"));
            projectDesc.setText(projectDetailsObj.getString("description"));

            Spanned s = Html.fromHtml(projectDetailsObj.getString("builderDescription"));
            builderDesc.setText(s);

            try {
                if(connection) {
                    new downloadImageFromURL().execute(projectDetailsObj.getString("builderLogo"), builderLogo, connection);
                } else {
                    byte [] encodeByte= projectDetailsObj.getString("builderLogo").getBytes();
                    Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    //Bitmap bitmap = (Bitmap) projectDetailsObj.get("builderLogo");
                    builderLogo.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                displayErrorMessage(e.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    * This method is used to download the image content from the specified URL and set it to the specified
    * ImageView
    * */

    class downloadImageFromURL extends AsyncTask<Object, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Object... strings) {
            String imageURL = strings[0].toString();
            imgObj = (ImageView) strings[1];
            connection = (Boolean) strings[2];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imgObj.setImageBitmap(bitmap);

            int bytes = bitmap.getByteCount();
            //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
            //int bytes = b.getWidth()*b.getHeight()*4;

            ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
            bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

            byte[] byteArray = buffer.array();

            /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();*/

            if(connection) {
                long status = dbHandler.addProjectListDetails(getIntent().getExtras().getString("pID"), addressLine1.getText().toString(),
                        addressLine2.getText().toString(), city.getText().toString(), projectDesc.getText().toString(), byteArray, builderDesc.getText().toString() );
                if(status >= 1) {
                    displayErrorMessage("Synced to local database");
                } else {
                    displayErrorMessage("Already synced to local database");
                }
            }
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
        getMenuInflater().inflate(R.menu.menu_project_details, menu);
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
