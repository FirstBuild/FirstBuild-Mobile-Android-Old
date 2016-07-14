package com.firstbuild.androidapp.opal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.tools.MainQueue;
import com.firstbuild.viewutil.OpalScheduleGridLayout;

import java.util.Calendar;
import java.util.HashSet;

/**
 * Created by hans on 16. 7. 13..
 */
public class OpalScheduleFragment extends Fragment {

    private String TAG = OpalScheduleFragment.class.getSimpleName();
    private OpalScheduleGridLayout timeSlotContainer;
    private ViewGroup weekdaysContainer;
    private Button applyAllDay;

    private View currentSelectedWeekDay;

    // Key : weekday view Id , value : selected timeslot view id
    private SparseArrayCompat<HashSet<Integer>> timeSlotTracker = new SparseArrayCompat<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_opal_schedule, container, false);
        timeSlotContainer = (OpalScheduleGridLayout)view.findViewById(R.id.time_slot_item_container);
        weekdaysContainer = (ViewGroup)view.findViewById(R.id.weekdays_header);
        applyAllDay = (Button)view.findViewById(R.id.apply_all_day_btn);

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

        // Select the current day, somehow immediate update results in awkward selection UI
        // So use delayed Update
        MainQueue.postDelayed(new Runnable() {
            @Override
            public void run() {
                initTodayWeekdayHeader(weekdaysContainer);
            }
        }, 100);

        return view;
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
