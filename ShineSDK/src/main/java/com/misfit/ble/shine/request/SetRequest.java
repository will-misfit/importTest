package com.misfit.ble.shine.request;

public abstract class SetRequest extends Request {

    @Override
    public void onRequestSent(int status) {
        mIsCompleted = true;
    }
}
