package com.example.instagramclone.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing fragments for tabs
 */
public class SectionPagesAdapter extends FragmentPagerAdapter {
    private static final String TAG = "SectionPagesAdapter";

    private final List<Fragment> mFragmentList = new ArrayList<>();

    public SectionPagesAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }


    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment( Fragment fragment){
        mFragmentList.add(fragment);
    }
}
