package com.misfit.ble.sample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.misfit.ble.sample.R;
import com.misfit.ble.sample.model.SpecifiedAnimationSetting;
import com.misfit.ble.sample.utils.Preference;
import com.misfit.ble.setting.pluto.PlutoSequence;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SpecifiedAnimationSettingView extends LinearLayout implements Preference<SpecifiedAnimationSetting> {

    private SpecifiedAnimationSetting mSetting;

    @Bind(R.id.pref_led)
    NumberPreferenceEditText mLedPref;
    @Bind(R.id.pref_color)
    NumberPreferenceEditText mColorPref;
    @Bind(R.id.pref_repeats)
    NumberPreferenceEditText mRepeatPref;
    @Bind(R.id.pref_time_between_repeats)
    NumberPreferenceEditText mTimeBetweenRepeatsPref;

    public SpecifiedAnimationSettingView(Context context) {
        super(context);
        init(context);
    }

    public SpecifiedAnimationSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.specified_animation_setting_view, this, true);
        ButterKnife.bind(this, this);
    }


    @Override
    public void setTitle(String title) {

    }

    @Override
    public void setValue(SpecifiedAnimationSetting value) {
        mSetting = value;
        mLedPref.setValue((int) mSetting.led.getValue());
        mColorPref.setValue((int) mSetting.color.getValue());
        mRepeatPref.setValue((int) mSetting.repeats);
        mTimeBetweenRepeatsPref.setValue((int) mSetting.timeBetweenRepeats);
    }

    @Override
    public SpecifiedAnimationSetting getValue() {
        SpecifiedAnimationSetting setting = new SpecifiedAnimationSetting();
        setting.led = new PlutoSequence.LED(mLedPref.getValue().shortValue());
        setting.color = new PlutoSequence.Color(mColorPref.getValue().shortValue());
        setting.repeats = mRepeatPref.getValue().byteValue();
        setting.timeBetweenRepeats = mTimeBetweenRepeatsPref.getValue().shortValue();
        mSetting = setting;
        return mSetting;
    }
}
