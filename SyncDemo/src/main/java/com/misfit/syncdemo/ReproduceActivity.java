package com.misfit.syncdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.misfit.syncdemo.reproduce.SyncReproducer;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

public class ReproduceActivity extends AppCompatActivity {

    private final static int REQ_FILE_CHOOSER = 1;

    private SyncReproducer mSyncReproducer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproduce);
        ButterKnife.bind(this);
        mSyncReproducer = new SyncReproducer();
    }

    @OnClick(R.id.btn_choose_file)
    void showFileChooser() {
        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
        intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
        startActivityForResult(intent, REQ_FILE_CHOOSER);
    }

    private void handleOnFileChoose(String path) {
        try {
            mSyncReproducer.startFromSdk(this, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE_CHOOSER && resultCode == RESULT_OK) {
            if (data != null) {
                ExFilePickerParcelObject object = data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                if (object.count > 0) {
                    handleOnFileChoose(object.path + object.names.get(0));
                }
            }
        }
    }
}
