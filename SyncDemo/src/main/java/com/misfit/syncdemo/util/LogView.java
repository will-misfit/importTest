package com.misfit.syncdemo.util;

import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.misfit.syncsdk.utils.MLog;

public class LogView extends TextView implements MLog.LogNode {
    public LogView(Context context) {
        super(context);
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(true);
        setHorizontallyScrolling(true);
        setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void clear(){
        setText("");
    }

    @Override
    public void printLog(int priority, String tag, final String msg) {
        ((Activity) getContext()).runOnUiThread((new Thread(new Runnable() {
            @Override
            public void run() {
                // Display the text we just generated within the LogView.
                append("\n");
                append(msg);
                int offset=getLineCount()*getLineHeight();
                if(offset>getHeight()){
                    scrollTo(0,offset-getHeight());
                }
            }
        })));
    }
}
