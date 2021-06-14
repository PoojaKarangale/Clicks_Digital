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
import com.pakhi.clicksdigital.Utils.ConstFirebase;

import java.util.HashMap;
import java.util.Map;

public class NotificationCountDatabase extends SQLiteOpenHelper {

    public static final String dbName = "user.db";
    public static final String notificationTable = "notificationTable";

    public NotificationCountDatabase(@Nullable Context context) {
        super(context, dbName, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists "+notificationTable
        +" ("+ Const.grpOrUserID+" TEXT PRIMARY KEY, "
        + Const.number+" TEXT, "
        + Const.mute+ " TEXT"+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + notificationTable);
        onCreate(db);
    }
    public boolean checkTableIfExists(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from "+notificationTable, null);
        if(res.getCount()>0){
            return true;
        }return false;
    }

    public void insertData(HashMap<String, String> key) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Const.grpOrUserID, key.get(Const.grpOrUserID));
        values.put(Const.number, key.get(Const.number));
        values.put(Const.mute, key.get(Const.mute));

        db.insert(notificationTable, null, values);
        Log.i("Inserted ---", key.get(Const.grpOrUserID));
        /*ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, String> m : key.entrySet()) {
            contentValues.put(m.getKey(), m.getValue());
        }
        db.insert(notificationTable, null, contentValues);*/
        //db.execSQL("insert into "+notificationTable+" values ("+key.get(Const.grpOrUserID)+","+key.get(Const.number)+","+key.get(Const.mute)+")");
    }

    public void updateDataNotification(HashMap<String, String> hmap) {

        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Const.grpOrUserID, hmap.get(Const.grpOrUserID));
        values.put(Const.number,hmap.get(Const.number));

        values.put(Const.mute,hmap.get(Const.mute));

        myDB.update(notificationTable, values, Const.grpOrUserID + "= ? ", new String[]{hmap.get(Const.grpOrUserID)});


    }
    public Cursor getAllData(String user){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projections = {Const.grpOrUserID, Const.number,Const.mute};
        String selection = Const.grpOrUserID+" LIKE ?";
        String[] seletion_args={user};
        Cursor cursor=db.query(notificationTable, projections,selection,seletion_args,null,null,null);
        cursor.moveToFirst();
        return cursor;
    }
    public String getSqliteUser_data(String COL_KEY, String user) {
        Log.i("Entered getSqlite","enter");
        Cursor res = getAllData(user);
        //Log.i("number : ", res.getString(1));
        String outPut  ="";
        Log.i("rows---", String.valueOf(res.getCount()));
        Log.i("col---", String.valueOf(res.getColumnCount()));
        if (res.getCount() > 0){
            Log.i("count greater ","than 0");
            try {
                Log.i("output before--", outPut);
                outPut= res.getString(res.getColumnIndex(COL_KEY));
                Log.i("output after--", outPut);
            }catch (Exception e){

            }
        }
            return outPut;
    }
}
