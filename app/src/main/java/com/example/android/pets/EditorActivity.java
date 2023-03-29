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

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract;
import com.example.android.pets.data.PetsContract.PetsEntry;
import com.example.android.pets.data.PetsDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;
    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;
    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;
    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are: 0 for unknown gender, 1 for male, 2 for female.
     */
    private String mName;
    private int mGender =0  ;
    private String mBread ;
    private int mWeight=0;

   private SQLiteDatabase database;
   private PetsDbHelper petsDbHelper;

   private final int LOADER_ID=2;
   private  Uri petUri;

   private ImageView mPhotoView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;
    static final int READ_EXTERNAL_STORAGE=3;
    String photoPath=null;
    Bitmap cameraPhoto;
    Context context;
    int isCamera=0;


    private boolean hasChanged=false;
   private View.OnTouchListener mOnTouchListener =new View.OnTouchListener() {
       @Override
       public boolean onTouch(View view, MotionEvent motionEvent) {
           hasChanged=true;
           return false;
       }
   };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        context=EditorActivity.this;

        petsDbHelper=new PetsDbHelper(this);
        database=petsDbHelper.getWritableDatabase();


        //get data uri  from intent
        Intent receivedIntent= getIntent();
        petUri=receivedIntent.getData();

        if(petUri==null){
            //so open from fab button to add pet
            setTitle(R.string.editor_activity_title_new_pet);
        }else {
           // so come from list item to edit pet
            setTitle(R.string.Edit_Pet_title);
            getSupportLoaderManager().initLoader(LOADER_ID,null,this);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mPhotoView=(ImageView) findViewById(R.id.photo_Iv);
        FloatingActionButton cameraButton=(FloatingActionButton) findViewById(R.id.fab_camera);
        FloatingActionButton galleryButton=(FloatingActionButton)findViewById(R.id.fab_gallery);

        mNameEditText .setOnTouchListener(mOnTouchListener);
        mBreedEditText .setOnTouchListener(mOnTouchListener);
        mWeightEditText .setOnTouchListener(mOnTouchListener);
        mGenderSpinner .setOnTouchListener(mOnTouchListener);
        cameraButton.setOnTouchListener(mOnTouchListener);
        galleryButton.setOnTouchListener(mOnTouchListener);

        setupSpinner();

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "openCamera", Toast.LENGTH_SHORT).show();
                openCameraIntent();

            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ask for permission
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {//if permission granted
                    openGalleryIntent();
                }else {
                    // You can directly ask for the permission.

                    requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                            READ_EXTERNAL_STORAGE);
                }


            }
        });



    }
/************************************************************************************************************/
    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender =PetsEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }
    /**************************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         if(petUri==null){
          MenuItem menuItem= menu.findItem(R.id.action_delete);
          menuItem.setVisible(false);
    }
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                savePicture(cameraPhoto);
                    insertData();

                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
              showDeleteDialogue();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar

            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if(!hasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener= new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showDialogue(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*-----------------------------------------------------------------------------------------------**/
// intents result come from gallery or camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //come from camera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //bitmap photo
            cameraPhoto = data.getParcelableExtra("data");
            mPhotoView.setImageBitmap(cameraPhoto);
            isCamera=1;


        }else  if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            //set uri to save in database
            photoPath=selectedImageUri.toString();
            mPhotoView.setImageURI(selectedImageUri);
            isCamera=0;
        }
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    //to open gallery or camera
    private void openCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryIntent();
                    }else {
                    Toast.makeText(this,"can,t upload photos from gallery",Toast.LENGTH_SHORT).show();
                }
        }

    }

    public void savePicture(Bitmap cameraPhoto) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createFileAtInternal(photoPath));
            cameraPhoto.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    File createFileAtInternal(String fileName){
        String directoryName = "PetsApp";
        // fileName = "pets.png";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory =cw.getDir(directoryName,context.MODE_PRIVATE);


        if(!directory.exists()){
            directory.mkdir();
            File file= new File(directory,fileName);

            return  file;
        }else {
            File file= new File(directory,fileName);

            return  file;
        }

    }


    /*-------------------------------------------------------------------------------------------------------*/
    private void insertData() {
        mName = mNameEditText.getText().toString().trim();
        mBread = mBreedEditText.getText().toString().trim();
        if(mBread.isEmpty()){
            mBread="UNKOWEN BREED";
        }
        String weight = mWeightEditText.getText().toString().trim();

        try {
            mWeight = Integer.parseInt(weight);
        } catch (NumberFormatException e) {
            e.printStackTrace();

        }

        if (mName.isEmpty()) {
            Toast.makeText(this, "insert name please", Toast.LENGTH_LONG).show();
           // Toast.makeText(this, "invalid data..edit your data", Toast.LENGTH_LONG).show();
        }else if(  mWeight < 0) {
           Toast.makeText(this, "invalid weight", Toast.LENGTH_SHORT).show();
        }
            else {

            ContentValues insertedValue = new ContentValues();
            insertedValue.put(PetsEntry.COLUMN_PET_NAME, mName);
            insertedValue.put(PetsEntry.COLUMN_PET_BREED, mBread);
            insertedValue.put(PetsEntry.COLUMN_PET_GENDER, mGender);
            insertedValue.put(PetsEntry.COLUMN_PET_WEIGHT, mWeight);

            insertedValue.put(PetsEntry.COLUMN_IS_CAMERA,isCamera);
            //come from gallery
            if(isCamera==0&& photoPath !=null){
                insertedValue.put(PetsEntry.COLUMN_PET_PHOTO_PATH,photoPath.toString());
            }
            //come from camera
            else if(isCamera==1){
                photoPath=mName+mBread+".png";
                insertedValue.put(PetsEntry.COLUMN_PET_PHOTO_PATH,photoPath);}
            try {
               // long hasInserted = database.insert(PetsEntry.TABLE_NAME, null, insertedValue);
  //insert data at insert mode when intent uri is null

   if (petUri==null /*insert mode*/) {
          if(isCamera==1){ savePicture(cameraPhoto);}
       Uri uri = getContentResolver().insert(PetsContract.PETS_URI, insertedValue);
       long hasInserted = ContentUris.parseId(uri);
       if (hasInserted > 0) {
        Toast.makeText(this, "data inserted number :  " + hasInserted, Toast.LENGTH_LONG).show();
        finish();
       }
    //update mode when intent has coming uri
   }else{
       if(hasChanged /*update mode*/) {
           if(isCamera==1){ savePicture(cameraPhoto);}
           int numOfUpdatedRow = getContentResolver().update(petUri, insertedValue, null, null);
           if (numOfUpdatedRow > 0) {
               Toast.makeText(this, "Pet is updated successfully ", Toast.LENGTH_LONG).show();
               finish();
           }
       }else {
           Toast.makeText(this, "Pet already Exist", Toast.LENGTH_LONG).show();
       }
   }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }


    }



    /************************************************************************************************************/
private void showDialogue( DialogInterface.OnClickListener discardButtonClickListener){


    DialogInterface.OnClickListener positiveButtonClickListener= new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
           if(dialog!=null) {
               dialog.dismiss();
           }

        }
    };


    AlertDialog.Builder builder=new AlertDialog.Builder(this);
    builder.setMessage(R.string.unsaved_changes_dialog_msg)
            .setPositiveButton(R.string.keep_editing,positiveButtonClickListener)
            .setNegativeButton(R.string.discard,discardButtonClickListener);


    builder.create().show();

}
    private void showDeleteDialogue(){
    AlertDialog.Builder deleteDialogBuilder =new AlertDialog.Builder(this);
    deleteDialogBuilder.setMessage("Delete the pet ?? ")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deletePet();
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

    private void deletePet() {
        int numOfDeletedRow = getContentResolver().delete(petUri, null, null);
        if (numOfDeletedRow > 0) {
            Toast.makeText(this, "Pet is Deleted successfully ", Toast.LENGTH_LONG).show();
            finish();
        }
        String directoryName = "PetsApp";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory =cw.getDir(directoryName,context.MODE_PRIVATE);
        File photoFile=new File(directory,photoPath);
        photoFile.delete();


    }


/**************************************************************************************************************/

    @Override
    public void onBackPressed() {

//if there is no changes happened
        if(!hasChanged){
            super.onBackPressed();
             return;
        }
        DialogInterface.OnClickListener discardButtonClickListener= new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showDialogue(discardButtonClickListener);


    }
/******************************************************************************************************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

    String[] projection={PetsEntry._ID,PetsEntry.COLUMN_PET_NAME
            ,PetsEntry.COLUMN_PET_BREED,PetsEntry.COLUMN_PET_GENDER
            ,PetsEntry.COLUMN_PET_WEIGHT,PetsEntry.COLUMN_IS_CAMERA
            ,PetsEntry.COLUMN_PET_PHOTO_PATH};
        return new CursorLoader(this,petUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

   if( data.moveToFirst()) {
       int idColumnIndex = data.getColumnIndex(PetsEntry.COLUMN_PET_NAME);
       int nameColumnIndex = data.getColumnIndex(PetsEntry.COLUMN_PET_NAME);
       int breedColumnIndex = data.getColumnIndex(PetsEntry.COLUMN_PET_BREED);
       int genderColumnIndex = data.getColumnIndex(PetsEntry.COLUMN_PET_GENDER);
       int weightColumnIndex = data.getColumnIndex(PetsEntry.COLUMN_PET_WEIGHT);
       int isCameraColumnIndex=data.getColumnIndex(PetsContract.PetsEntry.COLUMN_IS_CAMERA);
       int  pathColumnIndex= data.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_PHOTO_PATH);

       String name = data.getString(nameColumnIndex);
       String breed = data.getString(breedColumnIndex);
       int gender = data.getInt(genderColumnIndex);
       int weight = data.getInt(weightColumnIndex);
       int isCamera= data.getInt(isCameraColumnIndex);
       String path = data.getString(pathColumnIndex);
       //set photo name if is camera is 0 we need it

       mNameEditText.setText(name);
       mBreedEditText.setText(breed);
       mWeightEditText.setText(String.valueOf(weight));
       //set the spinner to the selection depending on gender number
       switch (gender) {
           case PetsEntry.GENDER_MALE:
               mGenderSpinner.setSelection(1);
               break;
           case PetsEntry.GENDER_FEMALE:
               mGenderSpinner.setSelection(2);
               break;
           default:
               mGenderSpinner.setSelection(0);
               break;
       }

       if(isCamera==0 && path!=null){
           //photo from gallery
           try {
               Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),Uri.parse(path));
              mPhotoView.setImageBitmap(bitmap);
           } catch (FileNotFoundException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }

           //photoIv.setImageURI(Uri.parse(path));
           //photo from camera ...name of photo is saved
       }else if(isCamera==1 && path!=null){
           Bitmap photoBitmap= loadFileFromStorage(path);
           mPhotoView .setImageBitmap(photoBitmap);

//come from dummy data with photoHolder
       }else if(isCamera==0 && path==null){
           Bitmap photoBitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.image_holder);
           mPhotoView.setImageBitmap(photoBitmap);

       }

   }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.getText().clear();
        mBreedEditText.getText().clear();
        mWeightEditText.getText().clear();

    }
/*------------------------------------------------------------------------------*/
    private Bitmap loadFileFromStorage(String photoPath){

        InputStream inputStream=null;
        Bitmap loadedPhoto=null;
        try {
            //open connection to the file path
            inputStream=new FileInputStream(createFileAtInternal(photoPath));
            //read file fromStream
            loadedPhoto= BitmapFactory.decodeStream(inputStream);
            // loadedphoto=BitmapFactory.decodeFile(photoPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return loadedPhoto;
    }
    /*----------------------------------------------------------------------------------------------------------*/

}