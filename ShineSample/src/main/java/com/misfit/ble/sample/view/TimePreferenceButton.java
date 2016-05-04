package com.misfit.ble.sample.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;


import com.misfit.ble.sample.R;
import com.misfit.ble.sample.model.Time;
import com.misfit.ble.sample.utils.Preference;

import java.util.Calendar;
import java.util.Locale;

public class TimePreferenceButton extends Button implements Preference<Time> {

    String mTitle;
    Time mTime;

    public TimePreferenceButton(Context context) {
        super(context);
        init();
    }

    public TimePreferenceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimePreferenceButton);
        String title = typedArray.getString(R.styleable.TimePreferenceButton_title_text);
        typedArray.recycle();
        if (title != null) {
            setTitle(title);
        }
    }

    private void init() {
        mTime = new Time();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 5);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mTime.hour = hourOfDay;
                        mTime.minute = minute;
                        setValue(mTime);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });
    }

    @Override
    public void setTitle(String title) {
        mTitle = title + " ";
        setValue(mTime);
    }

    @Override
    public void setValue(Time value) {
        mTime = value;
        setText(mTitle + String.format(Locale.getDefault(), "%02d:%02d", mTime.hour, mTime.minute));
    }

    @Override
    public Time getValue() {
        return mTime;
    }
}
