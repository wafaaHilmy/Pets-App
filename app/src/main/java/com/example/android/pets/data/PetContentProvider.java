package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.system.ErrnoException;
import android.util.Log;

import com.example.android.pets.data.PetsContract.PetsEntry;

public class PetContentProvider<uriMatcher> extends ContentProvider {
    private static final String TAG=PetContentProvider.class.getSimpleName();


    //initialize a PetDbHelper object to gain access to the pets database.
    PetsDbHelper mDbHelper;

    private static final int PET_ID = 101;
    private static final int PETS= 100;

    private static final UriMatcher sUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);
      static {
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PETS_PATH,PETS);
          Log.i("TAG", "static initializer: "+sUriMatcher.match(PetsContract.PETS_URI) );
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PETS_PATH+"/#",PET_ID);
      }





    @Override
    public boolean onCreate() {
        mDbHelper=new PetsDbHelper(getContext());
        return true;
    }
    /*------------------------------------------------------------------------------------------------------------------*/
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArguments, @Nullable String orderBy) {
       SQLiteDatabase database =mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match=sUriMatcher.match(uri);
          switch (match){
              case PETS:
            cursor=  database.query(PetsEntry.TABLE_NAME,
                         projection,selection,selectionArguments,null,null,orderBy);
            break;

              case PET_ID:
              String selections= new String(PetsEntry._ID+"=?");
              //get id from uri at last
              String [] selectionArgs= new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor=  database.query(PetsEntry.TABLE_NAME,
                          projection,selections,selectionArgs,null,null,orderBy);
                break;

              default:
                 throw new IllegalArgumentException(uri.toString()+" error happen in matcher");





          }
          //any data changed at uri we have to update the cursor
         cursor.setNotificationUri(getContext().getContentResolver()/* listener at activity*/, uri);
          return cursor;

    }
    /*--------------------------------------------------------------------------------------------------------------*/
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

/*------------------------------------------------------------------------------------------------------------------*/
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();


             long  id = database.insert(PetsEntry.TABLE_NAME, null, contentValues);

                if (id == -1) {
                    Log.e(TAG, "insert: error to insert a pet .return id =-1");
                    return null;
                }

      getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);

    }

    /*---------------------------------------------------------------------------------------------------------*/


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int  numOfDletedRows;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
               numOfDletedRows= database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                numOfDletedRows = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
              break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
       return numOfDletedRows;


    }
    /*---------------------------------------------------------------------------------------------------------*/

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
          SQLiteDatabase database=mDbHelper.getWritableDatabase();
        int numOfRows;
        switch (sUriMatcher.match(uri)){
              //update multiple rows
              case PETS:
                numOfRows = database.update(PetsEntry.TABLE_NAME,contentValues,selection,selectionArgs);
                  break;
              case PET_ID:
                  selection=PetsEntry._ID+"=?";
                  selectionArgs=new String[]{ String.valueOf(ContentUris.parseId(uri))};
                   numOfRows = database.update(PetsEntry.TABLE_NAME,contentValues,selection,selectionArgs);
                  break;
            default:
                throw new IllegalArgumentException(uri.toString()+" Update is not supported for \" + uri");
          }

        getContext().getContentResolver().notifyChange(uri,null);
        return numOfRows;
    }
}
