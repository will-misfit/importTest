package com.misfit.ble.sample.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.misfit.ble.sample.BaseActivity;
import com.misfit.ble.sample.MisfitShineService;
import com.misfit.ble.sample.R;
import com.misfit.ble.sample.model.Feature;
import com.misfit.ble.sample.model.SpecifiedAnimationSetting;
import com.misfit.ble.sample.model.SpecifiedVibeSetting;
import com.misfit.ble.sample.view.SpecifiedAnimationSettingView;
import com.misfit.ble.sample.view.SpecifiedVibeSettingView;
import com.misfit.ble.setting.pluto.PlutoSequence;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class BmwActivity extends BaseActivity {

    @Bind(R.id.lv_feature)
    ListView mFeatureContainer;
    @Bind(R.id.ll_parameter_container)
    LinearLayout mParameterContainer;

    Feature[] features = new Feature[]{new SpecifiedAnimation(), new SpecifiedVibration(), new SpecifiedNotification()};
    Feature mCurrentFeature;
    ArrayAdapter<Feature> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmw);
        ButterKnife.bind(this);

        mAdapter = new ArrayAdapter<Feature>(this, R.layout.item_feature, R.id.text, features) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =  super.getView(position, convertView, parent);
                if (mCurrentFeature != null && mCurrentFeature.toString().equals(features[position].toString())) {
                    view.setBackgroundColor(Color.WHITE);
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
                return view;
            }
        };
        mFeatureContainer.setAdapter(mAdapter);
    }

    @OnItemClick(R.id.lv_feature)
    void onFeatureClicked(AdapterView<?> parent, View view, int position, long id) {
        mParameterContainer.removeAllViews();
        mParameterContainer.addView(features[position].getParameterView());
        mCurrentFeature = features[position];
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.action)
    void action() {
        if (mCurrentFeature != null) {
            mCurrentFeature.action(mService);
        }
    }

    class SpecifiedAnimation extends Feature {

        private SpecifiedAnimationSettingView mView;

        public SpecifiedAnimation() {
            super("Specified Animation");
        }

        @Override
        public void action(MisfitShineService shineService) {
            SpecifiedAnimationSetting setting = mView.getValue();
            shineService.startSpecifiedAnimation(setting.led, setting.repeats, setting.timeBetweenRepeats, setting.color);
        }

        @Override
        public View getParameterView() {
            if (mView == null) {
                mView = new SpecifiedAnimationSettingView(BmwActivity.this);
                mView.setValue(getDefaultAnimationSetting());
            }
            return mView;
        }
    }

    @NonNull
    private SpecifiedAnimationSetting getDefaultAnimationSetting() {
        SpecifiedAnimationSetting setting = new SpecifiedAnimationSetting();
        setting.led = new PlutoSequence.LED(PlutoSequence.LED.SPECIFIED_SHORT);
        setting.color = new PlutoSequence.Color(PlutoSequence.Color.SPECIFIED_GREEN);
        setting.repeats = 1;
        setting.timeBetweenRepeats = 1;
        return setting;
    }

    class SpecifiedVibration extends Feature {

        private SpecifiedVibeSettingView mView;

        public SpecifiedVibration() {
            super("Specified Vibration");
        }

        @Override
        public void action(MisfitShineService shineService) {
            SpecifiedVibeSetting setting = mView.getValue();
            shineService.startSpecifiedVibration(setting.vibe, setting.repeats, setting.timeBetweenRepeats);
        }

        @Override
        public View getParameterView() {
            if (mView == null) {
                mView = new SpecifiedVibeSettingView(BmwActivity.this);
                mView.setValue(getDefaultVibeSetting());
            }
            return mView;
        }
    }

    @NonNull
    private SpecifiedVibeSetting getDefaultVibeSetting() {
        SpecifiedVibeSetting setting = new SpecifiedVibeSetting();
        setting.vibe = new PlutoSequence.Vibe(PlutoSequence.Vibe.SPECIFIED_SHORT);
        setting.repeats = 1;
        setting.timeBetweenRepeats = 1;
        return setting;
    }

    class SpecifiedNotification extends Feature {
        private SpecifiedAnimationSettingView mAnimationView;
        private SpecifiedVibeSettingView mVibeView;
        private LinearLayout mContainer;

        public SpecifiedNotification() {
            super("Specified Notification");
        }

        @Override
        public void action(MisfitShineService shineService) {
            SpecifiedAnimationSetting animationSetting = mAnimationView.getValue();
            SpecifiedVibeSetting vibeSetting = mVibeView.getValue();
            shineService.startSpecifiedNotification(animationSetting.led,
                    animationSetting.color,
                    animationSetting.repeats,
                    animationSetting.timeBetweenRepeats,
                    vibeSetting.vibe,
                    vibeSetting.repeats,
                    vibeSetting.timeBetweenRepeats
            );
        }

        @Override
        public View getParameterView() {
            if (mContainer == null) {
                mContainer = new LinearLayout(BmwActivity.this);
                mContainer.setOrientation(LinearLayout.VERTICAL);
                mAnimationView = new SpecifiedAnimationSettingView(BmwActivity.this);
                mVibeView = new SpecifiedVibeSettingView(BmwActivity.this);
                mContainer.addView(mAnimationView);
                mContainer.addView(mVibeView);
                mAnimationView.setValue(getDefaultAnimationSetting());
                mVibeView.setValue(getDefaultVibeSetting());
            }
            return mContainer;
        }
    }

}
