package com.misfit.syncdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class NavigationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_sync)
    void gotoSyncPage() {
        startActivity(new Intent(this, SyncActivity.class));
    }

    @OnClick(R.id.btn_reproduce)
    void gotoReProducePage() {
        startActivity(new Intent(this, ReproduceActivity.class));
    }
}
