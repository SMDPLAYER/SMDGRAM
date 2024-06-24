package org.telegram.ui

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BackDrawable
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCell

class PraySettingsActivity : AppCompatActivity() {
    var btnBack: ImageView? = null
    var holder1: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pray_settings)
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
            setText(LocaleController.getString("PraySettings", R.string.PraySettings))
        }
        val textCell1 = TextCell(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setText("Prayer time calculation method",true)
            setOnClickListener {
                val myIntent = Intent(this@PraySettingsActivity, PrayCalculationMethodActivity::class.java)
                this@PraySettingsActivity.startActivity(myIntent)
            }
        }
        val textCell2 = TextCell(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setText("Change the prayer time",true)
            setOnClickListener {
                val myIntent = Intent(this@PraySettingsActivity, PrayChangeTimeActivity::class.java)
                this@PraySettingsActivity.startActivity(myIntent)
            }
        }
        val textCell3 = TextCell(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setText("Notifications",false)
            setOnClickListener {
                val myIntent = Intent(this@PraySettingsActivity, PrayNotificationsActivity::class.java)
                this@PraySettingsActivity.startActivity(myIntent)
            }
        }
        holder1?.addView(headerCell)
        holder1?.addView(textCell1)
        holder1?.addView(textCell2)
        holder1?.addView(textCell3)
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