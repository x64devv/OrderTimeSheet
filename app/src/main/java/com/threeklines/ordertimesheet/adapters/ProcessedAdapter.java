package com.threeklines.ordertimesheet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.threeklines.ordertimesheet.R;
import com.threeklines.ordertimesheet.entities.Order;
import com.threeklines.ordertimesheet.entities.OrderProcess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProcessedAdapter extends RecyclerView.Adapter<ProcessedAdapter.ViewHolder> {
    ArrayList<OrderProcess> processes;
    ArrayList<Order> orders;

    public ProcessedAdapter(ArrayList<OrderProcess> processes, ArrayList<Order> orders) {
        this.processes = processes;
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_processed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < processes.size()){
            OrderProcess orderProcess = processes.get(position);
            holder.orderNumber.setText(orderProcess.getIdentifier());
            holder.orderDescription.setText(orderBinarySearch(orders, orderProcess.getIdentifier()).getDescription());
            holder.processedDate.setText(formatDate(orderProcess.getStartTime()));
            holder.timeSpent.setText(formatElapsed(TimeUnit.NANOSECONDS.toMinutes(orderProcess.getElapsedTime())));
            if (orderProcess.getSyncState().equals("false")){
                holder.syncState.setImageResource(R.drawable.ic_sync_done);
            }
            holder.syncState.setImageResource(R.drawable.ic_synchornise);
        }
    }

    @Override
    public int getItemCount() {
        return processes.size();
    }

    /**
     * This method does a binary search of the order in the list of orders using the order number
     *
     * @param orders      Arraylist of orders to search from.
     * @param orderNumber String of the order number to retrieve the order.
     * @return returns {@link Order} object with the order number supplied.
     */
    private Order orderBinarySearch(ArrayList<Order> orders, String orderNumber) {
        int start = 0;
        int end = orders.size() - 1;

        while (start <= end) {
            int midPos = (end - start) / 2;

            if (orders.get(midPos).getOrderNumber().compareTo(orderNumber) < 0) {
                start = midPos + 1;
            } else if (orders.get(midPos).getOrderNumber().compareTo(orderNumber) == 0) {
                return orders.get(midPos);
            } else {
                end = midPos - 1;
            }
        }
        return null;
    }

    private String formatDate(long nanoTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM, dd", Locale.ENGLISH);
        return simpleDateFormat.format(new Date(nanoTime));
    }

    private String formatElapsed(long minutes){
        if(minutes < 60) return "0 hrs, " + minutes + " mins";
        return minutes/60 + " hrs, " + minutes%60 + " mins";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderDescription, processedDate, timeSpent;
        ImageView syncState;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.order_number);
            orderDescription = itemView.findViewById(R.id.order_description);
            processedDate = itemView.findViewById(R.id.start_time);
            timeSpent = itemView.findViewById(R.id.time_spent);
        }
    }
}
