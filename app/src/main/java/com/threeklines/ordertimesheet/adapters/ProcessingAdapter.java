package com.threeklines.ordertimesheet.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
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

import static com.threeklines.ordertimesheet.entities.Constants.OPTIONS;
import static com.threeklines.ordertimesheet.entities.Constants.getLastOrder;

public class ProcessingAdapter extends RecyclerView.Adapter<ProcessingAdapter.ViewHolder> {
    private final String TAG = "ProcessingAdapter";
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
            OrderProcess orderProcess = processes.get(position);
            Order order = orderBinarySearch(orders, orderProcess.getIdentifier());
            holder.orderNumber.setText(orderProcess.getIdentifier());
            if (order != null) {
                holder.orderDesc.setText(order.getDescription());
            } else holder.orderDesc.setText(R.string.no_desc);

            holder.orderStated.setText(formatDate(orderProcess.getStartTime()));
            holder.orderElapsedTime.setText("Elapsed Time\n--, --");

            holder.toggleElapsed.setOnClickListener(view -> {
                /*
                 if clicked icon is changed and elapsed time from the stopwatch is displayed for 3 seconds and
                 then the time is hidden again.
                 */
                holder.toggleElapsed.setImageResource(R.drawable.ic_visibility_off);
                holder.orderElapsedTime.setText(formatElapsed(calculateElapsedTime(orderProcess.getStartTime(),
                        totalBreaksTime(orderProcess.getBreaks()), orderProcess.getIdentifier())));

                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        holder.toggleElapsed.setImageResource(R.drawable.ic_visibility);
                        holder.orderElapsedTime.setText("Elapsed Time\n--, --");
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
                    ContentValues values = new ContentValues();
                    values.put("personnel", newPersonnel);
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());
                    holder.extraPersonnel.setText("");

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

                    Constants.deletePref(context, orderProcess.getIdentifier() + "_reason");//removing the reason pref
                    Constants.deletePref(context, orderProcess.getIdentifier() + "_start_break");//removing the break start

                    String newBreaks = "";
                    if (orderProcess.getBreaks() == null) {
                        newBreaks = "#" + breakStart + "-" + System.currentTimeMillis() + "-" + reason;
                    } else newBreaks = "#" + breakStart + "-" + System.currentTimeMillis() + "-" + reason;

                    //updating break values everywhere
                    orderProcess.setBreaks(newBreaks);
                    ContentValues values = new ContentValues();
                    values.put("breaks", newBreaks);
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());

                    holder.startBtn.setIconResource(R.drawable.ic_pause);
                    holder.startBtn.setText(R.string.pause_text);
                    Constants.setCurrentOrderState(context, orderProcess.getIdentifier(), false);
                } else {
                    //Confirm pause alert
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm!")
                            .setMessage("Are you sure you want to pause the timer")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                Log.d(TAG, "onBindViewHolder: first dialog up");
                                OPTIONS.add("AM Tea");
                                OPTIONS.add("Lunch");
                                OPTIONS.add("PM Tea");
                                OPTIONS.add("Reassignment");
                                OPTIONS.add("Other");
                                new AlertDialog.Builder(context)
                                        .setTitle("Break Reason")
                                        .setAdapter(new ArrayAdapter<String>(
                                                context, R.layout.item_role, OPTIONS), (dialogInterface1, i1)
                                                -> {
                                            if (Constants.setBreakStart(context, orderProcess.getIdentifier(), System.currentTimeMillis(), i1)) {
                                                holder.startBtn.setIconResource(R.drawable.ic_play_arrow);
                                                holder.startBtn.setText(R.string.resume_text);
                                                Constants.setCurrentOrderState(context, orderProcess.getIdentifier(), true);
                                                Toast.makeText(context, "Order paused, please remember to resume when you get back", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .show();
                            })
                            .show();
                }
            });

            holder.finishBtn.setOnClickListener(view -> {
                boolean isPaused = Constants.getCurrentOrderState(context, orderProcess.getIdentifier());
                if (isPaused) {
                    //Closing any open breaks
                    long breakStart = Constants.getBreakStart(context, orderProcess.getIdentifier());
                    int reason = Constants.getReason(context, orderProcess.getIdentifier());

                    Constants.deletePref(context, orderProcess.getIdentifier() + "_reason");//removing the reason pref
                    Constants.deletePref(context, orderProcess.getIdentifier() + "_start_break");//removing the start pref


                    String newBreaks = "";
                    if (orderProcess.getBreaks() == null) {
                        newBreaks = breakStart + "-" + System.currentTimeMillis() + "-" + reason;
                    } else newBreaks = "#" + breakStart + "-" + System.currentTimeMillis() + "-" + reason;

                    //updating break values everywhere
                    orderProcess.setBreaks(newBreaks);
                    ContentValues values = new ContentValues();
                    values.put("breaks", newBreaks);
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());
                } else {
                    ContentValues values = new ContentValues();
                    values.put("end_time", System.currentTimeMillis());
                    values.put("sync_state", "false");
                    DB.getInstance(context).updateProcess(values, orderProcess.getIdentifier());
                }
                Constants.deletePref(context, orderProcess.getIdentifier());
                Constants.setLastCompleted(context, orderProcess.getIdentifier());

                processes.remove(position);
                notifyDataSetChanged();
            });
        }
    }


    @Override
    public int getItemCount() {
        String lastEntry = getLastOrder(context);
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
                    if (o.getIdentifier().equals(lastEntry) && o.getEndTime() == 0){
                        processes.add(o);
                    }
                }
            }
        }
        return processes.size();
    }

    private String formatElapsed(long minutes) {
        if (minutes < 60) {
            return "Elapsed Time\nh:0, m:" + minutes;
        } else {
            return "Elapsed Time\nh:" + (minutes / 60) + ", m:" + (minutes % 60);
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM, hh:mm", Locale.ENGLISH);
        return "Started\n" + simpleDateFormat.format(new Date(startTime));

    }

    /**
     * This is a method that calculates completed breaks if the process is paused then the current break is not included.
     * Breaks are stored as a string in the format #breakInfo# where break info is stored in the format starTime-endTime-reason
     * so  the result would be for example #19393911893198-1318319313913173197-1#
     *
     * @param breaks String of the breaks
     * @return long of the total time between breaks.
     */
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

    /**
     * This is a method to calculate the elapsed time and takes into account if the process is paused or not
     * and then subtract the time accordingly using the formular [(now-start)-breaks]
     *
     * @param startTime      long of the start time of the process
     * @param breaksDuration long of the total breaks duration
     * @param identifier     string of order number or style number
     * @return returns long of minutes of the elapsed time.
     */
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
