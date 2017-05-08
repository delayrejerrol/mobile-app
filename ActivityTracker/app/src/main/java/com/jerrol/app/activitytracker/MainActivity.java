package com.jerrol.app.activitytracker;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.jerrol.app.activitytracker.adapter.RecyclerViewCursorAdapter;
import com.jerrol.app.activitytracker.database.Database;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.rv_project_container) RecyclerView rvProjectContainer;
    @BindView(R.id.app_bar) AppBarLayout mAppBarLayout;
    @BindView(R.id.nsv_container) NestedScrollView mNestedScrollViewContainer;
    @BindView(R.id.layout_empty_container) LinearLayout mLayoutEmptyContainer;

    @BindView(R.id.btn_create_new_project) Button mBtnCreateNewProject;

    @BindView(R.id.container_new_project) LinearLayout mContainerNewProject;
    @BindView(R.id.edittext_project_title) EditText mEditTextProjectTitle;
    @BindView(R.id.edittext_project_description) EditText mEditTextProjectDescription;

    private ProjectAdapter projectAdapter;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvProjectContainer.setLayoutManager(new LinearLayoutManager(this));
        Database db = new Database(this);
        Cursor cursor = db.getProjectList();
        if(cursor.getCount() > 0) {
            rvProjectContainer.setVisibility(View.VISIBLE);
            mLayoutEmptyContainer.setVisibility(View.GONE);
        } else {
            mAppBarLayout.setExpanded(false);
        }
        projectAdapter = new ProjectAdapter(db.getProjectList());
        rvProjectContainer.setAdapter(projectAdapter);
        db.close();
    }

    @OnClick(R.id.btn_create_new_project)
    public void createNewProject(View view) {
        view.setVisibility(View.GONE);
        mContainerNewProject.setVisibility(View.VISIBLE);
        mNestedScrollViewContainer.scrollTo(0, mNestedScrollViewContainer.getNestedScrollAxes());
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mDialogView = getLayoutInflater().inflate(R.layout.layout_new_project, null);
        final EditText mEditTextProjectTitle = (EditText) mDialogView.findViewById(R.id.edittext_project_title);
        final EditText mEditTextProjectDescription = (EditText) mDialogView.findViewById(R.id.edittext_project_description);
        builder.setView(mDialogView)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Database db = new Database(MainActivity.this);
            if(db.insertProject(mEditTextProjectTitle.getText().toString(), mEditTextProjectDescription.getText().toString()) > 0) {
                Toast.makeText(MainActivity.this, "New Project has been created", Toast.LENGTH_LONG).show();
                Cursor cursor = db.getProjectList();
                if(cursor.getCount() > 0) {
                    rvProjectContainer.setVisibility(View.VISIBLE);
                    mLayoutEmptyContainer.setVisibility(View.GONE);
                    if(hasAppbarOffset) {
                        mAppBarLayout.removeOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                            appBarLayout.setExpanded(true);
                        });
                    }
                }
                projectAdapter.swapCursor(cursor);
            }
            db.close();
            dialog.dismiss();
        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();*/
    }

    @OnClick(R.id.btn_save_new_project)
    public void saveNewProject(View view) {
        Database db = new Database(MainActivity.this);
        if(db.insertProject(mEditTextProjectTitle.getText().toString(), mEditTextProjectDescription.getText().toString()) > 0) {
            Toast.makeText(MainActivity.this, "New Project has been created", Toast.LENGTH_LONG).show();
            Cursor cursor = db.getProjectList();
            if(cursor.getCount() > 0) {
                rvProjectContainer.setVisibility(View.VISIBLE);
                mLayoutEmptyContainer.setVisibility(View.GONE);
                mAppBarLayout.setExpanded(true);
                mEditTextProjectTitle.setText("");
                mEditTextProjectDescription.setText("");
            }
            projectAdapter.swapCursor(cursor);
            //projectAdapter.notifyItemInserted(cursor.getCount() - 1);
            mContainerNewProject.setVisibility(View.GONE);
            mBtnCreateNewProject.setVisibility(View.VISIBLE);
        }
        db.close();
    }

    @OnClick(R.id.btn_cancel_new_project)
    public void cancelNewProject(View view) {
        mEditTextProjectTitle.setText("");
        mEditTextProjectDescription.setText("");
        mContainerNewProject.setVisibility(View.GONE);
        mBtnCreateNewProject.setVisibility(View.VISIBLE);
    }

    class ProjectAdapter extends RecyclerViewCursorAdapter<ProjectAdapter.ViewHolder> {

        ProjectAdapter(Cursor cursor) {
            super(cursor);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_content_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(ViewHolder holder, Cursor cursor) {
            holder.mProjectId = cursor.getString(cursor.getColumnIndex(Database._ID));
            holder.mTextViewProjectName.setText(cursor.getString(cursor.getColumnIndex(Database.TBL_PROJECT.TITLE)));
            holder.mTextViewProjectDescription.setText(cursor.getString(cursor.getColumnIndex(Database.TBL_PROJECT.DESCRIPTION)));
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            String mProjectId;
            //@BindView(R.id.tv_project_name) TextView mTextViewProjectName;
            @BindView(R.id.tv_project_name)
            TextView mTextViewProjectName;
            @BindView(R.id.tv_project_description) TextView mTextViewProjectDescription;
            @BindView(R.id.iv_show_more)
            ImageView mImageViewShowMore;

            ViewHolder (View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
                mImageViewShowMore.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

                if(v == mImageViewShowMore) {
                    ContextThemeWrapper ctw = new ContextThemeWrapper(getBaseContext(), R.style.CustomPopupTheme);
                    PopupMenu popupMenu = new PopupMenu(ctw, v);
                    MenuInflater menuInflater = popupMenu.getMenuInflater();
                    menuInflater.inflate(R.menu.menu_main, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if(id == R.id.action_delete) {

                        }
                        return false;
                    });
                    popupMenu.show();
                } else {
                    Log.i("MainActivity", "onClick");
                    Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putString(Database._ID, mProjectId);
                    intent.putExtras(bundle);
                    getApplicationContext().startActivity(intent);
                }
            }
        }
    }
}
