package com.nearapps.library.keyboard_simple

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.XmlRes

open class KeyboardSimple {

    private lateinit var context: Context

    private var isCap = false

    private var isAllCaps = false

    private var keyboardType = KeyboardType.NORMAL

    private val keyboardNormal by lazy { Keyboard(context, R.xml.keyboard_simple_normal) }
    private val keyboardNormalModeChange by lazy { Keyboard(context, R.xml.keyboard_simple_normal_mode_change) }
    private val keyboardNormalMore by lazy { Keyboard(context, R.xml.keyboard_simple_normal_more_symbol) }

    private val keyboardLetter by lazy { Keyboard(context, R.xml.keyboard_simple_letter) }

    private val keyboardLowercaseLetter by lazy { Keyboard(context, R.xml.keyboard_simple_lowercase_letter_only) }

    private val keyboardUppercaseLetter by lazy { Keyboard(context, R.xml.keyboard_simple_uppercase_letter_only) }

    private val keyboardLetterNumber by lazy { Keyboard(context, R.xml.keyboard_simple_letter_number) }

    private val keyboardNumber by lazy { Keyboard(context, R.xml.keyboard_simple_number) }

    private val keyboardNumberDecimal by lazy { Keyboard(context, R.xml.keyboard_simple_number_decimal) }

    private val keyboardPhone by lazy { Keyboard(context, R.xml.keyboard_simple_phone) }

    private var keyboardCustom: Keyboard? = null
    private var keyboardCustomModeChange: Keyboard? = null
    private var keyboardCustomMore: Keyboard? = null

    private var currentKeyboard: Keyboard

    private lateinit var keyboardViewGroup: View
    private var keyboardView: KeyboardSimpleView? = null

    private var currentEditText: EditText? = null


    private val editTextArray by lazy {
        SparseArray<EditText>()
    }
    private val keyboardTypeArray by lazy {
        SparseArray<Int>()
    }

    private lateinit var showAnimation: Animation
    private lateinit var hideAnimation: Animation

    private lateinit var onTouchListener: View.OnTouchListener
    private lateinit var globalFocusChangeListener: ViewTreeObserver.OnGlobalFocusChangeListener

    private var onKeyboardActionListener: KeyboardView.OnKeyboardActionListener? = null
    private var onKeyDoneListener: OnKeyListener? = null
    private var onKeyCancelListener: OnKeyListener? = null
    private var onKeyExtraListener: OnKeyListener? = null

    private var vibrator: Vibrator? = null

    private var audioManager: AudioManager? = null

    private var isVibrationEffect = false
    private var isPlaySoundEffect = false
    private var isBringToFront = false

    companion object{
        private const val TAG = "SimpleKeyboard"
        private const val ANIM_DURATION_TIME = 200L
        //-----------------------------------------------
        const val KEYCODE_SHIFT = -1
        const val KEYCODE_MODE_CHANGE = -2
        const val KEYCODE_CANCEL = -3
        const val KEYCODE_DONE = -4
        const val KEYCODE_DELETE = -5
        const val KEYCODE_ALT = -6
        const val KEYCODE_SPACE = 32
        const val KEYCODE_NONE = 0
        //-----------------------------------------------
        const val KEYCODE_MODE_BACK = -101
        const val KEYCODE_BACK = -102
        const val KEYCODE_MORE = -103
        //-----------------------------------------------
        const val KEYCODE_SIMPLE_SHIFT = -201
        const val KEYCODE_SIMPLE_MODE_CHANGE = -202
        const val KEYCODE_SIMPLE_CANCEL = -203
        const val KEYCODE_SIMPLE_DONE = -204
        const val KEYCODE_SIMPLE_DELETE = -205
        const val KEYCODE_SIMPLE_ALT = -206
        //-----------------------------------------------
        const val KEYCODE_SIMPLE_MODE_BACK = -251
        const val KEYCODE_SIMPLE_BACK = -252
        const val KEYCODE_SIMPLE_MORE = -253
    }

    constructor(dialog: Dialog, keyboardParentView: ViewGroup? = null):
            this(dialog.window!!, keyboardParentView)

    constructor(activity: Activity, keyboardParentView: ViewGroup? = null):
            this(activity.window, keyboardParentView)

    constructor(window: Window, keyboardParentView: ViewGroup? = null):
            this(window.context,
                window.decorView.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup,
                keyboardParentView
            )

    constructor(context: Context, rootView: ViewGroup, keyboardParentView: ViewGroup?):
            this(context,
                rootView,
                keyboardParentView,
                LayoutInflater.from(context).inflate(R.layout.keyboard_simple_container, null),
                R.id.keyboardView
            )

    constructor(context: Context, rootView: ViewGroup, keyboardParentView: ViewGroup?, keyboardContainer: View, @IdRes keyboardViewId: Int) {
        this.context = context
        currentKeyboard = keyboardNormal
        initKeyboardView(context)
        initKeyboardView(rootView,keyboardParentView,keyboardContainer,keyboardViewId)

    }


    open fun initKeyboardView(context: Context){

    }

    private fun initKeyboardView(rootView: ViewGroup,keyboardParentView: ViewGroup?,keyboardContainer: View,@IdRes keyboardViewId: Int){
        //初始化键盘相关
        keyboardViewGroup = keyboardContainer
        keyboardView = keyboardViewGroup.findViewById(keyboardViewId)

        keyboardView?.let {
            it.keyboard = currentKeyboard
            it.isEnabled = true
            it.isPreviewEnabled = false
            it.onKeyboardActionListener = object: KeyboardView.OnKeyboardActionListener{

                override fun swipeRight() {
                    onKeyboardActionListener?.swipeRight()
                }

                override fun onPress(primaryCode: Int) {
                    onKeyboardActionListener?.onPress(primaryCode)
                }

                override fun onRelease(primaryCode: Int) {
                    onKeyboardActionListener?.onRelease(primaryCode)
                }

                override fun swipeLeft() {
                    onKeyboardActionListener?.swipeLeft()
                }

                override fun swipeUp() {
                    onKeyboardActionListener?.swipeUp()
                }

                override fun swipeDown() {
                    onKeyboardActionListener?.swipeDown()
                }

                override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
                    if(primaryCode != 0){
                        playSoundEffect()
                        sendVibrationEffect()
                    }
                    performKey(primaryCode, keyCodes)
                }

                override fun onText(text: CharSequence?) {
                    onKeyboardActionListener?.onText(text)
                }
            }

            isCap = it.isCap()
            isAllCaps = it.isAllCaps()

            keyboardViewGroup.isVisible = false
        }

        showAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
        showAnimation.duration = ANIM_DURATION_TIME

        hideAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f)
        hideAnimation.duration = ANIM_DURATION_TIME

        hideAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                if(keyboardViewGroup.isVisible){
                    keyboardViewGroup.isVisible = false
                }
            }
            override fun onAnimationStart(animation: Animation?) {}
        })

        onTouchListener = View.OnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_UP){
                viewFocus(v)
            }
            false
        }

        globalFocusChangeListener = ViewTreeObserver.OnGlobalFocusChangeListener {oldFocus, newFocus ->
            if(newFocus is EditText){
                if(editTextArray.containsKey(newFocus.id)){
                    viewFocus(newFocus)
                    hide()
                }
            }
        }

        if(keyboardParentView != null){
            keyboardParentView.addView(keyboardViewGroup)
        }else{
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM
            rootView.addView(keyboardViewGroup, params)
        }

        rootView.viewTreeObserver.addOnGlobalFocusChangeListener(globalFocusChangeListener)
    }

    fun sendKey(primaryCode: Int){
        if(primaryCode < 0){
            performKey(primaryCode, IntArray(1) { primaryCode })
        }
    }

    private fun performKey(primaryCode: Int, keyCodes: IntArray?){
        when(primaryCode){
            KEYCODE_SHIFT -> keyShift()
            KEYCODE_MODE_CHANGE -> keyModeChange()
            KEYCODE_CANCEL -> keyCancel(primaryCode)
            KEYCODE_DONE -> keyDone(primaryCode)
            KEYCODE_DELETE -> keyDelete()
            KEYCODE_ALT -> keyAlt()
            KEYCODE_MODE_BACK -> keyBack(false)
            KEYCODE_BACK -> keyBack(true)
            KEYCODE_MORE -> keyMore()
            KEYCODE_SIMPLE_SHIFT -> keyShift()
            KEYCODE_SIMPLE_MODE_CHANGE -> keyModeChange()
            KEYCODE_SIMPLE_CANCEL -> keyCancel(primaryCode)
            KEYCODE_SIMPLE_DONE -> keyDone(primaryCode)
            KEYCODE_SIMPLE_DELETE -> keyDelete()
            KEYCODE_SIMPLE_ALT -> keyAlt()
            KEYCODE_SIMPLE_MODE_BACK -> keyBack(false)
            KEYCODE_SIMPLE_BACK -> keyBack(true)
            KEYCODE_SIMPLE_MORE -> keyMore()
            in -999..-300 -> keyExtra(primaryCode)
            in 32..Int.MAX_VALUE -> keyInput(primaryCode)
            else -> Log.w(TAG,"primaryCode:$primaryCode")
        }
        //Log.w(TAG,"--> primaryCode:$primaryCode keyCodes:${keyCodes?.contentToString()}")
        onKeyboardActionListener?.onKey(primaryCode,keyCodes)
    }

    fun setKeyboardCustom(keyboard: Keyboard){
        this.keyboardCustom = keyboard
    }

    fun setKeyboardCustomModeChange(keyboard: Keyboard){
        this.keyboardCustomModeChange = keyboard
    }

    fun setKeyboardCustomMore(keyboard: Keyboard){
        this.keyboardCustomMore = keyboard
    }

    fun setKeyboardCustom(@XmlRes xmlLayoutResId: Int){
        this.keyboardCustom = Keyboard(context,xmlLayoutResId)
    }

    fun setKeyboardCustomModeChange(@XmlRes xmlLayoutResId: Int){
        this.keyboardCustomModeChange = Keyboard(context,xmlLayoutResId)
    }

    fun setKeyboardCustomMore(@XmlRes xmlLayoutResId: Int){
        this.keyboardCustomMore = Keyboard(context,xmlLayoutResId)
    }

    fun getKeyboardType(): Int{
        return keyboardType
    }

    private fun disableShowSoftInput(editText: EditText){
        try {
            val method = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.java)
            method.isAccessible = true
            method.invoke(editText, false)
        } catch (e: Exception) {
            editText.inputType = InputType.TYPE_NULL
        }
    }

    private fun viewFocus(v: View){
        if(v is EditText){
            v.hideSystemInputMethod()
            disableShowSoftInput(v)
            if(v.hasFocus()){
                currentEditText = v
                keyboardType = keyboardTypeArray[v.id]!!
                switchKeyboard()
                show()
            }
        }
    }

    fun register(editText: EditText,keyboardType: Int) {
        editTextArray[editText.id] = editText
        keyboardTypeArray[editText.id] = keyboardType
        editText.setOnTouchListener(onTouchListener)
    }

    fun onResume(){
        currentEditText?.let {
            if(it.hasFocus()){
                it.postDelayed({ it.hideSystemInputMethod() },100)
            }
        }
        isPlaySoundEffect = querySoundEffectsEnabled()
    }

    fun onDestroy(){
        currentEditText?.let {
            it.clearAnimation()
            currentEditText = null
        }
        editTextArray.clear()
        keyboardTypeArray.clear()
    }

    fun isShow(): Boolean{
        return keyboardViewGroup.isVisible
    }

    private fun show(){
        if(!keyboardViewGroup.isVisible){
            keyboardViewGroup.apply {
                isVisible = true
                if(isBringToFront){
                    bringToFront()
                }
                clearAnimation()
                startAnimation(showAnimation)
            }
        }
    }

    open fun hide(){
        if(keyboardViewGroup.isVisible){
            keyboardViewGroup.apply {
                clearAnimation()
                startAnimation(hideAnimation)
            }
        }
    }

    fun setBackground(drawable: Drawable?){
        drawable?.let {
            keyboardViewGroup.background = drawable
        }
    }

    fun setBackgroundResource(drawableId: Int){
        keyboardViewGroup.setBackgroundResource(drawableId)
    }

    fun getKeyboardView(): KeyboardSimpleView? {
        return keyboardView
    }

    fun getKeyboardViewConfig(): KeyboardSimpleView.Config?{
        return keyboardView?.getConfig()
    }

    fun setKeyboardViewConfig(config: KeyboardSimpleView.Config){
        keyboardView?.setConfig(config)
    }

    //----------------------------------

    private fun isSoundEffectsEnabled(): Boolean{
        return isPlaySoundEffect
    }

    private fun setSoundEffectEnabled(soundEffectEnabled: Boolean){
        this.isPlaySoundEffect = soundEffectEnabled
        setSoundEffectsEnabled(isPlaySoundEffect)
    }

    fun isVibrationEffectEnabled(): Boolean{
        return isVibrationEffect
    }

    fun setVibrationEffectEnabled(vibrationEffectEnabled: Boolean){
        this.isVibrationEffect = vibrationEffectEnabled
    }

    fun setBringToFront(bringToFront: Boolean){
        this.isBringToFront = bringToFront
    }

    fun isBringToFront() = isBringToFront

    fun setOnKeyboardActionListener(listener: KeyboardView.OnKeyboardActionListener?){
        this.onKeyboardActionListener = listener
    }

    fun setOnKeyDoneListener(listener: OnKeyListener?){
        this.onKeyDoneListener = listener
    }

    fun setOnKeyCancelListener(listener: OnKeyListener?){
        this.onKeyCancelListener = listener
    }

    fun setOnKeyExtraListener(listener: OnKeyListener?){
        this.onKeyExtraListener = listener
    }

    interface OnKeyListener{
        fun onKey(editText: View?,primaryCode: Int)
    }

    //----------------------------------

    private fun switchKeyboard(){
        when(keyboardType){
            KeyboardType.NORMAL -> {
                currentKeyboard = keyboardNormal
            }
            KeyboardType.NORMAL_MODE_CHANGE -> {
                currentKeyboard = keyboardNormalModeChange
            }
            KeyboardType.NORMAL_MORE -> {
                currentKeyboard = keyboardNormalMore
            }
            KeyboardType.LETTER -> {
                currentKeyboard = keyboardLetter
            }
            KeyboardType.LOWERCASE_LETTER_ONLY -> {
                currentKeyboard = keyboardLowercaseLetter
            }
            KeyboardType.UPPERCASE_LETTER_ONLY -> {
                currentKeyboard = keyboardUppercaseLetter
            }
            KeyboardType.LETTER_NUMBER -> {
                currentKeyboard = keyboardLetterNumber
            }
            KeyboardType.NUMBER -> {
                currentKeyboard = keyboardNumber
            }
            KeyboardType.NUMBER_DECIMAL -> {
                currentKeyboard = keyboardNumberDecimal
            }
            KeyboardType.PHONE -> {
                currentKeyboard = keyboardPhone
            }
            KeyboardType.CUSTOM -> {
                currentKeyboard = keyboardCustom ?: keyboardNormal
            }
            KeyboardType.CUSTOM_MODE_CHANGE -> {
                currentKeyboard = keyboardCustomModeChange ?: keyboardNormalModeChange
            }
            KeyboardType.CUSTOM_MORE -> {
                currentKeyboard = keyboardCustomMore ?: keyboardNormalMore
            }

        }

        keyboardView?.run {
            keyboard = currentKeyboard
        }

    }

    private fun keyModeChange(){
        when(keyboardType){
            KeyboardType.NORMAL -> {
                keyboardType = KeyboardType.NORMAL_MODE_CHANGE
            }
            KeyboardType.CUSTOM -> {
                keyboardType = KeyboardType.CUSTOM_MODE_CHANGE
            }
            KeyboardType.CUSTOM_MORE -> {
                keyboardType = KeyboardType.CUSTOM_MODE_CHANGE
            }
        }

        switchKeyboard()
    }

    private fun keyCancel(primaryCode: Int){
        hide()
        onKeyCancelListener?.onKey(currentEditText,primaryCode)
    }

    private fun keyDone(primaryCode: Int){
        hide()
        onKeyDoneListener?.onKey(currentEditText,primaryCode)
    }

    private fun keyAlt(){

    }

    private fun keyBack(isBack: Boolean){
        when(keyboardType){
            KeyboardType.NORMAL_MODE_CHANGE -> {
                keyboardType = KeyboardType.NORMAL
            }
            KeyboardType.NORMAL_MORE -> {
                keyboardType = if(isBack) KeyboardType.NORMAL else KeyboardType.NORMAL_MODE_CHANGE
            }
            KeyboardType.CUSTOM_MODE_CHANGE -> {
                keyboardType = KeyboardType.CUSTOM
            }
            KeyboardType.CUSTOM_MORE -> {
                keyboardType = if(isBack) KeyboardType.CUSTOM else KeyboardType.CUSTOM_MODE_CHANGE
            }
        }

        switchKeyboard()
    }

    private fun keyMore(){

        when(keyboardType){
            KeyboardType.NORMAL ->              keyboardType = KeyboardType.NORMAL_MORE
            KeyboardType.NORMAL_MODE_CHANGE ->  keyboardType = KeyboardType.NORMAL_MORE
            KeyboardType.CUSTOM ->              keyboardType = KeyboardType.CUSTOM_MORE
            KeyboardType.CUSTOM_MODE_CHANGE ->  keyboardType = KeyboardType.CUSTOM_MORE
        }
        switchKeyboard()
    }

    private fun keyInput(primaryCode: Int){
        currentEditText?.let {
            val start = it.selectionStart
            val end = it.selectionEnd

            it.text?.replace(start,end, primaryCode.toChar().toString())
            if(isCap && !isAllCaps){
                isCap = false
                isAllCaps = false
                toLowerCaseKey(currentKeyboard)

                keyboardView?.run {
                    setCap(isCap)
                    setAllCaps(isAllCaps)
                    keyboard = currentKeyboard
                }

            }

        }
    }

    private fun querySoundEffectsEnabled(): Boolean {
        return Settings.System.getInt(context.contentResolver,
            Settings.System.SOUND_EFFECTS_ENABLED, 0) != 0
    }

    private fun setSoundEffectsEnabled(enabled: Boolean){
        Settings.System.putInt(context.contentResolver,
            Settings.System.SOUND_EFFECTS_ENABLED, if(enabled) 1 else 0)
    }

    private fun playSoundEffect(effectType: Int = AudioManager.FX_KEYPRESS_STANDARD){
        if(isPlaySoundEffect){
            try{
                if(audioManager == null){
                    audioManager = (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)
                }
                audioManager?.playSoundEffect(effectType)
            }catch (e: Exception){
                Log.w(TAG,e)
            }
        }
    }

    private fun sendVibrationEffect(){
        if(isVibrationEffect){
            try {
                if(vibrator == null){
                    vibrator = (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
                }
                vibrator?.let {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else {
                        it.vibrate(16L)
                    }
                }
            }catch (e: Exception){
                Log.w(TAG,e)
            }
        }
    }

    private fun keyDown(keycode: Int,action: Int = KeyEvent.ACTION_DOWN){
        currentEditText?.let {
            if(it.text.isNotEmpty()){
                it.onKeyDown(keycode, KeyEvent(action,keycode))
            }
        }
    }

    private fun keyDelete(){
        keyDown(KeyEvent.KEYCODE_DEL)
    }

    private fun keyExtra(primaryCode: Int){
        Log.d(TAG,"primaryCode:$primaryCode")
        onKeyExtraListener?.onKey(currentEditText,primaryCode)
    }

    private fun keyShift(){
        if(isAllCaps){
            toLowerCaseKey(currentKeyboard)
            toUpperCaseKey(currentKeyboard)
        }

        when {
            isAllCaps -> {
                isAllCaps = false
                isCap = false
            }
            isCap -> {
                isAllCaps = true
            }
            else -> {
                isCap = true
                isAllCaps = false
            }
        }

        keyboardView?.let {
            it.setCap(isCap)
            it.setAllCaps(isAllCaps)
            it.keyboard = currentKeyboard
        }



    }

    private fun toUpperCaseKey(keyboard: Keyboard){
        keyboard.run {
            for(key in keys){
                if(key.label?.length == 1){
                    val c = key.label.toString()[0]
                    if(c.isLowerCase()){
                        val letter = c.toUpperCase()
                        key.label = letter.toString()
                        key.codes[0] = letter.toInt()
                    }
                }
            }
        }
    }

    private fun toLowerCaseKey(keyboard: Keyboard){
        keyboard.run {
            for(key in keys){
                if(key.label?.length == 1){
                    val c = key.label.toString()[0]
                    if(c.isUpperCase()){
                        val letter = c.toLowerCase()
                        key.label = letter.toString()
                        key.codes[0] = letter.toInt()
                    }
                }
            }
        }
    }


    object KeyboardType{
        const val           NORMAL                  = 0x00000001
        internal const val  NORMAL_MODE_CHANGE      = 0x00000002
        internal const val  NORMAL_MORE             = 0x00000003
        const val           LETTER                  = 0x00000011
        const val           LOWERCASE_LETTER_ONLY   = 0x00000101
        const val           UPPERCASE_LETTER_ONLY   = 0x00000102
        const val           LETTER_NUMBER           = 0x00000201
        const val           NUMBER                  = 0x00000301
        const val           NUMBER_DECIMAL          = 0x00000302
        const val           PHONE                   = 0x00000303
        const val           CUSTOM                  = 0x00001001
        const val           CUSTOM_MODE_CHANGE      = 0x00001002
        const val           CUSTOM_MORE             = 0x00001003
    }
}