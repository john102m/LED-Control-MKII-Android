package com.example.ledcontrollmkii.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;

import com.example.ledcontrollmkii.MyInterface;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener
{
    MyInterface _myInterface;
    int _row;
//    TimePickerDialog.OnTimeSetListener mCallback;
//    public void setInterface(MyInterface myInterface) {
//        this._myInterface = myInterface;
//    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker.


        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            _row = bundle.getInt("row", 0);
        }

        // Create a new instance of TimePickerDialog and return it.
        return new TimePickerDialog(getActivity(), this::onTimeSet, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time the user picks.
//        String selectedTime = hourOfDay + ":" + minute;
//        Log.i("Frag", "onTimeSet: " + selectedTime);
        _myInterface.onTimeSet(_row, hourOfDay,minute);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            _myInterface = (MyInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

}
