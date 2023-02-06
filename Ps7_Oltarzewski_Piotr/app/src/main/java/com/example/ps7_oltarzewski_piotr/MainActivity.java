package com.example.ps7_oltarzewski_piotr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.nazwa_miasta;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.opis;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.temperatura;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.pogoda;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.ikona_id;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.min_temp;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.max_temp;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.jednostka_wiatru;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.stopien_wiatru;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.szybkosc_wiatru;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.jednostka_szybkosci_wiatru;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.wiatr;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.jednostka_zachmurzenia;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.zachmurzenie;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.jednostka_cisnienia;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.cisnienie;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.jednostka_temp;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.chmury;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.koordynaty;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.koordynaty_lat;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.koordynaty_lon;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.wilgot;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.jednostka_wilgotnosci;
import static com.example.ps7_oltarzewski_piotr.CurrentWeatherConstants.glowne;


public class MainActivity extends AppCompatActivity {


    private final int PERMISSION_ALL = 1;
    private final int ACCESS_FINE_LOCATION_PERMISSION_KEY = 20;
    private final int ACCESS_COARSE_LOCATION_PERMISSION_KEY = 25;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final String nazwa_pref = "my_prefs";
    private static final String ostatni_json = "last_json";
    private static final String ostanie_miasto_id = "last_city_id";
    private static final String ostatnie_lon_id = "last_lon";
    private static final String ostanie_lat_id = "last_lat";

    private RetrieveData uzyskajData;
    private DataAdapter adapterData;

    private TableLayout parametryTabeli;
    private TableLayout pogodaTabela;
    private NestedScrollView zagniezdzonyView;

    private Button prognozaPrzycisk;

    private TextView miastoNazwaText;
    private ImageView ikonaPogody;
    private TextView tekstTemperatury;
    private TextView pogodaOpisText;

    private TextView wilgotnoscTekst;
    private TextView cisnienieTekst;
    private TextView minimalnaTempTekst;
    private TextView maksymalnaTempTekst;
    private TextView zachmurzenieTekst;
    private TextView koordynatyTekst;
    private TextView szybkoscWiatruTekst;
    private TextView stopienWiatruTeskt;

    private Toolbar pasek;
    private ListView listaView;

    private SimpleCursorAdapter kursorAdapter;

    private LocationManager MenadzerLokalizacji;
    private LocationListener listenerLokalizacji;

    private static final String klucz_api = "0547eb1ce9af96469e2ba1e4a3c1cd8f";
    private static final String url_api = "http://api.openweathermap.org/data/2.5/weather?";
    private static final String url_api_miastoId = "id=";
    private static final String url_api_jednostki_metryczne = "&units=metric&APPID=";
    private static final String url_api_koordynaty_lat = "lat=";
    private static final String url_api_koordynaty_lon = "&lon=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapterData = new DataAdapter(getApplicationContext());
        uzyskajData = new RetrieveData();

        pasek = findViewById(R.id.toolbar);
        setSupportActionBar(pasek);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Aplikacja pogodowa");
        }

        zagniezdzonyView = findViewById(R.id.nest_view);
        parametryTabeli = findViewById(R.id.tabela_parametry);
        pogodaTabela = findViewById(R.id.pogoda_table_layout);
        maksymalnaTempTekst = findViewById(R.id.max_temp_text_view);
        minimalnaTempTekst = findViewById(R.id.min_tem_text_view);
        cisnienieTekst = findViewById(R.id.cisnienie_text_view);
        wilgotnoscTekst = findViewById(R.id.wilgotnosc_text_view);
        zachmurzenieTekst = findViewById(R.id.zachmurzenie_text_view);
        szybkoscWiatruTekst = findViewById(R.id.predkosc_wiatru_text_view);
        stopienWiatruTeskt = findViewById(R.id.kierunek_wiatru_text_view);
        koordynatyTekst = findViewById(R.id.koordynaty_text_view);
        miastoNazwaText = findViewById(R.id.city_name_text_view);
        ikonaPogody = findViewById(R.id.pogoda_ikona_imageView);
        tekstTemperatury = findViewById(R.id.temperatura_textView);
        pogodaOpisText = findViewById(R.id.opis_pogody_textView);
        listaView = findViewById(R.id.list_view);
        prognozaPrzycisk = findViewById(R.id.prognoza_button);

        parametryTabeli.setVisibility(View.INVISIBLE);
        pogodaTabela.setVisibility(View.INVISIBLE);
        prognozaPrzycisk.setVisibility(View.INVISIBLE);

        adapterData.createDatabase();

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        MenadzerLokalizacji = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        listenerLokalizacji = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                uzyskajData = new RetrieveData();
                uzyskajData.execute(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));

                SharedPreferences.Editor editor = getSharedPreferences(nazwa_pref, MODE_PRIVATE).edit();
                editor.putString(ostanie_lat_id, Double.toString(location.getLatitude()));
                editor.putString(ostatnie_lon_id, Double.toString(location.getLongitude()));
                editor.apply();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        pasek.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.app_bar_search_location) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            MenadzerLokalizacji.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, listenerLokalizacji);
                        }
                        else {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                        }
                    }
                }
                return false;
            }
        });

        int[] to = new int[] {
                R.id.city_name_list,
                R.id.lista_koordynatow,
        };

        kursorAdapter = new SimpleCursorAdapter(this, R.layout.city_list_item, adapterData.getAllCities(), adapterData.getColumns(), to, 0);
        listaView.setAdapter(kursorAdapter);

        listaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) listaView.getItemAtPosition(i);
                Map<String, String> cityCoords = adapterData.getCityCoords(cursor);
                String cityID = adapterData.getCityId(cursor);
                String cityLat = cityCoords.get("LAT");
                String cityLon = cityCoords.get("LON");

                SharedPreferences.Editor editor = getSharedPreferences(nazwa_pref, MODE_PRIVATE).edit();
                editor.putString(ostanie_miasto_id, cityID);
                editor.putString(ostanie_lat_id, cityLat);
                editor.putString(ostatnie_lon_id, cityLon);
                editor.apply();

                if(cityID == null) {
                    Toast.makeText(getApplicationContext(), "City not found.", Toast.LENGTH_SHORT).show();
                }
                else {
                    setNestViewVisible();
                    pasek.clearFocus();

                    uzyskajData = new RetrieveData();
                    uzyskajData.execute(cityID);
                    adapterData.close();
                }
            }
        });

        prognozaPrzycisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getBaseContext(), WeatherForecastActivity.class);

                SharedPreferences preferences = getSharedPreferences(nazwa_pref, MODE_PRIVATE);

                if (!preferences.getString(ostanie_lat_id, "").isEmpty() && !preferences.getString(ostatnie_lon_id, "").isEmpty()) {
                    myIntent.putExtra("lat", preferences.getString(ostanie_lat_id, ""));
                    myIntent.putExtra("lon", preferences.getString(ostatnie_lon_id, ""));
                    startActivity(myIntent);
                }
                else if (!preferences.getString(ostanie_miasto_id, "").isEmpty()) {
                    myIntent.putExtra("city_id", preferences.getString(ostanie_miasto_id, ""));
                    startActivity(myIntent);
                }

            }
        });


        listaView.setVisibility(View.GONE);

        if (!getSharedPreferences(nazwa_pref, MODE_PRIVATE).getString(ostatni_json, "").isEmpty()) {
            uzyskajData.execute();
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_PERMISSION_KEY:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "We need the permission to allow app to check your geo coordinates.", Toast.LENGTH_SHORT).show();
                }
                break;

            case ACCESS_COARSE_LOCATION_PERMISSION_KEY:
                if (!(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), "We need the permission to allow app to check your geo coordinates.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



    private void setNestViewVisible() {
        listaView.setVisibility(View.GONE);
        zagniezdzonyView.setVisibility(View.VISIBLE);
        prognozaPrzycisk.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.szukaj_miasta_app_bar);

        SearchView search = (SearchView) searchItem.getActionView();
        search.setIconified(false);
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                menuItem.getActionView().requestFocus();
                setListViewVisible();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                setNestViewVisible();
                return true;
            }
        });

        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListViewVisible();
            }
        });


        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setNestViewVisible();
                return false;
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                setNestViewVisible();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                setListViewVisible();

                kursorAdapter.getFilter().filter(s);
                return false;
            }
        });

        kursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return adapterData.filterByName(charSequence.toString());
            }
        });

        return true;
    }

    private void setListViewVisible() {
        zagniezdzonyView.setVisibility(View.GONE);
        listaView.setVisibility(View.VISIBLE);
        prognozaPrzycisk.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setNestViewVisible();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    class RetrieveData extends AsyncTask<String, Void, String> {

        private String[] mainParameters;
        private String[] windParameters;
        private String[] cloudsParameters;
        private String[] geoCoords;
        private String[] weatherParameters;

        public RetrieveData() {
            mainParameters = new String[] {temperatura, cisnienie, wilgot, min_temp, max_temp};
            windParameters = new String[] {szybkosc_wiatru, stopien_wiatru};
            cloudsParameters = new String[] {zachmurzenie};
            geoCoords = new String[] {koordynaty_lon, koordynaty_lat};
            weatherParameters = new String[] {ikona_id, opis};
        }

        private HashMap<String, String> getData(String response) {
            HashMap<String, String> values = new HashMap<>();

            if(response == null) {
                response = "THERE WAS AN ERROR";
            }

            try {
                JSONObject jsonObject = new JSONObject(response);

                SharedPreferences.Editor editor = getSharedPreferences(nazwa_pref, MODE_PRIVATE).edit();
                editor.putString(ostatni_json, jsonObject.toString());
                editor.apply();

                String value;

                for (String mainParameter : mainParameters) {
                    if(jsonObject.getJSONObject(glowne).has(mainParameter)) {
                        value = jsonObject.getJSONObject(glowne).getString(mainParameter);
                        values.put(mainParameter, value);
                    }
                }

                for (String windParameter : windParameters) {
                    if(jsonObject.getJSONObject(wiatr).has(windParameter)) {
                        value = jsonObject.getJSONObject(wiatr).getString(windParameter);
                        values.put(windParameter, value);
                    }
                }

                for (String cloudsParameter : cloudsParameters) {
                    if(jsonObject.getJSONObject(chmury).has(cloudsParameter)) {
                        value = jsonObject.getJSONObject(chmury).getString(cloudsParameter);
                        values.put(cloudsParameter, value);
                    }
                }

                for (String geoCoord : geoCoords) {
                    if(jsonObject.getJSONObject(koordynaty).has(geoCoord)) {
                        value = jsonObject.getJSONObject(koordynaty).getString(geoCoord);
                        values.put(geoCoord, value);
                    }
                }

                for (String weatherParameter : weatherParameters) {
                    if(jsonObject.getJSONArray(pogoda).getJSONObject(0).has(weatherParameter)) {
                        value = jsonObject.getJSONArray(pogoda).getJSONObject(0).getString(weatherParameter);
                        values.put(weatherParameter, value);
                    }
                }

                if(jsonObject.has(nazwa_miasta)) {
                    value = jsonObject.getString(nazwa_miasta);
                    values.put(nazwa_miasta, value);
                }

            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage(), e);
            }

            return values;

        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                if (strings.length == 0) {
                    return getSharedPreferences(nazwa_pref, MODE_PRIVATE).getString(ostatni_json, "");
                }

                URL url = new URL(url_api + url_api_miastoId + strings[0] + url_api_jednostki_metryczne + klucz_api);

                if (strings.length == 2) {
                    url = new URL(url_api + url_api_koordynaty_lat + strings[0] + url_api_koordynaty_lon + strings[1] + url_api_jednostki_metryczne + klucz_api);
                }

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

        @Override
        protected void onPostExecute(String response) {

            if(response == null) {
                Toast.makeText(getApplicationContext(), "There was an error.", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> map = getData(response);

            parametryTabeli.setVisibility(View.VISIBLE);
            pogodaTabela.setVisibility(View.VISIBLE);
            prognozaPrzycisk.setVisibility(View.VISIBLE);

            wilgotnoscTekst.setText((map.get(wilgot)) != null ? map.get(wilgot) + jednostka_wilgotnosci : "no info");
            cisnienieTekst.setText((map.get(cisnienie)) != null ? map.get(cisnienie) + jednostka_cisnienia : "no info");
            maksymalnaTempTekst.setText((map.get(max_temp)) != null ? String.format("%.1f", Double.parseDouble(map.get(max_temp))) + jednostka_temp : "no info");
            minimalnaTempTekst.setText((map.get(min_temp)) != null ? String.format("%.1f", Double.parseDouble(map.get(min_temp))) + jednostka_temp : "no info");
            zachmurzenieTekst.setText((map.get(zachmurzenie)) != null ? map.get(zachmurzenie) + jednostka_zachmurzenia : "no info");
            szybkoscWiatruTekst.setText((map.get(szybkosc_wiatru)) != null ? map.get(szybkosc_wiatru) + jednostka_szybkosci_wiatru : "no info");
            stopienWiatruTeskt.setText((map.get(stopien_wiatru)) != null ? map.get(stopien_wiatru) + jednostka_wiatru : "no info");
            koordynatyTekst.setText((map.get(koordynaty_lat) != null || map.get(koordynaty_lon) != null) ? "[" + map.get(koordynaty_lat) + ", " + map.get(koordynaty_lon) + "]" : "no info");
            miastoNazwaText.setText((map.get(nazwa_miasta)) != null ? map.get(nazwa_miasta) : "no info");
            ikonaPogody.setImageDrawable(getIcon(map.get(ikona_id)));
            tekstTemperatury.setText((map.get(temperatura)) != null ? String.format("%.0f", Double.parseDouble(map.get(temperatura))) + jednostka_temp : "no info");
            pogodaOpisText.setText((map.get(opis)) != null ? map.get(opis) : "no info");
        }

        private Drawable getIcon(String name) {
            String iconName = "icon_" + name;
            int iconId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            return getResources().getDrawable(iconId);
        }
    }
}
