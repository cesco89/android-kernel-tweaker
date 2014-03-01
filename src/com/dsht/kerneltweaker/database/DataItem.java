package com.dsht.kerneltweaker.database;

public class DataItem {

	//private variables
    int _id;
    String _name;
    String _filename;
    String _value;
    String _category;
     
    // Empty constructor
    public DataItem(){
         
    }
    // constructor
    public DataItem(int id, String name, String _value, String filename, String category){
        this._id = id;
        this._name = name;
        this._value = _value;
        this._filename = filename;
        this._category = category;
    }
     
    // constructor
    public DataItem(String name, String _value, String filename, String category){
        this._name = name;
        this._value = _value;
        this._filename = filename;
        this._category = category;
    }
    // getting ID
    public int getID(){
        return this._id;
    }
     
    // setting id
    public void setID(int id){
        this._id = id;
    }
     
    // getting name
    public String getName(){
        return this._name;
    }
     
    // setting name
    public void setName(String name){
        this._name = name;
    }
     
    // getting value
    public String getValue(){
        return this._value;
    }
     
    // setting value
    public void setValue(String value){
        this._value = value;
    }

    // getting value
    public String getFileName(){
        return this._filename;
    }
     
    // setting value
    public void setFileName(String filename){
        this._filename = filename;
    }
    
 // getting value
    public String getCategory(){
        return this._category;
    }
     
    // setting value
    public void setCategory(String category){
        this._category = category;
    }

}
