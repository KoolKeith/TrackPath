package personal.free.trackpath;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import personal.free.trackpath.db.DBHelper;
import personal.free.trackpath.db.Data;

/**
 * Monitor GPS and update internal information on elapsed path.
 *
 */
public class TrackerService extends Service implements LocationListener {
    private final IBinder mBinder = new LocalBinder();

    private boolean isRunning = false;
    private static String outputFile = "/storage/sdcard/Download/a.xml";

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private LocationManager locationManager;
    private Date startTime;
    private long timeDiff;
    private double totPath;
    private double minAlt;
    private double maxAlt;
    private double altD;
    private Location lastLocation;
    private DBHelper dbHelper;

    public static void setOutputFile(String outputFile) {
        TrackerService.outputFile = outputFile;
    }

    public static String getOutputFile() {
        return outputFile;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public class LocalBinder extends Binder {
        TrackerService getService() { return TrackerService.this; }
    }

    double getTotPath() { return totPath; }
    long getTimeDiff() { return timeDiff; }
    double getAltD() { return altD; }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startTime = new Date();
        totPath = 0.0;
        minAlt = 1e30;
        maxAlt = -1e30;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0L, this);
        isRunning = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // TODO Store path info to db.
//
//            }
//        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        String txt = locationString(location);
        Log.v("onLocationChanged", txt);
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();

        if (lastLocation != null) {
            double haversineDist = haversine(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude());
            totPath += haversineDist;
        }
        Date curDate = new Date();
        timeDiff = curDate.getTime() - startTime.getTime();
        if (location.getAltitude() < minAlt)
            minAlt = location.getAltitude();
        if (location.getAltitude() > maxAlt)
            maxAlt = location.getAltitude();
        altD = (maxAlt - minAlt) / 1000.0;

        lastLocation = location;

        Data tableDataItem = new Data(location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());

        DBHelper.getDB().AddItem(tableDataItem);
        writePosition(location, curDate);
    }

    public static void writePosition(Location location, Date curDate) {
        File aFile = new File(outputFile);
        Document doc;
        try {
            if (aFile.exists()) {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file://"+outputFile);
            } else {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.createElement("positions"));

            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            Log.e("onLocationChanged", e.getMessage());
            return;
        }

        // Update archive xml
        Element root = doc.getDocumentElement();
        Element pos = doc.createElement("position");
        pos.setAttribute("date", sdf.format(curDate));
        pos.setAttribute("lon", Double.toString(location.getLongitude()));
        pos.setAttribute("lat", Double.toString(location.getLatitude()));
        pos.setAttribute("alt", Double.toString(location.getAltitude()));
        root.appendChild(pos);

        try {
            TransformerFactory.newInstance().newTransformer()
                    .transform(new DOMSource(doc), new StreamResult(outputFile));
        } catch (TransformerException e) {
            Log.e("onLocationChanged", e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private String locationString(Location location) {
        return (location==null)?"Unknown"
                :"Lat: "+location.getLatitude()
                +"  Lon: "+location.getLongitude()
                +"  Alt: "+location.getAltitude();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double EarthR = 6372.8; // In kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EarthR * c;
    }
}
