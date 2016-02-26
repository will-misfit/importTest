package com.misfit.syncsdk;

import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.utils.ContextManager;
import com.misfit.syncsdk.utils.SdkConstants;

import java.io.File;

/**
 * Created by Will Hou on 2/1/16.
 */
public class LogTest extends InstrumentationTestCase {
    private final static String TAG = "LogTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ContextManager.getInstance().setContext(getInstrumentation().getContext().getApplicationContext());
    }

    public void testSaveSession() {
        LogSession session = new LogSession(SdkConstants.SYNC_MODE_MANUAL, "2.6.4", "abc");
        File preSessionFile = getSessionFile(session.getId());
        assertTrue(!preSessionFile.exists());
        session.setFirmwareVersion("SH1.0");
        session.setSerialNumber("SH12345678");
        session.save();
        Log.w(TAG, "sessionId=" + session.getId());
        SystemClock.sleep(500); //wait for write to file
        File postSessionFile = getSessionFile(session.getId());
        assertTrue(postSessionFile.exists() && postSessionFile.isFile());
    }

    public void testEventProcedure() {
        LogEvent event = new LogEvent(1, "TEST");
        event.start("parameter");
        SystemClock.sleep(500);
        event.end(LogEvent.RESULT_SUCCESS, "success");
        Log.w(TAG, "expect=500, real=" + event.getDuration());
        assertDuration(event.getDuration(), 500);
    }

    public void testSaveEvent() {
        LogSession session = new LogSession(SdkConstants.SYNC_MODE_MANUAL, "2.6.4", "abc");
        session.setFirmwareVersion("SH1.0");
        session.setSerialNumber("SH12345678");
        session.save();
        Log.w(TAG, "sessionId=" + session.getId());

        LogEvent eventA = new LogEvent(1, "TEST1");
        eventA.start("parameter");
        SystemClock.sleep(400);
        eventA.end(LogEvent.RESULT_SUCCESS, "success");
        Log.w(TAG, "expect=500, real=" + eventA.getDuration());
        assertDuration(eventA.getDuration(), 400);

        LogEvent eventB = new LogEvent(2, "TEST2");
        eventB.start("parameter");
        SystemClock.sleep(1000);
        eventB.end(LogEvent.RESULT_FAILURE, "failure");
        Log.w(TAG, "expect=500, real=" + eventB.getDuration());
        assertDuration(eventB.getDuration(), 1000);

        File preSessionFile = getEventsFile(session.getId());
        assertTrue(!preSessionFile.exists());
        session.appendEvent(eventA);
        session.appendEvent(eventB);
        SystemClock.sleep(500); //wait for write to file
        File postSessionFile = getEventsFile(session.getId());
        assertTrue(postSessionFile.exists() && postSessionFile.isFile());
    }

    private void assertDuration(long duration, int target) {
        assertTrue(duration > target - 5 && duration < target + 5);
    }

    private File getSessionFile(String sessionId) {
        return new File(getInstrumentation().getContext().getFilesDir().getAbsolutePath() + "/session_" + sessionId);
    }

    private File getEventsFile(String sessionId) {
        return new File(getInstrumentation().getContext().getFilesDir().getAbsolutePath() + "/events_" + sessionId);
    }
}
