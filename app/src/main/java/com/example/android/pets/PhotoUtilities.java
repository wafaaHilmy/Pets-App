package com.example.android.pets;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoUtilities {

    Context context;
    String fileName;

    public PhotoUtilities(Context context,String fileName) {
        this.context=context;
        //name of picture
        this.fileName=fileName;
    }

    public void savePicture(Bitmap cameraPhoto) {


        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createFileAtInternal(fileName));
            cameraPhoto.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            Toast.makeText(context, "photoSaved", Toast.LENGTH_LONG).show();

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
//create file at internal storage
    File createFileAtInternal(String fileName){
        String directoryName = "petsApp";
        // fileName = "pets.png";
        ContextWrapper cw = new ContextWrapper(context);
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

}
