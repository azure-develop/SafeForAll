package ph.dlsu.s11.caih.machineproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sp;
    private final String TAG = "main";
    private ImageView iv_logo;
    private Button btn_register, btn_login, btn_confirm;
    private TextView tv_first;
    private EditText et_email, et_password;
    private ProgressBar pb_load;
    private ImageButton ib_back;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        btn_register.setOnClickListener(v -> {
            tv_first.setText("Register");
            changeUI();
        });

        btn_login.setOnClickListener(v -> {
            tv_first.setText("Log In");
            changeUI();
        });

        btn_confirm.setOnClickListener(v -> {
            email = et_email.getText().toString().trim();
            password = et_password.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(getApplicationContext(), "Do not leave any fields blank", Toast.LENGTH_SHORT).show();
            }else if(!(Patterns.EMAIL_ADDRESS.matcher(email).matches())){
                Toast.makeText(getApplicationContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            }else if (password.length() < 8){
                Toast.makeText(getApplicationContext(), "Password too short, minimum of 8 characters!", Toast.LENGTH_SHORT).show();
            }else if(tv_first.getText().toString().equalsIgnoreCase("Register")) {
                pb_load.setVisibility(View.VISIBLE);
                register();
            }else{
                pb_load.setVisibility(View.VISIBLE);
                login();
            }
        });

        ib_back.setOnClickListener(v -> {
            btn_confirm.setVisibility(View.GONE);
            tv_first.setVisibility(View.GONE);
            et_email.setVisibility(View.GONE);
            et_password.setVisibility(View.GONE);
            pb_load.setVisibility(View.GONE);
            ib_back.setVisibility(View.GONE);

            iv_logo.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.VISIBLE);
        });

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){        //firebase keeps user logged in app until uninstall or sign out function
            Toast.makeText(MainActivity.this, "Currently logged in as " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
            Log.d(TAG, currentUser.getEmail());
            pb_load.setVisibility(View.VISIBLE);
            //function query and check if said user has details or not, if no details then redirect to detail activity
            loggedIn(currentUser.getEmail());
        }
    }

    private void register(){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pb_load.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.d(TAG, "createUserWithEmail:success " + user.getEmail());
                    Intent i = new Intent(MainActivity.this, DetailActivity.class);
                    i.putExtra("user", email);
                    i.putExtra("location", "main");
                    finish();
                    startActivity(i);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure ", task.getException());
                    if(task.getException().getMessage().equalsIgnoreCase("The email address is already in use by another account.")){
                        Toast.makeText(MainActivity.this, "Email already used", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                }
                // ...
            }
        });
    }

    private void login(){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pb_load.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success ");
                    loggedIn(email);
                } else if(task.getException().getMessage().equalsIgnoreCase("An internal error has occurred. [ Connection reset ]")){
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure ", task.getException());
                    Toast.makeText(MainActivity.this, "Connection reset, try again", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Email or Password wrong, try again", Toast.LENGTH_LONG).show();
                }
                // ...
            }
        });
    }

    private void loggedIn(String user){
        db.collection("users").document(user).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                String a = String.valueOf(document.getData().get("email1"));
                                String b = String.valueOf(document.getData().get("email2"));
                                String c = String.valueOf(document.getData().get("phone1"));
                                String d = String.valueOf(document.getData().get("phone2"));
                                Log.d(TAG, a+" "+b+" "+c+" "+d);
                                //In case logging in a new device
                                sp = getSharedPreferences("safeforall", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("user", document.getId());     //== email == user
                                editor.putString("email1", a);
                                editor.putString("email2", b);
                                editor.putLong("phone1", Long.parseLong(c));
                                editor.putLong("phone2", Long.parseLong(d));
                                editor.apply();
                                pb_load.setVisibility(View.GONE);
                                finish();
                                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                            } else {
                                Log.d(TAG, "No such document " + user);
                                Toast.makeText(MainActivity.this, "No needed info", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                                i.putExtra("user", user);
                                i.putExtra("location", "main");
                                pb_load.setVisibility(View.GONE);
                                finish();
                                startActivity(i);
                            }
                        } else if(task.getException().getMessage().equalsIgnoreCase("Failed to get document because the client is offline.")){
                            Toast.makeText(MainActivity.this, "You are offline, try again later", Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(MainActivity.this, "Login process failed, try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private  void changeUI(){
        iv_logo.setVisibility(View.GONE);
        btn_register.setVisibility(View.GONE);
        btn_login.setVisibility(View.GONE);
        btn_confirm.setVisibility(View.VISIBLE);
        tv_first.setVisibility(View.VISIBLE);
        et_email.setVisibility(View.VISIBLE);
        et_password.setVisibility(View.VISIBLE);
        ib_back.setVisibility(View.VISIBLE);
    }

    private void init(){
        iv_logo = findViewById(R.id.iv_logo);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        tv_first = findViewById(R.id.tv_first);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        pb_load = findViewById(R.id.pb_load);
        ib_back = findViewById(R.id.ib_back);

        btn_confirm.setVisibility(View.GONE);
        tv_first.setVisibility(View.GONE);
        et_email.setVisibility(View.GONE);
        et_password.setVisibility(View.GONE);
        pb_load.setVisibility(View.GONE);
        ib_back.setVisibility(View.GONE);
    }
}