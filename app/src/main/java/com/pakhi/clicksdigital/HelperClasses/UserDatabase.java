package com.pakhi.clicksdigital.HelperClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "user.db";
    public static final String TABLE_NAME = "current_user";

    public UserDatabase(@Nullable Context context) {
        super(context, TABLE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME
                + " (" + ConstFirebase.USER_ID + " TEXT PRIMARY KEY,"
                + ConstFirebase.USER_NAME + " TEXT,"
                + ConstFirebase.USER_BIO + " TEXT,"
                + ConstFirebase.IMAGE_URL + " TEXT,"
                + ConstFirebase.USER_TYPE + " TEXT,"
                + ConstFirebase.CITY + " TEXT,"
                + "expectations_from_us" + " TEXT,"
                + "experiences" + " TEXT,"
                + "gender" + " TEXT,"
                + "number" + " TEXT,"
                + "offer_to_community" + " TEXT,"
                + "speaker_experience" + " TEXT,"
                + "email" + " TEXT,"
                + "weblink" + " TEXT,"
                + "working" + " TEXT,"
                + "last_name" + " TEXT,"
                + "company" + " TEXT,"
                + "country" + "TEXT,"
                + "referred_by" + "TEXT" +
                ")");
        // db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("TESTINGUSERDB", "----------------- onupgrade");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        //db.close();
    }

  /*  public boolean insertData(String userid, String name, String bio, String image_url, String user_type) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues =new ContentValues();
        contentValues.put(col1,userid);
        contentValues.put(col2,name);
        contentValues.put(col3,bio);
        contentValues.put(col4,image_url);
        contentValues.put(col5,user_type);
       long result = db.insert(TABLE_NAME,null,contentValues);
       if (result==-1)return false;
       return true;
    }*/
/*  public String getName(String  COL) {

      String rv = "not found";
      SQLiteDatabase db = this.getWritableDatabase();

      *//*String whereclause = "ID=?";
      String[] whereargs = new String[]{String.valueOf(id)};
      Cursor csr = db.query(TABLE_NAME,null,whereclause,whereargs,null,null,null);*//*
      Cursor csr=db.rawQuery("select * from " + TABLE_NAME, null);
      if (csr.moveToFirst()) {
          rv = csr.getString(csr.getColumnIndex(COL));
      }
      return rv;
  }*/

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        //+" where "+Const.USER_ID+" = "+currentUserID
        //db.close();
        //if (res != null && res.getCount() > 0) {
            res.moveToFirst();
        //}
        return res;
    }

    public User getSqliteUser() {
        Cursor res = getAllData();
        User user = new User(res.getString(res.getColumnIndex(ConstFirebase.USER_ID)), res.getString(res.getColumnIndex(ConstFirebase.USER_NAME)),
                res.getString(res.getColumnIndex(ConstFirebase.USER_BIO)), res.getString(res.getColumnIndex(ConstFirebase.IMAGE_URL)), res.getString(res.getColumnIndex(ConstFirebase.USER_TYPE)),
                res.getString(res.getColumnIndex(ConstFirebase.CITY)), res.getString(res.getColumnIndex("expectations_from_us")),
                res.getString(res.getColumnIndex("experiences")), res.getString(res.getColumnIndex("gender")), res.getString(res.getColumnIndex("number")),
                res.getString(res.getColumnIndex("offer_to_community")), res.getString(res.getColumnIndex("speaker_experience")), res.getString(res.getColumnIndex("email")),
                res.getString(res.getColumnIndex("weblink")), res.getString(res.getColumnIndex("working")), res.getString(res.getColumnIndex("last_name")),
                res.getString(res.getColumnIndex("company")), res.getString(res.getColumnIndex("country")), res.getString(res.getColumnIndex("referred_by"))
        );
        return user;
    }

    public String getSqliteUser_data(String COL_KEY) {
        Cursor res = getAllData();
        return res.getString(res.getColumnIndex(COL_KEY));
    }

    public boolean insertData(HashMap<String, String> userItems) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, String> m : userItems.entrySet()) {
            contentValues.put(m.getKey(), m.getValue());
        }
        long result = db.insert(TABLE_NAME, null, contentValues);
        Log.d("TESTINGUSERDB", "----------------- result long " + result);
        //  db.close();
        if (result == -1) return false;
        return true;
    }

    public boolean updateData(String[] key, String[] value, String Id) {

        SQLiteDatabase myDB = this.getWritableDatabase();
        String strFilter = ConstFirebase.USER_ID + "=" + Id;
        ContentValues args = new ContentValues();
        for (int i = 0; i < key.length; i++) {
            args.put(key[i], value[i]);
        }
        try {
            myDB.update(TABLE_NAME, args, strFilter, null);
            myDB.close();
            return true;

        } catch (SQLiteException e) {
            myDB.close();
            return false;
        }
    }

}