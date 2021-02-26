package uk.ac.kcl.sufcwmillionapplication.api.impl;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.kcl.sufcwmillionapplication.api.ShareDao;
import uk.ac.kcl.sufcwmillionapplication.bean.DailyQuote;
import uk.ac.kcl.sufcwmillionapplication.bean.SymbolInfo;
import uk.ac.kcl.sufcwmillionapplication.utils.CommonUtils;
import uk.ac.kcl.sufcwmillionapplication.utils.NetworkUtils;

public class YahooShareDaoImpl implements ShareDao {

    private static final String YAHOO_FINANCE_API = "https://query1.finance.yahoo.com/";
    private static final String YAHOO_FINANCE_QUOTE = "https://finance.yahoo.com/quote/";

    @Override
    public SymbolInfo getInfoOfSymbol(String symbol) {
        StringBuffer url = new StringBuffer(YAHOO_FINANCE_QUOTE);
        url.append(symbol);
        String html = NetworkUtils.fetchUrl(url.toString());
        if(CommonUtils.isEmptyString(html) || !html.contains("QuoteSummaryStore")){
            return null;
        }
        String htmlSplit1[] = html.split("root.App.main =");
        if(htmlSplit1.length < 2){
            return null;
        }
        String jsonString = htmlSplit1[1].split("\\(this\\)")[0];
        jsonString = jsonString.split(";\n")[0];
        Gson gson = new Gson();
        try{
            JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
            JsonObject ctx = jsonObject.getAsJsonObject("context");
            JsonObject dispatcher = ctx.getAsJsonObject("dispatcher");
            JsonObject stores = dispatcher.getAsJsonObject("stores");
            JsonObject quoteSummaryStore = stores.getAsJsonObject("QuoteSummaryStore");
            JsonObject quoteType = quoteSummaryStore.getAsJsonObject("quoteType");
            SymbolInfo symbolInfo = new SymbolInfo();
            symbolInfo.setLongName(quoteType.get("longName").getAsString());
            symbolInfo.setShortName(quoteType.get("shortName").getAsString());
            symbolInfo.setSymbol(symbol);
            return symbolInfo;
        }catch (Exception ex){
            ex.printStackTrace();
            Log.e("ShareDao","JSON Error", ex);
        }
        return null;
    }

    @Override
    public List<DailyQuote> getHistoryQuotes(String symbol, Date startDate, Date endDate) {

        StringBuffer url = new StringBuffer(YAHOO_FINANCE_API);
        url.append("v8/finance/chart/");
        url.append(symbol);
        url.append("?");
        url.append("period1=");
        url.append(startDate.getTime() / 1000);
        url.append("&period2=");
        url.append(endDate.getTime() / 1000);
        url.append("&interval=1d&events=history");

        List<DailyQuote> quotes = new ArrayList<>();
        String json = NetworkUtils.fetchUrl(url.toString());
        if(CommonUtils.isEmptyString(json)){
            return quotes;
        }

        Gson gson = new Gson();

        try {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            JsonObject chart = jsonObject.getAsJsonObject("chart");
            JsonObject result = chart.getAsJsonArray("result").get(0).getAsJsonObject();
            JsonObject indicators = result.getAsJsonObject("indicators");
            JsonObject quote = indicators.getAsJsonArray("quote").get(0).getAsJsonObject();
            JsonObject adjclose = indicators.getAsJsonArray("adjclose").get(0).getAsJsonObject();
            JsonArray timestamp = result.getAsJsonArray("timestamp");

            JsonArray openList = quote.getAsJsonArray("open");
            JsonArray closeList = quote.getAsJsonArray("close");
            JsonArray lowList = quote.getAsJsonArray("low");
            JsonArray highList = quote.getAsJsonArray("high");
            JsonArray volumeList = quote.getAsJsonArray("volume");
            JsonArray adjcloseList = adjclose.getAsJsonArray("adjclose");

            int size = timestamp.size();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < size ; i ++){
                Date date = new Date(timestamp.get(i).getAsLong() * 1000);
                DailyQuote dailyQuote = DailyQuote.createDailyQuote();
                dailyQuote.adjclose = adjcloseList.get(i).getAsDouble();
                dailyQuote.close = closeList.get(i).getAsDouble();
                dailyQuote.open = openList.get(i).getAsDouble();
                dailyQuote.date = sdf.format(date);
                dailyQuote.high = highList.get(i).getAsDouble();
                dailyQuote.low = lowList.get(i).getAsDouble();
                dailyQuote.volume = volumeList.get(i).getAsDouble();
                quotes.add(dailyQuote);
            }

        }catch (Exception ex){
            Log.e("ShareDao","Extract data error", ex);
            Log.d("ShareDao", json);
        }

        return quotes;
    }

}