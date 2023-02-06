package com.example.ps7_oltarzewski_piotr;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataAdapter {

    private final Context mKontekst;
    private SQLiteDatabase baza;
    private DatabaseHelper bazaHelper;

    private final String nazwa_tabeli = "CITIES_LIST";

    private static final String kolumna_id = "_id";
    private static final String nazwa_kolumny = "_name";
    private static final String kraj_kolumna = "_country";
    private static final String koordynaty_lon_kolumna = "_coord_lon";
    private static final String koordynaty_lat_kolumna = "_coord_lat";

    public DataAdapter(Context context)
    {
        this.mKontekst = context;
        bazaHelper = new DatabaseHelper(mKontekst);
    }

    public String[] getColumns() {
        String[] columns = new String[] {
                nazwa_kolumny, koordynaty_lat_kolumna, koordynaty_lon_kolumna
        };

        return columns;
    }

    public DataAdapter createDatabase() throws SQLException
    {
        try
        {
            bazaHelper.createDatabase();
        }
        catch (IOException mIOException)
        {
            System.out.println("UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapter open() throws SQLException
    {
        try
        {
            bazaHelper.openDataBase();
            bazaHelper.close();
            baza = bazaHelper.getReadableDatabase();

        }
        catch (SQLException mSQLException)
        {

            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        bazaHelper.close();
    }

    public Cursor getAllCities()
    {
        open();
        try
        {
            String sql = "SELECT * FROM " + nazwa_tabeli;

            Cursor mCur = baza.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            close();
            return mCur;
        }
        catch (SQLException mSQLException)
        {

            close();
            throw mSQLException;
        }
    }

    public Cursor filterByName(String name) {
        open();
        try
        {


            Cursor mCur = baza.query(nazwa_tabeli, new String[] {kolumna_id, nazwa_kolumny, koordynaty_lat_kolumna, koordynaty_lon_kolumna}, nazwa_kolumny + " LIKE ?", new String[] {name + "%"}, null, null, null);

            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            close();
            return mCur;
        }
        catch (SQLException mSQLException)
        {

            close();
            throw mSQLException;
        }
    }

    public String getCityId(Cursor cursor) {

        open();
        boolean isCursorEmpty = true;
        String sql = "SELECT " + kolumna_id + " FROM " + nazwa_tabeli + " WHERE " + nazwa_kolumny + " = ?";

        String cityName = cursor.getString(cursor.getColumnIndex(nazwa_kolumny));

        Cursor crs = baza.rawQuery(sql, new String[] {cityName});

        if (crs != null)
        {
            isCursorEmpty = crs.moveToNext();
        }
        else
        {
            return null;
        }

        if(!isCursorEmpty) {
            return null;
        }

        String response = cursor.getString(cursor.getColumnIndex(kolumna_id));
        System.out.println(response);
        crs.close();

        close();

        return response;
    }

    public Map<String, String> getCityCoords(Cursor cursor) {

        open();
        boolean isCursorEmpty = true;
        String sql = "SELECT " + koordynaty_lon_kolumna + " ," + koordynaty_lat_kolumna + " FROM " + nazwa_tabeli + " WHERE " + nazwa_kolumny + " = ?";

        String cityName = cursor.getString(cursor.getColumnIndex(nazwa_kolumny));

        Cursor crs = baza.rawQuery(sql, new String[] {cityName});

        if (crs != null)
        {
            isCursorEmpty = crs.moveToNext();
        }
        else
        {
            return null;
        }

        if(!isCursorEmpty) {
            return null;
        }

        String lon = cursor.getString(cursor.getColumnIndex(koordynaty_lon_kolumna));
        String lat = cursor.getString(cursor.getColumnIndex(koordynaty_lat_kolumna));

        crs.close();
        close();

        Map<String, String> coords = new HashMap<>();
        coords.put("LON", lon);
        coords.put("LAT", lat);

        return coords;
    }

}
