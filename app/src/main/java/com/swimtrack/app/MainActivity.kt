package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 60, 40, 40)
        root.setBackgroundColor(Color.rgb(190, 235, 255))

        val title = TextView(this)
        title.text = "SwimTrack"
        title.textSize = 32f
        title.setTextColor(Color.rgb(16, 32, 51))

        val subtitle = TextView(this)
        subtitle.text = "Gestão pessoal de tempos de natação"
        subtitle.textSize = 18f
        subtitle.setTextColor(Color.rgb(16, 32, 51))

        val info = TextView(this)
        info.text = "v1.0\n\nTempos • TAC • Swimrankings • WhatsApp"
        info.textSize = 16f
        info.setTextColor(Color.rgb(16, 32, 51))

        root.addView(title)
        root.addView(subtitle)
        root.addView(info)

        setContentView(root)
    }
}
