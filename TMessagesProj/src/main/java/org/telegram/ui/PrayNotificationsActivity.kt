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

class PrayNotificationsActivity : AppCompatActivity() {
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
            setText("Pray times")
        }

        holder1?.addView(headerCell)
        addMethod("Fajr",0)
        addMethod("Morning",1)
        addMethod("Dhuhur",2)
        addMethod("Asr",3)
        addMethod("Maghrib",4)
        addMethod("Isha",5)
    }
    fun addMethod(name:String,id:Int){
        val method1 = LinearLayout(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener {
                val list = Prefs.prayNotificationsBlockList
                Prefs.prayNotificationsBlockList = if (list.contains("$name,") || list.contains(name)){
                    list.replace("$name,","").replace(name,"")
                }else{
                   "$list$name,"
                }

                finish()
            }
            val textCell = TextCell(this@PrayNotificationsActivity).apply {
                setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                setText(name, true)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }
            addView(textCell)
            if (Prefs.prayNotificationsBlockList.contains(name))
                addView(ImageView(context).apply {
                    setPadding(16.px,0,16.px,0)
                    setImageResource(R.drawable.msg_folders_muted)
                })
            else
                addView(ImageView(context).apply {
                setPadding(16.px,0,16.px,0)
                setImageResource(R.drawable.msg_notifications)
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