package personal.free.trackpath.db;

/**
 * Database management methods.
 *
 * Created by r-k- on 13/10/15.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Database related methods and callbacks.
 *
 * Created by r-k- on 23/06/15.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

    public static final String DB_NAME = "tracks";
    public static final int DB_VERSION = 1;

    private static DBHelper dbHelper;
    public static DBHelper getDB() { return dbHelper; }

    private ConnectionSource source;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        dbHelper = this;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        if (connectionSource == null) {
            try {
                this.source = new JdbcConnectionSource("jdbc:sqlite:"+DB_NAME);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else
            source = connectionSource;
        createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        if (connectionSource == null) {
            try {
                this.source = new JdbcConnectionSource("jdbc:sqlite:"+DB_NAME);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else
            source = connectionSource;
        // TODO Backup tables ?
        try {
            TableUtils.dropTable(source, Data.class, true);
            TableUtils.dropTable(source, Paths.class, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createTables();
    }

    /**
     * Add a new item to the Paths table.
     * @param item the values to be inserted.
     * @return True on success.
     */
    public boolean AddItem(Paths item) {
        Dao.CreateOrUpdateStatus result = getRuntimeExceptionDao(Paths.class).createOrUpdate(item);
        return  result.isCreated() || result.isCreated();
    }

    public boolean AddItem(Data item) {
        Dao.CreateOrUpdateStatus result = getRuntimeExceptionDao(Data.class).createOrUpdate(item);
        return  result.isCreated() || result.isCreated();
    }

    public List<Data> getData() {
        return getRuntimeExceptionDao(Data.class).queryForAll();
    }

    public List<Paths> getPaths() {
        return getRuntimeExceptionDao(Paths.class).queryForAll();
    }

    private void createTables() {
        try {
            TableUtils.createTableIfNotExists(source, Data.class);
            TableUtils.createTableIfNotExists(source, Paths.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
