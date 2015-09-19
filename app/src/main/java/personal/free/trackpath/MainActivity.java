package personal.free.trackpath;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A simple main window with the initialization button and position visualization.
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private final SimpleDateFormat tdf = new SimpleDateFormat("HH:mm:ss");
    private final NumberFormat nf = new DecimalFormat("#0.000");
    private final NumberFormat nfm = new DecimalFormat("#0.##");
    private String outputFile = "/storage/sdcard/Download/a.xml";

    private Date startTime;
    private double totPath;
    private double minAlt;
    private double maxAlt;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.button)).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // TODO Custom dialog to specify the output file location.
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (((Button) findViewById(R.id.button)).getText().toString().equals("Start")) {
            // Start monitor
            ((Button) findViewById(R.id.button)).setText("Stop");

            startTime = new Date();
            totPath = 0.0;
            minAlt = 1e30;
            maxAlt = -1e30;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0L, this);
        } else {
            ((Button) findViewById(R.id.button)).setText("Start");

            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String txt = locationString(location);
        Log.v("onLocationChanged", txt);
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
        ((TextView) findViewById(R.id.textView)).setText(txt);

        if (lastLocation != null) {
            double haversineDist = haversine(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude());
            totPath += haversineDist;
            ((TextView) findViewById(R.id.textView3)).setText(nf.format(totPath));
        }
        Date curDate = new Date();
        long timeDiff = curDate.getTime() - startTime.getTime();
        ((TextView) findViewById(R.id.textView7)).setText(tdf.format(new Date(timeDiff)));
        if (location.getAltitude() < minAlt)
            minAlt = location.getAltitude();
        if (location.getAltitude() > maxAlt)
            maxAlt = location.getAltitude();
        double altD = (maxAlt - minAlt) / 1000.0;
        ((TextView) findViewById(R.id.textView5)).setText(nfm.format(altD));

        lastLocation = location;

        File aFile = new File(outputFile);
        Document doc;
        try {
            if (aFile.exists()) {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(outputFile);
            } else {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.createElement("positions"));

            }
        } catch (SAXException e) {
            Log.e("onLocationChanged", e.getMessage());
            return;
        } catch (IOException e) {
            Log.e("onLocationChanged", e.getMessage());
            return;
        } catch (ParserConfigurationException e) {
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
        } catch (TransformerConfigurationException e) {
            Log.e("onLocationChanged", e.getMessage());
        } catch (TransformerException e) {
            Log.e("onLocationChanged", e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("onStatusChanged", provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("onProviderEnabled", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("onProviderDisabled", provider);
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
