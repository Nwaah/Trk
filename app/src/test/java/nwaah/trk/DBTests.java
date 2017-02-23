package nwaah.trk;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DBTests {
    @Mock Context context;
    DatabaseHelper db;
    @Mock
    SQLiteDatabase sql;

    @Before
    public void prepare()
    {
        db = new DatabaseHelper(context);
    }


}
