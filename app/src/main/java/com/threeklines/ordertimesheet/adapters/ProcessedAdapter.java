package com.threeklines.ordertimesheet.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.threeklines.ordertimesheet.R;
import com.threeklines.ordertimesheet.entities.Constants;
import com.threeklines.ordertimesheet.entities.DB;
import com.threeklines.ordertimesheet.entities.Order;
import com.threeklines.ordertimesheet.entities.OrderProcess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.threeklines.ordertimesheet.entities.Constants.getLastCompleted;
import static com.threeklines.ordertimesheet.entities.Constants.getLastOrder;

public class ProcessedAdapter extends RecyclerView.Adapter<ProcessedAdapter.ViewHolder> {
    ArrayList<OrderProcess> processes;
    ArrayList<Order> orders;
    Context context;

    public ProcessedAdapter(Context context, ArrayList<OrderProcess> processes, ArrayList<Order> orders) {
        this.processes = processes;
        this.orders = orders;
        this.context = context;
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
            Order order = orderBinarySearch(orders, orderProcess.getIdentifier());
            holder.orderNumber.setText(orderProcess.getIdentifier());
            if (order != null) holder.orderDescription.setText(order.getDescription());
            else holder.orderDescription.setText(R.string.no_desc);
            holder.processedDate.setText(formatDate(orderProcess.getStartTime()));
            holder.timeSpent.setText(formatElapsed(calculateElapsedTime(orderProcess.getStartTime(), totalBreaksTime(orderProcess.getBreaks()), orderProcess.getIdentifier())));
            if (orderProcess.getSyncState().equals("false")){
                holder.syncState.setImageResource(R.drawable.ic_sync_done);
            }
            holder.syncState.setImageResource(R.drawable.ic_synchornise);
        }
    }

    @Override
    public int getItemCount() {
        String lastEntry = getLastCompleted(context);
        if (!lastEntry.equals("zero")) {
            boolean flag = true;
            for (OrderProcess op : processes) {
                if (op.getIdentifier().equals(lastEntry)) {
                    flag = false;
                }
            }
            if (flag) {
                ArrayList<OrderProcess> temp = DB.getInstance(context).getAllProcess();
                for (OrderProcess o: temp ){
                    if(o.getIdentifier().equals(lastEntry) && o.getEndTime() != 0){
                        processes.add(o);
                    }
                }
            }
        }
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
    private long totalBreaksTime(String breaks) {
        long totalTime = 0;
        if (breaks != null && !breaks.equals("")) {
            Log.d("ProcessingAdapter", "totalBreaksTime: breaks: " + breaks);
            String[] fullBreaks = breaks.split("#");
            for (String fullBreak : fullBreaks) {
                String[] breakInfo = fullBreak.split("-");
                if (breakInfo[0].equals("")) continue;
                long bStart = Long.parseLong(breakInfo[0]);
                long bEnd = Long.parseLong(breakInfo[1]);
                long breakDuration = bEnd - bStart;
                totalTime += breakDuration;
            }
        }
        return totalTime;
    }

    private long calculateElapsedTime(long startTime, long breaksDuration, String identifier) {
        long currentTime = System.currentTimeMillis();
        boolean isPaused = Constants.getCurrentOrderState(context, identifier);
        long grossTime = currentTime - startTime;
        long netTime = grossTime - breaksDuration;
        if (isPaused) {
            long breakStart = Constants.getBreakStart(context, identifier);
            long currentBreak = currentTime - breakStart;
            netTime -= currentBreak;
            return TimeUnit.MILLISECONDS.toMinutes(netTime);
        }

        return TimeUnit.MILLISECONDS.toMinutes(netTime);
    }

    private String formatDate(long startTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM, hh:mm", Locale.ENGLISH);
        return "Started\n" + simpleDateFormat.format(new Date(startTime));

    }


    private String formatElapsed(long minutes) {
        if (minutes < 60) {
            return "Elapsed Time\nh:0, m:" + minutes;
        } else {
            return "Elapsed Time\nh:" + (minutes / 60) + ", m:" + (minutes % 60);
        }
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
            syncState = itemView.findViewById(R.id.sync_state);
        }
    }
}
