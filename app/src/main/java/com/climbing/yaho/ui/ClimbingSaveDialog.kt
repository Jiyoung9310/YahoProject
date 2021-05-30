package com.climbing.yaho.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.climbing.yaho.R
import com.climbing.yaho.dp

class ClimbingSaveDialog(context: Context) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_climbing_save_network)

    }

    override fun onStart() {
        super.onStart()

        val metrics = DisplayMetrics()
        val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        val dialogWidth = (metrics.widthPixels - 24.dp * 2)

        window?.apply {
            setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }
    }

}