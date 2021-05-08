package com.android.yaho.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.yaho.R
import com.android.yaho.dp

class ClimbingDoneDialog(context: Context,
                         private val climbingTimeText: String,
                         private val onClickGoal: () -> Unit,
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_climbing_done)

        findViewById<TextView>(R.id.tvMessage)?.let {
            it.text = context.getString(R.string.dialog_climbing_done_message, climbingTimeText)
        }

        findViewById<ConstraintLayout>(R.id.btnGoal)?.let {
            it.setOnClickListener { onClickGoal.invoke() }
        }

        findViewById<TextView>(R.id.btnCancel)?.let {
            it.setOnClickListener { dismiss() }
        }

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
            setCanceledOnTouchOutside(true)
        }
    }

}