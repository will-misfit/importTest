package com.misfit.syncdemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.View;
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
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Clear?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clear();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .create().show();
                return true;
            }
        });
    }

    public void clear() {
        setText("");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MLog.registerLogNode(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MLog.unregisterLogNode(this);
    }

    @Override
    public void printLog(int priority, final String tag, final String msg) {
        ((Activity) getContext()).runOnUiThread((new Thread(new Runnable() {
            @Override
            public void run() {
                // Display the text we just generated within the LogView.
                append("\n");
                append(String.format("%s| %s", tag, msg));
                int offset = getLineCount() * getLineHeight();
                if (offset > getHeight()) {
                    scrollTo(0, offset - getHeight());
                }
            }
        })));
    }
}
