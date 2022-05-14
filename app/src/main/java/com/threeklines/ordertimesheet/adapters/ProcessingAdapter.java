package com.threeklines.ordertimesheet.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.threeklines.ordertimesheet.R;
import com.threeklines.ordertimesheet.entities.Constants;
import com.threeklines.ordertimesheet.entities.DB;
import com.threeklines.ordertimesheet.entities.Order;
import com.threeklines.ordertimesheet.entities.OrderProcess;
import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProcessingAdapter extends RecyclerView.Adapter<ProcessingAdapter.ViewHolder> {
    ArrayList<OrderProcess> processes;
    ArrayList<Order> orders;
    Context context;

    public ProcessingAdapter(Context context, ArrayList<OrderProcess> processes, ArrayList<Order> orders) {
        this.processes = processes;
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_processing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < processes.size()) {
            OrderProcess orderProcess = new OrderProcess();

            if (orderProcess.getStopwatch() == null) {
                //Setting the stopwatch using the android Ticker to allow it work even if the device is sleeping
                orderProcess.setStopwatch(
                        Stopwatch.createUnstarted(
                                new Ticker() {
                                    public long read() {
                                        return android.os.SystemClock.elapsedRealtimeNanos(); // requires API Level 17
                                    }
                                }));
            }
            Order order = orderBinarySearch(orders, orderProcess.getIdentifier());
            holder.orderNumber.setText(orderProcess.getIdentifier());
            if (order != null) {
                holder.orderDesc.setText(order.getDescription());
            } else holder.orderDesc.setText(R.string.no_desc);

            holder.orderStated.setText(formatDate(orderProcess.getStartTime()));
            holder.orderElapsedTime.setText("--, --");

            holder.toggleElapsed.setOnClickListener(view -> {
                /*
                 if clicked icon is changed and elapsed time from the stopwatch is displayed for 3 seconds and
                 then the time is hidden again.
                 */
                holder.toggleElapsed.setImageResource(R.drawable.ic_visibility_off);
                holder.orderElapsedTime.setText(formatElapsed(orderProcess.getStopwatch().elapsed(TimeUnit.MINUTES)));
                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        holder.toggleElapsed.setImageResource(R.drawable.ic_visibility);
                        holder.orderElapsedTime.setText("--, --");
                    }
                }.start();
            });

            holder.extraPersonnelLayout.setEndIconOnClickListener(view -> {
             /* this is an onclick for the plus icon on the add personnel so when clicked it checks if there is a name
                entered, if exists it updates the extra personnel and if not it alerts the user.
             */
                String morePersonnel = holder.extraPersonnel.getText().toString();
                if (!morePersonnel.equals("")) {
                    String personnel = orderProcess.getExtraPersonnel();
                    String newPersonnel = "";
                    if (personnel.equals("")) {
                        newPersonnel += personnel;
                        orderProcess.setExtraPersonnel(morePersonnel);
                    } else {
                        newPersonnel = personnel + "#" + morePersonnel;
                        orderProcess.setExtraPersonnel(newPersonnel);
                    }


                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Error!")
                            .setMessage("Empty field. Enter Name and try again")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            });

            holder.startBtn.setOnClickListener(view -> {
                boolean isPaused = Constants.getCurrentOrderState(context, orderProcess.getIdentifier());
                if (isPaused) {
                    //retrieving break details
                    long breakStart = Constants.getBreakStart(context, orderProcess.getIdentifier());
                    int reason = Constants.getReason(context, orderProcess.getIdentifier());

                    Constants.deletePref(context, orderProcess.getIdentifier()+"_reason");//removing the reason pref
                    Constants.deletePref(context, orderProcess.getIdentifier()+"_start");//removing the reason pref

                    String newBreaks = "";
                    if (orderProcess.getBreaks().equals("")){
                        newBreaks = breakStart + "-" + System.nanoTime() + "-" + reason;
                    } else newBreaks = "#" + breakStart + "-" + System.nanoTime() + "-" + reason;

                    //updating break values everywhere
                    orderProcess.setBreaks(newBreaks);
                    ContentValues values = new ContentValues();
                    values.put("breaks", newBreaks);
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());

                    holder.startBtn.setIconResource(R.drawable.ic_pause);
                    holder.startBtn.setText(R.string.pause_text);
                    orderProcess.getStopwatch().start();
                    Constants.setCurrentOrderState(context, orderProcess.getIdentifier(), false);
                } else {
                    //Confirm pause alert
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm!")
                            .setMessage("Are you sure you want to pause the timer")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                new AlertDialog.Builder(context)
                                        .setTitle("Break Reason")
                                        .setAdapter(new ArrayAdapter<String>(
                                                context, R.layout.item_role, Constants.OPTIONS), (dialogInterface1, i1)
                                                -> {
                                            if (Constants.setBreakStart(context, orderProcess.getIdentifier(), System.nanoTime(), i1)){
                                                holder.startBtn.setIconResource(R.drawable.ic_play_arrow);
                                                holder.startBtn.setText(R.string.play_text);
                                                orderProcess.getStopwatch().stop();
                                                Constants.setCurrentOrderState(context, orderProcess.getIdentifier(), true);
                                                Toast.makeText(context, "Order paused, please remember to resume when you get back", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            })
                            .show();
                }
            });

            holder.finishBtn.setOnClickListener(view -> {
                boolean isPaused = Constants.getCurrentOrderState(context, orderProcess.getIdentifier());
                if (isPaused){
                    //Closing any open breaks
                    long breakStart = Constants.getBreakStart(context, orderProcess.getIdentifier());
                    int reason = Constants.getReason(context, orderProcess.getIdentifier());

                    Constants.deletePref(context, orderProcess.getIdentifier()+"_reason");//removing the reason pref
                    Constants.deletePref(context, orderProcess.getIdentifier()+"_start");//removing the start pref


                    String newBreaks = "";
                    if (orderProcess.getBreaks().equals("")){
                        newBreaks = breakStart + "-" + System.nanoTime() + "-" + reason;
                    } else newBreaks = "#" + breakStart + "-" + System.nanoTime() + "-" + reason;

                    //updating break values everywhere
                    orderProcess.setBreaks(newBreaks);
                    ContentValues values = new ContentValues();
                    values.put("breaks", newBreaks);
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());
                } else {
                    ContentValues values = new ContentValues();
                    values.put("end_time", System.nanoTime());
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());
                }
                orderProcess.getStopwatch().stop();
                Constants.deletePref(context, orderProcess.getIdentifier());

                processes.remove(position);
                notifyDataSetChanged();
            });
        }
    }


    @Override
    public int getItemCount() {
        return processes.size();
    }

    private String formatElapsed(long minutes) {
        if (minutes < 60) {
            return "h:0, m:" + minutes;
        } else {
            return "h:" + (minutes / 60) + ", m:" + (minutes % 60);
        }
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

    /**
     * this is a method to format the date
     *
     * @param startTime Long of the epoch time the processing of the order was started
     * @return a sting in the form "day, month, hour:minute"
     */
    private String formatDate(long startTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MM, hh:mm", Locale.ENGLISH);
        return simpleDateFormat.format(new Date(startTime));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderDesc, orderStated, orderElapsedTime;
        ImageView toggleElapsed;
        TextInputLayout extraPersonnelLayout;
        TextInputEditText extraPersonnel;
        MaterialButton startBtn, finishBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.order_number);
            orderDesc = itemView.findViewById(R.id.order_description);
            orderStated = itemView.findViewById(R.id.start_time);
            orderElapsedTime = itemView.findViewById(R.id.elapsed_time);
            toggleElapsed = itemView.findViewById(R.id.img_toggle);
            extraPersonnelLayout = itemView.findViewById(R.id.extra_personnel_layout);
            extraPersonnel = itemView.findViewById(R.id.extra_personnel);
            startBtn = itemView.findViewById(R.id.start_pause);
            finishBtn = itemView.findViewById(R.id.finish_btn);
        }
    }
}
