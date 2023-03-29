package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import com.example.android.pets.data.PetsContract.PetsEntry;

public class PetsDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME =PetsEntry.DATA_BASE_NAME;

    public PetsDbHelper(@Nullable Context context) {
        super(context,DATABASE_NAME, null,DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL(CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_PETS_TABLE);
        onCreate(sqLiteDatabase);

    }

    private static final String CREATE_PETS_TABLE="CREATE TABLE " + PetsEntry.TABLE_NAME +"(" +
            PetsEntry._ID + " INTEGER PRIMARY KEY, "
            + PetsEntry.COLUMN_PET_NAME +" TEXT NOT NULL ,"
            + PetsEntry. COLUMN_PET_BREED+ " TEXT NOT NULL ,"
            + PetsEntry.COLUMN_PET_GENDER + " TEXT NOT NULL ,"
            +PetsEntry .COLUMN_PET_WEIGHT + " INTEGER NOT NULL,"
            +PetsEntry .COLUMN_IS_CAMERA + " INTEGER,"
            +PetsEntry .COLUMN_PET_PHOTO_PATH + " TEXT )";



//delete table
    private static final String DELETE_PETS_TABLE =
            "DROP TABLE IF EXISTS " + PetsEntry.TABLE_NAME;

}
