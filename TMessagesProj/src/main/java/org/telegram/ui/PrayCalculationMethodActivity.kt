package org.telegram.ui

import android.content.Intent
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.components.local.Prefs
import org.telegram.ui.ActionBar.BackDrawable
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCell
import org.telegram.ui.Components.TableLayout.LayoutParams

class PrayCalculationMethodActivity : AppCompatActivity() {
    var btnBack: ImageView? = null
    var holder1: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pray_calculation_method)
        supportActionBar?.hide()
        findViewById<LinearLayout>(R.id.topView).setBackgroundColor(getThemedColor(Theme.key_actionBarDefault))

        btnBack = findViewById(R.id.btnBack)
        btnBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon))

        holder1 = findViewById(R.id.holder1)
        holder1?.removeAllViews()
        val headerCell = HeaderCell(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setText("Method")
        }

        holder1?.addView(headerCell)
        addMethod("Shia Ithna-Ashari, Leva Institute, Qum",0)
        addMethod("University of Islamic Sciences, Karachi",1)
        addMethod("Islamic Society of North America",2)
        addMethod("Muslim World League",3)
        addMethod("Umm Al-Qura University, Makkah",4)
        addMethod("Egyptian General Authority of Survey",5)
        addMethod("Institute of Geophysics, University of Tehran",7)
        addMethod("Gulf Region",8)
        addMethod("Kuwait",9)
        addMethod("Qatar",10)
        addMethod("Majlis Ugama Islam Singapura, Singapore",11)
        addMethod("Union Organization islamic de France",12)
        addMethod("Diyanet İşleri Başkanlığı, Turkey",13)
        addMethod("Spiritual Administration of Muslims of Russia",14)
        addMethod("Moonsighting Committee Worldwide (also requires shafaq parameter)",15)
        addMethod("Dubai (unofficial)",16)
        addMethod("Custom",99)
    }
    fun addMethod(name:String,id:Int){
        val method1 = LinearLayout(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener {
                Prefs.prayCalculationMethod = id
                finish()
            }
            val textCell = TextCell(this@PrayCalculationMethodActivity).apply {
                setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                setText(name, true)
                layoutParams=LinearLayout.LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }
            addView(textCell)
            if (Prefs.prayCalculationMethod==id)
                addView(ImageView(context).apply {
                    setPadding(16.px,0,16.px,0)
                    setImageResource(R.drawable.img_selected_dark)
                })
        }
        holder1?.addView(method1)
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
}
val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()