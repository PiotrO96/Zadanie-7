package com.example.ps7_oltarzewski_piotr;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class WeatherForecastActivity extends AppCompatActivity {

    private static final String klucz_api = "0547eb1ce9af96469e2ba1e4a3c1cd8f";
    private static final String api_url = "http://api.openweathermap.org/data/2.5/forecast?";
    private static final String api_url_jednostki_metryczne = "&units=metric&APPID=";
    private static final String api_url_miasto_id = "id=";
    private static final String api_url_koordynaty_lat = "lat=";
    private static final String api_url_koordynaty_lon = "&lon=";

    private TableLayout tabela_prognozy;

    private String obecneMiastoId;
    private String obecneMiastoLon;
    private String obecneMiastoLat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        tabela_prognozy = findViewById(R.id.prognoza_tableLayout);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            obecneMiastoId = bundle.getString("city_id");
            obecneMiastoLon = bundle.getString("lon");
            obecneMiastoLat = bundle.getString("lat");
        }

        if (obecneMiastoId != null) {
            new DataRetriever().execute(obecneMiastoId);
        } else if (obecneMiastoLat != null && obecneMiastoLon != null) {
            new DataRetriever().execute(obecneMiastoLat, obecneMiastoLon);
        }
    }

    public TableRow createRow() {
        TableRow wiersz = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        wiersz.setLayoutParams(layoutParams);
        wiersz.setPadding(0, 30, 0, 30);

        return wiersz;
    }

    public View getDayForecastLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.day_forecast, null, false);

        return layout;
    }

    public TableRow getWeatherRowDate(String time) {
        TableRow woiersz = createRow();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);

            Calendar kalendarz = Calendar.getInstance();
            kalendarz.setTime(date);

            TextView textView = new TextView(this);
            textView.setText(new SimpleDateFormat("dd-MM-yyyy").format(kalendarz.getTime()));

            woiersz.addView(textView);

            return woiersz;

        } catch (ParseException e) {
            System.out.println(e.toString());
        }

        return null;
    }

    class DataRetriever extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url;

                if (strings.length == 1) {
                    url = new URL(api_url + api_url_miasto_id + strings[0] + api_url_jednostki_metryczne + klucz_api);
                }
                else if (strings.length == 2) {
                    url = new URL(api_url + api_url_koordynaty_lat + strings[0] + api_url_koordynaty_lon + strings[1] + api_url_jednostki_metryczne + klucz_api);
                }
                else {
                    return null;
                }


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String linia;
                    while ((linia = bufferedReader.readLine()) != null) {
                        stringBuilder.append(linia).append("\n");
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

        @Override
        protected void onPostExecute(String response) {
            if(response == null) {
                Toast.makeText(getApplicationContext(), "There was an error.", Toast.LENGTH_SHORT).show();
                return;
            }

            getData(response);
        }

        private boolean checkNewRowHour(String date) throws ParseException {
            Date pelnaData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);

            Calendar kalendarz = Calendar.getInstance();
            kalendarz.setTime(pelnaData);

            return kalendarz.get(Calendar.HOUR_OF_DAY) == 0;
        }

        private void getData(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);

                int numerLini = jsonObject.getInt(ForecastConstants.cnt);

                JSONArray jsonArray = jsonObject.getJSONArray(ForecastConstants.lista);

                TableRow wierszz = createRow();

                for (int i = 0; i < numerLini; i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    String data = json.getString(ForecastConstants.tekst_dt);

                    if (i == 0) {
                        tabela_prognozy.addView(getWeatherRowDate(data));
                    }

                    if (checkNewRowHour(data) && (i != 0)) {
                        tabela_prognozy.addView(wierszz);
                        tabela_prognozy.addView(getWeatherRowDate(data));
                        wierszz = createRow();
                    }

                    String temperatura = json.getJSONObject(ForecastConstants.glowne).getString(ForecastConstants.temperatura);
                    String ikona = json.getJSONArray(ForecastConstants.pogoda).getJSONObject(0).getString(ForecastConstants.ikona_id);
                    String czas = json.getString(ForecastConstants.tekst_dt);

                    wierszz.addView(getLayoutWithData(czas, ikona, temperatura));
                }

                tabela_prognozy.addView(wierszz);

            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage(), e);

            } catch (ParseException e) {
                Log.e("ERROR", e.getMessage(), e);
            }
        }

        private View getLayoutWithData(String time, String icon, String temp) {
            View layout = getDayForecastLayout();

            TextView czsTextview = layout.findViewById(R.id.czas_prognozy_textView);
            ImageView prognozaImageVieww = layout.findViewById(R.id.prognoza_ikona_imageView);
            TextView temperaturaTextView = layout.findViewById(R.id.prognoza_temp_textView);

            try {
                Calendar kalendarz = Calendar.getInstance();
                Date pierwszaData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
                kalendarz.setTime(pierwszaData);

                czsTextview.setText(new SimpleDateFormat("HH:mm").format(kalendarz.getTime()));
            } catch (ParseException e) {
                System.out.println(e.toString());
            }

            prognozaImageVieww.setImageDrawable(getIcon(icon));
            temperaturaTextView.setText(String.format("%.0f", Double.parseDouble(temp)) + CurrentWeatherConstants.jednostka_temp);

            return layout;
        }

        private Drawable getIcon(String name) {
            String nazwaIkona = "icon_" + name;
            int idIkony = getResources().getIdentifier(nazwaIkona, "drawable", getPackageName());
            return getResources().getDrawable(idIkony);
        }
    }
}
