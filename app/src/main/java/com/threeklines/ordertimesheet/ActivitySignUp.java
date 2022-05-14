package com.threeklines.ordertimesheet;

import android.content.ContentValues;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.threeklines.ordertimesheet.entities.Constants;
import com.threeklines.ordertimesheet.entities.DB;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;

public class ActivitySignUp extends AppCompatActivity {
    TextInputEditText id, username, password, confirmPassword;
    MaterialAutoCompleteTextView role;
    TextInputLayout roleLayout;
    MaterialButton signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        id = findViewById(R.id.emp_number);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        role = findViewById(R.id.role);
        roleLayout = findViewById(R.id.role_layout);
        signUpBtn = findViewById(R.id.btn_su);



        ArrayList<String> roles = new ArrayList<>();
        roles.add("Checking");
        roles.add("Distributing");
        roles.add("Packing");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, R.layout.item_role, roles);
        role.setAdapter(roleAdapter);


        signUpBtn.setOnClickListener(view -> {
            //preparing salt
//            byte[] salt = new byte[16];
//            Constants.SECURE_RANDOM.nextBytes(salt);

            if (confirmPassword.getText().toString().equals("") || password.getText().toString().equals("")){
                new AlertDialog.Builder(this)
                        .setTitle("Error!")
                        .setMessage("Can not leave fields empty, please enter all fields.")
                        .setPositiveButton("Ok", null)
                        .show();
                return;
            }

            String uId = id.getText().toString();
            String uname = username.getText().toString();
            String upass = password.getText().toString();
            String cpass = confirmPassword.getText().toString();
            String urole = role.getText().toString();

            Log.d("PASS", "onCreate: " + upass);
            Log.d("PASS", "onCreate: " + cpass);

            if (uId.equals("") || uname.equals("")  || urole.equals("")){
                new AlertDialog.Builder(this)
                        .setTitle("Error!")
                        .setMessage("Can not leave fields empty, please enter all fields.\n" + upass + "\n" + cpass)
                        .setPositiveButton("Ok", null)
                        .show();
                        
            } else if (!upass.equals(cpass)){
                new AlertDialog.Builder(this)
                        .setTitle("Error!")
                        .setMessage("Passwords do not match. Please check your passwords. and try again.")
                        .setPositiveButton("Ok", null)
                        .show();
            } else {
                ContentValues values = new ContentValues();
                values.put("id", uId);
                values.put("username", uname);
                values.put("password", upass);
                values.put("role", urole);
                if (DB.getInstance(this).insertUser(values)){
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Sign up failed please try again later!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * This is a method that hashes your password using the PBKDF2 hashing algorithm
     * @param password clear text password
     * @return string of the hashed password.
     */
    private String passwordHash(String password, byte[] salt){
        try {
            //initializing the key spec and generating the hash bytes
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.encodeToString(hash,0 );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

}