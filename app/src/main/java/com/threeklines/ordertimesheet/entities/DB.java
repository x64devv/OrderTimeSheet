package com.threeklines.ordertimesheet.entities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is class inherits {@link SQLiteOpenHelper} that helps safely interact with our database.
 */
public class DB extends SQLiteOpenHelper {
    private static DB instance;
    private static final String CREATE_ORDERS_TB = "CREATE TABLE orders (order_number varchar ,item_code varchar , " +
            " style_number varchar,description varchar ,quantity integer )";
    private static final String CREATE_ORDER_PROCESSES_TB = "CREATE TABLE order_processes (identifier varchar, station varchar  ,start_time long " +
            " ,end_time long ,process varchar ,subprocess varchar ,personnel varchar ,breaks varchar, sync_state varchar, elapsed_time long)";
    private static final String CREATE_USER_TB = "CREATE TABLE users (id varchar ,username varchar ,password varchar ,role varchar ,orders_started varchar ,orders_finished varchar )";

    private static final String DELETE_ORDERS_TB = "DROP TABLE orders";
    private static final String DELETE_ORDER_PROCESSES_TB = "DROP TABLE order_processes";
    private static final String DELETE_USER_TB = "DROP TABLE users";

    public DB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * This a method that makes sure that at any given time we have one of the Database Helper
     * @param context this is the context of where the method will be called
     * @return returns an instance of the {@link DB} class
     */
    public static synchronized DB getInstance(Context context){
        DB db;
        synchronized (DB.class){
            if (instance == null){
                instance = new DB(context, "dcts.db", null, 1);
            }
            db = instance;
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ORDERS_TB);
        sqLiteDatabase.execSQL(CREATE_ORDER_PROCESSES_TB);
        sqLiteDatabase.execSQL(CREATE_USER_TB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_ORDERS_TB);
        sqLiteDatabase.execSQL(DELETE_ORDER_PROCESSES_TB);
        sqLiteDatabase.execSQL(DELETE_USER_TB);
        onCreate(sqLiteDatabase);
    }

    /**
     * This is a method to add a new user to the database
     * @param user this parameter would be a {@link ContentValues} with the values to be added where the key is the db
     *             and the value being the value to be entered in that column.
     * @return the method returns boolean true if successful and false if otherwise.
     */
    public boolean insertUser(ContentValues user){
        return this.getWritableDatabase().insert("users", null, user) > -1;
    }

    /**
     * This is a method to add a new order process to the database
     * @param process this parameter would be a {@link OrderProcess}  with the values to be added where the key is the db
     *       and the value being the value to be entered in that column.
     * @return the method returns boolean true if successful and false if otherwise.
     */
    public boolean insertProcess(ContentValues process){
        return this.getWritableDatabase().insert("order_processes", null, process) >-1;
    }

    /**
     * This is a method to add a new order process to the database
     * @param order this parameter would be a {@link Order}  with the values to be added where the key is the db
     *      and the value being the value to be entered in that column.
     * @return the method returns boolean true if successful and false if otherwise.
     */
    public boolean insertOrder(ContentValues order){
        return this.getWritableDatabase().insert("orders", null, order) > -1;
    }

    /**
     * This is a method for updating a user's information.
     * @param values this parameter would be a {@link ContentValues} with the values to be added where the key is he db
     *      and the value being the value to be entered in that column.
     * @param id the id is the id of the user you want to update.
     * @return returns true if successful and false otherwise
     */
    public boolean updateUser(ContentValues values, String id){
        return this.getWritableDatabase().update("users", values, "id = ?", new String[]{id}) > -1;
    }

    /**
     * This is a method to update the state of an order's process
     * @param values his parameter would be a {@link ContentValues} with the values to be added where the key is he db
     *      and the value being the value to be entered in that column.
     * @param id this would be the identifier of the order either order number or the style code for some cases
     * @return returns true if successful and false otherwise.
     */
    public boolean updateProcess(ContentValues values, String id){
        return this.getWritableDatabase().update("order_processes", values, "identifier = ?", new String[]{id}) > -1;
    }
    /**
     * This is a method to update order information
     * @param values his parameter would be a {@link ContentValues} with the values to be added where the key is he db
     *      and the value being the value to be entered in that column.
     * @param id this would be the identifier of the order either order number or the style code for some cases
     * @return returns true if successful and false otherwise.
     */
    public boolean updateOrder(ContentValues values, String id){
        return this.getWritableDatabase().update("orders", values, "order_number = ?", new String[]{id}) > -1;
    }

    /**
     * this is a method to that checks if  a user exists and should be allowed to authenticate.
     * @param username this would  be the user's username
     * @param password this would be a password hash
     * @return returns User details if successful and null otherwise.
     */
    @SuppressLint("Range")
    public User userExists(String username, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor users = db.rawQuery("SELECT * FROM users WHERE username LIKE '" + username + "' AND password LIKE '" +password + "'", null);
        if (users.moveToFirst()){

                User user = new User();
                user.setUserid(users.getString(users.getColumnIndex("id")));
                user.setUsername(users.getString(users.getColumnIndex("username")));
                user.setRole(users.getString(users.getColumnIndex("role")));
                users.close();
                return user;

        }
        users.close();
        return null;
    }

    /**
     * This method returns all processes in the db.
     * @return {@link ArrayList<OrderProcess>} , returns and arraylist of order processes.
     */
    @SuppressLint("Range")
    public ArrayList<OrderProcess> getAllProcess(){
        ArrayList<OrderProcess> orderProcesses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM order_processes", null);
        if (result.moveToFirst()){
            do {
                OrderProcess orderProcess = new OrderProcess();
                orderProcess.setIdentifier(result.getString(result.getColumnIndex("identifier")));
                orderProcess.setStation(result.getString(result.getColumnIndex("station")));
                orderProcess.setStartTime(result.getLong(result.getColumnIndex("start_time")));
                orderProcess.setEndTime(result.getLong((result.getColumnIndex("end_time"))));
                orderProcess.setDepartment(result.getString(result.getColumnIndex("process")));
                orderProcess.setDeptProcess(result.getString(result.getColumnIndex("subprocess")));
                orderProcess.setExtraPersonnel(result.getString(result.getColumnIndex("personnel")));
                orderProcess.setBreaks(result.getString(result.getColumnIndex("breaks")));
                orderProcess.setSyncState(result.getString(result.getColumnIndex("sync_state")));
                orderProcess.setElapsedTime(result.getLong(result.getColumnIndex("elapsed_time")));
                orderProcesses.add(orderProcess);
            } while (result.moveToNext());
        }
        result.close();
        return orderProcesses;
    }

    /**
     * This method gets all the orders in the db
     * @return {@link ArrayList<Order>} , returns and arraylist of orders.
     */
    @SuppressLint("Range")
    public ArrayList<Order> getAllOrders(){
        ArrayList<Order> orders = new ArrayList<>();
        Cursor result = this.getReadableDatabase().rawQuery("SELECT * FROM orders", null);
        if (result.moveToFirst()) {
            do {/*
            (varchar order_number, varchar item_code, " +
            "varchar style_number, varchar description, int quantity)";*/
                Order order = new Order();
                order.setOrderNumber(result.getString(result.getColumnIndex("order_number")));
                order.setItemCode(result.getString(result.getColumnIndex("item_code")));
                order.setStyleCode(result.getString(result.getColumnIndex("style_number")));
                order.setDescription(result.getString(result.getColumnIndex("description")));
                order.setQuantity(result.getInt(result.getColumnIndex("quantity")));
                orders.add(order);
            } while (result.moveToNext());
        }
        result.close();
        return orders;
    }

}

















