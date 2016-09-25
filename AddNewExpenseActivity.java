package com.example.fateme.topic_05;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddNewExpenseActivity extends AppCompatActivity {

    private static final String TAG = "AddNewExpenseActivity";

    protected static Intent getIntent(Context context) {
        Intent intent = new Intent(context, AddNewExpenseActivity.class);
        return intent;
    }

    private static final String PREFERENCES_NAME = "com.example.fateme.topic_05" ;
    private static final String KEY_SENDER = "sender";

    private Button mButtonSave;
    private EditText mEditSubject;
    private EditText mEditCost;
    private EditText mEditSender;

    private SharedPreferences mSharedPreferences;

    private int mCode;

    private String mSubject;
    private int mCost;
    private long mTime;
    private String mSender;

    ProgressDialog postProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewexpense);

        setTitle(R.string.addnewexpense_activity_title);

        mButtonSave = (Button) findViewById(R.id.button_save);
        mEditSubject = (EditText) findViewById(R.id.edit_subject);
        mEditCost = (EditText) findViewById(R.id.edit_cost);
        mEditSender = (EditText) findViewById(R.id.edit_sender);

        mSharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        mEditSender.setText(mSharedPreferences.getString(KEY_SENDER, null));

        mButtonSave.setOnClickListener(mOnSaveButtonClickListener);
    }

    private View.OnClickListener mOnSaveButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            addExpense();
        }

    };

    private void addExpense(){
        mButtonSave.setEnabled(false);
        if(isDataEntered())
        {
            mSubject = mEditSubject.getText().toString().trim();
            mCost = Integer.parseInt(mEditCost.getText().toString().trim());
            mTime = System.currentTimeMillis();
            mSender = mEditSender.getText().toString().trim();
            mSharedPreferences.edit().putString(KEY_SENDER, mSender).commit();

            postProgress = new ProgressDialog(AddNewExpenseActivity.this);
            postProgress.setCancelable(false);
            postProgress.setMessage(getResources().getString(R.string.progress_dialog_message));
            postProgress.show();

            Network.postExpense(mSubject, mCost, mSender, mTime, AddNewExpenseActivity.this, mPostCallback);
            Log.d(TAG,"insert");
        } else{
            mButtonSave.setEnabled(true);
            Toast.makeText(this, R.string.toast_text, Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean isDataEntered() {
        return mEditSubject.getText().toString().trim().length()> 0
                && mEditCost.getText().toString().trim().length()>0
                && mEditSender.getText().toString().trim().length()>0;
    }

    private Expense.ExpenseCreatedCallback mExpenseCreatedCallback = new Expense.ExpenseCreatedCallback() {

        public void onExpenseCreated(Expense expense) {
            postProgress.dismiss();

            setResult(RESULT_OK);
            finish();
        }

    };

    private Network.AddExpenseCallback mPostCallback = new Network.AddExpenseCallback() {

        @Override
        public void onSendExpenseResult(Boolean isSuccessful, Exception exception) {
            mButtonSave.setEnabled(true);

            if(isSuccessful) {
                Expense.insertExpense(mSubject, mCost, mSender, mTime, AddNewExpenseActivity.this, mExpenseCreatedCallback);
            }
            else{
                postProgress.dismiss();

                Toast.makeText(AddNewExpenseActivity.this, R.string.failed_toast_text, Toast.LENGTH_SHORT).show();
            }
        }

    };

}