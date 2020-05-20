package pl.eizodev.app.stocks;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.eizodev.app.entity.Stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class StockWIG20 extends Stock {

    private static String ticker(String body) {
        Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" width=\"1%\"><b><a href=\"q/[?]s=[a-z]{3}\">(.+?)</a></b></td>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);
        matcher.find();
        return matcher.group(1);
    }

    private static String name(String body) {
        Pattern pattern = Pattern.compile(" {23}<td id=\"f10\" align=\"left\">(.+?)</td>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);
        matcher.find();
        return matcher.group(1);
    }

    private static float price(String body) {
        Pattern pattern = Pattern.compile(" {23}<td id=\"f13\"><b><span id=\"aq_[a-z]{3}_c[0-3]\">(.+?)</span></b></td>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);
        matcher.find();
        return Float.parseFloat(matcher.group(1));
    }

    private static String changePerc(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" nowrap><b><span id=\"aq_[a-z]{3}_m1\"><span id=\"c[1-2]\">(.+?)</span></span></b></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }

    private static float priceChange(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" nowrap><span id=\"aq_[a-z]{3}_m2\"><span id=\"c[1-2]\">(.+?)</span></span></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return Float.parseFloat(matcher.group(1));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String volume(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\"><span id=\"aq_[a-z]{3}_v2\">(.+?)</span></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }

    private static String date(String body) {
        Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" nowrap><span id=\"aq_[a-z]{3}_t2\">(.+?)</span></td>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);
        matcher.find();
        return matcher.group(1);
    }

    public List<Stock> getAllStocksWIG20() {
        final String WIG20 = "https://stooq.pl/t/?i=532";
        List<Stock> stocks = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(WIG20).get();
            String[] body = doc.body().html().split("\n");

            for (int i = 0; i < body.length; i++) {
                if (Pattern.matches(" {22}<tr id=\"r_[0-9]{1,2}\">", body[i])) {
                    String ticker = ticker(body[++i]);
                    String name = name(body[++i]);
                    float price = price(body[++i]);
                    String changePerc = changePerc(body[++i]);
                    float priceChange = priceChange(body[++i]);
                    String volume = volume(body[++i]);
                    String date = date(body[++i]);
                    stocks.add(new Stock(ticker, name, price, changePerc, volume));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocks;
    }
}