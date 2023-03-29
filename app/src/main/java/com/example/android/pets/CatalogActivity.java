/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract;
import com.example.android.pets.data.PetsContract.PetsEntry;
import com.example.android.pets.data.PetsDbHelper;

import java.io.File;
import java.util.ArrayList;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
        , PetRecyclerViewAdapter.OnItemListener {
    PetsDbHelper petsDbHelper;
    SQLiteDatabase sqLiteDatabase;

    PetRecyclerViewAdapter mPetRecyclerViewAdapter;
    RecyclerView mRecyclerView;
    int CURSOR_LOADER_ID=1;
    View emptyView;

//set click listener for each item in list
    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivity(intent);
        }
    };
/*******************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(onClickListener);

//use to access database before
        petsDbHelper=new PetsDbHelper(this);
       sqLiteDatabase= petsDbHelper.getWritableDatabase();
       //view appear at empty case
       emptyView=findViewById(R.id.empty_view);

    //set the recyclerview
        mRecyclerView= (RecyclerView) findViewById(R.id.recycler_view_layout);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        //load data from database in background thread
       getSupportLoaderManager().initLoader(CURSOR_LOADER_ID,null,this);



       // queryPetData();
      //  displayDatabaseInfo();

    }

/****************************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                 insertDummyData();

                return true;

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
               showDeleteDialogue();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

/********************************************************************************************/

    /**
     *  helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        PetsDbHelper mDbHelper = new PetsDbHelper(this);
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.

       // Cursor cursor = db.rawQuery("SELECT * FROM " +PetsEntry.TABLE_NAME, null);
        String[] projection ={PetsEntry._ID
                , PetsEntry.COLUMN_PET_NAME
                ,PetsEntry.COLUMN_PET_BREED
                ,PetsEntry.COLUMN_PET_GENDER
                ,PetsEntry.COLUMN_PET_WEIGHT
                ,PetsEntry.COLUMN_IS_CAMERA
                ,PetsEntry.COLUMN_PET_PHOTO_PATH};


     Cursor cursor= getContentResolver().query(PetsContract.PETS_URI,projection,null,null,null);

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
/*----------------------------------------------------------------------------------------------------*/
  private void insertDummyData(){
      ContentValues insertedValue=new ContentValues();
      insertedValue.put(PetsEntry.COLUMN_PET_NAME,"garfield");
      insertedValue.put(PetsEntry.COLUMN_PET_BREED,"tabby");
      insertedValue.put(PetsEntry.COLUMN_PET_GENDER,PetsEntry.GENDER_MALE);
      insertedValue.put(PetsEntry.COLUMN_PET_WEIGHT, 7);
      insertedValue.put(PetsEntry.COLUMN_IS_CAMERA,0);
      insertedValue.put(PetsEntry.COLUMN_PET_PHOTO_PATH, (String) null);

      try {
          //old insert before content provider
         /* long hasInserted = sqLiteDatabase.insert(PetsEntry.TABLE_NAME,null,insertedValue);*/
          Uri uri=getContentResolver().insert(PetsContract.PETS_URI,insertedValue);
          long hasInserted= ContentUris.parseId(uri);

          if (hasInserted>0) {
              Toast.makeText(this," dummy data inserted",Toast.LENGTH_LONG).show();
             // displayDatabaseInfo();

          }
      } catch (Exception e) {
          e.printStackTrace();
      }


  }

  /*--------------------------------------------------------------------------------------------------------------------*/

/*******************************************************************************************/
    @Override
    protected void onStart() {
        super.onStart();
      //  displayDatabaseInfo();

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection ={PetsEntry._ID
                , PetsEntry.COLUMN_PET_NAME
                ,PetsEntry.COLUMN_PET_BREED
                ,PetsEntry.COLUMN_PET_GENDER
                ,PetsEntry.COLUMN_PET_WEIGHT
               ,PetsEntry.COLUMN_IS_CAMERA
                ,PetsEntry.COLUMN_PET_PHOTO_PATH};
        return new CursorLoader(this,PetsContract.PETS_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()==0)
        {
            Toast.makeText(this,"empty list",Toast.LENGTH_LONG).show();
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
        mPetRecyclerViewAdapter=new PetRecyclerViewAdapter(this,data,this);
        mRecyclerView.setAdapter(mPetRecyclerViewAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetRecyclerViewAdapter.clear();

    }

    @Override
    public void OnListItemClick(int id) {
        Uri petUri=ContentUris.withAppendedId(PetsContract.PETS_URI,id);
        Intent intent=new Intent(this,EditorActivity.class);
        intent.setData(petUri);
        startActivity(intent);
    }

    /*---------------------------------------------------------------------------------------------------------*/
    private void showDeleteDialogue(){
        AlertDialog.Builder deleteDialogBuilder =new AlertDialog.Builder(this);
        deleteDialogBuilder.setMessage("Are you sure ?Delete All pets ?? ")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAllPets();
                    }
                });
        deleteDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        deleteDialogBuilder.create().show();

    }

    private void deleteAllPets() {
        int numOfDeletedRow = getContentResolver().delete(PetsContract.PETS_URI, null, null);
        if (numOfDeletedRow > 0) {
            Toast.makeText(this, "Pets are Deleted successfully ", Toast.LENGTH_LONG).show();
        }

        String directoryName = "PetsApp";
       //delete directory that have all saves photos came from camera
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory =cw.getDir(directoryName,getApplicationContext().MODE_PRIVATE);
        directory.delete();
    }
}
