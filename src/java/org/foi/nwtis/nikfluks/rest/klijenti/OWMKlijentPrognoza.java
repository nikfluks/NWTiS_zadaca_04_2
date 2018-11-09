package org.foi.nwtis.nikfluks.rest.klijenti;

import java.io.StringReader;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.nikfluks.web.podaci.MeteoPodaci;
import org.foi.nwtis.nikfluks.web.podaci.MeteoPrognoza;

/**
 * Klasa nasljeđuje OWMKlijent i služi za dohvaćanje meteo prognoze za određeno parkiralište. Prognoza je za svaka 3 sata za naredna
 * 5 dana.
 *
 * @author Nikola
 * @version 1
 */
public class OWMKlijentPrognoza extends OWMKlijent {

    /**
     * Konstruktor klase, prosljeđuje apiKey nadklasi.
     *
     * @param apiKey apiKey koji se prosljeđuje
     */
    public OWMKlijentPrognoza(String apiKey) {
        super(apiKey);
    }

    /**
     * Dohvaća meteo prognozu za odabrano parkiralište, čita dobivenu prognozu za svaka 3 sata i sprema u polje MeteoPrognoza[]
     *
     * @param id id parkirališta
     * @param latitude latitude parkirališta za koje se dohvaća prognoza
     * @param longitude longitude parkirališta za koje se dohvaća prognoza
     * @return
     */
    public MeteoPrognoza[] getWeatherForecast(int id, String latitude, String longitude) {
        try {
            String odgovor = dohvatiMeteoPodatke(latitude, longitude);
            JsonReader reader = Json.createReader(new StringReader(odgovor));
            JsonObject jo = reader.readObject();

            int brojPrognoza = jo.getJsonNumber("cnt").intValue();
            MeteoPrognoza[] meteoPrognoze = new MeteoPrognoza[brojPrognoza];
            JsonArray jsonPolje = jo.getJsonArray("list");
            Date dan = new Date(System.currentTimeMillis());

            for (int i = 0; i < brojPrognoza; i++) {
                jo = jsonPolje.getJsonObject(i);
                MeteoPodaci mp = postaviMeteoPodatke(jo);
                MeteoPrognoza meteoPrognoza = new MeteoPrognoza(id, dan, mp);
                meteoPrognoze[i] = meteoPrognoza;
            }
            return meteoPrognoze;
        } catch (Exception ex) {
            System.err.println("Greska pri dohvacanju prognoze za 5 dana!");
            return null;
        }
    }

    /**
     * Dohvaća meteo prognozu sa web servisa temeljem latitude i longitude. Prognoza je za svaka 3 sata za naredna 5 dana.
     *
     * @param latitude latitude parkirališta za koje se dohvaća prognoza
     * @param longitude longitude parkirališta za koje se dohvaća prognoza
     * @return prognoze dohvaćene sa web servisa
     */
    private String dohvatiMeteoPodatke(String latitude, String longitude) {
        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_Forecast_Path());
        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);

        String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
        return odgovor;
    }

    /**
     * Postavlja dobivene atribute prognoze u objekt MeteoPodaci mp
     *
     * @param jo json objekt pomoću kojeg čitamo dobivene json podatke
     * @return objekt MeteoPodaci mp
     */
    private MeteoPodaci postaviMeteoPodatke(JsonObject jo) {
        MeteoPodaci mp = new MeteoPodaci();
        mp.setTemperatureValue(new Double(jo.getJsonObject("main").getJsonNumber("temp").doubleValue()).floatValue());//°C
        mp.setTemperatureMin(new Double(jo.getJsonObject("main").getJsonNumber("temp_min").doubleValue()).floatValue());//°C
        mp.setTemperatureMax(new Double(jo.getJsonObject("main").getJsonNumber("temp_max").doubleValue()).floatValue());//°C
        mp.setTemperatureUnit("°C");
        mp.setHumidityValue(new Double(jo.getJsonObject("main").getJsonNumber("humidity").doubleValue()).floatValue());//%
        mp.setHumidityUnit("%");
        mp.setPressureValue(new Double(jo.getJsonObject("main").getJsonNumber("pressure").doubleValue()).floatValue());//hPa
        mp.setPressureUnit("hPa");
        mp.setWindSpeedValue(new Double(jo.getJsonObject("wind").getJsonNumber("speed").doubleValue()).floatValue());//m/s
        mp.setWindDirectionValue(new Double(jo.getJsonObject("wind").getJsonNumber("deg").doubleValue()).floatValue());//°
        mp.setCloudsValue(jo.getJsonObject("clouds").getInt("all"));//%
        mp.setWeatherNumber(jo.getJsonArray("weather").getJsonObject(0).getInt("id"));
        mp.setCloudsName(jo.getJsonArray("weather").getJsonObject(0).getString("main"));//npr Rain, Snow...
        mp.setWeatherValue(jo.getJsonArray("weather").getJsonObject(0).getString("description"));//detaljniji opis gore navedenog
        mp.setWeatherIcon(jo.getJsonArray("weather").getJsonObject(0).getString("icon"));
        //u atributu dt nalazi se broj milisekundi koji predstavlja vrijeme (timestamp) za koje je dobivena prognoza
        //znaci za svaka 3 sata naredih 5 dana, ne oznacava trenutno ili last update vrijeme iako se tu sprema tako!
        mp.setLastUpdate(new Date(jo.getJsonNumber("dt").bigDecimalValue().longValue() * 1000));//long
        return mp;
    }

}
