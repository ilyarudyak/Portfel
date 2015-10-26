package com.ilyarudyak.android.portfel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.analytics.PortfelApplication;
import com.ilyarudyak.android.portfel.service.MarketUpdateService;
import com.ilyarudyak.android.portfel.settings.SettingsActivity;
import com.ilyarudyak.android.portfel.ui.adapter.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int INDEX_OF_TAB_WITH_FAB = 0;
    private static final String DIALOG_FRAGMENT_TAG = "dialog_add_stock";
    private ViewPager mViewPager;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFab = (FloatingActionButton) findViewById(R.id.main_fab_add);
        setFab();

        setToolbar();
        setViewPager();
        setTabLayout();

        // set up default values
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // set up alarm for update service
        MarketUpdateService.setServiceAlarm(this);

        // start tracking for google analytics
        ((PortfelApplication) getApplication()).startTracking();
    }

    // helper methods
    private void setTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }
    private void setViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new ViewPagerAdapter(this, getFragmentManager()));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case INDEX_OF_TAB_WITH_FAB:
                        mFab.show();
                        break;

                    default:
                        mFab.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
    }
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }
    private void setFab() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddStockDialogFragment().show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        });
    }

    // --------------------- options menu ---------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}















