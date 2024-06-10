package org.telegram.ui

import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextPaint
import android.text.TextUtils
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LiteMode
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.BackDrawable
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.EditTextCell
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCheckCell
import org.telegram.ui.Components.MotionBackgroundDrawable
import kotlin.math.max


class HiddenPasscodeActivity : AppCompatActivity() {
    var btnBack: ImageView? = null
    var holder1: LinearLayout? = null
    var holder2: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hidden_passcode)
        supportActionBar?.hide()
        findViewById<LinearLayout>(R.id.topView).setBackgroundColor(getThemedColor(Theme.key_actionBarDefault))

        btnBack = findViewById(R.id.btnBack)
        btnBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon))

        holder1 = findViewById(R.id.holder1)
        holder2 = findViewById(R.id.holder2)

        if (SharedConfig.hiddenPasscodeHash.length != 0)
            showPasscodeOption()
        else showDisabledOption()

    }

    fun showDisabledOption(isChecked:Boolean=false) {
        holder1?.removeAllViews()
        val headerCell = HeaderCell(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setText(LocaleController.getString("SecurityTitle", R.string.SecurityTitle))
        }
        holder1?.addView(headerCell)


        val textCell = TextCheckCell(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setTextAndCheck(
                LocaleController.getString(R.string.HiddenPasscode),
                isChecked,
                false
            )
            setOnClickListener {
                checkBox.setChecked(!checkBox.isChecked, true)
            }
            checkBox.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    showPasscodeOption()
                    SharedConfig.hiddenPasscodeHash = ""
                    SharedConfig.passByHiddenPasscode = true
                } else {
                    Toast.makeText(this@HiddenPasscodeActivity,"Hidden Passcode cleared!",Toast.LENGTH_SHORT).show()
                    SharedConfig.hiddenPasscodeHash = ""
                    SharedConfig.passByHiddenPasscode = true
                    showDisabledOption()
                }
            }
        }
        holder1?.addView(textCell)
        holder2?.removeAllViews()
    }

    fun showPasscodeOption() {
        showDisabledOption(true)

        val etPasscode = object : EditTextCell(
            this,
            getString(R.string.EnterYourPasscode),
            false,
            4
        ) {}.apply { setNumberTypePassword() }
        holder2?.addView(etPasscode)


        val etPasscodeConfirm = object : EditTextCell(
            this,
            getString(R.string.ReEnterYourPasscode),
            false,
            4
        ) {}.apply {
            setNumberTypePassword()
        }
        holder2?.addView(etPasscodeConfirm)


        holder2?.addView(View(this).apply {
            val param = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            setLayoutParams(param)
        })


        val textView = TextView(this).apply {
            background =
                Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 8f)
            setLines(1)
            setSingleLine(true)
            setGravity(Gravity.CENTER_HORIZONTAL)
            ellipsize = TextUtils.TruncateAt.END
            setGravity(Gravity.CENTER)
            setTextColor(
                Theme.getColor(
                    Theme.key_featuredStickers_buttonText,
                    ThemeDelegate()
                )
            )
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            setTypeface(AndroidUtilities.bold())

            setPadding(10.px, 10.px, 10.px, 10.px)
            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            params.setMargins(16.px, 16.px, 16.px, 16.px)
            setLayoutParams(params)
            setText(getText(R.string.Confirm))

            setOnClickListener {
                if (etPasscode.text.toString() == etPasscodeConfirm.text.toString() && etPasscode.text.length == 4) {
                    SharedConfig.hiddenPasscodeHash = etPasscode.text.toString()
                    SharedConfig.passByHiddenPasscode = true
                    Toast.makeText(
                        this@HiddenPasscodeActivity,
                        "Successfully hidden passcode set",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBackPressedDispatcher.onBackPressed()
                } else if (etPasscode.text.length != 4) {
                    Toast.makeText(
                        this@HiddenPasscodeActivity,
                        "Passcode length must be 4",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (etPasscode.text.toString() != etPasscodeConfirm.text.toString()) {
                    Toast.makeText(
                        this@HiddenPasscodeActivity,
                        "Passcodes do not match!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@HiddenPasscodeActivity,
                        "Try another passcode",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

        holder2?.addView(textView)

    }


    val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()

    val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()


    fun getThemedColor(key: Int): Int {
        return Theme.getColor(key, ThemeDelegate())
    }

    fun setItemsColor(color: Int) {
        if (btnBack != null) {
            val drawable: Drawable? = btnBack?.getDrawable()
            if (drawable is BackDrawable) {
                drawable.setRotatedColor(color)
            } else if (drawable is BitmapDrawable) {
                btnBack?.setColorFilter(
                    PorterDuffColorFilter(
                        color,
                        PorterDuff.Mode.SRC_IN
                    )
                )
            }
        }
    }

    class ThemeDelegate : Theme.ResourcesProvider {
        var parentProvider: Theme.ResourcesProvider? = null
        private val currentColors = SparseIntArray()
        val chat_actionBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val chat_actionBackgroundSelectedPaint =
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val chat_actionBackgroundGradientDarkenPaint =
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val chat_actionTextPaint = TextPaint()
        val chat_actionTextPaint2 = TextPaint()
        val chat_botButtonPaint = TextPaint()
        private var serviceBitmap: Bitmap? = null
        var serviceBitmapShader: BitmapShader? = null
        private var serviceBitmapMatrix: Matrix? = null
        var serviceMessageColorBackup = 0
        var serviceSelectedMessageColorBackup = 0

        init {
            chat_actionTextPaint.textSize =
                AndroidUtilities.dp((max(16.0, SharedConfig.fontSize.toDouble()) - 2).toFloat())
                    .toFloat()
            chat_actionTextPaint2.textSize =
                AndroidUtilities.dp((max(16.0, SharedConfig.fontSize.toDouble()) - 2).toFloat())
                    .toFloat()
            chat_botButtonPaint.textSize = AndroidUtilities.dp(15f).toFloat()
            chat_botButtonPaint.setTypeface(AndroidUtilities.bold())
            chat_actionBackgroundGradientDarkenPaint.setColor(0x15000000)
        }

        override fun getColor(key: Int): Int {
            return if (parentProvider != null) {
                parentProvider!!.getColor(key)
            } else Theme.getColor(key)
        }

        override fun getDrawable(drawableKey: String): Drawable {
            return if (parentProvider != null) {
                parentProvider!!.getDrawable(drawableKey)
            } else Theme.getThemeDrawable(drawableKey)
        }

        override fun getCurrentColor(key: Int): Int {
            return if (parentProvider != null) {
                parentProvider!!.getCurrentColor(key)
            } else super.getCurrentColor(key)
        }

        override fun getPaint(paintKey: String): Paint {
            when (paintKey) {
                Theme.key_paint_chatActionBackground -> return chat_actionBackgroundPaint
                Theme.key_paint_chatActionBackgroundSelected -> return chat_actionBackgroundSelectedPaint
                Theme.key_paint_chatActionBackgroundDarken -> return chat_actionBackgroundGradientDarkenPaint
                Theme.key_paint_chatActionText -> return chat_actionTextPaint
                Theme.key_paint_chatActionText2 -> return chat_actionTextPaint2
                Theme.key_paint_chatBotButton -> return chat_botButtonPaint
            }
            return if (parentProvider != null) {
                parentProvider!!.getPaint(paintKey)
            } else super.getPaint(paintKey)
        }

        override fun hasGradientService(): Boolean {
            return if (parentProvider != null) {
                parentProvider!!.hasGradientService()
            } else Theme.hasGradientService()
        }

        @JvmOverloads
        fun applyChatServiceMessageColor(
            custom: IntArray? = null,
            wallpaperOverride: Drawable? = null,
            currentWallpaper: Drawable? = null,
            overrideIntensity: Float? = null
        ) {
            val serviceColor = getColor(Theme.key_chat_serviceBackground)
            val servicePressedColor = getColor(Theme.key_chat_serviceBackgroundSelected)
            val drawable = wallpaperOverride ?: currentWallpaper!!
            val drawServiceGradient =
                (drawable is MotionBackgroundDrawable || drawable is BitmapDrawable) && SharedConfig.getDevicePerformanceClass() != SharedConfig.PERFORMANCE_CLASS_LOW && LiteMode.isEnabled(
                    LiteMode.FLAG_CHAT_BACKGROUND
                )
            if (drawServiceGradient) {
                var newBitmap: Bitmap? = null
                if (drawable is MotionBackgroundDrawable) {
                    newBitmap = drawable.bitmap
                } else if (drawable is BitmapDrawable) {
                    newBitmap = drawable.bitmap
                }
                if (serviceBitmap != newBitmap) {
                    serviceBitmap = newBitmap
                    serviceBitmapShader =
                        BitmapShader(serviceBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    if (serviceBitmapMatrix == null) {
                        serviceBitmapMatrix = Matrix()
                    }
                }
                chat_actionTextPaint.setColor(-0x1)
                chat_actionTextPaint2.setColor(-0x1)
                chat_actionTextPaint.linkColor = -0x1
                chat_botButtonPaint.setColor(-0x1)
            } else {
                serviceBitmap = null
                serviceBitmapShader = null
                chat_actionTextPaint.setColor(getColor(Theme.key_chat_serviceText))
                chat_actionTextPaint2.setColor(getColor(Theme.key_chat_serviceText))
                chat_actionTextPaint.linkColor = getColor(Theme.key_chat_serviceLink)
            }
            chat_actionBackgroundPaint.setColor(serviceColor)
            chat_actionBackgroundSelectedPaint.setColor(servicePressedColor)
            if (serviceBitmapShader != null && (currentColors.indexOfKey(Theme.key_chat_serviceBackground) < 0 || drawable is MotionBackgroundDrawable || drawable is BitmapDrawable)) {
                var colorMatrix = ColorMatrix()
                if (drawable is MotionBackgroundDrawable) {
                    val intensity = drawable.intensity.toFloat()
                    if (intensity >= 0) {
                        colorMatrix.setSaturation(1.6f)
                        AndroidUtilities.multiplyBrightnessColorMatrix(
                            colorMatrix,
                            if (isDark) .97f else .92f
                        )
                        AndroidUtilities.adjustBrightnessColorMatrix(
                            colorMatrix,
                            if (isDark) +.12f else -.06f
                        )
                    } else {
                        colorMatrix.setSaturation(1.1f)
                        AndroidUtilities.multiplyBrightnessColorMatrix(
                            colorMatrix,
                            if (isDark) .4f else .8f
                        )
                        AndroidUtilities.adjustBrightnessColorMatrix(
                            colorMatrix,
                            if (isDark) +.08f else -.06f
                        )
                    }
                } else {
                    colorMatrix.setSaturation(1.6f)
                    AndroidUtilities.multiplyBrightnessColorMatrix(
                        colorMatrix,
                        if (isDark) .9f else .84f
                    )
                    AndroidUtilities.adjustBrightnessColorMatrix(
                        colorMatrix,
                        if (isDark) -.04f else +.06f
                    )
                }
                if (drawable is MotionBackgroundDrawable) {
                    var intensity = drawable.intensity.toFloat()
                    if (overrideIntensity != null) {
                        intensity = overrideIntensity
                    }
                    if (intensity >= 0) {
                        colorMatrix.setSaturation(1.8f)
                        AndroidUtilities.multiplyBrightnessColorMatrix(colorMatrix, .97f)
                        AndroidUtilities.adjustBrightnessColorMatrix(colorMatrix, +.03f)
                    } else {
                        colorMatrix.setSaturation(0.5f)
                        AndroidUtilities.multiplyBrightnessColorMatrix(colorMatrix, .35f)
                        AndroidUtilities.adjustBrightnessColorMatrix(colorMatrix, +.03f)
                    }
                } else {
                    colorMatrix.setSaturation(1.6f)
                    AndroidUtilities.multiplyBrightnessColorMatrix(colorMatrix, .97f)
                    AndroidUtilities.adjustBrightnessColorMatrix(colorMatrix, +.06f)
                }
                chat_actionBackgroundPaint.setShader(serviceBitmapShader)
                chat_actionBackgroundPaint.setColorFilter(ColorMatrixColorFilter(colorMatrix))
                chat_actionBackgroundPaint.setAlpha(0xff)
                chat_actionBackgroundSelectedPaint.setShader(serviceBitmapShader)
                colorMatrix = ColorMatrix(colorMatrix)
                AndroidUtilities.multiplyBrightnessColorMatrix(colorMatrix, .85f)
                chat_actionBackgroundSelectedPaint.setColorFilter(ColorMatrixColorFilter(colorMatrix))
                chat_actionBackgroundSelectedPaint.setAlpha(0xff)
            } else {
                chat_actionBackgroundPaint.setColorFilter(null)
                chat_actionBackgroundPaint.setShader(null)
                chat_actionBackgroundSelectedPaint.setColorFilter(null)
                chat_actionBackgroundSelectedPaint.setShader(null)
            }
        }

        override fun applyServiceShaderMatrix(
            w: Int,
            h: Int,
            translationX: Float,
            translationY: Float
        ) {
            if ( /*backgroundDrawable == null || */serviceBitmap == null || serviceBitmapShader == null) {
                super.applyServiceShaderMatrix(w, h, translationX, translationY)
            } else {
                Theme.applyServiceShaderMatrix(
                    serviceBitmap,
                    serviceBitmapShader,
                    serviceBitmapMatrix,
                    w,
                    h,
                    translationX,
                    translationY
                )
            }
        }

        override fun isDark(): Boolean {
            return if (parentProvider != null) parentProvider!!.isDark else Theme.isCurrentThemeDark()
        }
    }
}