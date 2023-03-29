package com.example.android.pets.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.content.ContentResolver;

public final class PetsContract {

    private PetsContract(){

    }


    public static final class PetsEntry implements BaseColumns {
        public static final String DATA_BASE_NAME="shelter";

        public static final String TABLE_NAME="pets";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public final static String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER ="gender";
        public final static String COLUMN_PET_WEIGHT = "weight";
        public static final String COLUMN_IS_CAMERA ="camera";
        public final static String COLUMN_PET_PHOTO_PATH = "photo";

        // Possible values for the gender of the pet.
        //gender is 0 for unknown and 1 for male and  2 for female
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;


        //The MIME type of the CONTENT_URI for a list of pets.
         // "vnd.android.cursor.dir/contact"/"com.example.android.pets"/"pets"
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +PETS_PATH;


         // The MIME type of the CONTENT_URI for a single pet.
        //"vnd.android.cursor.item/contact"/"com.example.android.pets"/"pets"
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PETS_PATH;



    }
    //"com.example.android.pets"
  public static final String CONTENT_AUTHORITY="com.example.android.pets";
    public static final String PETS_PATH="pets";





    //  "content://com.example.android.pets"
    public static Uri  BASE_URI=Uri.parse("content://"+CONTENT_AUTHORITY);


    /*   CONTENT://com.example.android.pets/pets"  */
   public static Uri PETS_URI=Uri.withAppendedPath(BASE_URI,PETS_PATH);





}
