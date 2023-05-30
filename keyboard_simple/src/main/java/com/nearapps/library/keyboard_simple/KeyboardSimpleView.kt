package com.nearapps.library.keyboard_simple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

open class KeyboardSimpleView : KeyboardView {


    private var isCap = false

    private var isAllCaps = false

    private lateinit var config: Config

    private val paint by lazy { Paint() }

    companion object {
        const val iconRatio = 0.5f

    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes){
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        config = Config(context)

        val a = context.obtainStyledAttributes(attrs, R.styleable.SimpleKeyboardView)

        a.indexCount.let {
            config.run {
                for (i in 0 until it) {
                    when (val attr = a.getIndex(i)) {
                        R.styleable.SimpleKeyboardView_kkbDeleteDrawable -> deleteDrawable = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbCapitalDrawable -> capitalDrawable = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbCapitalLockDrawable -> capitalLockDrawable = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbCancelDrawable -> cancelDrawable = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbCancelDrawable -> spaceDrawable = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_android_labelTextSize -> labelTextSize = a.getDimensionPixelSize(attr, labelTextSize)
                        R.styleable.SimpleKeyboardView_android_keyTextSize -> keyTextSize = a.getDimensionPixelSize(attr, keyTextSize)
                        R.styleable.SimpleKeyboardView_android_keyTextColor -> keyTextColor = a.getColor(attr, keyTextColor)
                        R.styleable.SimpleKeyboardView_kkbKeyIconColor -> keyIconColor = a.getColor(attr, ContextCompat.getColor(context, R.color.keyboard_simple_key_icon_color))
                        R.styleable.SimpleKeyboardView_kkbKeySpecialTextColor -> keySpecialTextColor = a.getColor(attr, keySpecialTextColor)
                        R.styleable.SimpleKeyboardView_kkbKeyDoneTextColor -> keyDoneTextColor = a.getColor(attr, keyDoneTextColor)
                        R.styleable.SimpleKeyboardView_kkbKeyNoneTextColor -> keyNoneTextColor = a.getColor(attr, keyNoneTextColor)
                        R.styleable.SimpleKeyboardView_android_keyBackground -> keyBackground = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbSpecialKeyBackground -> specialKeyBackground = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbDoneKeyBackground -> doneKeyBackground = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbNoneKeyBackground -> noneKeyBackground = a.getDrawable(attr)
                        R.styleable.SimpleKeyboardView_kkbKeyDoneTextSize -> keyDoneTextSize = a.getDimensionPixelSize(attr, keyDoneTextSize)
                        R.styleable.SimpleKeyboardView_kkbKeyDoneText -> keyDoneText = a.getString(attr)
                    }
                }
            }
            a.recycle()
        }

        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

    }

    fun getConfig(): Config {
        return config
    }

    fun setConfig(config: Config) {
        this.config = config
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawKeyboard(canvas, keyboard?.keys)
    }

    private fun drawKeyboard(canvas: Canvas,keys: List<Keyboard.Key>?){
        keys?.let {
            for (key in it) {
                drawKey(canvas, key)
            }
        }
    }

    private fun drawKey(canvas: Canvas, key: Keyboard.Key) {
        when (key.codes[0]) {
            KeyboardSimple.KEYCODE_SHIFT -> drawShiftKey(canvas, key)
            KeyboardSimple.KEYCODE_MODE_CHANGE -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
            KeyboardSimple.KEYCODE_CANCEL -> drawCancelKey(canvas, key)
            KeyboardSimple.KEYCODE_DONE -> drawDoneKey(canvas, key)
            KeyboardSimple.KEYCODE_DELETE -> drawDeleteKey(canvas, key)
            KeyboardSimple.KEYCODE_ALT -> drawAltKey(canvas, key)
            KeyboardSimple.KEYCODE_SPACE -> drawKey(canvas, key, config.keyBackground, config.keyTextColor, config.spaceDrawable)
            KeyboardSimple.KEYCODE_NONE -> drawNoneKey(canvas, key)
            KeyboardSimple.KEYCODE_MODE_BACK -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
            KeyboardSimple.KEYCODE_BACK -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
            KeyboardSimple.KEYCODE_MORE -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
            in -399..-300 -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
            else -> drawKey(canvas, key, config.keyBackground, config.keyTextColor)
        }
    }

    private fun drawCancelKey(canvas: Canvas, key: Keyboard.Key) {
        drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor, config.cancelDrawable)
    }

    private fun drawDoneKey(canvas: Canvas, key: Keyboard.Key) {
        config.keyDoneText?.let {
            key.label = it
        }
        drawKey(canvas, key, config.doneKeyBackground, config.keyDoneTextColor, null, true)
    }

    private fun drawNoneKey(canvas: Canvas, key: Keyboard.Key) {
        drawKey(canvas, key, config.noneKeyBackground, config.keyNoneTextColor)
    }

    private fun drawAltKey(canvas: Canvas, key: Keyboard.Key) {
        drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
    }

    private fun drawDeleteKey(canvas: Canvas, key: Keyboard.Key) {
        drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor, config.deleteDrawable)
    }

    private fun drawShiftKey(canvas: Canvas, key: Keyboard.Key) {
        when {
            isAllCaps ->  drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor, config.capitalLockDrawable)
            isCap -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor, config.capitalDrawable)
            else -> drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor, config.lowerDrawable)
        }
    }

    private fun drawKey(canvas: Canvas, key: Keyboard.Key, keyBackground: Drawable?, textColor: Int, iconDrawable: Drawable? = key.icon, isDone: Boolean = false) { keyBackground?.run {
            if (key.codes[0] != 0) {
                state = key.currentDrawableState
            }

            setBounds(
                key.x.plus(paddingLeft),
                key.y.plus(paddingTop),
                key.x.plus(paddingLeft).plus(key.width),
                key.y.plus(paddingTop).plus(key.height)
            )
            draw(canvas)
        }

        iconDrawable?.run {

            val drawable = DrawableCompat.wrap(this)
            config.keyIconColor?.takeIf { it != 0 }?.let {
                drawable.setTint(it)
            }

            key.icon = drawable

            var iconWidth = key.icon.intrinsicWidth.toFloat()
            var iconHeight = key.icon.intrinsicHeight.toFloat()

            val widthRatio = iconWidth.div(key.width.toFloat())
            val heightRatio = iconHeight.div(key.height.toFloat())

            if (widthRatio <= heightRatio) {

                val ratio = heightRatio.coerceAtMost(iconRatio)
                iconWidth = iconWidth.div(heightRatio).times(ratio)
                iconHeight = iconHeight.div(heightRatio).times(ratio)

            } else {

                val ratio = widthRatio.coerceAtMost(iconRatio)
                iconWidth = iconWidth.div(widthRatio).times(ratio)
                iconHeight = iconHeight.div(widthRatio).times(ratio)

            }

            val left = key.x.plus(paddingLeft).plus(key.width.minus(iconWidth).div(2f)).toInt()
            val top = key.y.plus(paddingTop).plus(key.height.minus(iconHeight).div(2f)).toInt()
            val right = left.plus(iconWidth).toInt()
            val bottom = top.plus(iconHeight).toInt()
            key.icon.setBounds(left, top, right, bottom)
            key.icon.draw(canvas)

        } ?: key.label?.let {
            if (isDone) {
                paint.textSize = config.keyDoneTextSize.toFloat()
            } else if (it.length > 1 && key.codes.size < 2) {
                paint.textSize = config.labelTextSize.toFloat()
            } else {
                paint.textSize = config.keyTextSize.toFloat()
            }
            paint.color = textColor
            paint.typeface = Typeface.DEFAULT

            canvas.drawText(
                it.toString(),
                key.x.plus(paddingLeft).plus(key.width.div(2f)),
                key.y.plus(paddingTop).plus(key.height.div(2.0f)).plus(
                    paint.textSize.minus(paint.descent()).div(2.0f)
                ),
                paint
            )

        }

    }


    fun setCap(isCap: Boolean) {
        this.isCap = isCap
    }

    fun isCap(): Boolean {
        return isCap
    }

    fun setAllCaps(isAllCaps: Boolean) {
        this.isAllCaps = isAllCaps
    }

    fun isAllCaps(): Boolean {
        return isAllCaps
    }

    open class Config(context: Context) {

        var deleteDrawable = context.getDrawable(R.drawable.keyboard_simple_key_delete)
        var lowerDrawable = context.getDrawable(R.drawable.keyboard_simple_key_lower)
        var capitalDrawable = context.getDrawable(R.drawable.keyboard_simple_key_cap)
        var capitalLockDrawable = context.getDrawable(R.drawable.keyboard_simple_key_all_caps)
        var cancelDrawable = context.getDrawable(R.drawable.keyboard_simple_key_cancel)
        var spaceDrawable = context.getDrawable(R.drawable.keyboard_simple_key_space)

        var labelTextSize = context.resources.getDimensionPixelSize(R.dimen.keyboard_simple_label_text_size)

        var keyTextSize = context.resources.getDimensionPixelSize(R.dimen.keyboard_simple_text_size)

        var keyTextColor = ContextCompat.getColor(context, R.color.keyboard_simple_key_text_color)

        var keyIconColor: Int? = null

        var keySpecialTextColor = ContextCompat.getColor(context, R.color.keyboard_simple_key_special_text_color)

        var keyDoneTextColor = ContextCompat.getColor(context, R.color.keyboard_simple_key_done_text_color)

        var keyNoneTextColor = ContextCompat.getColor(context, R.color.keyboard_simple_key_none_text_color)

        var keyBackground = context.getDrawable(R.drawable.keyboard_simple_key_bg)

        var specialKeyBackground = context.getDrawable(R.drawable.keyboard_simple_special_key_bg)

        var doneKeyBackground = context.getDrawable(R.drawable.keyboard_simple_done_key_bg)
        var noneKeyBackground = context.getDrawable(R.drawable.keyboard_simple_none_key_bg)

        var keyDoneTextSize = context.resources.getDimensionPixelSize(R.dimen.keyboard_simple_done_text_size)

        var keyDoneText: CharSequence? = context.getString(R.string.keyboard_simple_key_done_text)

    }


}