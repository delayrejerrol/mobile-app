package com.jerrol.app.activitytracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerrol.app.activitytracker.adapter.RecyclerViewCursorAdapter;
import com.jerrol.app.activitytracker.database.Database;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskActivity extends AppCompatActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container) RecyclerView mRecyclerViewTaskContainer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.edittext_task_list_name) EditText mEditTextTaskListName;

    String projectId;

    TaskListAdapter taskListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerViewTaskContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Bundle bundle = getIntent().getExtras();
        projectId = bundle.getString(Database._ID);

        Database database = new Database(this);
        taskListAdapter = new TaskListAdapter(database.getTaskList(projectId));
        mRecyclerViewTaskContainer.setAdapter(taskListAdapter);
        database.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_task) {

        }
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_save_task_list)
    public void saveTaskList(View view) {
        Database db = new Database(this);
        if(mEditTextTaskListName.getText().toString().equals("")) {
            Toast.makeText(this, "Please enter task name...", Toast.LENGTH_LONG).show();
            return;
        }
        if(db.insertTaskList(projectId, mEditTextTaskListName.getText().toString()) > 0 ) {
            taskListAdapter.swapCursor(db.getTaskList(projectId));
            mRecyclerViewTaskContainer.smoothScrollToPosition(taskListAdapter.getItemCount() - 1);
            mEditTextTaskListName.setText("");
        }
        db.close();
    }

    class TaskListAdapter extends RecyclerViewCursorAdapter<TaskListAdapter.ViewHolder> {

        TaskListAdapter(Cursor cursor) {
            super(cursor);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_list, parent, false);

            return new ViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(ViewHolder holder, Cursor cursor) {
            holder.taskId = cursor.getString(cursor.getColumnIndex(Database._ID));
            holder.mTextViewTaskName.setText(cursor.getString(cursor.getColumnIndex(Database.TBL_TASK_LIST.NAME)));
            Database db = new Database(getApplicationContext());
            holder.taskListItemAdapter = new TaskListItemAdapter(db.getTaskListItem(holder.taskId));
            holder.mRecyclerViewTaskItemContainer.setAdapter(holder.taskListItemAdapter);
            db.close();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            String taskId;
            @BindView(R.id.tv_task_name) TextView mTextViewTaskName;
            @BindView(R.id.iv_show_more) ImageView mImageViewShowMore;
            @BindView(R.id.rv_task_item_container) RecyclerView mRecyclerViewTaskItemContainer;
            @BindView(R.id.btn_add_card) Button mButtonAddCard;
            @BindView(R.id.cv_task_name) CardView mCardViewTaskNameContainer;
            @BindView(R.id.et_task_name) EditText mEditTextTaskName;
            @BindView(R.id.btn_add_new_task) Button mButtonAddNewTask;
            @BindView(R.id.btn_close) Button mButtonClose;

            TaskListItemAdapter taskListItemAdapter;

            ViewHolder (View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);

                mRecyclerViewTaskItemContainer.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            }

            @Override
            public void onClick(View v) {
                Log.i("TaskListAdapter", "onClick");
            }

            @OnClick({R.id.btn_add_card, R.id.btn_add_new_task, R.id.btn_close, R.id.cv_task_name})
            public void buttonClicked(View view) {
                if(view == mButtonAddCard) {
                    showViewsOnAdd();
                } else if (view == mButtonAddNewTask) {
                    Database db = new Database(getApplicationContext());
                    if(db.insertTaskListItem(taskId, mEditTextTaskName.getText().toString()) > 0 ) {
                        taskListItemAdapter.swapCursor(db.getTaskListItem(taskId));
                    }
                    db.close();
                    hideViewsOnAdd();
                } else if (view == mButtonClose) {
                    hideViewsOnAdd();
                } else if (view == mCardViewTaskNameContainer) {
                    mEditTextTaskName.requestFocus();
                }
            }

            private void hideViewsOnAdd() {
                mEditTextTaskName.setText("");
                mCardViewTaskNameContainer.setVisibility(View.GONE);
                mButtonAddNewTask.setVisibility(View.GONE);
                mButtonClose.setVisibility(View.GONE);
                mButtonAddCard.setVisibility(View.VISIBLE);
            }

            private void showViewsOnAdd() {
                mEditTextTaskName.setText("");
                mCardViewTaskNameContainer.setVisibility(View.VISIBLE);
                mButtonAddNewTask.setVisibility(View.VISIBLE);
                mButtonClose.setVisibility(View.VISIBLE);
                mButtonAddCard.setVisibility(View.GONE);
            }
        }
    }

    class TaskListItemAdapter extends RecyclerViewCursorAdapter<TaskListItemAdapter.ViewHolder> {

        TaskListItemAdapter(Cursor cursor) {
            super(cursor);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(ViewHolder holder, Cursor cursor) {
            holder.mTextViewTaskItemName.setText(cursor.getString(cursor.getColumnIndex(Database.TBL_TASK_ITEM.NAME)));
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_task_item_name) TextView mTextViewTaskItemName;
            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
