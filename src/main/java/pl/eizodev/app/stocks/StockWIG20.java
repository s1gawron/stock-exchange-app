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

@Getter
@Setter
@NoArgsConstructor
public class StockWIG20 extends Stock {

    public List<Stock> getAllStocksWIG20() {
        final String WIG20 = "https://stooq.pl/t/?i=532";
        List<Stock> stocks = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(WIG20).get();

            //ALR
            String priceALR = doc.select("#aq_alr_c3").text();
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
            String priceJSW = doc.select("#aq_jsw_c3").text();
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
            String pricePKO = doc.select("#aq_pko_c3").text();
            String changePKO = doc.select("#aq_pko_m1").text();
            String volumePKO = doc.select("#aq_pko_v2").text();

            //PLY
            String pricePLY = doc.select("#aq_ply_c3").text();
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

            stocks.add(new Stock("ALR", "ALIOR", parsePrice(priceALR), changeString(changeALR), volumeString(volumeALR)));
            stocks.add(new Stock("CCC", "CCC", parsePrice(priceCCC), changeString(changeCCC), volumeString(volumeCCC)));
            stocks.add(new Stock("CDR", "CDPROJEKT", parsePrice(priceCDR), changeString(changeCDR), volumeString(volumeCDR)));
            stocks.add(new Stock("CPS", "CYFRPOLSAT", parsePrice(priceCPS), changeString(changeCPS), volumeString(volumeCPS)));
            stocks.add(new Stock("DNP", "DINOPL", parsePrice(priceDNP), changeString(changeDNP), volumeString(volumeDNP)));
            stocks.add(new Stock("JSW", "JSW", parsePrice(priceJSW), changeString(changeJSW), volumeString(volumeJSW)));
            stocks.add(new Stock("KGH", "KGHM", parsePrice(priceKGHM), changeString(changeKGHM), volumeString(volumeKGHM)));
            stocks.add(new Stock("LPP", "LPP", parsePrice(priceLPP), changeString(changeLPP), volumeString(volumeLPP)));
            stocks.add(new Stock("LTS", "LOTOS", parsePrice(priceLTS), changeString(changeLTS), volumeString(volumeLTS)));
            stocks.add(new Stock("MBK", "MBANK", parsePrice(priceMBK), changeString(changeMBK), volumeString(volumeMBK)));
            stocks.add(new Stock("OPL", "ORANGEPL", parsePrice(priceOPL), changeString(changeOPL), volumeString(volumeOPL)));
            stocks.add(new Stock("PEO", "PEKAO", parsePrice(pricePEO), changeString(changePEO), volumeString(volumePEO)));
            stocks.add(new Stock("PGE", "PGE", parsePrice(pricePGE), changeString(changePGE), volumeString(volumePGE)));
            stocks.add(new Stock("PGN", "PGNIG", parsePrice(pricePGN), changeString(changePGN), volumeString(volumePGN)));
            stocks.add(new Stock("PKN", "PKNORLEN", parsePrice(pricePKN), changeString(changePKN), volumeString(volumePKN)));
            stocks.add(new Stock("PKO", "PKOBP", parsePrice(pricePKO), changeString(changePKO), volumeString(volumePKO)));
            stocks.add(new Stock("PLY", "PLAY", parsePrice(pricePLY), changeString(changePLY), volumeString(volumePLY)));
            stocks.add(new Stock("PZU", "PZU", parsePrice(pricePZU), changeString(changePZU), volumeString(volumePZU)));
            stocks.add(new Stock("SPL", "SANPL", parsePrice(priceSPL), changeString(changeSPL), volumeString(volumeSPL)));
            stocks.add(new Stock("TPE", "TAURONPE", parsePrice(priceTPE), changeString(changeTPE), volumeString(volumeTPE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocks;
    }
}