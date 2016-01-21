package com.misfit.ble.sample.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.misfit.ble.sample.MisfitShineService;
import com.misfit.ble.sample.R;
import com.misfit.ble.sample.utils.StopWatch;
import com.misfit.ble.sample.utils.logger.LogView;
import com.misfit.ble.sample.utils.logger.MFLog;
import com.misfit.ble.shine.ShineDevice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestSyncAndConnectActivity extends AppCompatActivity {

    private final static String TAG = TestSyncAndConnectActivity.class.getSimpleName();

    @Bind(R.id.edit_times)
    EditText editTimes;

    @Bind(R.id.edit_timeout)
    EditText editTimeout;

    @Bind(R.id.edit_test_interval)
    EditText editTestInterval;

    @Bind(R.id.edit_close_delay)
    EditText editCloseDelay;

    @Bind(R.id.text_result)
    LogView textLog;

    @Bind(R.id.text_status)
    TextView textStatus;

    @Bind(R.id.text_progress)
    TextView textProgress;

    final static int STATE_IDLE = 1;
    final static int STATE_CONNECTED = 2;
    final static int STATE_CLOSED = 3;
    final static int STATE_CONNECTING = 4;
    final static long TIME_OUT = -1L;

    List<Long> connectTimes;
    int totalTimes;
    int timeout;
    int testInterval;
    int closeDelay;

    int currentTimes;
    int timeoutCnt;
    long startTime;

    ConnectStateListener currentStateListener;
    Timer timer;
    MisfitShineService shineService;
    ShineDevice device;

    StopWatch stopWatch = new StopWatch();
    StopWatch closeWatch = new StopWatch();

    int state = STATE_IDLE;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MisfitShineService.SHINE_SERVICE_CONNECTED:
                    state = STATE_CONNECTED;
                    android.util.Log.i(TAG, "connected");
                    break;
                case MisfitShineService.SHINE_SERVICE_CLOSED:
                    MFLog.i(TAG, String.format("!![%d]\tconnection closed, time=%dms", getTime(), closeWatch.stop()));
                    state = STATE_CLOSED;
                    break;
                case MisfitShineService.SHINE_SERVICE_OPERATION_END:
                case MisfitShineService.SHINE_SERVICE_OTA_RESET:
                case MisfitShineService.SHINE_SERVICE_OTA_PROGRESS_CHANGED:
                case MisfitShineService.SHINE_SERVICE_STREAMING_USER_INPUT_EVENTS_RECEIVED_EVENT:
                case MisfitShineService.SHINE_SERVICE_BUTTON_EVENTS:
                case MisfitShineService.SHINE_SERVICE_RSSI_READ:
                default:
                    super.handleMessage(msg);
            }
        }
    };

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            shineService = ((MisfitShineService.LocalBinder) service).getService();
            shineService.setHandler(handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            shineService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_sync_and_connect);
        ButterKnife.bind(this);
        device = getIntent().getParcelableExtra("device");
        bindToService();
        MFLog.setViewLogger(textLog);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static Bundle getOpenBundle(ShineDevice device) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("device", device);
        return bundle;
    }

    void bindToService() {
        Intent bindIntent = new Intent(this, MisfitShineService.class);
        startService(bindIntent);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MisfitShineService.resetConnectingTimeout();
        unbindService(serviceConnection);
        MFLog.setViewLogger(null);
        MFLog.setLogFileName(null);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @OnClick(R.id.btn_test_connect)
    void onTestConnect() {
        MFLog.i(TAG, "start test connect");
        cancelTaskIfExist();
        textLog.setText("");
        textLog.scrollTo(0,0);
        MFLog.setLogFileName(getDate("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
        hideIME(editCloseDelay);
        if (initialParams()) {
            updateTestingProgress();
            setTestingStatus("Running");
            testConnect();
        }
    }

    @OnClick(R.id.btn_cancel)
    void onStopTest() {
        textStatus.setText("Stop");
        cancelTaskIfExist();
    }

    private String getDate(String format, long timeInMills) {
        SimpleDateFormat formater = new SimpleDateFormat(format, Locale.CHINESE);
        return formater.format(new Date(timeInMills));
    }

    private void updateTestingProgress() {
        textProgress.post(new Runnable() {
            @Override
            public void run() {
                textProgress.setText(String.format("timeout:%d\t\t%d/%d", timeoutCnt, currentTimes + 1, totalTimes));
            }
        });
    }

    void setTestingStatus(final String status) {
        textStatus.post(new Runnable() {
            @Override
            public void run() {
                textStatus.setText(status);
            }
        });
    }

    public void setState(int state) {
        this.state = state;
    }

    void cancelTaskIfExist() {
        //TODO:how to really cancel task?
        closeWatch.start();
        shineService.close();
        MFLog.i(TAG, String.format("close() method cost time=%dms",closeWatch.stop()));
        closeWatch.start();
        if (currentStateListener != null) {
            currentStateListener.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
    }

    private boolean initialParams() {
        try {
            currentTimes = 0;
            timeoutCnt = 0;
            startTime = System.currentTimeMillis();
            connectTimes = new ArrayList<>();
            totalTimes = Integer.parseInt(editTimes.getText().toString());
            timeout = Integer.parseInt(editTimeout.getText().toString()) * 1000;
            testInterval = Integer.parseInt(editTestInterval.getText().toString()) * 1000;
            closeDelay = Integer.parseInt(editCloseDelay.getText().toString()) * 1000;
            MisfitShineService.setConnectingTimeout(timeout);
            state = STATE_IDLE; //force set to idle
            currentStateListener = new ConnectStateListener();
            MFLog.i(TAG, String.format("params: total=%d, timeout=%ds, testInterval=%ds, closeDelay=%ds", totalTimes, timeout / 1000, testInterval / 1000, closeDelay / 1000));
        } catch (Exception ex) {
            MFLog.i(TAG, "parameter error!");
            return false;
        }
        return true;
    }

    private void testConnect() {
        connectOnce();
    }

    private long getTime() {
        long time = System.currentTimeMillis() - startTime;
        return time;
    }

    public static void hideIME(View v) {
        InputMethodManager manager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void connectOnce() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTestingProgress();
                if (state == STATE_IDLE || state == STATE_CLOSED) {
                    stopWatch.start();
                    MFLog.i(TAG, String.format("[%d]\tround %d:", getTime(), currentTimes + 1));
                    MFLog.i(TAG, String.format("\t[%d]\tstart connect", getTime()));
                    if (shineService.connect(device, currentStateListener)) {
                        setState(STATE_CONNECTING);
                    } else {
                        MFLog.i(TAG, String.format("[%d]\tround %d, try connect failure", getTime(), currentTimes+ 1));
                    }
                } else {
                    MFLog.i(TAG, String.format("[%d]\tround %d, wrong state, state=%d", getTime(), currentTimes+ 1, state));
                }
            }
        }, testInterval);
    }

    class ConnectStateListener implements MisfitShineService.ConnectTimeoutListener {

        boolean isCanceled = false;
        Timer timer = new Timer();

        void cancel() {
            isCanceled = true;
        }

        @Override
        public void onShineConnectTimeout() {
            if (isCanceled) {
                return;
            }
            connectTimes.add(TIME_OUT);
            timeoutCnt++;
            MFLog.i(TAG, String.format("\t[%d]\tconnect timeout", getTime()));
            process();
        }

        @Override
        public void onShineConnected() {
            if (isCanceled) {
                return;
            }
            long dt = stopWatch.stop();
            connectTimes.add(dt);
            MFLog.i(TAG, String.format("\t[%d]\tconnect time: %dms", getTime(), dt));
            process();
        }

        private void process() {
            currentTimes++;
            if (currentTimes < totalTimes) {
                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MFLog.i(TAG, String.format("\t[%d]\tclose connection", getTime()));
                        closeWatch.start();
                        shineService.close();
                        MFLog.i(TAG, String.format("close() method cost time=%dms",closeWatch.stop()));
                        closeWatch.start();
                        connectOnce();
                    }
                }, closeDelay);
            } else {
                MFLog.i(TAG, getSummary());
                MFLog.printList(TAG, connectTimes);
                setTestingStatus("finished");
            }
        }

        boolean isSuccessConnectionTime(long time) {
            return time >= 0; //because timeout set to -1
        }

        private String getSummary() {
            long max;
            long min;
            long sum = 0;

            if (connectTimes != null && connectTimes.size() > 0) {
                max = Integer.MIN_VALUE;
                min = Integer.MAX_VALUE;
                for (Long time : connectTimes) {
                    if (isSuccessConnectionTime(time)) {
                        max = Math.max(time, max);
                        min = Math.min(time, min);
                        sum += time;
                    }
                }
                int successCnt = totalTimes - timeoutCnt;
                long ave = -1;
                if (successCnt > 0) {
                    ave = sum / successCnt;
                }
                String txt = String.format(
                        "===================================\ntotal: %d \ntimeout count: %d\nminimum connect time: %dms\nmaximum connect time: %dms\naverage connect time: %dms", totalTimes, timeoutCnt, min, max, ave);
                return txt;
            } else {
                return String.format("===================================\nno connect success record\ntotal:%d\ntime out:%d\n", totalTimes, timeoutCnt);
            }

        }
    }
}
