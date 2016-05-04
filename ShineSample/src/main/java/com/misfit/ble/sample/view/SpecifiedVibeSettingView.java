package com.misfit.ble.sample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.misfit.ble.sample.R;
import com.misfit.ble.sample.model.SpecifiedVibeSetting;
import com.misfit.ble.sample.utils.Preference;
import com.misfit.ble.setting.pluto.PlutoSequence;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SpecifiedVibeSettingView extends LinearLayout implements Preference<SpecifiedVibeSetting> {

    private SpecifiedVibeSetting mSetting;

    @Bind(R.id.pref_vibe)
    NumberPreferenceEditText mVibePref;
    @Bind(R.id.pref_repeats)
    NumberPreferenceEditText mRepeatPref;
    @Bind(R.id.pref_time_between_repeats)
    NumberPreferenceEditText mTimeBetweenRepeatsPref;

    public SpecifiedVibeSettingView(Context context) {
        super(context);
        init(context);
    }

    public SpecifiedVibeSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.specified_vibe_setting_view, this, true);
        ButterKnife.bind(this, this);
    }


    @Override
    public void setTitle(String title) {

    }

    @Override
    public void setValue(SpecifiedVibeSetting value) {
        mSetting = value;
        mVibePref.setValue((int) mSetting.vibe.getValue());
        mRepeatPref.setValue((int) mSetting.repeats);
        mTimeBetweenRepeatsPref.setValue((int) mSetting.timeBetweenRepeats);
    }

    @Override
    public SpecifiedVibeSetting getValue() {
        SpecifiedVibeSetting setting = new SpecifiedVibeSetting();
        setting.vibe = new PlutoSequence.Vibe(mVibePref.getValue().shortValue());
        setting.repeats = mRepeatPref.getValue().byteValue();
        setting.timeBetweenRepeats = mTimeBetweenRepeatsPref.getValue().shortValue();
        mSetting = setting;
        return mSetting;
    }
}
