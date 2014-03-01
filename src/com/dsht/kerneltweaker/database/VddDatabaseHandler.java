package com.dsht.kerneltweaker.database;

import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class VddDatabaseHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "kernelTweakerVdd";
 
    // Contacts table name
    private static final String TABLE_CONTACTS = "VddBootValues";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_VALUE = "value";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_CAT = "category";
    
 
    public VddDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_VALUE + " TEXT," + KEY_FILENAME + " TEXT," + KEY_CAT + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new contact
    public void addItem(DataItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName()); // Contact Name
        values.put(KEY_VALUE, item.getValue()); // Contact Phone
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_CAT, item.getCategory());
 
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    DataItem getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                KEY_NAME, KEY_VALUE, KEY_FILENAME, KEY_CAT }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        DataItem item = new DataItem(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        // return contact
        return item;
    }
     
    // Getting All Contacts
    public List<DataItem> getAllItems() {
        List<DataItem> contactList = new ArrayList<DataItem>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataItem item = new DataItem();
                item.setID(Integer.parseInt(cursor.getString(0)));
                item.setName(cursor.getString(1));
                item.setValue(cursor.getString(2));
                item.setFileName(cursor.getString(3));
                item.setCategory(cursor.getString(4));
                // Adding contact to list
                contactList.add(item);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return contactList;
    }
 
    // Updating single contact
    public int updateItem(DataItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_VALUE, item.getValue());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_CAT, item.getCategory());
 
        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(item.getID()) });
    }
 
    // Deleting single item by ID
    public void deleteItem(DataItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(item.getID()) });
        db.close();
    }
    
    public void deleteItemByName(String name) 
    {
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_NAME + " = ?" , new String[] {name});
        db.close();
    }
    
    public void deleteItemById(int ID) 
    {
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?" , new String[] {String.valueOf(ID)});
        db.close();
    }
 
 
    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
 
        // return count
        return count;
    }
    
    public void deleteAllItems() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_CONTACTS, null, null);
    	db.close();
    }
 
}
