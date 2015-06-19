package com.sravan.demoapp.DatabaseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by lenovo on 6/16/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/com.sravan.demoapp/databases/";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "demoapp_db";

    private static final String TABLE_PROLIST = "projectlist_tb";
    private static final String TABLE_PROLIST_DTS = "projectlist_details_tb";

    private Context myContext;

    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Create tables again

        onCreate(sqLiteDatabase);
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    /**
     * Description: Used to open the required database created in the local machine
     * Input: database name
     * Output: connection object to database
     */

    public void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        sqLiteDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * Description: Used to sync the Project List to local database retrieved from the URL
     * Input: JSONArray in string format containing the project list details
     * Output: Status of insert query performed on database
     */

    public long addProjectsList(String output) {
        long status = -1;
        try {
            JSONObject projectListObj = new JSONObject();
            JSONArray projectListArr = new JSONArray(output);

            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("delete from "+ TABLE_PROLIST);

            ContentValues values = new ContentValues();
            for(int i=0; i<projectListArr.length(); i++) {
                projectListObj = projectListArr.getJSONObject(i);
                values.put("id", projectListObj.getString("id"));
                values.put("project_name", projectListObj.getString("projectName"));
                values.put("latitude", projectListObj.getString("lat"));
                values.put("longitude", projectListObj.getString("lon"));
                status = db.insert(TABLE_PROLIST, null, values);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return  -1;
        }
        return status;
    }

    /**
     * Description: Used to sync the Project Details based on ID to local database retrieved from the URL
     * Input: Project ID, addressLine1, addressLine2, city, description
     * Output: Status of insert query performed on database
     */

    public long addProjectListDetails(String id, String addrLine1, String addrLine2, String city, String description, byte[] builderLogo, String builderDescription) {
        long status = -1;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String getDetails = "SELECT * FROM " + TABLE_PROLIST_DTS +" WHERE id='"+id+"'";
            Cursor cursor = db.rawQuery(getDetails, null);
            if (!cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("address_line1", addrLine1);
                values.put("address_line2", addrLine2);
                values.put("city", city);
                values.put("projectdescription", description);
                values.put("builderlogo", builderLogo);
                values.put("builderdescription", builderDescription);
                status = db.insert(TABLE_PROLIST_DTS, null, values);
                cursor.close();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return  -1;
        }
        return status;
    }

    /*
    * Description: Used to get the list of projects from local db which are displayed on listview
    * Input:
    * Output: JSONArray containing the list of projects available
    * */

    public JSONArray getProjectsList() {
        JSONArray projectListArr = new JSONArray();
        JSONObject projectListObj = new JSONObject();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String getDetails = "SELECT * FROM " + TABLE_PROLIST;
            Cursor cursor = db.rawQuery(getDetails, null);
            if (cursor.moveToFirst()) {
                do {
                    projectListObj.put("id", cursor.getString(0));
                    projectListObj.put("projectName", cursor.getString(1));
                    projectListObj.put("lat", cursor.getString(2));
                    projectListObj.put("lon", cursor.getString(3));

                    projectListArr.put(projectListObj);
                    projectListObj = new JSONObject();
                } while (cursor.moveToNext());
            } else {
                projectListObj.put("error", "No records found in local db");
                projectListArr.put(projectListObj);
            }
            cursor.close();
            return projectListArr;
        } catch (Exception e) {
            try {
                projectListObj.put("error", "Error connecting to database");
                projectListArr.put(projectListObj);
                return projectListArr;
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
        return projectListArr;
    }

    /*
    * Description: Used to get the project details based on project ID from local android database
    * Input: Project ID
    * Output: JSONObject containing project details
    * */

    public JSONObject getProjectListDetails(String id) {
        JSONObject projectListDetailsObj = new JSONObject();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String getDetails = "SELECT * FROM " + TABLE_PROLIST_DTS +" WHERE id ='"+id+"'";
            Cursor cursor = db.rawQuery(getDetails, null);
            if (cursor.moveToFirst()) {
                do {
                    projectListDetailsObj.put("addressLine1", cursor.getString(1));
                    projectListDetailsObj.put("addressLine2", cursor.getString(2));
                    projectListDetailsObj.put("city", cursor.getString(3));
                    projectListDetailsObj.put("description", cursor.getString(4));
                    projectListDetailsObj.put("builderLogo", cursor.getBlob(5));
                    projectListDetailsObj.put("builderDescription", cursor.getString(6));
                } while (cursor.moveToNext());
            } else {
                projectListDetailsObj.put("error", "No records found in local db");
            }
            cursor.close();
            return projectListDetailsObj;
        } catch (Exception e) {
            try {
                projectListDetailsObj.put("error", "Error connecting to database");
                return projectListDetailsObj;
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return projectListDetailsObj;
    }
}
