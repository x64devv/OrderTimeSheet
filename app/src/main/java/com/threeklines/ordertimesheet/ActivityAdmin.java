package com.threeklines.ordertimesheet;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityAdmin extends AppCompatActivity {

    TextView saveToFile, uploadFile, lastUpload, lastSave, startServer, serverStatus;
    RecyclerView connectedList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        uploadFile = findViewById(R.id.upload_orders);
        saveToFile = findViewById(R.id.save_orders);
        lastUpload = findViewById(R.id.last_upload);
        lastSave = findViewById(R.id.last_update);
        startServer = findViewById(R.id.start_server);
        serverStatus = findViewById(R.id.server_status);
        connectedList = findViewById(R.id.connected_devices);

        connectedList.setLayoutManager(new LinearLayoutManager(this));

    }
}