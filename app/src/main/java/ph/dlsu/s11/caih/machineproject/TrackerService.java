package ph.dlsu.s11.caih.machineproject;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


public class TrackerService extends Service {
    private Handler mHandler = new Handler();
    private final String TAG = "mainService";
    private TrackerUtility trackerUtility;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        trackerUtility = new TrackerUtility(getApplicationContext());
        Log.d(TAG, "onCreate: ");
        // run on another thread
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "tracker thread");
//                trackerUtility.track();
            }
        });

    }
}