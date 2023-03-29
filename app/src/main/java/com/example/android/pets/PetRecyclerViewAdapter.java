package com.example.android.pets;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.pets.data.PetsContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PetRecyclerViewAdapter extends RecyclerView.Adapter<PetRecyclerViewAdapter.PetViewHolder> {

    Context mContext ;
   Cursor mData;
   public interface OnItemListener{
       public void OnListItemClick(int id);
   }
   OnItemListener mOnItemListener;
    int currentId;

    PetRecyclerViewAdapter (Context context, Cursor data,OnItemListener onItemListener){
        mContext=context;
        mData=data;
        mOnItemListener=onItemListener;


    }


    @Override
    public PetRecyclerViewAdapter.PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View list_item_view= LayoutInflater.from(mContext).inflate(R.layout.pet_list_item,parent,false);
        return new PetViewHolder(list_item_view);
    }

    @Override
    public void onBindViewHolder(PetRecyclerViewAdapter.PetViewHolder holder, int position) {

        int  idColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry._ID);
        int  nameColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_NAME);
        int   breedColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_BREED);
        int  genderColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_GENDER);
        int  weightColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_WEIGHT);

        int  isCameraColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry.COLUMN_IS_CAMERA);
        int  pathColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_PHOTO_PATH);

        mData.moveToPosition(position);

        int id= mData.getInt(idColumnIndex);
        String name= mData.getString(nameColumnIndex);
        String breed= mData.getString(breedColumnIndex);
        int gender= mData.getInt(genderColumnIndex);
        int weight=mData.getInt(weightColumnIndex);

        int isCamera=mData.getInt(isCameraColumnIndex);
        String path =mData.getString(pathColumnIndex);
        Log.e("is camera//////",""+isCamera + isCameraColumnIndex);
        Log.e("is path//////",""+path+pathColumnIndex);


        holder.nameTv.setText(name);
        holder.breadTv.setText(breed);
        String genderText=updateGender(gender);
        holder.genderTv.setText(genderText);
        holder.weightTv.setText(String.valueOf(weight));

       if(isCamera==0 && path!=null){
            //photo from gallery so parse uri directly
           try {
               Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),Uri.parse(path));
               holder.photoIv.setImageBitmap(bitmap);
           } catch (FileNotFoundException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }

           //holder.photoIv.setImageURI(Uri.parse(path));

            //photo from camera ...name of photo is saved in database so load it from internal storage
        }else if(isCamera==1&& path!=null){
           Bitmap photoBitmap= loadFileFromStorage(path);
           holder.photoIv.setImageBitmap(photoBitmap);

        }else if(isCamera==0 && path==null){
           // here path is null lick in insert dummy data * so attatch photo holder
           Bitmap photoBitmap= BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image_holder);
           holder.photoIv.setImageBitmap(photoBitmap);

       }




    }

    @Override
    public int getItemCount() {
        return  mData.getCount();

    }

/*************************************************************************************/
   public class  PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTv;
        TextView breadTv;
        TextView genderTv;
        TextView  weightTv;
        ImageView photoIv;

        public PetViewHolder(View itemView) {
            super(itemView);

            nameTv= (TextView) itemView.findViewById(R.id.pet_name_list_item);
            breadTv= (TextView) itemView.findViewById(R.id.pet_bread_list_item);
            genderTv= (TextView) itemView.findViewById(R.id.pet_gender_list_item);
            weightTv= (TextView) itemView.findViewById(R.id.pet_weight_list_item);
            photoIv=(ImageView) itemView.findViewById(R.id.pet_image_list_item);


            itemView.setOnClickListener(this);

        }

    @Override
    public void onClick(View view) {
        int clickedPosition =getAdapterPosition();
        mData.moveToPosition(clickedPosition);
        int  idColumnIndex=mData.getColumnIndex(PetsContract.PetsEntry._ID);
       currentId = mData.getInt(idColumnIndex);

        mOnItemListener.OnListItemClick(currentId);

    }
}
/***************************************************************************************/

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

    File createFileAtInternal(String fileName){
        String directoryName = "PetsApp";

        ContextWrapper cw = new ContextWrapper(mContext);
        File directory =cw.getDir(directoryName,mContext.MODE_PRIVATE);


        if(!directory.exists()){
            //create directory and create file inside it his name is photo name
            directory.mkdir();
            File file= new File(directory,fileName);
            return  file;
        }else {
            File file= new File(directory,fileName);
            return  file;
        }

    }

/*-------------------------------------------------------------------------------------------------------------------*/

private String updateGender(int gender){

       String genderText;
        if (gender== PetsContract.PetsEntry.GENDER_MALE) {
            genderText = "Male" ;// Male

        } else if (gender== PetsContract.PetsEntry.GENDER_FEMALE) {
            genderText ="Female"; // Female
        } else {
            genderText = "Unknown"; // Unknown
        }
        return genderText;
    }

    public void clear() {
   int size= mData.getCount();
        mData.close();
        notifyItemRangeRemoved(0, size);
    }



    }


