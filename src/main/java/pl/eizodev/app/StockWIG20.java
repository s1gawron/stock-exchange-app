package pl.eizodev.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.eizodev.app.entity.Stock;

class StockWIG20 {
    private List<Stock> stocks;

    private Float parsePrice(String s) {
        return Float.parseFloat(s);
    }

    private String changeString(String sChange) {
        if (sChange.isEmpty()) {
            sChange = "0";
        }
        return sChange;
    }

    private String volumeString(String sVolume) {
        if (sVolume.isEmpty()) {
            sVolume = "0";
        }
        return sVolume;
    }

    public StockWIG20() {
        final String WIG20 = "https://stooq.pl/t/?i=532";
        stocks = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(WIG20).get();

            //ALR
            String priceALR = doc.select("#aq_alr_c2").text();
            String changeALR = doc.select("#aq_alr_m1").text();
            String volumeALR = doc.select("#aq_alr_v2").text();

            //CCC
            String priceCCC = doc.select("#aq_ccc_c2").text();
            String changeCCC = doc.select("#aq_ccc_m1").text();
            String volumeCCC = doc.select("#aq_ccc_v2").text();

            //CDR
            String priceCDR = doc.select("#aq_cdr_c1").text();
            String changeCDR = doc.select("#aq_cdr_m1").text();
            String volumeCDR = doc.select("#aq_cdr_v2").text();

            //CPS
            String priceCPS = doc.select("#aq_cps_c2").text();
            String changeCPS = doc.select("#aq_cps_m1").text();
            String volumeCPS = doc.select("#aq_cdr_v2").text();

            //DNP
            String priceDNP = doc.select("#aq_dnp_c1").text();
            String changeDNP = doc.select("#aq_dnp_m1").text();
            String volumeDNP = doc.select("#aq_dnp_v2").text();

            //JSW
            String priceJSW = doc.select("#aq_jsw_c2").text();
            String changeJSW = doc.select("#aq_jsw_m1").text();
            String volumeJSW = doc.select("#aq_jsw_v2").text();

            //KGHM
            String priceKGHM = doc.select("#aq_kgh_c2").text();
            String changeKGHM = doc.select("#aq_kgh_m1").text();
            String volumeKGHM = doc.select("#aq_kgh_v2").text();

            //LPP
            String priceLPP = doc.select("#aq_lpp_c0").text();
            String changeLPP = doc.select("#aq_lpp_m1").text();
            String volumeLPP = doc.select("#aq_lpp_v2").text();

            //LTS
            String priceLTS = doc.select("#aq_lts_c2").text();
            String changeLTS = doc.select("#aq_lts_m1").text();
            String volumeLTS = doc.select("#aq_lts_v2").text();

            //MBK
            String priceMBK = doc.select("#aq_mbk_c1").text();
            String changeMBK = doc.select("#aq_mbk_m1").text();
            String volumeMBK = doc.select("#aq_mbk_v2").text();

            //OPL
            String priceOPL = doc.select("#aq_opl_c3").text();
            String changeOPL = doc.select("#aq_opl_m1").text();
            String volumeOPL = doc.select("#aq_opl_v2").text();

            //PEO
            String pricePEO = doc.select("#aq_peo_c2").text();
            String changePEO = doc.select("#aq_peo_m1").text();
            String volumePEO = doc.select("#aq_peo_v2").text();

            //PGE
            String pricePGE = doc.select("#aq_pge_c3").text();
            String changePGE = doc.select("#aq_pge_m1").text();
            String volumePGE = doc.select("#aq_pge_v2").text();

            //PGN
            String pricePGN = doc.select("#aq_pgn_c3").text();
            String changePGN = doc.select("#aq_pgn_m1").text();
            String volumePGN = doc.select("#aq_pgn_v2").text();

            //PKN
            String pricePKN = doc.select("#aq_pkn_c2").text();
            String changePKN = doc.select("#aq_pkn_m1").text();
            String volumePKN = doc.select("#aq_pkn_v2").text();

            //PKO
            String pricePKO = doc.select("#aq_pko_c2").text();
            String changePKO = doc.select("#aq_pko_m1").text();
            String volumePKO = doc.select("#aq_pko_v2").text();

            //PLY
            String pricePLY = doc.select("#aq_ply_c2").text();
            String changePLY = doc.select("#aq_ply_m1").text();
            String volumePLY = doc.select("#aq_ply_v2").text();

            //PZU
            String pricePZU = doc.select("#aq_pzu_c2").text();
            String changePZU = doc.select("#aq_pzu_m1").text();
            String volumePZU = doc.select("#aq_pzu_v2").text();

            //SPL
            String priceSPL = doc.select("#aq_spl_c1").text();
            String changeSPL = doc.select("#aq_spl_m1").text();
            String volumeSPL = doc.select("#aq_spl_v2").text();

            //TPE
            String priceTPE = doc.select("#aq_tpe_c3").text();
            String changeTPE = doc.select("#aq_tpe_m1").text();
            String volumeTPE = doc.select("#aq_tpe_v2").text();

            //Obj
           stocks.add(new Stock(1L, null, "ALR", "ALIOR", parsePrice(priceALR), 0, changeString(changeALR), volumeString(volumeALR), 0));
           stocks.add(new Stock(2L, null, "CCC", "CCC", parsePrice(priceCCC), 0, changeString(changeCCC), volumeString(volumeCCC), 0));
           stocks.add(new Stock(3L, null, "CDR", "CDPROJEKT", parsePrice(priceCDR), 0, changeString(changeCDR), volumeString(volumeCDR), 0));
           stocks.add(new Stock(4L, null, "CPS", "CYFRPOLSAT", parsePrice(priceCPS), 0, changeString(changeCPS), volumeString(volumeCPS), 0));
           stocks.add(new Stock(5L, null, "DNP", "DINOPL", parsePrice(priceDNP), 0, changeString(changeDNP), volumeString(volumeDNP), 0));
           stocks.add(new Stock(6L, null, "JSW", "JSW", parsePrice(priceJSW), 0, changeString(changeJSW), volumeString(volumeJSW), 0));
           stocks.add(new Stock(7L, null, "KGHM", "KGHM", parsePrice(priceKGHM), 0, changeString(changeKGHM), volumeString(volumeKGHM), 0));
           stocks.add(new Stock(8L, null, "LPP", "LPP", parsePrice(priceLPP), 0, changeString(changeLPP), volumeString(volumeLPP), 0));
           stocks.add(new Stock(9L, null, "LTS", "LOTOS", parsePrice(priceLTS), 0, changeString(changeLTS), volumeString(volumeLTS), 0));
           stocks.add(new Stock(10L, null, "MBK", "MBANK", parsePrice(priceMBK), 0, changeString(changeMBK), volumeString(volumeMBK), 0));
           stocks.add(new Stock(11L, null, "OPL", "ORANGEPL", parsePrice(priceOPL), 0, changeString(changeOPL), volumeString(volumeOPL), 0));
           stocks.add(new Stock(12L, null, "PEO", "PEKAO", parsePrice(pricePEO), 0, changeString(changePEO), volumeString(volumePEO), 0));
           stocks.add(new Stock(13L, null, "PGE", "PGE", parsePrice(pricePGE), 0, changeString(changePGE), volumeString(volumePGE), 0));
           stocks.add(new Stock(14L, null, "PGN", "PGNIG", parsePrice(pricePGN), 0, changeString(changePGN), volumeString(volumePGN), 0));
           stocks.add(new Stock(15L, null, "PKN", "PKNORLEN", parsePrice(pricePKN), 0, changeString(changePKN), volumeString(volumePKN), 0));
           stocks.add(new Stock(16L, null, "PKO", "PKOBP", parsePrice(pricePKO), 0, changeString(changePKO), volumeString(volumePKO), 0));
           stocks.add(new Stock(17L, null, "PLY", "PLAY", parsePrice(pricePLY), 0, changeString(changePLY), volumeString(volumePLY), 0));
           stocks.add(new Stock(18L, null, "PZU", "PZU", parsePrice(pricePZU), 0, changeString(changePZU), volumeString(volumePZU), 0));
           stocks.add(new Stock(19L, null, "SPL", "SANPL", parsePrice(priceSPL), 0, changeString(changeSPL), volumeString(volumeSPL), 0));
           stocks.add(new Stock(20L, null, "TPE", "TAURONPE", parsePrice(priceTPE), 0, changeString(changeTPE), volumeString(volumeTPE), 0));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Stock> getAll() {
        return stocks;
    }

    public Stock getByTicker(String ticker) {
        Optional<Stock> first = stocks.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .findFirst();
        return first.get();
    }
}