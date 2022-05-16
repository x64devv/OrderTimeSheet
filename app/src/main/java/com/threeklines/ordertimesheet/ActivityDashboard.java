package com.threeklines.ordertimesheet;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.threeklines.ordertimesheet.adapters.PagerAdapter;
import com.threeklines.ordertimesheet.entities.Constants;
import com.threeklines.ordertimesheet.entities.DB;
import com.threeklines.ordertimesheet.entities.Order;
import com.threeklines.ordertimesheet.entities.OrderProcess;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;

public class ActivityDashboard extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 pager;
    PagerAdapter pagerAdapter;
    BottomSheetDialog sheetDialog;
    TextView lastLoad;
    int badge = 0;
    String station = "";
    String role = "";
    String filePath = "";
    boolean readGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        tabLayout = findViewById(R.id.tab_layout);
        pager = findViewById(R.id.pager2);

        station = getIntent().getStringExtra("username");
        role = getIntent().getStringExtra("role");

        ((TextView) findViewById(R.id.username)).setText(station);
        ((TextView) findViewById(R.id.role)).setText(role);

        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bsb.setPeekHeight(140);
        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        pager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Running");
                if (badge > 0) {
                    tab.getOrCreateBadge().setNumber(badge);
                } else tab.removeBadge();
            } else tab.setText("Complete");
        }).attach();
    }

    public void clickResponse(View view) {
        if (view.getId() == R.id.new_tx) {

            final String[] strExtraPersonnel = {""};
            sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            View newProcessView = LayoutInflater.from(this).inflate(R.layout.dialog_new_process, findViewById(R.id.dialog_new_process_layout));
            sheetDialog.setContentView(newProcessView);
            MaterialAutoCompleteTextView department = sheetDialog.findViewById(R.id.department);
            MaterialAutoCompleteTextView process = sheetDialog.findViewById(R.id.process);
            TextInputEditText orderNumber = sheetDialog.findViewById(R.id.order_number);
            TextInputEditText extraPersonnel = sheetDialog.findViewById(R.id.personnel);
            TextInputLayout extraPersonnelLayout = sheetDialog.findViewById(R.id.personnel_layout);
            MaterialButton startBtn = sheetDialog.findViewById(R.id.start_process);

            department.setAdapter(new ArrayAdapter<String>(this, R.layout.item_role, loadDeptList()));
            department.setOnItemClickListener((adapterView, view1, i, l) -> {
                process.setAdapter(new ArrayAdapter<String>(this, R.layout.item_role, loadProcessList(i)));
            });

            extraPersonnelLayout.setEndIconOnClickListener(view1 -> {
                String name = extraPersonnel.getText().toString();
                if (name.equals("")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Error!")
                            .setMessage("Name field empty. Enter value and try again.")
                            .setPositiveButton("Ok", null)
                            .show();
                } else {
                    if (strExtraPersonnel[0].equals("")) strExtraPersonnel[0] = name;
                    else strExtraPersonnel[0] += "#" + name;
                }
                extraPersonnel.setText("");
            });

            startBtn.setOnClickListener(view1 -> {
                if (department.getText().toString().equals("") || process.getText().toString().equals("") || orderNumber.getText().toString().equals("")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Only personnel can be left empty. Enter values and try again")
                            .setPositiveButton("Ok", null)
                            .show();
                } else {
                    long timeNow = System.currentTimeMillis();
                    ContentValues values = new ContentValues();
                    values.put("identifier", orderNumber.getText().toString());
                    values.put("station", station);
                    values.put("start_time", timeNow);
                    values.put("process", department.getText().toString());
                    values.put("subprocess", process.getText().toString());
                    values.put("personnel", strExtraPersonnel[0]);
                    if (DB.getInstance(this).insertProcess(values)) {
                        Constants.setLastOrder(this, orderNumber.getText().toString());
                        Toast.makeText(this, "Order Process started!", Toast.LENGTH_SHORT).show();
                        sheetDialog.dismiss();
                    } else {
                        Toast.makeText(this, "Order Process could not be started.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            sheetDialog.show();
        }
        if (view.getId() == R.id.sync_tx) {
            sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            View syncView = LayoutInflater.from(this).inflate(R.layout.dialog_sync, findViewById(R.id.dialog_sync_layout));
            sheetDialog.setContentView(syncView);
            TextView loadOrders = sheetDialog.findViewById(R.id.sync_orders);
            TextView writeToFile = sheetDialog.findViewById(R.id.upload_orders);
            lastLoad = sheetDialog.findViewById(R.id.last_update);
            long ll = Constants.getLastUpdate(this);
            if (ll>0){
                lastLoad.setText(new SimpleDateFormat("MMM, dd, hh:mm", Locale.ENGLISH).format(new Date(ll)));
                lastLoad.setTextColor(Color.GREEN);
            }
            loadOrders.setOnClickListener(view1 -> {

                if (SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    askForAndroid11ReadFilesPermission();
                }

                if (SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "App requires permission to access files. Grant permissions and try again.", Toast.LENGTH_LONG).show();

                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                200);

                    }
                }

                new MaterialFilePicker()
                        // Pass a source of context. Can be:
                        //    .withActivity(Activity activity)
                        //    .withFragment(Fragment fragment)
                        //    .withSupportFragment(androidx.fragment.app.Fragment fragment)
                        .withActivity(this)
                        // With cross icon on the right side of toolbar for closing picker straight away
                        .withCloseMenu(true)
                        // Showing hidden files
                        .withHiddenFiles(true)
                        // Don't apply filter to directories names
                        .withFilterDirectories(false)
                        .withTitle("Select file")
                        .withRequestCode(1234)
                        .start();
            });

            writeToFile.setOnClickListener(view1 -> {
                Toast.makeText(this, "Please wait writing to file.", Toast.LENGTH_SHORT).show();
                ArrayList<OrderProcess> rawProcesses = DB.getInstance(this).getAllProcess();
                ArrayList<Order> rawOrders = DB.getInstance(this).getAllOrders();
                ArrayList<OrderProcess> completeProcesses = new ArrayList<>();
                ArrayList<Order> completeOrders = new ArrayList<>();

                File dir = getExternalFilesDir(null);
                if (!dir.exists()) dir.mkdirs();

                File file = new File(dir.getAbsolutePath()+"/time_sheet_"+new SimpleDateFormat("MMM_dd_HH:mm", Locale.ENGLISH).format(new Date())+".csv");
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file, true));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (OrderProcess rp: rawProcesses){
                    if (rp.getEndTime() != 0){
                        completeProcesses.add(rp);
                    }
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm", Locale.ENGLISH);
                for (OrderProcess co: completeProcesses){
                    try {
                        writer.write(co.getIdentifier()+","+","+","+
                                co.getStation()+","+df.format(new Date(co.getStartTime()))+","+df.format(new Date(co.getEndTime()))+","+co.getDepartment()+","+co.getDeptProcess()+
                                ","+co.getExtraPersonnel()+","+co.getBreaks()+"\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (Order order : rawOrders){
                        if (co.getIdentifier().equals(order.getOrderNumber())){
                            completeOrders.add(order);
//                             try {
//                                writer.write(co.getIdentifier()+","+order.getDescription()+","+order.getQuantity()+","+
//                                co.getStation()+","+co.getStartTime()+","+co.getEndTime()+","+co.getDepartment()+","+co.getDeptProcess()+
//                                ","+co.getExtraPersonnel()+","+co.getBreaks()+"\n");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }

                new AlertDialog.Builder(this)
                        .setTitle("Success")
                        .setMessage("Writing to file complete data saved in\n\n"+file.getAbsolutePath())
                        .show();



            });
            sheetDialog.show();
        }

        if (view.getId() == R.id.profile_tx)
            Toast.makeText(this, "Not Available at the moment.", Toast.LENGTH_SHORT).show();

        if (view.getId() == R.id.so_tx) System.exit(0);

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private void askForAndroid11ReadFilesPermission() {
        try {

            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
            startActivityForResult(intent, 200);
        } catch (Exception e) {
//            myTimber(e)
//            val intent = Intent()
//            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//            startActivityForResult(intent, FILE_PICKER_ANDROID11_REQUEST_CODE)
            Toast.makeText(this, "Pile of turd just dropped", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> loadDeptList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Checking");
        list.add("Distribution");
        list.add("Packing");
        return list;
    }

    private ArrayList<String> loadProcessList(int dept) {
        ArrayList<String> list = new ArrayList<>();
        if (dept == 0) {
            list.add("Sorting");
            list.add("Ticketing");
            list.add("Gunning");
        } else if (dept == 1) {
            list.add("Distributing");
        } else {
            list.add("Packing");
            list.add("Packing & Distributing");
        }
        return list;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Handler handler = new Handler(Looper.getMainLooper());

        if (requestCode == 1234 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            handler.post(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("File")
                        .setMessage("Confirm loading files from \n" + filePath)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            if (loadOrders()) {
                                Toast.makeText(this, "Orders loaded successfully.", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        if (requestCode == 200 && resultCode == RESULT_OK) {
            readGranted = true;
        }

    }

    private boolean loadOrders() {
        File file = new File(filePath);
        if (file.exists()) {
            try {
//                "CREATE TABLE orders (order_number varchar ,item_code varchar , " +
//                        " style_number varchar,description varchar ,quantity integer )"
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] orderInfo = line.split(",");
                    ContentValues values = new ContentValues();
                    values.put("order_number", orderInfo[0]);
                    values.put("item_code", orderInfo[1]);
                    values.put("style_number", orderInfo[2]);
                    values.put("description", orderInfo[3]);
                    values.put("quantity", orderInfo[4]);
                    DB.getInstance(this).insertOrder(values);
                }
                return true;
            } catch (IOException e) {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Data is not properly formatted please check your file and try again.")
                        .setPositiveButton("Ok", null)
                        .show();
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


}