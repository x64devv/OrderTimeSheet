package com.threeklines.ordertimesheet.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.threeklines.ordertimesheet.R;
import com.threeklines.ordertimesheet.adapters.ProcessedAdapter;
import com.threeklines.ordertimesheet.entities.DB;
import com.threeklines.ordertimesheet.entities.Order;
import com.threeklines.ordertimesheet.entities.OrderProcess;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProcessed#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProcessed extends Fragment {

    RecyclerView processedRecycler;
    ArrayList<OrderProcess> processes;
    ArrayList<OrderProcess> completeProcesses;
    ArrayList<Order> orders;

    public FragmentProcessed() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentProcessed.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentProcessed newInstance(String param1, String param2) {
        FragmentProcessed fragment = new FragmentProcessed();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_processed, container, false);
        processes = DB.getInstance(getContext()).getAllProcess();
        orders = DB.getInstance(getContext()).getAllOrders();
        completeProcesses = new ArrayList<>();

        for (OrderProcess op: processes){
            if (!(op.getEndTime() == 0)){
                completeProcesses.add(op);
            }
        }
        Collections.sort(orders, (order, t1) -> {
            int val = order.getOrderNumber().compareTo(t1.getOrderNumber());
            if (val < 0) return -1;
            else if (val == 0) return 0;
            return 1;
        });
        processedRecycler = layout.findViewById(R.id.list_processed);
        processedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ProcessedAdapter adapter = new ProcessedAdapter(getContext(), completeProcesses, orders);
        processedRecycler.setAdapter(adapter);
        return layout;
    }


}