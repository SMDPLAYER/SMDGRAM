package org.telegram.ui

import android.app.AlertDialog
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
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.components.local.Prefs
import org.telegram.ui.ActionBar.BackDrawable
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCell
import org.telegram.ui.Components.TableLayout.LayoutParams

class PrayChangeTimeActivity : AppCompatActivity() {
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
        addMethod("Fajr",Prefs.addTimeFajr){
            Prefs.addTimeFajr = it
        }
        addMethod("Morning",Prefs.addTimeMorning){
            Prefs.addTimeMorning = it
        }
        addMethod("Dhuhur",Prefs.addTimeDhuhur){
            Prefs.addTimeDhuhur = it
        }
        addMethod("Asr",Prefs.addTimeAsr){
            Prefs.addTimeAsr = it
        }
        addMethod("Maghrib",Prefs.addTimeMaghrib){
            Prefs.addTimeMaghrib = it
        }
        addMethod("Isha",Prefs.addTimeIsha){
            Prefs.addTimeIsha = it
        }
    }
    fun addMethod(name:String,selected:Int,block: (Int) -> Unit){
        val method1 = LinearLayout(this).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL

            val textCell = TextCell(this@PrayChangeTimeActivity).apply {
                setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                setText(name, true)
                layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }
            addView(textCell)
            val textCell1 = TextCell(this@PrayChangeTimeActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                )
                setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                setText("0 min", true)
            }
            addView(textCell1)
            setOnClickListener {
                showNumberPickerDialog(selected){number->
                    textCell1.setText("$number min",true)
                    block(number)
                }
            }

        }
        holder1?.addView(method1)
    }

    private fun showNumberPickerDialog(selected:Int,block:(Int)->Unit) {
        // Create a LinearLayout programmatically
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        // Create a NumberPicker programmatically
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 200
            value = selected + 100
            wrapSelectorWheel = false
            displayedValues = Array(201) { (it - 100).toString() }
        }

        // Add the NumberPicker to the LinearLayout
        linearLayout.addView(numberPicker)

        // Create and show an AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Pick a number")
            .setView(linearLayout)
            .setPositiveButton("OK") { dialog, _ ->
                val selectedNumber = numberPicker.value - 100
                // Handle the selected number
                Toast.makeText(this, "Selected number: $selectedNumber", Toast.LENGTH_SHORT).show()
                block(selectedNumber)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
//    private fun showNumberPickerDialog(selected:Int,block:(Int)->Unit) {
//        // Create a LinearLayout programmatically
//        val linearLayout = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//            setPadding(50, 40, 50, 10)
//        }
//
//        // Create a NumberPicker programmatically
//        val numberPicker = NumberPicker(this).apply {
//            minValue = -100
//            maxValue = 100
//            value = selected
//            wrapSelectorWheel = false
//        }
//
//        // Add the NumberPicker to the LinearLayout
//        linearLayout.addView(numberPicker)
//
//        // Create and show an AlertDialog
//        AlertDialog.Builder(this)
//            .setTitle("Pick a number")
//            .setView(linearLayout)
//            .setPositiveButton("OK") { dialog, _ ->
//
//                val selectedNumber = numberPicker.value
//                block(selectedNumber)
//                // Handle the selected number
//                Toast.makeText(this, "Selected number: $selectedNumber", Toast.LENGTH_SHORT).show()
//                dialog.dismiss()
//            }
//            .setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .create()
//            .show()
//    }

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