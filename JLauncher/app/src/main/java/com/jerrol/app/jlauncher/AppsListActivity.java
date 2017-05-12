package com.jerrol.app.jlauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppsListActivity extends AppCompatActivity {

    private PackageManager manager;
    private List<AppDetail> apps;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);

        loadApps();
        loadListView();
        addClickListener();
    }

    private void loadApps() {
        manager = getPackageManager();
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            apps.add(app);
        }

        list = (RecyclerView) findViewById(R.id.apps_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager.setAutoMeasureEnabled(false);

        list.setLayoutManager(gridLayoutManager);
        RecyclerAdapter adapter = new RecyclerAdapter(apps);
        list.setAdapter(adapter);
    }

    private void loadListView() {
        //list = (RecyclerView) findViewById(R.id.apps_list);

        //RecyclerAdapter adapter = new RecyclerAdapter();
        /*ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this, R.layout.list_item, apps) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageDrawable(apps.get(position).icon);

                TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
                appLabel.setText(apps.get(position).label);

                TextView appName = (TextView) convertView.findViewById(R.id.item_app_name);
                appName.setText(apps.get(position).name);

                return convertView;
            }
        };*/

        //list.setAdapter(adapter);
    }

    private void addClickListener() {
        /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = manager.getLaunchIntentForPackage(apps.get(position).name.toString());
                AppsListActivity.this.startActivity(i);
            }
        });*/
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private List<AppDetail> list;
        public RecyclerAdapter(List<AppDetail> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.appIcon.setImageDrawable(apps.get(position).icon);
            holder.appLabel.setText(apps.get(position).label);
            //holder.appName.setText(apps.get(position).name);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            ImageView appIcon;
            TextView appLabel;
            TextView appName;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                appIcon = (ImageView) itemView.findViewById(R.id.item_app_icon);
                appLabel = (TextView) itemView.findViewById(R.id.item_app_label);
                //appName = (TextView) itemView.findViewById(R.id.item_app_name);
            }

            @Override
            public void onClick(View v) {
                Intent i = manager.getLaunchIntentForPackage(apps.get(getAdapterPosition()).name.toString());
                startActivity(i);
            }
        }
    }
}
