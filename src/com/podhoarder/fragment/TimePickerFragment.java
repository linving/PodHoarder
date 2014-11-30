package com.podhoarder.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Emil on 2014-11-30.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private static final String LOG_TAG="com.podhoarder.fragment.TimePickerFragment";
    private OnTimePickedListener mOnTimePickedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Log.i(LOG_TAG,"Picked " + hourOfDay + ":" + minute + "!");
        if (mOnTimePickedListener != null)
            mOnTimePickedListener.onTimePicked(view,hourOfDay,minute);
    }

    public interface OnTimePickedListener {
        public void onTimePicked(TimePicker view, int hourOfDay, int minute);
    }

    public OnTimePickedListener getTimePickedListener() {
        return mOnTimePickedListener;
    }

    public void setOnTimePickedListener(OnTimePickedListener listener) {
        mOnTimePickedListener = listener;
    }
}
