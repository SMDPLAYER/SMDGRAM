package org.telegram.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.telegram.messenger.AndroidUtilities.dp
import org.telegram.messenger.R

class PrayTimesView @JvmOverloads constructor(
    context: Context,

    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var k = true
    private var height0 = 50f
    private val holderPrayTimes:LinearLayout

    init {
        // Inflate XML layout
        LayoutInflater.from(context).inflate(R.layout.view_pray_times, this, true)
        holderPrayTimes = findViewById(R.id.holderPrayTimes)
        holderPrayTimes.setOnClickListener {
            val lp1 = holderPrayTimes.layoutParams

            height0 = if (k)50f  else 50f

            lp1.height = dp(height0)

            holderPrayTimes.layoutParams = lp1

            k = !k
            requestLayout()
        }
    }



    fun getHeight1(): Int {
        return dp(height0)
    }

}