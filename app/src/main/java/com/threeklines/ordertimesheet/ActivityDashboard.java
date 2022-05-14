package com.threeklines.ordertimesheet;

import android.content.ContentValues;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.threeklines.ordertimesheet.adapters.PagerAdapter;
import com.threeklines.ordertimesheet.entities.DB;

import java.util.ArrayList;

public class ActivityDashboard extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 pager;
    PagerAdapter pagerAdapter;
    BottomSheetDialog sheetDialog;
    int badge = 0;
    String station = "";
    String role = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        tabLayout = findViewById(R.id.tab_layout);
        pager = findViewById(R.id.pager2);

        station = getIntent().getStringExtra("username");
        role = getIntent().getStringExtra("role");

        ((TextView)findViewById(R.id.username)).setText(station);
        ((TextView)findViewById(R.id.role)).setText(role);

        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bsb.setPeekHeight(140);
        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        pager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
           if (position == 0) {
               tab.setText("Running");
               if (badge > 0 ){
                   tab.getOrCreateBadge().setNumber(badge);
               }else tab.removeBadge();
           }
           else tab.setText("Complete");
        }).attach();
    }

    public void clickResponse(View view){
        if (view.getId() == R.id.new_tx){

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
                if (name.equals("")){
                    new AlertDialog.Builder(this)
                            .setTitle("Error!")
                            .setMessage("Name field empty. Enter value and try again.")
                            .setPositiveButton("Ok", null)
                            .show();
                } else {
                    if (strExtraPersonnel[0].equals("")) strExtraPersonnel[0] = name;
                    else strExtraPersonnel[0] += "#" + name;
                }
            });

            startBtn.setOnClickListener(view1 -> {
                if (department.getText().toString().equals("") || process.getText().toString().equals("") || orderNumber.getText().toString().equals("")){
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Only personnel can be left empty. Enter values and try again")
                            .setPositiveButton("Ok", null)
                            .show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put("identifier", orderNumber.getText().toString());
                    values.put("station", station);
                    values.put("start_time", System.nanoTime());
                    values.put("process", department.getText().toString());
                    values.put("subprocess", process.getText().toString());
                    values.put("personnel", strExtraPersonnel[0]);
                    if (DB.getInstance(this).insertProcess(values)){
                        Toast.makeText(this, "Order Process started!", Toast.LENGTH_SHORT).show();
                        sheetDialog.dismiss();
                    } else {
                        Toast.makeText(this, "Order Process could not be started.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            sheetDialog.show();
        } if (view.getId() == R.id.sync_tx){
            View syncView = LayoutInflater.from(this).inflate(R.layout.dialog_sync, findViewById(R.id.dialog_sync_layout));
            sheetDialog.setContentView(syncView);
            sheetDialog.show();
        }

        if (view.getId() ==  R.id.profile_tx) Toast.makeText(this, "Not Available at the moment.", Toast.LENGTH_SHORT).show();

        if (view.getId() == R.id.so_tx) System.exit(0);

    }

    private ArrayList<String> loadDeptList(){
        ArrayList<String> list = new ArrayList<>();
        list.add("Checking");
        list.add("Distribution");
        list.add("Packing");
        return list;
    }

    private ArrayList<String> loadProcessList(int dept) {
        ArrayList<String> list = new ArrayList<>();
        if (dept == 0){
            list.add("Sorting");
            list.add("Ticketing");
            list.add("Gunning");
        } else if (dept == 1){
            list.add("Distributing");
        } else {
            list.add("Packing");
            list.add("Packing & Distributing");
        }
        return list;
    }

}