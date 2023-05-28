package com.isep.dataengineservice.Models.Trip;

import java.util.Arrays;
import java.util.List;


// A GREAT THANKS TO CHATGPT FOR SAVING ME A LOT OF TIME WRITING THESE LISTS.
public interface StreetKeywords  {
    List<String> frenchStreets = Arrays.asList("cour", "immeuble", "rue", "chemin", "allée", "impasse", "rond-point",
            "passerelle", "esplanade", "chaussée", "voie", "sentier",
            "cours", "tunnel", "autoroute", "rocade", "périphérique", "route",
            "départementale", "cité", "hôtel", "métro",  "avenue", "maison", "gare", "métro", "Gendarmerie", "école", "hopital",
            "lycée", "villa", "collège", "hôpital",  "restaurant"
    );
    List<String> englishStreets = Arrays.asList(
            "street",  "drive", "road", "lane", "terrace",
            "court", "way", "circle", "loop", "alley",
            "highway", "freeway", "motorway", "interstate", "expressway", "neighborhood", "house", "restaurant"
    );

    List<String> germanStreets = Arrays.asList(
            "straße", "gasse", "weg", "allee", "platz", "ring", "boulevard",
            "steg", "brücke", "tunnel", "autobahn", "bundesstraße", "landstraße",
            "kreisstraße", "staatsstraße", "hauptstraße", "dorfstraße", "pfad"
    );
    List<String> dutchStreets = Arrays.asList(
            "straat", "laan", "weg", "steeg", "plein", "gracht", "kade",
            "pad", "dreef", "erf", "hof", "burg", "donk", "allee", "brug",
            "tunnel", "autosnelweg", "rijksweg", "provinciale weg", "gemeentelijke weg"
    );
    List<String> italianStreets = Arrays.asList(
            "via", "viale", "piazza", "strada", "corso", "largo", "vicolo",
            "borgo", "piazzale", "passeggiata", "lungomare", "rotatoria",
            "autostrada", "superstrada", "strada statale", "strada provinciale", "strada comunale"
    );
    List<String> spanishStreets = Arrays.asList(
            "calle", "avenida", "bulevar", "paseo", "carrera",
            "callejón", "pasaje", "ronda", "camino", "carretera", "autopista", "autovía",
            "vía", "travesía", "alameda", "costanera", "extrarradio", "polígono"
    );
    List<String> chineseStreets = Arrays.asList(
            "街", "路", "大道", "胡同", "巷", "弄", "里", "庭",
            "园", "广场", "桥", "隧道", "高速公路", "快速路",
            "城市快速路", "国道", "省道", "县道", "乡道", "村道"
    );
    List<String> hindiStreets = Arrays.asList(
            "सड़क", "मार्ग", "गली", "चौक", "विराज", "विधान", "पथ",
            "रास्ता", "विश्वविद्यालय", "नगर", "मोहल्ला", "आवास", "बाजार"
    );
    List<String> japaneseStreets = Arrays.asList(
            "通り", "道", "路地", "橋", "大通り", "街", "路",
            "広場", "丁目", "番地", "町", "町並み", "アーケード", "新道",
            "高速道路", "自動車道", "環状線", "幹線道路", "都道府県道", "市道", "町道", "村道"
    );
    List<String> arabicStreets = Arrays.asList(
            "شارع", "طريق", "جادة", "سبيل", "ممر", "زقاق", "حارة",
            "ساحة", "ميدان", "كورنيش", "دوار",  "نفق"
    );
    List<String> afrikaansStreets = Arrays.asList(
            "straat", "weg", "laan", "rylaan", "steg", "pad", "brug",
            "roete", "sirkel", "hoofweg", "sypaadjie", "boulevard", "plein"
    );
    List<String> swahiliStreets = Arrays.asList(
            "barabara", "njia", "gati", "barabara kuu", "barabara ndogo", "mkondo", "uwanja",
            "barabara ya mzunguko", "barabara ya kuelekea", "barabara ya jirani", "barabara ya mtaa"
    );
    List<String> greekStreets = Arrays.asList(
            "οδός", "δρόμος", "λεωφόρος", "πλατεία", "στενό", "γέφυρα", "πεζόδρομος",
            "περιφερειακός", "αυτοκινητόδρομος", "εθνική οδός", "επαρχιακή οδός", "τοπική οδός"
    );
    List<String> portugueseStreets = Arrays.asList(
            "rua", "avenida", "travessa", "estrada", "rodovia", "viela", "beco", "viaduto", "ponte", "boulevard", "escola", "vila"
    );
    List<String> polishStreets = Arrays.asList(
            "ulica", "aleja", "droga", "most", "ścieżka", "szosa", "tronka",
            "bulwar", "plac", "skrzyżowanie", "rondo", "pas", "ścieżka rowerowa"
    );
    List<String> swedishStreets = Arrays.asList(
            "gata", "väg", "allé", "stig", "bro", "torg", "promenad",
            "rondell", "motorväg", "led", "huvudväg", "gångväg", "cykelväg"
    );
}
