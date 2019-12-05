package edu.ivytech.crimetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static edu.ivytech.crimetracker.CrimeDbSchema.*;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.name,null,values);
    }
    public void updateCrime(Crime c) {
        String uuidString = c.getmId().toString();
        ContentValues values = getContentValues(c);
        mDatabase.update(CrimeTable.name, values,CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper wrapper;
        wrapper = queryCrimes(CrimeTable.Cols.UUID + " = ?",new String[]{id.toString()});
        try {
            if(wrapper.getCount() == 0) {
                return null;
            }
            wrapper.moveToFirst();
            return wrapper.getCrime();
        } finally {
            wrapper.close();
        }

    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(CrimeTable.name,
                null,whereClause,whereArgs,null, null, null);
        return new CrimeCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID,crime.getmId().toString());
        values.put(CrimeTable.Cols.TITLE,crime.getmTitle());
        values.put(CrimeTable.Cols.DATE,crime.getmDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.ismSolved()? 1 : 0);
        return values;
    }

}
