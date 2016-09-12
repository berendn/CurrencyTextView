package nl.qrk.currencytextview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.math.BigDecimal;

/**
 * Created by Berend on 2-4-2016.
 */
public class CurrencyTextView extends TextView {

    private static final String tag = CurrencyTextView.class.getSimpleName();
    private final Rect euroSignTextBounds = new Rect();
    private final Rect euroTextBounds = new Rect();
    private final Rect digitsTextBounds = new Rect();
    private String euroSign = "\u20AC";
    private String comma = ",";
    private String euros = "0";
    private String cents = "00";
    private String euroscomma = euros + comma;
    private float euroSignTextSizeInPixels = 12;
    private float euroTextSizeInPixels = 12;
    private float digitsTextSizeInPixels = 12;
    private Paint euroSignTextPaint;
    private Paint euroTextPaint;
    private Paint digitsTextPaint;

    public CurrencyTextView(Context context) {
        super(context);
        init();
    }

    public CurrencyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CurrencyTextView);
        euroTextSizeInPixels = a.getDimension(R.styleable.CurrencyTextView_euroTextSize, getTextSize());
        euroSignTextSizeInPixels = a.getDimension(R.styleable.CurrencyTextView_eurosignTextSize, getTextSize());
        digitsTextSizeInPixels = a.getDimension(R.styleable.CurrencyTextView_digitsTextSize, getTextSize());
        a.recycle();

        euroSignTextSizeInPixels = Math.min(euroSignTextSizeInPixels, euroTextSizeInPixels);
        digitsTextSizeInPixels = Math.min(digitsTextSizeInPixels, euroTextSizeInPixels);

        init();
    }

    public CurrencyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CurrencyTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init()
    {
        setText(euroSign + euroscomma + cents);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, euroTextSizeInPixels);
        setSingleLine();
        setIncludeFontPadding(false);

        setPaintFlags(Paint.ANTI_ALIAS_FLAG);
        getPaint().setColor(getTextColors().getDefaultColor());

        euroSignTextPaint = new Paint(getPaint());
        euroSignTextPaint.setTextSize(euroSignTextSizeInPixels);

        euroTextPaint = new Paint(getPaint());
        euroTextPaint.setTextSize(euroTextSizeInPixels);

        digitsTextPaint = new Paint(getPaint());
        digitsTextPaint.setTextSize(digitsTextSizeInPixels);

        refreshTextBounds();
    }


    void refreshTextBounds()
    {
        euroSignTextPaint.getTextBounds(euroSign, 0, euroSign.length(), euroSignTextBounds);
        euroTextPaint.getTextBounds(euros, 0, euros.length(), euroTextBounds);
        digitsTextPaint.getTextBounds(cents, 0, cents.length(), digitsTextBounds);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        float baseline = getHeight() - euroTextPaint.descent();
        int x = 0;

        canvas.drawText(euroSign, x, baseline, euroSignTextPaint);

        x += euroSignTextPaint.measureText(euroSign) * 1.5;
        canvas.drawText(euroscomma, x, baseline, euroTextPaint);

        x += euroTextPaint.measureText(euroscomma);
        canvas.drawText(cents, x, baseline - euroTextBounds.height() + digitsTextBounds.height(), digitsTextPaint);
    }

    public void setAmount(BigDecimal b)
    {
        if (b == null)
        {
            return;
        }

        long euro = b.longValue();

        // The remainder of ONE is everything after the comma.
        cents = b.abs().remainder(BigDecimal.ONE).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        // Remove the 0, and keep the change ;)
        cents = cents.substring(2, 4);

        euros = Long.toString(euro);
        if (euro == 0 && b.compareTo(BigDecimal.ZERO) < 0)
        {
            euros = "-" + euros;
        }

        euroscomma = euros + comma;

        refreshTextBounds();
        setText(euroSign + euros + comma + cents);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = 0;
        desiredWidth += euroSignTextPaint.measureText(euroSign) * 1.5;
        desiredWidth += euroTextPaint.measureText(euroscomma);
        desiredWidth += digitsTextPaint.measureText(cents);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY)
        {
            // Must be this size
            width = widthSize;
        }
        else if (widthMode == MeasureSpec.AT_MOST)
        {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        }
        else
        {
            // Be whatever you want
            width = desiredWidth;
        }

        setMeasuredDimension(width, getMeasuredHeight());
    }
}
