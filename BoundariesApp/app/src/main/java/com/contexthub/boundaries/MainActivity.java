package com.contexthub.boundaries;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.astuetz.PagerSlidingTabStrip;
import com.contexthub.boundaries.fragments.AboutFragment;
import com.contexthub.boundaries.fragments.GeofenceListFragment;
import com.contexthub.boundaries.fragments.GeofencesMapFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends FragmentActivity {

    @InjectView(R.id.tab_strip) PagerSlidingTabStrip tabStrip;
    @InjectView(R.id.view_pager) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setProgressBarIndeterminate(true);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        tabStrip.setViewPager(viewPager);
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private final int[] TITLES = new int[]{R.string.map, R.string.list, R.string.about};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(TITLES[position]);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new GeofencesMapFragment();
                case 1:
                    return new GeofenceListFragment();
                case 2:
                    return new AboutFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }
    }
}
