package personal.free.trackpath.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Store paths start timestamp.
 *
 * Created by r-k- on 14/10/15.
 */
@DatabaseTable()
public class Paths {
    @DatabaseField(generatedId = true,dataType = DataType.LONG)
    private long Id;
    @DatabaseField(dataType = DataType.DATE_TIME, canBeNull = false)
    private long StartPathTime;

    public Paths() {
    }

    public Paths(long startPathTime) {
        StartPathTime = startPathTime;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public long getStartPathTime() {
        return StartPathTime;
    }

    public void setStartPathTime(long startPathTime) {
        StartPathTime = startPathTime;
    }

    @Override
    public String toString() {
        return "Paths{" +
                "Id=" + Id +
                ", StartPathTime=" + StartPathTime +
                '}';
    }
}
