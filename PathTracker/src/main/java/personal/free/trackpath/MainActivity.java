package personal.free.trackpath;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import personal.free.trackpath.db.DBHelper;
import personal.free.trackpath.db.Data;
import personal.free.trackpath.db.Paths;

/**
 * A simple main window with the initialization button and position visualization.
 *
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static MainActivity activity;
    public static MainActivity getActivity() {
        return activity;
    }

    private TrackerService mTrackerService;
    private boolean mBound = false;
    private Timer timer;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat tdf = new SimpleDateFormat("HH:mm:ss");
    private final NumberFormat nf = new DecimalFormat("#0.000");
    private final NumberFormat nfm = new DecimalFormat("#0.##");
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        new DBHelper(this);

        intent = new Intent(this.getApplicationContext(), TrackerService.class);
        setContentView(R.layout.activity_main);
        //findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (mTrackerService != null && mTrackerService.isRunning()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.textView3)).setText(nf.format(mTrackerService.getTotPath()));
                            ((TextView) findViewById(R.id.textView5)).setText(nfm.format(mTrackerService.getAltD()));
                            ((TextView) findViewById(R.id.textView7)).setText(tdf.format(new Date(mTrackerService.getTimeDiff())));
                        }
                    });
                }
            }
        };

        timer.schedule(timerTask, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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
            // Custom dialog to specify the output file location.
            SettingsDialog settingsDialog = new SettingsDialog(this);
            settingsDialog.setOutFolder(TrackerService.getOutputFile());
            settingsDialog.show();

            return true;
        } else if (id == R.id.action_history) {
            setContentView(R.layout.paths_view);
            PathsAdapter pathsAdapter = new PathsAdapter();
            ((ListView) findViewById(R.id.listView)).setAdapter(pathsAdapter);
            ((ListView) findViewById(R.id.listView)).setOnItemClickListener(pathsAdapter);
            Toast.makeText(this, "Show history", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showMain(View v) {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {

        if (((Button) findViewById(R.id.button)).getText().toString().equals("Start")) {
            // Start monitor
            ((Button) findViewById(R.id.button)).setText(R.string.stopLabel);
            DBHelper.getDB().AddItem(new Paths(new Date()));
            startTimer();
            mBound = bindService(intent, mConnection, BIND_AUTO_CREATE);
        } else {
            ((Button) findViewById(R.id.button)).setText(R.string.startLabel);
            stopTimer();
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTrackerService = ((TrackerService.LocalBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mTrackerService = null;
        }
    };

    private final class PathsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private final List<Paths> paths = DBHelper.getDB().getPaths();

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public Object getItem(int position) {
            return paths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return paths.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = new LinearLayout(parent.getContext());

            TextView textView = new TextView(parent.getContext());
            textView.setText(paths.get(position).getStartPathTime().toString());
            layout.addView(textView);

            return layout;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Paths path = paths.get(position);

            // Search all the data items greater than the items' timestamp.
            List<Data> data = DBHelper.getDB().getData();
            // Get next paths boundary timestamp.
            Date endtime = path.getStartPathTime();
            for (Paths p : paths) {
                if (endtime.getTime() < p.getStartPathTime().getTime()
                        && path.getStartPathTime().getTime() < p.getStartPathTime().getTime()) {
                    endtime = p.getStartPathTime();
                }
            }
            boolean isLast = (endtime.getTime() == path.getStartPathTime().getTime());

            List<Data> foundItems = new ArrayList<>();
            for (Data d : data) {
                if (d.getPositionTime().getTime() > path.getStartPathTime().getTime()
                     && (isLast || d.getPositionTime().getTime() < endtime.getTime())) {
                    foundItems.add(d);
                }
            }

            // Show the paths' data information.
            ((ListView) findViewById(R.id.listView)).setAdapter(new LocationsAdapter(foundItems));
        }
    }

    private final class LocationsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private List<Data> pathData;

        public LocationsAdapter(List<Data> pathData) {
            this.pathData = pathData;
        }

        @Override
        public int getCount() {
            return pathData.size();
        }

        @Override
        public Object getItem(int position) {
            return pathData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return pathData.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(parent.getContext());
            textView.setText(pathData.get(position).getPositionTime().toString());
            layout.addView(textView);
            textView = new TextView(parent.getContext());
            textView.setText("Latitude:  "+pathData.get(position).getLatitude());
            layout.addView(textView);
            textView = new TextView(parent.getContext());
            textView.setText("Longitude: "+pathData.get(position).getLongitude());
            layout.addView(textView);
            textView = new TextView(parent.getContext());
            textView.setText("Altitude:   "+pathData.get(position).getAltitude());
            layout.addView(textView);

            return layout;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }
}
