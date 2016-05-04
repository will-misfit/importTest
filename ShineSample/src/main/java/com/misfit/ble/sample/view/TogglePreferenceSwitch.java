package com.misfit.ble.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Switch;

import com.misfit.ble.sample.R;
import com.misfit.ble.sample.utils.Preference;


public class TogglePreferenceSwitch extends Switch implements Preference<Boolean> {

    public TogglePreferenceSwitch(Context context) {
        super(context);
    }

    public TogglePreferenceSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TogglePreferenceSwitch);
        String title = typedArray.getString(R.styleable.TogglePreferenceSwitch_title_text);
        typedArray.recycle();
        if (title != null) {
            setTitle(title);
        }
    }

    @Override
    public void setTitle(String title) {
        setText(title);
    }

    @Override
    public void setValue(Boolean value) {
        setChecked(value);
    }

    @Override
    public Boolean getValue() {
        return isChecked();
    }
}
