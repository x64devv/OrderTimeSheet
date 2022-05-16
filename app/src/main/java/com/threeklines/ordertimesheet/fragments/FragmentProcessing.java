package com.threeklines.ordertimesheet.fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.threeklines.ordertimesheet.R;
import com.threeklines.ordertimesheet.adapters.ProcessingAdapter;
import com.threeklines.ordertimesheet.entities.DB;
import com.threeklines.ordertimesheet.entities.Order;
import com.threeklines.ordertimesheet.entities.OrderProcess;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProcessing#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProcessing extends Fragment {
    ArrayList<OrderProcess> processes;
    ArrayList<Order> orders;
    ArrayList<OrderProcess> incompleteProcesses;
    RecyclerView processesRecycler;

    public FragmentProcessing() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentProcessing.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentProcessing newInstance(String param1, String param2) {
        FragmentProcessing fragment = new FragmentProcessing();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processes = DB.getInstance(getContext()).getAllProcess();
        orders = DB.getInstance(getContext()).getAllOrders();
        incompleteProcesses = new ArrayList<>();

        for (OrderProcess op : processes){
            if (op.getEndTime() == 0){
                incompleteProcesses.add(op);
            }
        }

        Collections.sort(orders, (order, t1) -> {
            int val = order.getOrderNumber().compareTo(t1.getOrderNumber());
            if (val < 0) return -1;
            else if (val == 0) return 0;
            return 1;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_processing, container, false);
        processesRecycler = layout.findViewById(R.id.list_processing);
        processesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ProcessingAdapter adapter = new ProcessingAdapter(getContext(), incompleteProcesses, orders);
        processesRecycler.setAdapter(adapter);
        return layout;
    }
}