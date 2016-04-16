package com.misfit.syncdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.misfit.syncdemo.reproduce.SyncReproducer;
import com.misfit.syncdemo.util.LogView;
import com.misfit.syncsdk.utils.MLog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

public class ReproduceActivity extends AppCompatActivity {

    private final static int REQ_RAW_DATA_FILE = 1;
    private final static int REQ_SETTING_FILE = 2;

    private SyncReproducer mSyncReproducer;
    @Bind(R.id.logview)
    LogView mLogview;
    @Bind(R.id.tv_raw_data_path)
    TextView mReproduceDataPathTv;
    @Bind(R.id.tv_settings_path)
    TextView mSettingsPathTv;

    String mReproduceDataPath;
    String mSettingPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproduce);
        ButterKnife.bind(this);
        mSyncReproducer = new SyncReproducer(this);
        MLog.registerLogNode(mLogview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MLog.unregisterLogNode(mLogview);
    }

    @OnClick(R.id.btn_choose_reproduce_data)
    void chooseRawData() {
        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
        intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
        intent.putExtra(ExFilePicker.SET_START_DIRECTORY, Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp");
        startActivityForResult(intent, REQ_RAW_DATA_FILE);
    }

    @OnClick(R.id.btn_choose_settings)
    void chooseSettings() {
        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
        intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
        intent.putExtra(ExFilePicker.SET_START_DIRECTORY, Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp");
        startActivityForResult(intent, REQ_SETTING_FILE);
    }

    @OnClick(R.id.btn_start)
    void start() {
        try {
            mSyncReproducer.startFromSdk(mReproduceDataPath, mSettingPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_RAW_DATA_FILE && resultCode == RESULT_OK) {
            mReproduceDataPath = handleFileChooseDataIntent(data);
            mReproduceDataPathTv.setText("DATA: " + mReproduceDataPath);
        } else if (requestCode == REQ_SETTING_FILE && resultCode == RESULT_OK) {
            mSettingPath = handleFileChooseDataIntent(data);
            mSettingsPathTv.setText("SETTINGS: " + mSettingPath);
        }
    }

    private String handleFileChooseDataIntent(Intent data) {
        if (data != null) {
            ExFilePickerParcelObject object = data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
            if (object.count > 0) {
                return object.path + object.names.get(0);
            }
        }
        return null;
    }
}
