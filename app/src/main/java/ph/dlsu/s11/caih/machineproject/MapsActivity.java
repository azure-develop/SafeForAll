package ph.dlsu.s11.caih.machineproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private SharedPreferences sp;
    private ImageButton ib_logout, ib_sound, ib_sms, ib_edit, ib_email;
    private TextView tv_sms;
    private LinearLayout ll_buttons, ll_texts;
    private final String TAG = "mainMaps";
    private MediaPlayer mp;
    private LocationManager locationManager;
    private double latitude, longitude;
    private String user, email1, email2, phone1, phone2, locat;
    private LocationListener locationListener;
    private final SmsManager sms = SmsManager.getDefault();
    private HandlerThread handlerThread = null;
    private Looper looper = null;

    private final String HEN_EMU_NUM = "+15555215554";
    private static final int REQUEST_CHECK_SETTING = 1010;
    private static final int PERMISSION_REQUEST_FINE_CODE = 1;
    private static final int PERMISSION_REQUEST_SMS_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
    }

    protected void onStart () {
        super.onStart();

        LocationRequest locationRequest = LocationRequest.create().setInterval(5000)
                .setFastestInterval(2000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                }catch (ApiException e){
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTING);

                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });

        ib_sms.setVisibility(View.VISIBLE);
        tv_sms.setVisibility(View.VISIBLE);
        ll_buttons.setWeightSum(5);
        ll_texts.setWeightSum(5);

        tracker();

        sp = getSharedPreferences("safeforall", Context.MODE_PRIVATE);
        user = sp.getString("user", null);
        email1 = sp.getString("email1", null);
        email2 = sp.getString("email2", null);
        phone1 = String.valueOf(sp.getLong("phone1", 0));
        phone2 = String.valueOf(sp.getLong("phone2", 0));

        ib_edit.setOnClickListener(v -> {
            Intent i = new Intent(MapsActivity.this, DetailActivity.class);
            i.putExtra("user", user);
            i.putExtra("location", "maps");
            startActivity(i);
        });

        ib_logout.setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
            alertDialog.setTitle("Logging Out");
            alertDialog.setMessage("Are you sure you want to log out?");

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    alertDialog.dismiss();
                    logout();
                }
            });
            alertDialog.show();
        });

        final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mp = MediaPlayer.create(this, R.raw.shriek);;
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        ib_sound.setOnClickListener(v -> {
            mp.start();
            Log.d(TAG, "mp started");
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                Log.d(TAG, "mp completed");
            }
        });

        ib_email.setOnClickListener(v -> {
            String[] email = {email1, email2};
            String sub= "Safety concern to Emergency Contact";
            String msg = "I feel unsafe, my last location during this email was " + locat +
                    "\n\n\nSent from application SafeForAll by user " + user;
            if(locat == null){
                Toast.makeText(getApplicationContext(), "Email cannot be sent right now, try again", Toast.LENGTH_LONG).show();
            }else {
                JavaMailAPI mail = new JavaMailAPI(this, email, sub, msg);
                mail.execute();
                Toast.makeText(getApplicationContext(), "Email sent successfully", Toast.LENGTH_LONG).show();
            }
        });

        ib_sms.setOnClickListener(v -> {
            String msg = "I feel unsafe, my last location during this text message was " + locat +
                    "\n\n\nSent from application SafeForAll ";

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)){
                    showAlert(Manifest.permission.SEND_SMS);
                }else {
                    Log.d(TAG, "SMS: request perm");
                    requestPermissions(new String[] {Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SMS_CODE);
                }
            }else {
                if(locat == null){
                    Toast.makeText(getApplicationContext(), "SMS cannot be sent right now, try again", Toast.LENGTH_LONG).show();
                }else {
                    try {
                        sms.sendTextMessage(HEN_EMU_NUM, null, msg, null, null);
                        sms.sendTextMessage(phone1, null, msg, null, null);
                        sms.sendTextMessage(phone2, null, msg, null, null);
                        Toast.makeText(getApplicationContext(), "SMS sent successfully", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error occurred please try again", Toast.LENGTH_LONG).show();
                        Log.d(TAG, e.toString());
                    }
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        mp.release();
        Log.d(TAG, "mp released");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
    }

    public void tracker(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showAlert(Manifest.permission.ACCESS_FINE_LOCATION);
            }else {
                Log.d(TAG, "tracker: request perm");
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_CODE);
            }
        }else{
            handlerThread = new HandlerThread("MyHandlerThread");
            handlerThread.start();
            looper = handlerThread.getLooper();
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        locat = String.format("Latitude: %f Longitude: %f", latitude,longitude);
                        Log.d(TAG, String.format("%f + %f", latitude, longitude));
                        LatLng latLng = new LatLng(latitude, longitude);
                        runOnUiThread(()->{
                            mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
                        });
                    }
                }, looper);
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        runOnUiThread(()->{
                            mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
                        });
                    }
                }, looper);
            }
        }
    }

    //all requestPermissions come back to this function
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        switch(requestCode){
            case PERMISSION_REQUEST_FINE_CODE:{
                for(int i = 0, len = permissions.length; i < len; i++){
                    String permission = permissions[i];
                    if(permission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_DENIED){
                        Log.d(TAG, "onRequestPermissionsResult: denied then...");
                        if(shouldShowRequestPermissionRationale(permission)){
                            showAlert(permission);
                        }else {
                            showDialog("Since you have denied permission twice or checked never ask me again. " +
                                    "You now need to go to phone Settings > Privacy > Permission Manager > Location" +
                                    " to allow this app the permissions needed to work " +
                                    "or simply reinstall the app and remember to accept permissions.\n\n\n After clicking 'Understood'" +
                                    " below, the app will close.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            finishAffinity();
                                    }
                                }
                            });
                        }
                    }else if(permission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "onRequestPermissionsResult: fine loc granted now");
                        tracker();
                    }
                }
            }
            case PERMISSION_REQUEST_SMS_CODE:{
                for(int i = 0, len = permissions.length; i < len; i++){
                    String permission = permissions[i];
                    if(permission.equalsIgnoreCase(Manifest.permission.SEND_SMS) && grantResults[i] == PackageManager.PERMISSION_DENIED){
                        Log.d(TAG, "onRequestPermissionsResult: SMS denied then...");
                        if(shouldShowRequestPermissionRationale(permission)){
                            showAlert(permission);
                        }else {
                            showDialog("Since you did not give app the permission to send SMS." +
                                    " The Send SMS feature of the app will be disabled now.\n\n\n After clicking" +
                                    " 'Understood' below, the Send SMS button of the app will be disabled until" +
                                    " you enable permissions in phone Settings > Privacy > Permission Manager" +
                                    " > SMS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            ib_sms.setVisibility(View.GONE);
                                            tv_sms.setVisibility(View.GONE);
                                            ll_buttons.setWeightSum(4);
                                            ll_texts.setWeightSum(4);
                                    }
                                }
                            });
                        }
                    }else if(permission.equalsIgnoreCase(Manifest.permission.SEND_SMS) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "onRequestPermissionsResult: SMS granted now");
                        ib_sms.setVisibility(View.VISIBLE);
                        tv_sms.setVisibility(View.VISIBLE);
                        ll_buttons.setWeightSum(5);
                        ll_texts.setWeightSum(5);
                    }
                }
            }
        }
    }

    private void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage(message)
//                .setPositiveButton("OK", okListener)
                .setNegativeButton("Understood", okListener)
                .create()
                .show();
    }

    private void showAlert(String perm){
        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setTitle("Permission Request");
        Log.d(TAG, "showAlert: "+ perm);
        if(perm.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) || perm.equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            alertDialog.setMessage("Location Permissions are needed for application to work, if user doesn't allow permissions the app will close");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            alertDialog.dismiss();
                            finishAffinity();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            alertDialog.dismiss();
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_CODE);
                        }
                    });
            alertDialog.show();
        }else if(perm.equalsIgnoreCase(Manifest.permission.SEND_SMS)){
            alertDialog.setMessage("SMS Permissions are needed for this feature.");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            alertDialog.dismiss();
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SMS_CODE);
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHECK_SETTING){
            switch (resultCode){
                case Activity.RESULT_OK:
                    Toast.makeText(MapsActivity.this, "Location is turned on", Toast.LENGTH_SHORT).show();
                    tracker();
                    break;
                case Activity.RESULT_CANCELED:
                    showDialog("The app needs device location turned on to function. Please " +
                            "turn on device location to use the app. After clicking 'Understood' " +
                            "below, the app will close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_NEGATIVE:
                                    // proceed with logic by disabling the related features or quit the app.
                                    finishAffinity();
                            }
                        }
                    });
            }
        }
    }

    private void logout(){
        FirebaseAuth.getInstance().signOut();
        sp = getSharedPreferences("safeforall", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
        locationManager.removeUpdates(locationListener);
        Log.d(TAG, "logged out");
        finish();
        startActivity(new Intent(MapsActivity.this, MainActivity.class));
    }

    private void init(){
        ib_edit = findViewById(R.id.ib_edit);
        ib_email = findViewById(R.id.ib_email);
        ib_logout = findViewById(R.id.ib_logout);
        ib_sms = findViewById(R.id.ib_sms);
        ib_sound = findViewById(R.id.ib_sound);
        tv_sms = findViewById(R.id.tv_sms);
        ll_buttons = findViewById(R.id.ll_buttons);
        ll_texts = findViewById(R.id.ll_texts);
    }
}