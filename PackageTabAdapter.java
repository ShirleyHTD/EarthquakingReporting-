package com.example.earthquake_service_alarm;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.tabs.TabLayout;

public class PackageTabAdapter extends FragmentStatePagerAdapter {
    TabLayout tabLayout;

    public PackageTabAdapter(FragmentManager fm, TabLayout _tabLayout) {
        super(fm);
        this.tabLayout = _tabLayout;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0)
        {
            fragment = new EarthquakeListFragment();
        }
        else if (position == 1)
        {
            fragment = new EarthquakeMapFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
        {
            title = "List";
        }
        else if (position == 1)
        {
            title = "Map";
        }
        return title;
    }

}
