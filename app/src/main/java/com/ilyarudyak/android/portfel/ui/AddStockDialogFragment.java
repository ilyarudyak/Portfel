/***
 * Copyright (c) 2012 CommonsWare, LLC
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

package com.ilyarudyak.android.portfel.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class AddStockDialogFragment extends DialogFragment implements
        DialogInterface.OnClickListener {
    private View form = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        form = getActivity().getLayoutInflater().inflate(R.layout.fragment_market_dialog_add_stock, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setTitle(R.string.market_dialog_add_stock_title)
                .setView(form)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null).create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        EditText symbolEditText = (EditText) form.findViewById(R.id.market_dialog_add_stock_symbol);
        String[] symbols = {symbolEditText.getText().toString().toUpperCase()};
        new AddSymbolTask(getActivity()).execute(symbols);
    }

    // ------------------- AsyncTask class -----------------

    /**
     * Add symbol to the list that is shown on the market screen.
     * */
    private static class AddSymbolTask extends AsyncTask<String, Void, Integer> {

        private static final String TAG = AddSymbolTask.class.getSimpleName();
        private static final String NA = "N/A";

        private static final String TOAST_SUCCESS = "Stock is added to your watchlist";
        private static final String TOAST_ALREADY_IN_DB = "This stock is already in your watchlist";
        private static final String TOAST_NO_SUCH_SYMBOL = "No stock found with this symbol";

        private static final int CODE_SUCCESS = 0;
        private static final int CODE_ALREADY_IN_DB = 1;
        private static final int CODE_NO_SUCH_SYMBOL = 2;

        private Context context;

        public AddSymbolTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(String... symbols) {
            String symbol = symbols[0];
            PrefUtils.putSymbol(context, PrefUtils.STOCKS, symbol);

            Stock stock = null;
            try {
                stock = YahooFinance.get(symbol);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // if no stock found yahoo returns file with N/A for all fields
            if (stock == null || stock.getName().equals(NA)) {
                return CODE_NO_SUCH_SYMBOL;
            }

            // check if this stock is already in DB
            if (DataUtils.isAlreadyInWatchlist(context, stock)) {
                return CODE_ALREADY_IN_DB;
            }

            Uri uri = PortfolioContract.StockTable.CONTENT_URI;
            ContentValues cv = DataUtils.buildContentValues(stock);
            context.getContentResolver().insert(uri, cv);

            return CODE_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case CODE_SUCCESS:
                    Toast.makeText(context, TOAST_SUCCESS, Toast.LENGTH_LONG).show();
                    break;
                case CODE_ALREADY_IN_DB:
                    Toast.makeText(context, TOAST_ALREADY_IN_DB, Toast.LENGTH_LONG).show();
                    break;
                case CODE_NO_SUCH_SYMBOL:
                    Toast.makeText(context, TOAST_NO_SUCH_SYMBOL, Toast.LENGTH_LONG).show();
                    break;
                default:
                    throw new IllegalArgumentException("unknown error code");
            }
        }
    }

}

