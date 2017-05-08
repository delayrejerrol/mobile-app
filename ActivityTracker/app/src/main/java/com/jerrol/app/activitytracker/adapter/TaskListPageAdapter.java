package com.jerrol.app.activitytracker.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jerrol on 4/18/2017.
 */

public class TaskListPageAdapter extends FragmentPagerAdapter {

    public static int mPosition = 0;

    private List<Fragment> mListFragment;
    private ArrayList<String> mTitle;
    private Context mContext;

    public TaskListPageAdapter(Context context, FragmentManager fragmentManager, List<Fragment> fragments, ArrayList<String> title) {
        super(fragmentManager);
        mListFragment = fragments;
        mTitle = title;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return mListFragment.get(position);
    }

    @Override
    public int getCount() {
        return mListFragment.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        setPosition(position);

        return mTitle.get(position);
    }

    public static int getPosition() {
        return mPosition;
    }

    public void add(Class<Fragment> fragmentClass, String title, Bundle b) {
        mListFragment.add(Fragment.instantiate(mContext, fragmentClass.getName(), b));
        mTitle.add(title);
    }

    public static void setPosition(int pos) {
        TaskListPageAdapter.mPosition = pos;
    }
}
