package com.ilyarudyak.android.portfel.ui;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.service.MarketUpdateService;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MainActivity extends AppCompatActivity {

    private static final int INDEX_OF_TAB_WITH_FAB = 0;
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

        MarketUpdateService.setServiceAlarm(this);
    }

    // helper methods
    private void setTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }
    private void setViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new ViewPagerAdapter(this, getFragmentManager(), mFab));

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
                new AddSymbolTask().execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ------------------- AsyncTask class -----------------

    /**
     * Add symbol to the list that is shown on the market screen.
     * */
    private class AddSymbolTask extends AsyncTask<Void, Void, Void> {

        private final String TAG = AddSymbolTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... ignore) {
            PrefUtils.putSymbol(MainActivity.this, PrefUtils.STOCKS, "TWTR");

            Stock twtr = null;
            Stock twt;
            try {
                twtr = YahooFinance.get("TWTR");
                twt = YahooFinance.get("TWT");
                Log.d(TAG, "name=" + twt.getName() + "; price=" + twt.getQuote().getPrice());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "get problems with twt");
            }

            Uri uri = PortfolioContract.StockTable.CONTENT_URI;
            ContentValues cv = DataUtils.buildContentValues(twtr);
            MainActivity.this.getContentResolver().insert(uri, cv);


            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            Toast.makeText(MainActivity.this, "completed", Toast.LENGTH_LONG).show();
        }
    }


}















