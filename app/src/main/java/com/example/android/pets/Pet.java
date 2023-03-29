package com.example.android.pets;

public class Pet {
     int mID;
    private String mName;
    private int mGender  ;
    private String mBread ;
    private int mWeight ;



    Pet(int id , String name,  String bread ,int gender , int weight){
        mID=id;
        mName=name;
        mBread=bread;
        mWeight=weight;
        mGender=gender;

    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;}

    public int getGender() {
        return mGender;
    }

    public void setGender(int mGender) {
        this.mGender = mGender;
    }

    public String getBread() {
        return mBread;
    }

    public void setBread(String mBread) {
        this.mBread = mBread;
    }

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int mWeight) {
        this.mWeight = mWeight;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}
