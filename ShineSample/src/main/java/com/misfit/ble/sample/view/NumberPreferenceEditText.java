package com.misfit.ble.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import com.misfit.ble.sample.R;
import com.misfit.ble.sample.utils.Preference;


public class NumberPreferenceEditText extends EditText implements Preference<Integer> {

    String mTitle = "";
    private Rect mPrefixRect = new Rect();

    public NumberPreferenceEditText(Context context) {
        super(context);
        init();
    }

    public NumberPreferenceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPreferenceEditText);
        String title = typedArray.getString(R.styleable.NumberPreferenceEditText_title_text);
        typedArray.recycle();
        if (title != null) {
            setTitle(title);
        }
    }

    private void init() {
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getPaint().getTextBounds(mTitle, 0, mTitle.length(), mPrefixRect);
        mPrefixRect.right += getPaint().measureText(" "); // add some offset

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTitle != null) {
            canvas.drawText(mTitle, super.getCompoundPaddingLeft(), getBaseline(), getPaint());
        }
    }

    @Override
    public int getCompoundPaddingLeft() {
        return super.getCompoundPaddingLeft() + mPrefixRect.width();
    }

    @Override
    public void setTitle(String title) {
        mTitle = title;
        invalidate();
    }

    @Override
    public void setValue(Integer value) {
        setText(String.valueOf(value));
    }

    @Override
    public Integer getValue() {
        try {
            return Integer.valueOf(getText().toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
