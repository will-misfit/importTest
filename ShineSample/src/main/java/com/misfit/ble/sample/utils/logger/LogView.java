package com.misfit.ble.sample.utils.logger;

import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * View方式输出
 * <br>
 * author: houxg
 * <br>
 * create on 2015/1/3
 */
public class LogView extends TextView implements MFLog.LogNode {
    public LogView(Context context) {
        super(context);
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(true);
        setHorizontallyScrolling(true);
        setMovementMethod(ScrollingMovementMethod.getInstance());
    }


    @Override
    public void log(int priority, String tag, String content) {
        String priorityStr = MFLog.getPriorityStr(priority);
        final StringBuilder outputBuilder = new StringBuilder();

//        outputBuilder.append(priorityStr).append("\t")
//                .append(tag).append("\t")
//                .append(content).append("\n");
        outputBuilder
                .append(content).append("\n");

        // In case this was originally called from an AsyncTask or some other off-UI thread,
        // make sure the update occurs within the UI thread.
        ((Activity) getContext()).runOnUiThread((new Thread(new Runnable() {
            @Override
            public void run() {
                // Display the text we just generated within the LogView.
                append(outputBuilder.toString());
                int offset=getLineCount()*getLineHeight();
                if(offset>getHeight()){
                    scrollTo(0,offset-getHeight());
                }
            }
        })));
    }
}
