package ph.dlsu.s11.caih.machineproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

public class TrackerUtility {
    private Context mContext;
    private LocationManager locationManager;
    private Activity mActivity;
    private String locat;
    private LatLng latLng;
    private final String TAG = "mainTrackerUtility";
    private double latitude, longitude;
    private static final int PERMISSION_REQUEST_LOCATION_CODE = 1;

    public TrackerUtility(Context context, Activity activity){
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Service.LOCATION_SERVICE);
        mActivity = activity;
    }
    public TrackerUtility(Context context){
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Service.LOCATION_SERVICE);
    }


}
