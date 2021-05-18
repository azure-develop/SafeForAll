package ph.dlsu.s11.caih.machineproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private final String TAG = "mainDetail";
    private SharedPreferences sp;
    private EditText et_email1, et_email2, et_phone1, et_phone2;
    private Button btn_save;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();
        init();
    }
    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        user = extras.getString("user");

        btn_save.setOnClickListener(v -> {
            Map<String, Object> details = new HashMap<>();
            details.put("email1", et_email1.getText().toString());
            details.put("email2", et_email2.getText().toString());
            details.put("phone1", Long.parseLong(et_phone1.getText().toString()));
            details.put("phone2", Long.parseLong(et_phone2.getText().toString()));

            db.collection("users").document(user)
                    .set(details)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            sp = getSharedPreferences("safeforall", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("user", user);
                            editor.putString("email1", et_email1.getText().toString());
                            editor.putString("email2", et_email2.getText().toString());
                            editor.putLong("phone1", Long.parseLong(et_phone1.getText().toString()));
                            editor.putLong("phone2", Long.parseLong(et_phone2.getText().toString()));
                            editor.apply();
                        startActivity(new Intent(DetailActivity.this, MapsActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        });
    }

    @Override
    public void onBackPressed(){
        finishAffinity();
    }

    private void init(){
        et_email1  = findViewById(R.id.et_email1);
        et_email2 = findViewById(R.id.et_email2);
        et_phone1 = findViewById(R.id.et_phone1);
        et_phone2 = findViewById(R.id.et_phone2);
        btn_save = findViewById(R.id.btn_save);
    }
}