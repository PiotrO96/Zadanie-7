package com.example.ps7_oltarzewski_piotr;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String nazwa_bazy = "cities_db.db";
    private static final String nazwa_tabeli = "CITIES_LIST";
    private static String sciezka_bazy = "";

    private SQLiteDatabase mBazaDanych;
    private final Context mKontekts;

    public DatabaseHelper(Context context) {
        super(context, nazwa_bazy, null, 1);

        if(android.os.Build.VERSION.SDK_INT >= 17){
            sciezka_bazy = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            sciezka_bazy = "/data/data/" + context.getPackageName() + "/databases/";
        }

        this.mKontekts = context;
    }

    public void createDatabase() throws IOException
    {
        //Jesli baza nie istnieje skopiuj z assetow.

        boolean mIstnienieBazy = checkDataBase();
        if(!mIstnienieBazy)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Kopiowanie bazy z assetow
                copyDataBase();

            }
            catch (IOException mIOException)
            {
                System.out.println(mIOException.toString());
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + nazwa_tabeli);

        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkDataBase()
    {
        File sciezkaBazy = new File(sciezka_bazy + nazwa_bazy);
        return sciezkaBazy.exists();
    }

    private void copyDataBase() throws IOException
    {
        InputStream mWejscie = mKontekts.getAssets().open(nazwa_bazy);
        String mWyjsciePlik = sciezka_bazy + nazwa_bazy;
        OutputStream mWyjscie = new FileOutputStream(mWyjsciePlik);
        byte[] mBufer = new byte[1024];
        int mDlugosc;
        while ((mDlugosc = mWejscie.read(mBufer))>0)
        {
            mWyjscie.write(mBufer, 0, mDlugosc);
        }
        mWyjscie.flush();
        mWyjscie.close();
        mWejscie.close();
    }

    //Otworz baze danych zeby mozna wykonac bylo zapytanie
    public boolean openDataBase() throws SQLException
    {
        String mPath = sciezka_bazy + nazwa_bazy;

        mBazaDanych = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);

        return mBazaDanych != null;
    }

    @Override
    public synchronized void close()
    {
        if(mBazaDanych != null)
            mBazaDanych.close();
        super.close();
    }
}
