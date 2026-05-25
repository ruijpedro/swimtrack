package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.view.Gravity
import android.widget.*
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var root: LinearLayout

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

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(34, 40, 34, 40)
        root.setBackgroundColor(bg)
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        root.addView(icon)
        root.addView(title("SWIMTRACK"))
        root.addView(subtitle("IMPORTAÇÃO SWIMRANKINGS • TAC • TEMPOS"))

        val atletaId = input("ID Swimrankings", get("id"))
        root.addView(atletaId)

        root.addView(button("📥 IMPORTAR SWIMRANKINGS") {

            val id = atletaId.text.toString().trim()

            prefs.edit().putString("id", id).apply()

            importarSwimrankings(id)

        })

        root.addView(button("📤 EXPORTAR WHATSAPP") {

            exportarWhatsApp()

        })

        root.addView(button("🗑 LIMPAR DADOS") {

            prefs.edit().clear().apply()

            buildScreen()

        })

        root.addView(section("PERFIL IMPORTADO"))

        root.addView(info("Estado", get("estado")))
        root.addView(info("Nome", get("nome_importado")))
        root.addView(info("Clube", get("clube_importado")))
        root.addView(info("URL", get("ultimo_url")))

        root.addView(section("TEMPOS IMPORTADOS"))

        val tempos = get("tempos_importados")

        if (tempos.isBlank()) {

            root.addView(
                info(
                    "Sem tempos",
                    "Carrega em IMPORTAR SWIMRANKINGS."
                )
            )

        } else {

            tempos.split(";;").forEach {

                val p = it.split("|")

                if (p.size >= 3) {

                    root.addView(
                        info(
                            p[0],
                            "Tempo: ${p[1]}\n" +
                                    "Data: ${p[2]}\n" +
                                    "TAC: ${compararTempo(p[1], tacBase(p[0]))}"
                        )
                    )

                }

            }

        }

        root.addView(section("FONTES"))

        root.addView(
            info(
                "Fontes",
                "Swimrankings • ANDL • FPN\n" +
                        "Sem ligação oficial às entidades."
            )
        )

        scroll.addView(root)

        setContentView(scroll)

    }

    private fun importarSwimrankings(id: String) {

        if (id.isBlank()) {

            Toast.makeText(
                this,
                "Introduz o ID Swimrankings.",
                Toast.LENGTH_LONG
            ).show()

            return

        }

        prefs.edit()
            .putString("estado", "A importar online...")
            .apply()

        buildScreen()

        thread {

            try {

                val url =
                    "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"

                val html = URL(url).readText()

                val nome = extrairNome(html)
                val clube = extrairClube(html)
                val tempos = extrairTempos(html)

                prefs.edit()
                    .putString("estado", "Importação concluída.")
                    .putString("ultimo_url", url)
                    .putString("nome_importado", nome)
                    .putString("clube_importado", clube)
                    .putString("tempos_importados", tempos)
                    .apply()

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "Importação concluída.",
                        Toast.LENGTH_LONG
                    ).show()

                    buildScreen()

                }

            } catch (e: Exception) {

                prefs.edit()
                    .putString(
                        "estado",
                        "Erro online: ${e.message}"
                    )
                    .apply()

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "Erro online.",
                        Toast.LENGTH_LONG
                    ).show()

                    buildScreen()

                }

            }

        }

    }

    private fun extrairNome(html: String): String {

        val title =
            Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
                .find(html)
                ?.groupValues
                ?.get(1)
                ?: ""

        return limparHtml(title)
            .replace("Swimrankings", "")
            .replace("-", "")
            .trim()

    }

    private fun extrairClube(html: String): String {

        val clean = limparHtml(html)

        val reg =
            Regex("Club\\s*([A-Za-z0-9 .'-]{2,80})")

        val m = reg.find(clean)

        return m?.groupValues?.get(1)?.trim()
            ?: "Por identificar"

    }

    private fun extrairTempos(html: String): String {

        val clean = limparHtml(html)

        val resultados = mutableListOf<String>()

        val provas = listOf(
            "50 Freestyle" to "50 Livres",
            "100 Freestyle" to "100 Livres",
            "200 Freestyle" to "200 Livres",
            "100 Butterfly" to "100 Mariposa",
            "200 Butterfly" to "200 Mariposa"
        )

        for ((en, pt) in provas) {

            val idx =
                clean.indexOf(en, ignoreCase = true)

            if (idx >= 0) {

                val trecho =
                    clean.substring(
                        idx,
                        minOf(idx + 400, clean.length)
                    )

                val tempo =
                    Regex("\\d{1,2}:\\d{2}\\.\\d{2}|\\d{2}\\.\\d{2}")
                        .find(trecho)
                        ?.value

                if (tempo != null) {

                    resultados.add(
                        "$pt|$tempo|Swimrankings"
                    )

                }

            }

        }

        return resultados.distinct()
            .joinToString(";;")

    }

    private fun limparHtml(txt: String): String {

        return Html.fromHtml(
            txt,
            Html.FROM_HTML_MODE_LEGACY
        )
            .toString()
            .replace("\\s+".toRegex(), " ")
            .trim()

    }

    private fun tacBase(prova: String): String {

        return when {

            prova.contains("50 Livres", true) ->
                "0:31.00"

            prova.contains("100 Livres", true) ->
                "1:07.50"

            prova.contains("200 Livres", true) ->
                "2:29.00"

            prova.contains("100 Mariposa", true) ->
                "1:07.50"

            else ->
                "Por importar"

        }

    }

    private fun compararTempo(
        tempo: String,
        tac: String
    ): String {

        val t = tempoSeg(tempo)
        val v = tempoSeg(tac)

        if (t == null || v == null)
            return "TAC por importar"

        val dif = t - v

        return if (dif <= 0)
            "✅ TAC atingido"
        else
            "⏳ faltam %.2f s".format(dif)

    }

    private fun tempoSeg(t: String): Double? {

        return try {

            val v = t.replace(",", ".")

            if (v.contains(":")) {

                val p = v.split(":")

                p[0].toDouble() * 60 +
                        p[1].toDouble()

            } else {

                v.toDouble()

            }

        } catch (e: Exception) {

            null

        }

    }

    private fun exportarWhatsApp() {

        val msg =
            "🏊‍♀️ SwimTrack\n\n" +
                    "Nome: ${get("nome_importado")}\n" +
                    "Clube: ${get("clube_importado")}\n" +
                    "ID: ${get("id")}\n\n" +
                    "Tempos:\n${formatarTempos()}"

        val intent =
            Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"

        intent.putExtra(
            Intent.EXTRA_TEXT,
            msg
        )

        startActivity(
            Intent.createChooser(
                intent,
                "Partilhar"
            )
        )

    }

    private fun formatarTempos(): String {

        val tempos =
            get("tempos_importados")

        if (tempos.isBlank())
            return "Sem tempos."

        return tempos.split(";;")
            .joinToString("\n") {

                val p = it.split("|")

                if (p.size >= 3)
                    "- ${p[0]}: ${p[1]}"
                else
                    ""

            }

    }

    private fun get(key: String): String {

        return prefs.getString(
            key,
            ""
        ) ?: ""

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

    private fun input(
        hint: String,
        value: String = ""
    ): EditText {

        val e = EditText(this)

        e.hint = hint
        e.setText(value)
        e.textSize = 16f
        e.setTextColor(white)
        e.setHintTextColor(soft)
        e.setBackgroundColor(card2)
        e.setPadding(22, 16, 22, 16)

        val lp =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        lp.setMargins(0, 0, 0, 14)

        e.layoutParams = lp

        return e

    }

    private fun button(
        text: String,
        action: () -> Unit
    ): Button {

        val b = Button(this)

        b.text = text
        b.textSize = 16f
        b.setTypeface(Typeface.DEFAULT_BOLD)
        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(blue)
        b.setPadding(20, 18, 20, 18)

        b.setOnClickListener {

            action()

        }

        val lp =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        lp.setMargins(0, 0, 0, 18)

        b.layoutParams = lp

        return b

    }

    private fun info(
        title: String,
        body: String
    ): LinearLayout {

        val c = LinearLayout(this)

        c.orientation =
            LinearLayout.VERTICAL

        c.setPadding(22, 18, 22, 18)

        c.setBackgroundColor(card2)

        val t = TextView(this)

        t.text = title
        t.textSize = 18f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(white)

        val b = TextView(this)

        b.text =
            if (body.isBlank())
                "Por definir"
            else
                body

        b.textSize = 15f
        b.setTextColor(soft)
        b.setPadding(0, 8, 0, 0)

        c.addView(t)
        c.addView(b)

        val lp =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        lp.setMargins(0, 0, 0, 14)

        c.layoutParams = lp

        return c

    }

}
