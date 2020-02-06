package fr.insee.rmes.queries.geo;

import java.util.HashMap;
import java.util.Map;

import fr.insee.rmes.config.Configuration;
import fr.insee.rmes.queries.Queries;

public class GeoQueries extends Queries {

    private static final String NONE = "none";
    private static final String QUERIES_FOLDER = "geographie/";

    /* IDENTIFICATION*/
    public static String getCommuneByCodeAndDate(String code, String date) {
        return queryWithCodeAndDateParam(code, date, "getCommuneByCodeAndDate.ftlh");
    }

    public static String getDepartementByCodeAndDate(String code, String date) {
        return queryWithCodeAndDateParam(code, date, "getDeptByCodeAndDate.ftlh");
    }

    public static String getRegionByCodeAndDate(String code, String date) {
        return queryWithCodeAndDateParam(code, date, "getRegionByCodeAndDate.ftlh");
    }

    public static String getArrondissementByCodeAndDate(String code, String date) {
        return queryWithCodeAndDateParam(code, date, "getArrondissementByCodeAndDate.ftlh");
    }
    
    /* LIST */
    public static String getListCommunes(String date) {
        return queryWithCodeAndDateParam(NONE, date, "getCommuneByCodeAndDate.ftlh");
    }
    
    public static String getListDept(String date) {
        return queryWithCodeAndDateParam(NONE, date, "getDeptByCodeAndDate.ftlh");
    }
    
    public static String getListRegion(String date) {
        return queryWithCodeAndDateParam(NONE, date, "getRegionByCodeAndDate.ftlh");
    }
    
    
    /* ASCENDANT / DESCENDANT */
    public static String getAscendantsCommune(String code, String date, String type) {     
        return getAscendantOrDescendantsQuery(code, date, type, "Commune", true);
    }
    
    public static String getAscendantsDepartement(String code, String date, String type) {     
        return getAscendantOrDescendantsQuery(code, date, type, "Departement", true);
    }
    
    public static String getDescendantsCommune(String code, String date, String type) {     
        return getAscendantOrDescendantsQuery(code, date, type, "Commune", false);
    }
    
    public static String getDescendantsDepartement(String code, String date, String type) {     
        return getAscendantOrDescendantsQuery(code, date, type, "Departement", false);
    }
    
    public static String getDescendantsRegion(String code, String date, String type) {     
        return getAscendantOrDescendantsQuery(code, date, type, "Region", false);
    }


    private static String getAscendantOrDescendantsQuery(String code, String date, String type, String typeOrigine, boolean ascendant) {
        Map<String, Object> params = buildCodeAndDateParams(code, date);
        params.put("type",type);
        params.put("typeOrigine",typeOrigine);
        params.put("ascendant",String.valueOf(ascendant));
        return buildRequest(QUERIES_FOLDER, "getAscendantsOrDescendantsByCodeTypeDate.ftlh", params);
    }

    

    
    /* UTILS */
    private static String queryWithCodeAndDateParam(String code, String date, String queryFile) {
        Map<String, Object> params = buildCodeAndDateParams(code, date);
        return buildRequest(QUERIES_FOLDER, queryFile, params);
    }

    private static Map<String, Object> buildCodeAndDateParams(String code, String date) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("date", date);
        return params;
    }
    
    @Deprecated
    public static String getCountry(String code) {
        return "SELECT ?uri ?intitule ?intituleEntier \n"
            + "FROM <http://rdf.insee.fr/graphes/geo/cog> \n"
            + "WHERE { \n"
            + "?uri rdf:type igeo:Etat . \n"
            + "?uri igeo:codeINSEE '"
            + code
            + "'^^xsd:token . \n"
            + "?uri igeo:nom ?intitule . \n"
            + "?uri igeo:nomEntier ?intituleEntier . \n"
            // Ensure that is not the dbpedia URI
            + "FILTER (REGEX(STR(?uri), '"
            + Configuration.getBaseHost()
            + "')) \n"
            + "FILTER (lang(?intitule) = 'fr') \n"
            + "FILTER (lang(?intituleEntier) = 'fr') \n"
            + "}";
    }

  


}
