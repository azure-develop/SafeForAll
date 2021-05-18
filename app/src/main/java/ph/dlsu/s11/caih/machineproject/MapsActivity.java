package ph.dlsu.s11.caih.machineproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    //TODO sms function/button ... maybe make tracker() || locationManager a Service to see if it'll be faster
    //TODO improve email function to accept String[] ... some other small tweaks
    private GoogleMap mMap;
    private SharedPreferences sp;
    private ImageButton ib_logout, ib_sound, ib_sms, ib_edit, ib_email;
    private final String TAG = "mainMaps";
    private MediaPlayer mp;
    private LocationManager locationManager;
    private double latitude, longitude;
    private String user, email1, email2, phone1, phone2, locat;
    private LocationListener locationListener;

    private static final int PERMISSION_REQUEST_LOCATION_CODE = 1;

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
            logout();
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
            String email = email1;
            String sub= "Safety concern to Emergency Contact";
            String msg = "I feel unsafe, my last location during this email was " + locat +
                    "\n\n\n Sent from application SafeForAll by user " + user;
            JavaMailAPI mail = new JavaMailAPI(this, email, sub, msg);
            mail.execute();

            String emaill = email2;
            String subb = "Safety concern to Emergency Contact";
            String msgg = "I feel unsafe, my last location during this email was " + locat +
                    "\n\n\n Sent from application SafeForAll by user " + user;
            mail = new JavaMailAPI(this, emaill, subb, msgg);
            mail.execute();

            Toast.makeText(getApplicationContext(), "Email sent successfully", Toast.LENGTH_LONG).show();
        });

        ib_sms.setOnClickListener(v -> {
            //TODO actual SMS code

            Toast.makeText(getApplicationContext(), "SMS sent successfully", Toast.LENGTH_LONG).show();
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
    }

    private void tracker(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showAlert();
            }else {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
            }
        }else{
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        locat = String.format("Latitude: %f Longitude: %f", latitude,longitude);
                        Log.d(TAG, String.format("%f + %f", latitude,longitude));
                        LatLng latLng = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
                    }
                });
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_REQUEST_LOCATION_CODE:{
                for(int i = 0, len = permissions.length; i < len; i++){
                    String permission = permissions[i];
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        if(shouldShowRequestPermissionRationale(permission)){
                            showAlert();
                        }else {
                            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
                            tracker();
                        }
                    }
                }
            }
        }
    }

    private void showAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setTitle("Permission Request");
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
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
                        tracker();
                    }
                });

        alertDialog.show();
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
    }
}