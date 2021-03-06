/***
 * Copyright (c) 2012-14 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * <p/>
 * From _The Busy Coder's Guide to Android Development_
 * https://commonsware.com/Android
 */

package com.ilyarudyak.android.portfel.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.ui.MarketFragment;
import com.ilyarudyak.android.portfel.ui.NewsFragment;
import com.ilyarudyak.android.portfel.ui.PortfolioFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public static final int NUMBER_OF_FRAGMENTS = 3;

    private Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public int getCount() {
        return NUMBER_OF_FRAGMENTS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MarketFragment.newInstance();
            case 1:
                return NewsFragment.newInstance();
            case 2:
                return PortfolioFragment.newInstance();
            default:
                throw new IllegalArgumentException("illegal position");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.tab_title_market);
            case 1:
                return mContext.getResources().getString(R.string.tab_title_news);
            case 2:
                return mContext.getResources().getString(R.string.tab_title_portfolio);
            default:
                throw new IllegalArgumentException("illegal position");
        }
    }
}