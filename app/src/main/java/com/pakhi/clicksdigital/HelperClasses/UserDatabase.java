package com.pakhi.clicksdigital.HelperClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pakhi.clicksdigital.Utils.Const;

import java.util.HashMap;
import java.util.Map;

public class UserDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="user.db";
    public static final String TABLE_NAME   ="current_user";

    public UserDatabase(@Nullable Context context) {
        super(context, TABLE_NAME, null, 1);
        SQLiteDatabase db=this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME
                + " (" + Const.USER_ID + " TEXT PRIMARY KEY,"
                + Const.USER_NAME + " TEXT,"
                + Const.USER_BIO + " TEXT,"
                + Const.IMAGE_URL + " TEXT,"
                + Const.USER_TYPE + " TEXT,"
                + Const.CITY + " TEXT,"
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
                + "company" + " TEXT"
                +
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

    public Cursor getAllData() {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor res=db.rawQuery("select * from " + TABLE_NAME, null);
        //+" where "+Const.USER_ID+" = "+currentUserID
        //db.close();
        return res;
    }

    public boolean insertData(HashMap<String, String> userItems) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        for (Map.Entry<String, String> m : userItems.entrySet()) {
            contentValues.put(m.getKey(), m.getValue());
        }
        long result=db.insert(TABLE_NAME, null, contentValues);
        Log.d("TESTINGUSERDB", "----------------- result long" + result);
        //  db.close();
        if (result == -1) return false;
        return true;
    }

    public boolean updateData(String[] key, String[] value, String Id) {

        SQLiteDatabase myDB=this.getWritableDatabase();
        String strFilter=Const.USER_ID + "=" + Id;
        ContentValues args=new ContentValues();
        for (int i=0; i < key.length; i++) {
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
