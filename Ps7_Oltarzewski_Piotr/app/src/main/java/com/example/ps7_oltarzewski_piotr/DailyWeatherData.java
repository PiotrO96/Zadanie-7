package com.example.ps7_oltarzewski_piotr;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class DailyWeatherData extends AsyncTask<String, Void, String> {

    private HashMap<String, String> names;
    private HashMap<String, String> units;
    private HashMap<String, Double> predictor;

    private String[] mainParameters;
    private String[] windParameters;
    private String[] cloudsParameters;

    private static final String glowne = "main";
    private static final String wiatr = "wind";
    private static final String chmury = "clouds";
    private static final String koordynaty = "coord";

    private static final String temperatura = "temp";
    private static final String cisnienie = "pressure";
    private static final String wilgotnosc = "humidity";
    private static final String min_temperatura = "temp_min";
    private static final String max_temperatura = "temp_max";
    private static final String szybkosc_wiatru = "speed";
    private static final String stopien_wiatru = "deg";
    private static final String zachmurzenie = "all";
    private static final String koordynaty_lon = "lon";
    private static final String koordynaty_lat = "lat";

    private static final String jendostka_temperatury = (char) 0x00B0 + " C";
    private static final String jednostka_nachylenia_wiatru = Character.toString((char) 0x00B0);
    private static final String jednostka_cisnienia = "hPa";
    private static final String jednostka_zachmurzenia = "%";
    private static final String jednostka_szybkosci_wiatru = "m/s";

    private static final String klucz_api = "0547eb1ce9af96469e2ba1e4a3c1cd8f";
    private static final String url_api_miasto_id = "http://api.openweathermap.org/data/2.5/weather?id=";

    public DailyWeatherData() {
        mainParameters = new String[] {temperatura, cisnienie, wilgotnosc, min_temperatura, max_temperatura};
        windParameters = new String[] {szybkosc_wiatru, stopien_wiatru};
        cloudsParameters = new String[] {zachmurzenie};
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(url_api_miasto_id + strings[0] + "&APPID=" + klucz_api);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            System.out.println("ERROR");
            return null;

        }
    }

   
}
