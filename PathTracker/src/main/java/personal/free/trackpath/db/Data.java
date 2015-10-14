package personal.free.trackpath.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Store the position information.
 *
 * Created by r-k- on 14/10/15.
 */
@DatabaseTable()
public class Data {
    @DatabaseField(generatedId = true,dataType = DataType.LONG)
    private long Id;
    @DatabaseField(dataType = DataType.DATE_LONG, canBeNull = false)
    private Date positionTime;
    @DatabaseField(dataType = DataType.DOUBLE, canBeNull = false)
    private double latitude;
    @DatabaseField(dataType = DataType.DOUBLE, canBeNull = false)
    private double longitude;
    @DatabaseField(dataType = DataType.DOUBLE, canBeNull = false)
    private double altitude;

    public Data() {
        positionTime = new Date();
    }

    public Data(double latitude, double longitude, double altitude) {
        positionTime = new Date();
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public Date getPositionTime() {
        return positionTime;
    }

    public void setPositionTime(Date positionTime) {
        this.positionTime = positionTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return "Data{" +
                "Id=" + Id +
                ", positionTime=" + positionTime +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                '}';
    }
}
