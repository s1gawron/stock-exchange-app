import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class StockWIG20 {
    private String ticker;
    private String name;
    private double tempPrice;
    private String change;
    private String volume;

    private StockWIG20(String ticker, String name, double tempPrice, String change, String volume) {
        this.ticker = ticker;
        this.name = name;
        this.tempPrice = tempPrice;
        this.change = change;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Symbol = '" + ticker + '\'' +
                ", Nazwa = '" + name + '\'' +
                ", Kurs = " + tempPrice +
                ", Zmiana = " + change  +
                ", Wolumen = " + volume;
    }

    private static Double parsePrice(String s){
        return Double.parseDouble(s);
    }

    private static String parseChange(String sChange){
        if (sChange.isEmpty()){
            sChange = "0";
        }
        return sChange;
    }

    private static String volumeString(String sVolume){
        if (sVolume.isEmpty()){
            sVolume = "0";
        }
        return sVolume;
    }

     static void getWig20() {

        final String WIG20 = "https://stooq.pl/t/?i=532";

        try {
            final Document doc = Jsoup.connect(WIG20).get();

            //ALR
            String priceALR = doc.select("#aq_alr_c2").text();
            String changeALR = doc.select("#aq_alr_m1").text();
            String volumeALR = doc.select("#aq_alr_v2").text();

            //CCC
            String priceCCC = doc.select("#aq_ccc_c1").text();
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
            StockWIG20 ALR = new StockWIG20("ALR", "ALIOR", parsePrice(priceALR), parseChange(changeALR), volumeString(volumeALR));
            StockWIG20 CCC = new StockWIG20("CCC", "CCC", parsePrice(priceCCC), parseChange(changeCCC), volumeString(volumeCCC));
            StockWIG20 CDR = new StockWIG20("CDR", "CDPROJEKT", parsePrice(priceCDR), parseChange(changeCDR), volumeString(volumeCDR));
            StockWIG20 CPS = new StockWIG20("CPS", "CYFRPOLSAT", parsePrice(priceCPS), parseChange(changeCPS), volumeString(volumeCPS));
            StockWIG20 DNP = new StockWIG20("DNP", "DINOPL", parsePrice(priceDNP), parseChange(changeDNP), volumeString(volumeDNP));
            StockWIG20 JSW = new StockWIG20("JSW", "JSW", parsePrice(priceJSW), parseChange(changeJSW), volumeString(volumeJSW));
            StockWIG20 KGHM = new StockWIG20("KGHM", "KGHM", parsePrice(priceKGHM), parseChange(changeKGHM), volumeString(volumeKGHM));
            StockWIG20 LPP = new StockWIG20("LPP", "LPP", parsePrice(priceLPP), parseChange(changeLPP), volumeString(volumeLPP));
            StockWIG20 LTS = new StockWIG20("LTS", "LOTOS", parsePrice(priceLTS), parseChange(changeLTS), volumeString(volumeLTS));
            StockWIG20 MBK = new StockWIG20("MBK", "MBANK", parsePrice(priceMBK), parseChange(changeMBK), volumeString(volumeMBK));
            StockWIG20 OPL = new StockWIG20("OPL", "ORANGEPL", parsePrice(priceOPL), parseChange(changeOPL), volumeString(volumeOPL));
            StockWIG20 PEO = new StockWIG20("PEO", "PEKAO", parsePrice(pricePEO), parseChange(changePEO), volumeString(volumePEO));
            StockWIG20 PGE = new StockWIG20("PGE", "PGE", parsePrice(pricePGE), parseChange(changePGE), volumeString(volumePGE));
            StockWIG20 PGN = new StockWIG20("PGN", "PGNIG", parsePrice(pricePGN), parseChange(changePGN), volumeString(volumePGN));
            StockWIG20 PKN = new StockWIG20("PKN", "PKNORLEN", parsePrice(pricePKN), parseChange(changePKN), volumeString(volumePKN));
            StockWIG20 PKO = new StockWIG20("PKO", "PKOBP", parsePrice(pricePKO), parseChange(changePKO), volumeString(volumePKO));
            StockWIG20 PLY = new StockWIG20("PLY", "PLAY", parsePrice(pricePLY), parseChange(changePLY), volumeString(volumePLY));
            StockWIG20 PZU = new StockWIG20("PZU", "PZU", parsePrice(pricePZU), parseChange(changePZU), volumeString(volumePZU));
            StockWIG20 SPL = new StockWIG20("SPL", "SANPL", parsePrice(priceSPL), parseChange(changeSPL), volumeString(volumeSPL));
            StockWIG20 TPE = new StockWIG20("TPE", "TAURONPE", parsePrice(priceTPE), parseChange(changeTPE), volumeString(volumeTPE));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

