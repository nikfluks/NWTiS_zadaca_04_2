package org.foi.nwtis.nikfluks.ejb.sb;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import org.foi.nwtis.nikfluks.rest.klijenti.GMKlijent;
import org.foi.nwtis.nikfluks.rest.klijenti.OWMKlijentPrognoza;
import org.foi.nwtis.nikfluks.web.podaci.Lokacija;
import org.foi.nwtis.nikfluks.web.podaci.MeteoPrognoza;

/**
 * Klasa je stateless zrno preko kojeg se dohvaćaju meteo prognoze sa web servisa
 *
 * @author Nikola
 * @version 1
 */
@Stateless
@LocalBean
public class MeteoKlijentZrno {

    private String apiKey;
    private String gmApiKey;

    /**
     * Postavlja apiKey i gmApiKey
     *
     * @param apiKey
     * @param gmApiKey
     */
    public void postaviKorisnickePodatke(String apiKey, String gmApiKey) {
        this.apiKey = apiKey;
        this.gmApiKey = gmApiKey;
    }

    /**
     * Dohvaća geolokacije podatke temeljem adrese
     *
     * @param adresa adrese temeljem koje se dohvaćaju geolokacijski podaci (latitude i longitude)
     * @return
     */
    public Lokacija dajLokaciju(String adresa) {
        GMKlijent gmk = new GMKlijent(gmApiKey);
        return gmk.getGeoLocation(adresa);
    }

    /**
     * Dohvaća meteo prognoze sa web servisa za zadano parkiralište
     *
     * @param id id parkirališta
     * @param adresa adresa parkirališta
     * @return
     */
    public MeteoPrognoza[] dajMeteoPrognoze(int id, String adresa) {
        OWMKlijentPrognoza klijentPrognoza = new OWMKlijentPrognoza(apiKey);
        Lokacija l = dajLokaciju(adresa);
        String lat = l.getLatitude();
        String lon = l.getLongitude();
        MeteoPrognoza[] meteoPrognoze = klijentPrognoza.getWeatherForecast(id, lat, lon);
        return meteoPrognoze;
    }
}
