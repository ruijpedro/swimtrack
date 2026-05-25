package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences

    private val bg = Color.rgb(18, 35, 70)
    private val card2 = Color.rgb(61, 82, 120)
    private val blue = Color.rgb(60, 170, 255)
    private val yellow = Color.rgb(255, 220, 45)
    private val white = Color.WHITE
    private val soft = Color.rgb(210, 225, 240)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("swimtrack", MODE_PRIVATE)
        buildScreen()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(34, 40, 34, 40)
        root.setBackgroundColor(bg)
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        val atletaId = input("ID Swimrankings", get("id"))

        root.addView(icon)
        root.addView(title("SWIMTRACK"))
        root.addView(subtitle("TEMPOS • TAC • SWIMRANKINGS"))
        root.addView(atletaId)

        root.addView(button("💾 Guardar ID") {
            prefs.edit().putString("id", atletaId.text.toString().trim()).apply()
            Toast.makeText(this, "ID guardado.", Toast.LENGTH_SHORT).show()
            buildScreen()
        })

        root.addView(button("🌐 Abrir Swimrankings") {
            prefs.edit().putString("id", atletaId.text.toString().trim()).apply()
            abrirSwimrankings()
        })

        root.addView(button("📤 Exportar WhatsApp") {
            exportarWhatsApp()
        })

        root.addView(button("🗑 Limpar dados") {
            prefs.edit().clear().apply()
            buildScreen()
        })

        root.addView(section("ATLETA"))
        root.addView(info("ID Swimrankings", get("id").ifBlank { "Por definir" }))
        root.addView(info("Ligação", linkSwimrankings()))
        root.addView(info("Estado", "Consulta direta no Swimrankings através do botão acima."))

        root.addView(section("DADOS"))
        root.addView(info("Tempos", "Consulta o perfil no Swimrankings e depois adicionamos a importação/extração manual assistida."))
        root.addView(info("TAC", "Preparado para comparar com TAC ANDL, Zonais e FPN."))
        root.addView(info("Recordes", "Preparado para recordes distritais e nacionais por escalão."))

        root.addView(section("DISCLAIMER"))
        root.addView(info("Aviso", disclaimer()))

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun abrirSwimrankings() {
        val url = linkSwimrankings()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun linkSwimrankings(): String {
        val id = get("id")
        return if (id.isBlank()) {
            "https://www.swimrankings.net"
        } else {
            "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"
        }
    }

    private fun exportarWhatsApp() {
        val msg =
            "🏊‍♀️ SwimTrack\n\n" +
            "ID Swimrankings: ${get("id").ifBlank { "Por definir" }}\n" +
            "Perfil: ${linkSwimrankings()}\n\n" +
            "Fontes: Swimrankings • ANDL • FPN"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(Intent.createChooser(intent, "Partilhar"))
    }

    private fun get(key: String): String {
        return prefs.getString(key, "") ?: ""
    }

    private fun title(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 34f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(white)
        t.gravity = Gravity.CENTER
        return t
    }

    private fun subtitle(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 14f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(soft)
        t.gravity = Gravity.CENTER
        t.setPadding(0, 8, 0, 22)
        return t
    }

    private fun section(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 20f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(yellow)
        t.setPadding(0, 30, 0, 12)
        return t
    }

    private fun input(hint: String, value: String = ""): EditText {
        val e = EditText(this)
        e.hint = hint
        e.setText(value)
        e.textSize = 16f
        e.setTextColor(white)
        e.setHintTextColor(soft)
        e.setBackgroundColor(card2)
        e.setPadding(22, 16, 22, 16)

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 14)
        e.layoutParams = lp

        return e
    }

    private fun button(text: String, action: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.textSize = 16f
        b.setTypeface(Typeface.DEFAULT_BOLD)
        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(blue)
        b.setPadding(20, 18, 20, 18)
        b.setOnClickListener { action() }

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 18)
        b.layoutParams = lp

        return b
    }

    private fun info(title: String, body: String): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.setPadding(22, 18, 22, 18)
        c.setBackgroundColor(card2)

        val t = TextView(this)
        t.text = title
        t.textSize = 18f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(white)

        val b = TextView(this)
        b.text = if (body.isBlank()) "Por definir" else body
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

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Não possui ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Os dados poderão ser consultados a partir de fontes públicas: Swimrankings, ANDL e FPN."
    }
}
