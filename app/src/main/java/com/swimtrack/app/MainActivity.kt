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
    private lateinit var content: LinearLayout

    private lateinit var tabAtleta: TextView
    private lateinit var tabTempos: TextView
    private lateinit var tabTac: TextView
    private lateinit var tabEvolucao: TextView
    private lateinit var tabMais: TextView

    private val bg = Color.rgb(18, 35, 70)
    private val card = Color.rgb(61, 82, 120)
    private val blue = Color.rgb(60, 170, 255)
    private val yellow = Color.rgb(255, 220, 45)
    private val white = Color.WHITE
    private val soft = Color.rgb(210, 225, 240)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("swimtrack", MODE_PRIVATE)

        buildUI()
    }

    private fun buildUI() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(28, 40, 28, 40)
        root.setBackgroundColor(bg)
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)

        val iconParams = LinearLayout.LayoutParams(220, 220)
        icon.layoutParams = iconParams

        root.addView(icon)
        root.addView(title("SWIMTRACK"))
        root.addView(subtitle("PERFIL • TEMPOS • TAC • EVOLUÇÃO"))

        buildTabs(root)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL

        root.addView(content)

        scroll.addView(root)

        setContentView(scroll)

        showAtleta()
    }

    private fun buildTabs(root: LinearLayout) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL

        tabAtleta = tab("ATLETA")
        tabTempos = tab("TEMPOS")
        tabTac = tab("TAC")
        tabEvolucao = tab("EVOLUÇÃO")
        tabMais = tab("MAIS")

        row.addView(
            tabAtleta,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            tabTempos,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            tabTac,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            tabEvolucao,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            tabMais,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        root.addView(row)

        tabAtleta.setOnClickListener {
            showAtleta()
        }

        tabTempos.setOnClickListener {
            showTempos()
        }

        tabTac.setOnClickListener {
            showTac()
        }

        tabEvolucao.setOnClickListener {
            showEvolucao()
        }

        tabMais.setOnClickListener {
            showMais()
        }
    }

    private fun clearContent(active: TextView) {

        content.removeAllViews()

        val tabs = listOf(
            tabAtleta,
            tabTempos,
            tabTac,
            tabEvolucao,
            tabMais
        )

        for (t in tabs) {
            t.setBackgroundColor(card)
            t.setTextColor(white)
        }

        active.setBackgroundColor(yellow)
        active.setTextColor(bg)
    }

    private fun showAtleta() {

        clearContent(tabAtleta)

        val nome = input("Nome", get("nome"))
        val clube = input("Clube", get("clube"))
        val id = input("ID Swimrankings", get("id"))
        val assoc = input("Associação", get("assoc"))
        val esp = input("Especialidade", get("esp"))

        content.addView(section("PERFIL"))

        content.addView(nome)
        content.addView(clube)
        content.addView(id)
        content.addView(assoc)
        content.addView(esp)

        content.addView(button("💾 GUARDAR PERFIL") {

            prefs.edit()
                .putString("nome", nome.text.toString())
                .putString("clube", clube.text.toString())
                .putString("id", id.text.toString())
                .putString("assoc", assoc.text.toString())
                .putString("esp", esp.text.toString())
                .apply()

            Toast.makeText(
                this,
                "Perfil guardado.",
                Toast.LENGTH_LONG
            ).show()
        })

        content.addView(button("🌐 ABRIR SWIMRANKINGS") {

            val url =
                "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=${id.text}"

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)
        })

        content.addView(
            info(
                "Estado",
                "Perfil guardado localmente."
            )
        )
    }

    private fun showTempos() {

        clearContent(tabTempos)

        val texto =
            inputMulti(
                "Colar resultados copiados do Swimrankings"
            )

        content.addView(section("IMPORTAR RESULTADOS"))
        content.addView(texto)

        content.addView(button("📥 IMPORTAR / ATUALIZAR") {

            val novos =
                extrairTempos(
                    texto.text.toString()
                )

            if (novos.isBlank()) {

                Toast.makeText(
                    this,
                    "Não encontrei tempos.",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                val atuais = get("tempos")

                val combinado =
                    combinarTempos(
                        atuais,
                        novos
                    )

                prefs.edit()
                    .putString(
                        "tempos",
                        combinado
                    )
                    .apply()

                Toast.makeText(
                    this,
                    "Tempos atualizados.",
                    Toast.LENGTH_LONG
                ).show()

                showTempos()
            }
        })

        content.addView(section("MELHORES TEMPOS"))

        val tempos = get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem dados",
                    "Importa resultados."
                )
            )

        } else {

            tempos.split(";;").forEach {

                val p = it.split("|")

                if (p.size >= 2) {

                    content.addView(
                        info(
                            p[0],
                            "Tempo: ${p[1]}"
                        )
                    )
                }
            }
        }
    }

    private fun showTac() {

        clearContent(tabTac)

        content.addView(section("TAC"))

        val tempos = get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem tempos",
                    "Importa resultados primeiro."
                )
            )

        } else {

            tempos.split(";;").forEach {

                val p = it.split("|")

                if (p.size >= 2) {

                    content.addView(
                        info(
                            p[0],
                            compararTac(
                                p[0],
                                p[1]
                            )
                        )
                    )
                }
            }
        }
    }

    private fun showEvolucao() {

        clearContent(tabEvolucao)

        content.addView(section("ASSISTENTE"))

        content.addView(
            info(
                "Análise",
                analisar()
            )
        )

        content.addView(
            info(
                "Objetivo",
                objetivo()
            )
        )
    }

    private fun showMais() {

        clearContent(tabMais)

        content.addView(section("EXPORTAR"))

        content.addView(button("📤 WHATSAPP") {
            exportarWhatsApp()
        })

        content.addView(button("🗑 LIMPAR APP") {

            prefs.edit()
                .clear()
                .apply()

            buildUI()
        })

        content.addView(
            info(
                "DISCLAIMER",
                disclaimer()
            )
        )
    }

    private fun extrairTempos(txt: String): String {

        val provas = listOf(
            "50 Freestyle" to "50 Livres",
            "100 Freestyle" to "100 Livres",
            "200 Freestyle" to "200 Livres",
            "400 Freestyle" to "400 Livres",
            "100 Butterfly" to "100 Mariposa",
            "200 Butterfly" to "200 Mariposa"
        )

        val resultados =
            mutableListOf<String>()

        val linhas =
            txt.lines()
                .map { it.trim() }

        for (i in linhas.indices) {

            for ((origem, pt) in provas) {

                if (
                    linhas[i].contains(
                        origem,
                        true
                    )
                ) {

                    val bloco =
                        linhas.subList(
                            i,
                            minOf(
                                i + 8,
                                linhas.size
                            )
                        ).joinToString(" ")

                    val tempo =
                        Regex(
                            "\\d{1,2}:\\d{2}[.,]\\d{2}|\\d{2}[.,]\\d{2}"
                        ).find(bloco)
                            ?.value
                            ?.replace(",", ".")

                    if (tempo != null) {

                        resultados.add(
                            "$pt|$tempo"
                        )
                    }
                }
            }
        }

        return resultados
            .distinct()
            .joinToString(";;")
    }

    private fun combinarTempos(
        atuais: String,
        novos: String
    ): String {

        val mapa =
            mutableMapOf<String, String>()

        fun add(lista: String) {

            if (lista.isBlank()) {
                return
            }

            lista.split(";;").forEach {

                val p = it.split("|")

                if (p.size >= 2) {

                    val prova = p[0]
                    val tempo = p[1]

                    val antigo = mapa[prova]

                    if (
                        antigo == null ||
                        tempoSeg(tempo)
                        <
                        tempoSeg(antigo)
                    ) {

                        mapa[prova] = tempo
                    }
                }
            }
        }

        add(atuais)
        add(novos)

        return mapa.entries.joinToString(";;") {
            "${it.key}|${it.value}"
        }
    }

    private fun tac(prova: String): String {

        return when {

            prova.contains(
                "50 Livres",
                true
            ) -> "0:31.00"

            prova.contains(
                "100 Livres",
                true
            ) -> "1:07.50"

            prova.contains(
                "200 Livres",
                true
            ) -> "2:29.00"

            prova.contains(
                "100 Mariposa",
                true
            ) -> "1:07.50"

            else -> ""
        }
    }

    private fun compararTac(
        prova: String,
        tempo: String
    ): String {

        val alvo =
            tempoSeg(
                tac(prova)
            )

        val atual =
            tempoSeg(tempo)

        if (alvo <= 0) {
            return "TAC por definir"
        }

        val dif = atual - alvo

        return if (dif <= 0) {
            "✅ TAC atingido"
        } else {
            "⏳ faltam %.2f s".format(dif)
        }
    }

    private fun analisar(): String {

        val tempos = get("tempos")

        if (tempos.isBlank()) {
            return "Sem dados."
        }

        return "A atleta apresenta evolução nas provas registadas. O SwimTrack identificou os TAC mais próximos automaticamente."
    }

    private fun objetivo(): String {

        val tempos = get("tempos")

        if (tempos.isBlank()) {
            return "Importa tempos."
        }

        var melhor = ""
        var difMin = 9999.0

        tempos.split(";;").forEach {

            val p = it.split("|")

            if (p.size >= 2) {

                val alvo =
                    tempoSeg(
                        tac(p[0])
                    )

                if (alvo > 0) {

                    val dif =
                        tempoSeg(p[1]) - alvo

                    if (dif < difMin) {

                        difMin = dif
                        melhor = p[0]
                    }
                }
            }
        }

        return if (melhor.isBlank()) {
            "Sem TAC definidos."
        } else {
            "Objetivo mais próximo: $melhor"
        }
    }

    private fun tempoSeg(t: String): Double {

        return try {

            val v =
                t.replace(",", ".")

            if (v.contains(":")) {

                val p = v.split(":")

                p[0].toDouble() * 60 +
                        p[1].toDouble()

            } else {

                v.toDouble()
            }

        } catch (e: Exception) {

            99999.0
        }
    }

    private fun exportarWhatsApp() {

        val msg =
            "🏊‍♀️ SwimTrack\n\n" +
                    "Atleta: ${get("nome")}\n" +
                    "Clube: ${get("clube")}\n\n" +
                    "Tempos:\n${get("tempos")}"

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

    private fun disclaimer(): String {

        return "SwimTrack é uma aplicação de uso pessoal.\n\n" +
                "Sem ligação oficial à FPN, ANDL ou Swimrankings."
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
        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(white)
        t.gravity = Gravity.CENTER

        return t
    }

    private fun subtitle(text: String): TextView {

        val t = TextView(this)

        t.text = text
        t.textSize = 14f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(soft)
        t.gravity = Gravity.CENTER
        t.setPadding(0, 8, 0, 24)

        return t
    }

    private fun section(text: String): TextView {

        val t = TextView(this)

        t.text = text
        t.textSize = 20f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(yellow)
        t.setPadding(0, 30, 0, 12)

        return t
    }

    private fun tab(text: String): TextView {

        val t = TextView(this)

        t.text = text
        t.gravity = Gravity.CENTER
        t.textSize = 12f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(white)
        t.setBackgroundColor(card)
        t.setPadding(8, 14, 8, 14)

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
        e.setBackgroundColor(card)

        e.setPadding(
            22,
            16,
            22,
            16
        )

        val lp =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        lp.setMargins(0, 0, 0, 14)

        e.layoutParams = lp

        return e
    }

    private fun inputMulti(
        hint: String
    ): EditText {

        val e = input(hint)

        e.minLines = 6
        e.gravity = Gravity.TOP

        return e
    }

    private fun button(
        text: String,
        action: () -> Unit
    ): Button {

        val b = Button(this)

        b.text = text
        b.textSize = 16f

        b.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(blue)

        b.setPadding(
            20,
            18,
            20,
            18
        )

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

        c.setPadding(
            22,
            18,
            22,
            18
        )

        c.setBackgroundColor(card)

        val t = TextView(this)

        t.text = title
        t.textSize = 18f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(white)

        val b = TextView(this)

        b.text = body
        b.textSize = 15f
        b.setTextColor(soft)

        b.setPadding(
            0,
            8,
            0,
            0
        )

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
