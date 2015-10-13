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
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple main window with the initialization button and position visualization.
 *
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private TrackerService mTrackerService;
    private boolean mBound = false;
    private Timer timer;

//    @SuppressLint("SimpleDateFormat")
//    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat tdf = new SimpleDateFormat("HH:mm:ss");
    private final NumberFormat nf = new DecimalFormat("#0.000");
    private final NumberFormat nfm = new DecimalFormat("#0.##");
//    private final String outputFile = "/storage/sdcard/Download/a.xml";
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(this.getApplicationContext(), TrackerService.class);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
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
            // TODO Custom dialog to specify the output file location.
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (((Button) findViewById(R.id.button)).getText().toString().equals("Start")) {
            // Start monitor
            ((Button) findViewById(R.id.button)).setText(R.string.stopLabel);
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
            TrackerService.LocalBinder binder = (TrackerService.LocalBinder) service;
            mTrackerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mTrackerService = null;
        }
    };
}
