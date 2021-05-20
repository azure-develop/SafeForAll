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

    public void track(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showAlert();
            }else {
                requestPermissions(mActivity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
            }
        }else{
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0,  new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        locationManager.removeUpdates(this);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        locat = String.format("Latitude: %f Longitude: %f", latitude,longitude);
                        Log.d(TAG, String.format("%f + %f", latitude, longitude));
                        latLng = new LatLng(latitude, longitude);

                    }
                });
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        locationManager.removeUpdates(this);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        locat = String.format("Latitude: %f Longitude: %f", latitude,longitude);
                        latLng = new LatLng(latitude, longitude);

                    }
                });
            }
        }
    }

    private void showAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
        alertDialog.setTitle("Permission Request");
        alertDialog.setMessage("Location Permissions are needed for application to work, if user doesn't allow permissions the app will close");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                alertDialog.dismiss();
                mActivity.finishAffinity();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                alertDialog.dismiss();
                requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
                Log.d(TAG, "dialogs");
                track();
            }
        });

        alertDialog.show();
    }

}
