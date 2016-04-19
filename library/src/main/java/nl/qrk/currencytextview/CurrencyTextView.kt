package nl.qrk.currencytextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import java.math.BigDecimal

/**
 * Created by Berend on 2-4-2016.
 */
class CurrencyTextView : TextView {
    private val euroSignTextBounds = Rect()
    private val euroTextBounds = Rect()
    private val digitsTextBounds = Rect()
    private val euroSign = "\u20AC"
    private val comma = ","
    private var euros = "0"
    private var cents = "00"
    private var euroscomma = euros + comma
    private var euroSignTextSizeInPixels = 12f
    private var euroTextSizeInPixels = 12f
    private var digitsTextSizeInPixels = 12f
    private var euroSignTextPaint: Paint? = null
    private var euroTextPaint: Paint? = null
    private var digitsTextPaint: Paint? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.CurrencyTextView)
        euroTextSizeInPixels = a.getDimension(R.styleable.CurrencyTextView_euroTextSize, textSize)
        euroSignTextSizeInPixels = a.getDimension(R.styleable.CurrencyTextView_eurosignTextSize, textSize)
        digitsTextSizeInPixels = a.getDimension(R.styleable.CurrencyTextView_digitsTextSize, textSize)
        a.recycle()

        euroSignTextSizeInPixels = Math.min(euroSignTextSizeInPixels, euroTextSizeInPixels)
        digitsTextSizeInPixels = Math.min(digitsTextSizeInPixels, euroTextSizeInPixels)

        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        text = euroSign + euroscomma + cents
        setTextSize(TypedValue.COMPLEX_UNIT_PX, euroTextSizeInPixels)
        setSingleLine()
        includeFontPadding = false

        paintFlags = Paint.ANTI_ALIAS_FLAG
        paint.color = textColors.defaultColor
        paint.typeface = Typeface.createFromAsset(context.assets, "AlexBrush-Regular.ttf");

        euroSignTextPaint = Paint(paint)
        euroSignTextPaint!!.textSize = euroSignTextSizeInPixels

        euroTextPaint = Paint(paint)
        euroTextPaint!!.textSize = euroTextSizeInPixels
        euroTextPaint!!.typeface = typeface

        digitsTextPaint = Paint(paint)
        digitsTextPaint!!.textSize = digitsTextSizeInPixels

        refreshTextBounds()
    }


    internal fun refreshTextBounds() {
        euroSignTextPaint!!.getTextBounds(euroSign, 0, euroSign.length, euroSignTextBounds)
        euroTextPaint!!.getTextBounds(euros, 0, euros.length, euroTextBounds)
        digitsTextPaint!!.getTextBounds(cents, 0, cents.length, digitsTextBounds)
    }

    override fun onDraw(canvas: Canvas) {
        val baseline = height - euroTextPaint!!.descent()
        var x = 0

        canvas.drawText(euroSign, x.toFloat(), baseline, euroSignTextPaint)

        x += (euroSignTextPaint!!.measureText(euroSign) * 1.5).toInt()
        canvas.drawText(euroscomma, x.toFloat(), baseline, euroTextPaint)

        x += euroTextPaint!!.measureText(euroscomma).toInt()
        canvas.drawText(cents, x.toFloat(), baseline - euroTextBounds.height() + digitsTextBounds.height(), digitsTextPaint)
    }

    override fun setTypeface(tf : Typeface){
        super.setTypeface(tf)
    }

    fun setAmount(b: BigDecimal?) {
        if (b == null) {
            return
        }

        val euro = b.toLong()

        // The remainder of ONE is everything after the comma.
        cents = b.abs().remainder(BigDecimal.ONE).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        // Remove the 0, and keep the change ;)
        cents = cents.substring(2, 4)

        euros = java.lang.Long.toString(euro)
        if (euro == 0L && b.compareTo(BigDecimal.ZERO) < 0) {
            euros = "-" + euros
        }

        euroscomma = euros + comma

        refreshTextBounds()
        text = euroSign + euros + comma + cents
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var desiredWidth = 0
        desiredWidth += (euroSignTextPaint!!.measureText(euroSign) * 1.5).toInt()
        desiredWidth += euroTextPaint!!.measureText(euroscomma).toInt()
        desiredWidth += digitsTextPaint!!.measureText(cents).toInt()

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        val width: Int

        // Measure Width
        if (widthMode == View.MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize)
        } else {
            // Be whatever you want
            width = desiredWidth
        }

        setMeasuredDimension(width, measuredHeight)
    }

    companion object {

        private val tag = CurrencyTextView::class.java.simpleName
    }
}
