package com.threeklines.ordertimesheet;

import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.threeklines.ordertimesheet.entities.Constants;
import com.threeklines.ordertimesheet.entities.DB;
import com.threeklines.ordertimesheet.entities.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class MainActivity extends AppCompatActivity {
    TextInputEditText username;
    TextInputEditText password;
    MaterialButton signInBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signInBtn = findViewById(R.id.btn_si);
        ((TextView) findViewById(R.id.si_link)).setOnClickListener(view -> startActivity(new Intent(this, ActivitySignUp.class)));

        signInBtn.setOnClickListener(view -> {

            if (password.getText().toString().equals("") || username.getText().toString().equals("")){
                new AlertDialog.Builder(this)
                        .setTitle("Failed!")
                        .setMessage("Password field is empty.")
                        .setPositiveButton("Ok", null)
                        .show();
                return;
            }
            String uname = username.getText().toString();
            String pass = password.getText().toString();
            Log.d("PASS", "onCreate: " + pass);
            User user = DB.getInstance(this).userExists(uname, pass );
            if (user != null) {
                Intent intent = new Intent(this, ActivityDashboard.class);
                intent.putExtra("username", uname);
                intent.putExtra("role", user.getRole());
                startActivity(intent);
                finish();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Failed!")
                        .setMessage("Signing in failed check your details and try again.")
                        .setPositiveButton("Ok", null)
                        .show();
                password.setText("");
            }
        });
    }

    /**
     * This is a method that hashes your password using the PBKDF2 hashing algorithm
     * @param password clear text password
     * @return string of the hashed password.
     */
    private String passwordHash(String password){
        try {
            //preparing salt


            //initializing the key spec and generating the hash bytes
            KeySpec spec = new PBEKeySpec(password.toCharArray(), Constants.SALT, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.encodeToString(hash,0 );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
}