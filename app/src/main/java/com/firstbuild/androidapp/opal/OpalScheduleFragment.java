package com.firstbuild.androidapp.opal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.OpalValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.productmanager.OpalInfo;
import com.firstbuild.androidapp.productmanager.ProductManager;
import com.firstbuild.commonframework.blemanager.BleManager;
import com.firstbuild.tools.MainQueue;
import com.firstbuild.tools.MathTools;
import com.firstbuild.viewutil.OTAConfirmDialogFragment;
import com.firstbuild.viewutil.OpalScheduleGridLayout;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

/**
 * Created by hans on 16. 7. 13..
 */
public class OpalScheduleFragment extends Fragment {

    private static final String TAG_HELP_TUTORIAL = "tag_help_tutorial";

    private String TAG = OpalScheduleFragment.class.getSimpleName();
    private OpalScheduleGridLayout timeSlotContainer;
    private ViewGroup weekdaysContainer;
    private Button applyAllDay;

    private View currentSelectedWeekDay;

    // Key : weekday view Id , value : selected timeslot view id
    private SparseArrayCompat<HashSet<Integer>> timeSlotTracker = new SparseArrayCompat<>();

    private int[] weekdayTvIds = new int[]{ R.id.sunday, R.id.monday, R.id.tuesday,
            R.id.wednesday, R.id.thursday, R.id.friday, R.id.saturday };

    private ArrayList<Integer> timeSlotTvIds = new ArrayList<>();

    private OpalInfo currentOpal;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_opal_schedule, container, false);
        timeSlotContainer = (OpalScheduleGridLayout)view.findViewById(R.id.time_slot_item_container);
        weekdaysContainer = (ViewGroup)view.findViewById(R.id.weekdays_header);
        applyAllDay = (Button)view.findViewById(R.id.apply_all_day_btn);

        // init Time Slot View id array list
        for(int i=0; i <timeSlotContainer.getChildCount(); i++) {
            timeSlotTvIds.add(i, timeSlotContainer.getChildAt(i).getId());
        }

        // Apply All btn
        applyAllDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v.isSelected() == false) {
                    onHandleApplyAllClicked(v);
                }
            }
        });

        timeSlotContainer.setDiagonalDragDetector(new OpalScheduleGridLayout.GridDiagonalDragDector() {
            @Override
            public void onDiagonalDragDetected(View start, View end) {

                int startIndex = timeSlotContainer.indexOfChild(start);
                int endIndex = timeSlotContainer.indexOfChild(end);

                HashSet<Integer> set = timeSlotTracker.get(currentSelectedWeekDay.getId());

                Boolean childSelectChanged = false;
                for(int i = startIndex; i < endIndex + 1; i++) {
                    View child = timeSlotContainer.getChildAt(i);

                    if(child.isSelected() == false) {
                        child.setSelected(true);
                        childSelectChanged = true;
                    }

                    set.add(child.getId());
                }

                if(childSelectChanged == true) {
                    onTimeSlotSelectionChanged();
                }
            }
        });

        // update schedule data read from the Opal Device into timeSlotTracker
        initTimeSlotTrackerFromScheduleData();

        // Select the current day of week
        initTodayWeekdayHeader(weekdaysContainer);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_help) {
            showHelpTutorial();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelpTutorial() {
        FragmentManager fm = getFragmentManager();

        if(fm != null &&
                fm.findFragmentByTag(TAG_HELP_TUTORIAL) != null) {
            // skip showing dialog as it is already shown
            return;
        }

        OpalHelpTutorialFragment helpFragment = OpalHelpTutorialFragment.getInstance();
        helpFragment.show(fm, TAG_HELP_TUTORIAL);
    }

    /**
     * initialize internal data structure based on the value read from the Opal Device
     */
    private void initTimeSlotTrackerFromScheduleData() {

        currentOpal = (OpalInfo) ProductManager.getInstance().getCurrent();
        byte[] schedule = currentOpal.getScheduleValue();

        HashSet<Integer> item;

        for(int i=0 ; i < weekdayTvIds.length ; i++) {

            int key = weekdayTvIds[i];

            // get schedule time slot info from each day of week
            if(timeSlotTracker.get(key) == null) {
                item = new HashSet<>();
            } else {
                item = timeSlotTracker.get(key);
                item.clear();
            }

            byte hourTimeSlot;
            boolean isSet;

            // starts at 12:00 am and 1 hour increment per each loop
            for(int j=0; j<schedule.length; j++) {

                hourTimeSlot = schedule[j];
                isSet = (( hourTimeSlot >> i ) & 1) == 1;

                if(isSet == true) {
                    Log.d(TAG, "[HANS] Time set : " + i + " bit" + " at " + j + " byte" );
                    TextView tv = (TextView)(timeSlotContainer.findViewById(timeSlotTvIds.get(j)));
                    Log.d(TAG, "[HANS] Time set : store view id where text is : " + tv.getText() );
                    item.add(timeSlotTvIds.get(j));
                }
            }

            timeSlotTracker.put(key, item);
        }
    }

    private void sendTimeSlotInfoToOpal() {

        ByteBuffer valueBuffer = ByteBuffer.allocate(24);
        boolean[][] scheduleTable = new boolean[24][8];

        for(int i = 0; i < timeSlotTracker.size(); i++) {
            int key = timeSlotTracker.keyAt(i);
            HashSet<Integer> value = timeSlotTracker.get(key);

            TextView weekday = (TextView)weekdaysContainer.findViewById(key);

            // View id
            for (Integer integer : value) {
                int index = timeSlotTvIds.indexOf(integer);
                TextView tv = (TextView)(timeSlotContainer.findViewById(integer));
                Log.d(TAG, "[HANS] \"On\" detected over : " + weekday.getText() + " : " + tv.getText() );
                scheduleTable[index][i] = true;
            }
        }

        for(int i=0; i<24; i++) {
            boolean[] row = scheduleTable[i];

            byte b = (byte)((row[0] ? 1 : 0) +
                            (row[1] ? 1<<1 : 0) +
                            (row[2] ? 1<<2 : 0) +
                            (row[3] ? 1<<3 : 0) +
                            (row[4] ? 1<<4 : 0) +
                            (row[5] ? 1<<5 : 0) +
                            (row[6] ? 1<<6 : 0) +
                            (row[7] ? 1<<7 : 0));

            valueBuffer.put(b);
        }

        Log.d(TAG, "[HANS] sendTimeSlotInfoToOpal : " + MathTools.byteArrayToHex(valueBuffer.array()));

        BleManager.getInstance().writeCharacteristics(currentOpal.bluetoothDevice, OpalValues.OPAL_SET_SCHEDULE_UUID, valueBuffer.array());
    }

    private void onHandleApplyAllClicked(View v) {

        // Update the UI
        v.setSelected(true);
        applyAllDay.setText(R.string.schedule_applied_all);

        // Copy current timeSlot selection status to other weekdays
        HashSet<Integer> selectedTimeSlots = timeSlotTracker.get(currentSelectedWeekDay.getId());

        for(int i = 0; i < weekdaysContainer.getChildCount(); i++) {
            View child = weekdaysContainer.getChildAt(i);
            if(child instanceof TextView && child.getId() != currentSelectedWeekDay.getId()) {
                timeSlotTracker.put(child.getId(), (HashSet<Integer>)selectedTimeSlots.clone());
            }

            if(child instanceof ImageView && i != weekdaysContainer.indexOfChild(currentSelectedWeekDay) + 7) {
                child.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause() IN");
        sendTimeSlotInfoToOpal();
        Log.d(TAG, "sendTimeSlotInfoToOpal in onPause() !!!");

    }

    private void initTodayWeekdayHeader(View weekDayHeader) {

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        View today;

        switch (day) {
            case Calendar.SUNDAY:
                today = weekDayHeader.findViewById(R.id.sunday);
                break;
            case Calendar.MONDAY:
                today = weekDayHeader.findViewById(R.id.monday);
                break;
            case Calendar.TUESDAY:
                today = weekDayHeader.findViewById(R.id.tuesday);
                break;
            case Calendar.WEDNESDAY:
                today = weekDayHeader.findViewById(R.id.wednesday);
                break;
            case Calendar.THURSDAY:
                today = weekDayHeader.findViewById(R.id.thursday);
                break;
            case Calendar.FRIDAY:
                today = weekDayHeader.findViewById(R.id.friday);
                break;
            case Calendar.SATURDAY:
                today = weekDayHeader.findViewById(R.id.saturday);
                break;
            default :
                today = null;
                break;
        }

        onHandleWeekdaysClicked(today);
    }

    public void onHandleTimeSlotClicked(View v) {

        // Toggle selection state
        v.setSelected(!v.isSelected());

        // update selection state when time slot selection status changes
        HashSet<Integer> selectedTimeSlotSet = timeSlotTracker.get(currentSelectedWeekDay.getId());
        if(v.isSelected()) {
            selectedTimeSlotSet.add(Integer.valueOf(v.getId()));
        }
        else {
            selectedTimeSlotSet.remove(Integer.valueOf(v.getId()));
        }

        // Check if applied all days mode is on, if so, cancel applied mode
        onTimeSlotSelectionChanged();
    }

    private void onTimeSlotSelectionChanged() {
        if(applyAllDay.isSelected() == true) {
            for(int i = 0; i < weekdaysContainer.getChildCount(); i++) {
                View child = weekdaysContainer.getChildAt(i);

                if(child instanceof ImageView) {
                    child.setVisibility(View.GONE);
                }
            }
            applyAllDay.setSelected(false);
            applyAllDay.setText(R.string.schedule_apply_all_days);
        }
    }

    public void onHandleWeekdaysClicked(View v) {

        // if current weekday is selected again, then skip handling !
        if(v == null ||
                currentSelectedWeekDay != null && currentSelectedWeekDay.getId() == v.getId()) {
            return;
        }

        // Update previous week day selection status to false
        if(currentSelectedWeekDay != null &&
                currentSelectedWeekDay.getId() != v.getId() ) {
            currentSelectedWeekDay.setSelected(false);
        }

        v.setSelected(true);
        currentSelectedWeekDay = v;

        // If there is no data structure for current selected weekday, create it lazily
        if(timeSlotTracker.get(currentSelectedWeekDay.getId()) == null) {
            timeSlotTracker.put(currentSelectedWeekDay.getId(), new HashSet<Integer>());
        }

        // update the current time slot UI using stored information
        HashSet<Integer> selectedTimeSlotSet = timeSlotTracker.get(currentSelectedWeekDay.getId());

        // If there is any slected status stored, then update the UI
        for(int i=0; i < timeSlotContainer.getChildCount(); i++) {

            View child = timeSlotContainer.getChildAt(i);

            if(selectedTimeSlotSet.contains(Integer.valueOf(child.getId())) == true) {
                child.setSelected(true);
            }
            else {
                child.setSelected(false);
            }
        }
    }
}
