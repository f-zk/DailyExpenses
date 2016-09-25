package com.example.fateme.topic_05;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class OrderItemsFragment extends Fragment {

    private static final String TAG = "OrderItemsFragment";

    private static final String EXTRA_ORDERITEM = "order";

    private ListView mListExpenses;
    private ProgressBar mProgressUpdate;

    private ArrayList<DataSetObserver> mObservers = new ArrayList<>(2);

    private Cursor mExpenseCursor;

    public static OrderItemsFragment newInstance(String orderItem) {
        OrderItemsFragment fragment = new OrderItemsFragment();

        Bundle args = new Bundle();
        args.putString(EXTRA_ORDERITEM, orderItem);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderitems, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListExpenses = (ListView) view.findViewById(R.id.list_expense);
        mProgressUpdate = (ProgressBar) view.findViewById(R.id.progress_update);

        mListExpenses.setAdapter(mAdapter);

        mListExpenses.setOnItemClickListener(mOnListExpensesItemClickListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mExpenseCursor != null) {
            mExpenseCursor.close();
        }
    }

    private ListAdapter mAdapter = new ListAdapter() {
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mObservers.add(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mObservers.remove(observer);
        }

        @Override
        public int getCount() {
            if (mExpenseCursor != null) {
                return mExpenseCursor.getCount();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            ExpenseDetailItem expenseDetailItem;

            if (convertView == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.item_name, parent, false);

                expenseDetailItem = new ExpenseDetailItem(view);
                view.setTag(expenseDetailItem);
            } else {
                view = convertView;
                expenseDetailItem = (ExpenseDetailItem) view.getTag();
            }

            mExpenseCursor.moveToPosition(position);

            Expense expense = new Expense(mExpenseCursor);

            expenseDetailItem.loadValue(expense);

            return view;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return mExpenseCursor.getCount() == 0;
        }
    };

    private AdapterView.OnItemClickListener mOnListExpensesItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent expenseDetail = ShowExpenseDetailsActivity.getIntent(getContext());

            mExpenseCursor.moveToPosition(position);

            Log.d(TAG, "cursor is: " + mExpenseCursor);

            Expense expense = new Expense(mExpenseCursor);

            Log.d(TAG, "expense is: " + expense);

            expenseDetail.putExtra(ShowExpenseDetailsActivity.KEY_EXTRA_DETAIL, expense);

            Log.d(TAG, "extra: " + expenseDetail.hasExtra(ShowExpenseDetailsActivity.KEY_EXTRA_DETAIL));

            startActivity(expenseDetail);
        }

    };

        protected void onUpdateStarted() {
            if (mListExpenses != null) {
                mListExpenses.setVisibility(View.INVISIBLE);
                mProgressUpdate.setVisibility(View.VISIBLE);
            }
        }

        public void onUpdateFinished() {
            if (mListExpenses != null) {
                mListExpenses.setVisibility(View.VISIBLE);
                mProgressUpdate.setVisibility(View.INVISIBLE);
            }
        }

        public void getExpenseData(Context context) {
            Bundle args = getArguments();

            String orderItem = args.getString(EXTRA_ORDERITEM);

            Expense.getExpenses(context, mExpensesCallback, orderItem);
        }

        private Expense.ExpensesCallback mExpensesCallback = new Expense.ExpensesCallback() {

            public void onExpensesReady(Cursor expensesCursor) {

                mExpenseCursor = expensesCursor;
                for (DataSetObserver observer : mObservers) {
                    observer.onChanged();

                }

            }

        };

        private class ExpenseDetailItem {

            private TextView mTextShowSubject;
            private TextView mTextShowCost;
            private TextView mTextShowSender;
            private TextView mTextShowTime;

            private ExpenseDetailItem(View root) {
                mTextShowSubject = (TextView) root.findViewById(R.id.text_subject);
                mTextShowCost = (TextView) root.findViewById(R.id.text_cost);
                mTextShowSender = (TextView) root.findViewById(R.id.text_sender);
                mTextShowTime = (TextView) root.findViewById(R.id.text_time);
            }

            private void loadValue(Expense expense) {
                String formattedTime = SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(expense.getTime()));

                mTextShowSubject.setText(expense.getSubject());
                mTextShowCost.setText("" + expense.getCost());
                mTextShowSender.setText(expense.getSender());
                mTextShowTime.setText(formattedTime);
            }
        }
    }
