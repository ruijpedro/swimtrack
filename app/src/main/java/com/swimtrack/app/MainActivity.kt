package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bg = Color.rgb(21, 45, 78)
        val card = Color.rgb(48, 70, 105)
        val card2 = Color.rgb(61, 82, 120)
        val blue = Color.rgb(80, 190, 255)
        val yellow = Color.rgb(255, 220, 45)
        val white = Color.WHITE
        val soft = Color.rgb(200, 215, 235)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bg)

        val scroll = ScrollView(this)
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(28, 40, 28, 30)
        content.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(resources.getIdentifier("ic_launcher", "mipmap", packageName))
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        val title = TextView(this)
        title.text = "SWIMTRACK"
        title.textSize = 34f
        title.setTypeface(Typeface.DEFAULT_BOLD)
        title.setTextColor(white)
        title.gravity = Gravity.CENTER

        val sub = TextView(this)
        sub.text = "NATAÇÃO COMPETITIVA • TEMPOS • TAC"
        sub.textSize = 14f
        sub.setTypeface(Typeface.DEFAULT_BOLD)
        sub.setTextColor(soft)
        sub.gravity = Gravity.CENTER
        sub.setPadding(0, 8, 0, 22)

        val season = TextView(this)
        season.text = "🏊 Constança • Época 2025/2026"
        season.textSize = 16f
        season.setTypeface(Typeface.DEFAULT_BOLD)
        season.setTextColor(yellow)
        season.gravity = Gravity.CENTER
        season.setPadding(18, 12, 18, 12)
        season.setBackgroundColor(card)

        content.addView(icon)
        content.addView(title)
        content.addView(sub)
        content.addView(season)

        content.addView(tabs(yellow, card2, white))
        content.addView(stats(card, white, soft))

        content.addView(sectionTitle("ATLETA", yellow))
        content.addView(infoCard("Nome", "Constança", card2, white, soft))
        content.addView(infoCard("Clube", "A definir", card2, white, soft))
        content.addView(infoCard("N.º Identificação", "A definir", card2, white, soft))
        content.addView(infoCard("Associação", "ANDL — Associação de Natação do Distrito de Leiria", card2, white, soft))
        content.addView(infoCard("Escalão", "Cálculo automático pela idade", card2, white, soft))
        content.addView(infoCard("Resultados relevantes", "A definir após importação do Swimrankings", card2, white, soft))
        content.addView(infoCard("Fontes", "Swimrankings • ANDL • FPN", card2, white, soft))

        content.addView(sectionTitle("AÇÕES", yellow))

        content.addView(actionButton("📥 Importar Swimrankings", blue) {
            Toast.makeText(this, "Importação Swimrankings será ligada na próxima fase.", Toast.LENGTH_LONG).show()
        })

        content.addView(actionButton("📤 Exportar resumo WhatsApp", blue) {
            val msg =
                "🏊‍♀️ SwimTrack\n" +
                "Atleta: Constança\n" +
                "Clube: A definir\n" +
                "N.º Identificação: A definir\n" +
                "Associação: ANDL\n\n" +
                "Tempos • TAC • Evolução\n\n" +
                "Fontes: Swimrankings • ANDL • FPN"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, "Partilhar"))
        })

        content.addView(infoCard("Disclaimer", disclaimer(), card2, white, soft))

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun tabs(yellow: Int, card: Int, white: Int): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 28, 0, 24)

        val names = listOf("Atleta", "Tempos", "TAC", "Evolução", "Mais")

        for (i in names.indices) {
            val t = TextView(this)
            t.text = names[i]
            t.gravity = Gravity.CENTER
            t.textSize = 13f
            t.setTypeface(Typeface.DEFAULT_BOLD)
            t.setTextColor(if (i == 0) Color.rgb(21, 45, 78) else white)
            t.setBackgroundColor(if (i == 0) yellow else card)
            t.setPadding(12, 14, 12, 14)

            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            lp.setMargins(3, 0, 3, 0)
            row.addView(t, lp)
        }

        return row
    }

    private fun stats(card: Int, white: Int, soft: Int): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.HORIZONTAL
        box.setPadding(12, 18, 12, 18)
        box.setBackgroundColor(card)

        box.addView(stat("0", "TEMPOS", white, soft), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        box.addView(stat("0", "TAC OK", white, soft), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        box.addView(stat("0", "OBJETIVOS", white, soft), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        return box
    }

    private fun stat(num: String, label: String, white: Int, soft: Int): LinearLayout {
        val l = LinearLayout(this)
        l.orientation = LinearLayout.VERTICAL
        l.gravity = Gravity.CENTER

        val n = TextView(this)
        n.text = num
        n.textSize = 28f
        n.setTypeface(Typeface.DEFAULT_BOLD)
        n.setTextColor(white)
        n.gravity = Gravity.CENTER

        val s = TextView(this)
        s.text = label
        s.textSize = 12f
        s.setTextColor(soft)
        s.gravity = Gravity.CENTER

        l.addView(n)
        l.addView(s)
        return l
    }

    private fun sectionTitle(text: String, color: Int): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 20f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(color)
        t.setPadding(0, 30, 0, 12)
        return t
    }

    private fun infoCard(title: String, body: String, bg: Int, white: Int, soft: Int): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.setPadding(22, 18, 22, 18)
        c.setBackgroundColor(bg)

        val t = TextView(this)
        t.text = title
        t.textSize = 18f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(white)

        val b = TextView(this)
        b.text = body
        b.textSize = 15f
        b.setTextColor(soft)
        b.setPadding(0, 8, 0, 0)

        c.addView(t)
        c.addView(b)

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 14)
        c.layoutParams = lp

        return c
    }

    private fun actionButton(text: String, color: Int, action: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.textSize = 16f
        b.setTypeface(Typeface.DEFAULT_BOLD)
        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(color)
        b.setPadding(20, 18, 20, 18)
        b.setOnClickListener { action() }
        return b
    }

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Não possui ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Dados previstos: Swimrankings, ANDL e FPN."
    }
}
