package com.example.fateme.topic_05;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_ADD_NEW_EXPENSE = 777;

    private static final String PREFERENCES_NAME = "com.example.fateme.topic_05" ;
    private static final String KEY_AUTO_UPDATE = "checked";

    private Button mButtonAdd;
    private Button mButtonUpdate;
    private CheckBox mAutoUpdateCheck;

    private SharedPreferences mSharedPreferences;

    private OrderItemsFragment[] mCurrentTab = new OrderItemsFragment[3];
    private OrderItemsFragment mPriceTab;
    private OrderItemsFragment mIdTab;
    private OrderItemsFragment mTimeTab;

    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonAdd = (Button) findViewById(R.id.button_add);
        mButtonUpdate = (Button) findViewById(R.id.button_update);
        mAutoUpdateCheck = (CheckBox) findViewById(R.id.check_autoupdate);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        mPager = (ViewPager) findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_price_order));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_id_order));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_time_order));

        mPriceTab= OrderItemsFragment.newInstance("COST DESC");;
        mIdTab = OrderItemsFragment.newInstance("ID ASC");
        mTimeTab = OrderItemsFragment.newInstance("TIME DESC");;

        mCurrentTab[0] = mPriceTab;
        mCurrentTab[1] = mIdTab;
        mCurrentTab[2] = mTimeTab;

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());

        mPager.setAdapter(pagerAdapter);

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        mButtonAdd.setOnClickListener(mOnAddButtonClickListener);
        mButtonUpdate.setOnClickListener(mOnUpdateButtonClickListener);

        mAutoUpdateCheck.setOnClickListener(mOnAutoUpdateCheckListener);

        mSharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        mAutoUpdateCheck.setChecked(isAutoUpdate());

        Account account = new Account("FZK", "topic05.expense");

        AccountManager am = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        am.addAccountExplicitly(account, null, null);

        ContentResolver.requestSync(account, "com.example.fateme.topic_05.provider", new Bundle());
        ContentResolver.addPeriodicSync(account, "com.example.fateme.topic_05.provider", new Bundle(), 1000L);

        Log.d(TAG, "sync statements");
    }

    @Override
    public void onResume(){
        super.onResume();

        if (isAutoUpdate()) {
            for (int i=0; i<3 ; i++) {
                ((OrderItemsFragment) mCurrentTab[i]).onUpdateStarted();
            }

            Network.getServerExpenseData(MainActivity.this, mGetExpenseCallback);
        }
        else {
            for (int i = 0; i < 3; i++) {
                ((OrderItemsFragment) mCurrentTab[i]).getExpenseData(MainActivity.this);
                Log.d(TAG, "get expense onResume");
            }
        }
    }

    private boolean isAutoUpdate() {
        return mSharedPreferences.getBoolean(KEY_AUTO_UPDATE,false);
    }

    private View.OnClickListener mOnAddButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = AddNewExpenseActivity.getIntent(MainActivity.this);
            startActivityForResult(intent, REQUEST_CODE_ADD_NEW_EXPENSE);
        }

    };

    private View.OnClickListener mOnUpdateButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            for (int i=0;i<3;i++) {
                ((OrderItemsFragment) mCurrentTab[i]).onUpdateStarted();
            }
            mButtonUpdate.setEnabled(false);

            Network.getServerExpenseData(MainActivity.this, mGetExpenseCallback);
        }

    };

    private View.OnClickListener mOnAutoUpdateCheckListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mAutoUpdateCheck.isChecked() == true) {
                AlertDialog.Builder mAutoUpdateDialog = new AlertDialog.Builder(MainActivity.this);

                mAutoUpdateDialog.setCancelable(false)
                        .setTitle(R.string.autoupdate_dialog_title)
                        .setMessage(R.string.autoupdate_dialog_message)
                        .setPositiveButton(R.string.autoupdate_dialog_positive_button, mOnDialogListener)
                        .setNegativeButton(R.string.autoupdate_dialog_negative_button, mOnDialogListener)
                        .setIcon(R.drawable.dialogicon)
                        .show();
            }
            else {
                mSharedPreferences.edit().putBoolean(KEY_AUTO_UPDATE,false).commit();
            }
        }

    };

    private DialogInterface.OnClickListener mOnDialogListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mAutoUpdateCheck.setChecked(true);
                    mSharedPreferences.edit().putBoolean(KEY_AUTO_UPDATE, true).commit();
                    break;
                case DialogInterface.BUTTON_NEGATIVE :
                    mAutoUpdateCheck.setChecked(false);
                    mSharedPreferences.edit().putBoolean(KEY_AUTO_UPDATE,false).commit();
                    break;
            }
        }

    };

    private Network.GetExpenseCallback mGetExpenseCallback = new Network.GetExpenseCallback() {

        @Override
        public void onUpdateExpenseResult(Boolean isSuccessful, Exception exception) {
            mButtonUpdate.setEnabled(true);
            for (int i=0; i<3; i++) {
                ((OrderItemsFragment) mCurrentTab[i]).onUpdateFinished();
            }
            if (isSuccessful) {
                for (int i = 0; i < 3; i++) {
                    ((OrderItemsFragment) mCurrentTab[i]).getExpenseData(MainActivity.this);
                }
            }
            else {
                Log.d(TAG, "" + exception);
                Toast.makeText(MainActivity.this, R.string.failed_toast_text, Toast.LENGTH_SHORT).show();

                for (int i = 0; i < 3; i++) {
                    ((OrderItemsFragment) mCurrentTab[i]).getExpenseData(MainActivity.this);
                }
            }
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_ADD_NEW_EXPENSE:
                if (resultCode == RESULT_OK) {
                    for (int i = 0; i < 3; i++) {
                        ((OrderItemsFragment) mCurrentTab[i]).getExpenseData(MainActivity.this);
                    }
                }
                break;
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        int mNumberOfTabs;

        public ViewPagerAdapter (FragmentManager fm, int numberOfTabs) {
            super(fm);

            this.mNumberOfTabs = numberOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return mCurrentTab[position];
                case 1:
                    return mCurrentTab[position];
                case 2:
                    return mCurrentTab[position];
                default:
                    return  null;
            }
        }

        @Override
        public int getCount() {
            return mNumberOfTabs;
        }

    }

}