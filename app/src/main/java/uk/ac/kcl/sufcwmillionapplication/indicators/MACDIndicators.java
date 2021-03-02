package uk.ac.kcl.sufcwmillionapplication.indicators;

import java.util.ArrayList;
import java.util.List;

import uk.ac.kcl.sufcwmillionapplication.bean.CalculateResult;
import uk.ac.kcl.sufcwmillionapplication.bean.DailyQuote;

public class MACDIndicators extends TechnicalIndicators {

    @Override
    public List<CalculateResult> calculate(List<DailyQuote> dailyQuoteList) {
        EMAIndicators ema26 = (EMAIndicators) IndicatorFactory.get(IndicatorNames.EMA_26);
        List<CalculateResult> res26 = ema26.calculate(dailyQuoteList);
        EMAIndicators ema12 = (EMAIndicators) IndicatorFactory.get(IndicatorNames.EMA_12);
        List<CalculateResult> res12 = ema12.calculate(dailyQuoteList);
        List<CalculateResult> results = new ArrayList<>();
        for(int i = 0; i < res12.size(); i++){
            CalculateResult cal = new CalculateResult();
            double value1 = res12.get(i).data;
            double value2 = res26.get(i).data;
            cal.date = res12.get(i).date;
            cal.data = value1 - value2;
            results.add(cal);
        }
        return results;
    }
}