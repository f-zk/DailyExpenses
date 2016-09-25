package com.example.fateme.topic_05;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Expense implements Parcelable{

    private static final String TAG = "Expense";

    protected static final String TABLE_NAME = "expense";

    private static final String ID = "id";
    private static final String SUBJECT = "subject";
    private static final String COST = "cost";
    private static final String SENDER = "sender";
    private static final String TIME = "time";

    private static final String[] COLUMNS = {
            ID,
            SUBJECT,
            COST,
            SENDER,
            TIME};

    protected static final String CREATE_PHRASE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMNS[1] + " TEXT, " +
            COLUMNS[2] + " INTEGER, " +
            COLUMNS[3] + " TEXT, " +
            COLUMNS[4] + " LONG)";

    protected static final String DELETE_PHRASE = "DELETE FROM " + TABLE_NAME ;

    protected Expense(Parcel source) {
        mId = source.readLong();
        mSubject = source.readString();
        mCost = source.readInt();
        mSender = source.readString();
        mTime = source.readLong();
    }

    public static final Creator<Expense> CREATOR = new Creator<Expense>() {

        @Override
        public Expense createFromParcel(Parcel source) {
            return new Expense(source);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mSubject);
        dest.writeInt(mCost);
        dest.writeString(mSender);
        dest.writeLong(mTime);
    }

    protected  interface ExpensesCallback {
        void onExpensesReady(Cursor expensesCursor);
    }

    protected  interface ExpenseCreatedCallback {
        void onExpenseCreated(Expense expense);
    }

    protected static void getExpenses(final Context context, final ExpensesCallback callback, final String orderItem) {
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground (Void...params){
                    SQLiteDatabase mDataBase = DataBaseOpenHelper.getInstance(context).getReadableDatabase();

                    Cursor c = mDataBase.query(TABLE_NAME, COLUMNS, null, null, null, null, orderItem);
                    return c;
            }

            @Override
            protected void onPostExecute (Cursor cursor){
                //Log.d(TAG, "cursor: " + cursor.getCount());
                callback.onExpensesReady(cursor);
            }
        }.execute((Void[]) null);
    }

    protected static void insertExpense(final String subject, final int cost, final String sender, final Long time, final Context context,
                                     final ExpenseCreatedCallback callback) {
        new AsyncTask<Void, Void, Expense>() {

            @Override
            protected Expense doInBackground(Void... params) {
                SQLiteDatabase mDataBase = DataBaseOpenHelper.getInstance(context).getWritableDatabase();

                ContentValues contentValues = new ContentValues(4);
                contentValues.put(SUBJECT, subject);
                contentValues.put(COST, cost);
                contentValues.put(SENDER, sender);
                contentValues.put(TIME, time);

                long id = mDataBase.insert(TABLE_NAME, null, contentValues);
                return new Expense(id, subject, cost, sender, time);
            }

            @Override
            protected void onPostExecute (Expense expense){
                callback.onExpenseCreated(expense);
            }
        }.execute((Void[])null);
    }

    protected static void insertExpense(final String subject, final int cost, final String sender,final long time, final Context context) {
        SQLiteDatabase mDataBase = DataBaseOpenHelper.getInstance(context).getWritableDatabase();

        ContentValues contentValues = new ContentValues(4);
        contentValues.put(SUBJECT, subject);
        contentValues.put(COST, cost);
        contentValues.put(SENDER, sender);
        contentValues.put(TIME, time);

        mDataBase.insert(TABLE_NAME, null, contentValues);
    }

    protected static void deleteExpenses(final Context context){
        SQLiteDatabase mDataBase = DataBaseOpenHelper.getInstance(context).getWritableDatabase();
        mDataBase.execSQL(DELETE_PHRASE);
    }

    private long mId;
    private String mSubject;
    private int mCost;
    private String mSender;
    private long mTime;

    protected Expense(Cursor c) {
        mId = c.getInt(0);
        mSubject = c.getString(1);
        mCost = c.getInt(2);
        mSender = c.getString(3);
        mTime = c.getLong(4);
    }

    private Expense(long id, String subject, int cost, String sender, long time) {
        mId = id;
        mSubject = subject;
        mCost = cost;
        mSender = sender;
        mTime = time;
    }

    protected Expense(String subject, int cost, String sender, long time) {
        mSubject = subject;
        mCost = cost;
        mSender = sender;
        mTime = time;
    }

    protected long getId() {
        return mId;
    }

    protected String getSubject() {
        return mSubject;
    }

    protected int getCost() {
        return mCost;
    }

    protected String getSender(){
        return mSender;
    }

    protected long getTime() {
        return mTime;
    }


}