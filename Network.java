package com.example.fateme.topic_05;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.text.BoringLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Network {

    private static final String TAG = "Network";

    private static final String SERVER_URL = "http://yashoid.com:8080/";

    private static final String SEND_EXPENSE_URL = SERVER_URL + "add";
    private static final String GET_EXPENSES_URL = SERVER_URL + "getExpenses";

    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";

    private static final String JSON_KEY_SENDER = "sender";
    private static final String JSON_KEY_TITLE = "title";
    private static final String JSON_KEY_COST = "price";
    private static final String JSON_KEY_TIME = "date";
    private static final String JSON_KEY_CATEGORY = "category";
    private static final String JSON_KEY_GROUP = "group";

    protected interface AddExpenseCallback{
        void onSendExpenseResult(Boolean isSuccessful, Exception exception);
    }
    public interface GetExpenseCallback{
        void onUpdateExpenseResult(Boolean isSuccessful, Exception exception);
    }


    protected static void postExpense(final String subject, final int cost, final String sender, final Long time, final Context context,
                                      final AddExpenseCallback callback){
        new AsyncTask<Void,Void,Exception>(){

            private Exception mException;

            @Override
            protected Exception doInBackground(Void... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(SEND_EXPENSE_URL);

                    JSONObject jObject = new JSONObject();
                    jObject.put(JSON_KEY_SENDER,sender);
                    jObject.put(JSON_KEY_TITLE, subject);
                    jObject.put(JSON_KEY_COST, cost);
                    jObject.put(JSON_KEY_TIME, time);
                    jObject.put(JSON_KEY_CATEGORY, 0);
                    jObject.put(JSON_KEY_GROUP, 0);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);

                    connection.setRequestMethod(METHOD_POST);
                    connection.setRequestProperty("Content-Type", "application/json;  charset=utf-8");

                    OutputStream os = connection.getOutputStream();
                    os.write(jObject.toString().getBytes("utf-8"));
                    os.flush();

                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "responseCode: " + responseCode);

                    if(responseCode <200 || responseCode>=300) {
                        mException = new Exception("Wrong response code: " + responseCode);
                    }
                    connection.disconnect();

                } catch (MalformedURLException e) {
                    mException = e;
                } catch (IOException e) {
                    mException = e;
                } catch (JSONException e) {
                    mException = e;
                    //why exception isn't thrown??
                    Log.i(TAG, "json exception: " + mException);
                }
                finally {
                    if (connection != null) {
                        try {
                            connection.disconnect();
                        } catch (Throwable t) { }
                    }
                }
                return mException;
            }
            @Override
            protected void onPostExecute (Exception exception){
                Boolean isSuccessful;

                if (exception != null){
                    isSuccessful = false;
                    Log.d(TAG, "" + exception);
                    callback.onSendExpenseResult(isSuccessful, exception);
                }
                else{
                    isSuccessful = true;
                    callback.onSendExpenseResult(isSuccessful, exception);
                }
            }
        }.execute((Void[])null);
    }

    public static void getServerExpenseData(final Context context,final GetExpenseCallback callback){
        new AsyncTask<Void,Void,Exception>(){

            private String mSender;
            private String mSubject;
            private int mCost;
            private Long mTime;

            private Exception mException;

            private ArrayList<Expense> mRow = new ArrayList<>();

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    URL url = new URL(GET_EXPENSES_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod(METHOD_GET);

                    InputStream is = connection.getInputStream();

                    StringBuilder sb = new StringBuilder();

                    byte[] buffer = new byte[512];
                    int len = 0;

                    while (len!=-1) {
                        if(is != null) {
                            len = is.read(buffer);
                        }
                        if (len>0) {
                            sb.append(new String(buffer, 0, len, "UTF-8"));
                        }
                    }
                    String response = sb.toString();

                    JSONArray responseJson = new JSONArray(response);

                    for(int i= 0; i < responseJson.length(); i++){
                        JSONObject record = responseJson.getJSONObject(i);

                        mSender = record.getString(JSON_KEY_SENDER);
                        mSubject = record.getString(JSON_KEY_TITLE);
                        mCost = record.getInt(JSON_KEY_COST);
                        mTime = record.getLong(JSON_KEY_TIME);
                        Log.d(TAG, "data fetched");

                        mRow.add(new Expense(mSubject, mCost, mSender, mTime));
                    }

                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "responseCode: " + responseCode);

                    connection.disconnect();

                    if((responseCode >= 200) && (responseCode < 300 )){
                        Expense.deleteExpenses(context);
                        Log.d(TAG, "Table is deleted!");
                        for(int i = 0; i< mRow.size();i++) {
                            Expense expense = mRow.get(i);
                            mSubject = expense.getSubject();
                            mCost = expense.getCost();
                            mSender = expense.getSender();
                            mTime = expense.getTime();
                            Expense.insertExpense(mSubject, mCost, mSender, mTime, context);
                            Log.d (TAG, "data inserted to database");
                        }
                    }
                    else {
                        mException = new Exception("Wrong response code: " + responseCode);
                    }
                } catch (MalformedURLException e) {
                    mException = e;
                } catch (IOException e) {
                    mException = e;
                } catch (JSONException e) {
                    mException = e;
                }
                return mException;
            }
            @Override
            protected void onPostExecute (Exception exception){
                Boolean isSuccessful;

                Log.d(TAG, "exception: " + exception);

                if (exception != null){
                    isSuccessful = false;
                    callback.onUpdateExpenseResult(isSuccessful, exception);
                }
                else{
                    isSuccessful = true;
                    callback.onUpdateExpenseResult(isSuccessful, exception);
                }
            }
        }.execute((Void[])null);
    }
}
