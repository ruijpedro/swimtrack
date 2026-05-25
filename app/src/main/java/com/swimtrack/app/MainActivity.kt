package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var content: LinearLayout
    private lateinit var prefs: SharedPreferences

    private lateinit var tabAtleta: TextView
    private lateinit var tabTempos: TextView
    private lateinit var tabTac: TextView
    private lateinit var tabEvolucao: TextView
    private lateinit var tabMais: TextView

    private val bg = Color.rgb(21, 45, 78)
    private val card = Color.rgb(48, 70, 105)
    private val card2 = Color.rgb(61, 82, 120)
    private val blue = Color.rgb(80, 190, 255)
    private val yellow = Color.rgb(255, 220, 45)
    private val white = Color.WHITE
    private val soft = Color.rgb(200, 215, 235)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("swimtrack_prefs", MODE_PRIVATE)

        if (get("nome").isBlank()) showSetupScreen() else showDashboard()
    }

    private fun showSetupScreen() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(28, 40, 28, 30)
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setBackgroundColor(bg)

        val icon = ImageView(this)
        icon.setImageResource(resources.getIdentifier("ic_launcher", "mipmap", packageName))
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        val title = title("SWIMTRACK")
        val sub = subtitle("CONFIGURAÇÃO DO ATLETA")

        val nome = input("Nome do atleta")
        val clube = input("Clube")
        val id = input("N.º Identificação / Swimrankings")
        val assoc = input("Associação", "ANDL")
        val ano = input("Ano de nascimento")
        val sexo = input("Sexo")
        val resultados = input("Resultados relevantes")
        val esp = input("Especialidade principal")

        val guardar = actionButton("💾 Guardar atleta") {
            prefs.edit()
                .putString("nome", nome.text.toString())
                .putString("clube", clube.text.toString())
                .putString("id", id.text.toString())
                .putString("assoc", assoc.text.toString())
                .putString("ano", ano.text.toString())
                .putString("sexo", sexo.text.toString())
                .putString("resultados", resultados.text.toString())
                .putString("esp", esp.text.toString())
                .apply()
            showDashboard()
        }

        root.addView(icon)
        root.addView(title)
        root.addView(sub)
        root.addView(nome)
        root.addView(clube)
        root.addView(id)
        root.addView(assoc)
        root.addView(ano)
        root.addView(sexo)
        root.addView(resultados)
        root.addView(esp)
        root.addView(guardar)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showDashboard() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bg)

        val scroll = ScrollView(this)
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(28, 40, 28, 30)
        content.gravity = Gravity.CENTER_HORIZONTAL

        buildHeader()
        buildTabs()
        showAtleta()

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun buildHeader() {
        val icon = ImageView(this)
        icon.setImageResource(resources.getIdentifier("ic_launcher", "mipmap", packageName))
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        content.addView(icon)
        content.addView(title("SWIMTRACK"))
        content.addView(subtitle("NATAÇÃO COMPETITIVA • TEMPOS • TAC"))

        val season = TextView(this)
        season.text = "🏊 ${get("nome")} • Época 2025/2026"
        season.textSize = 16f
        season.setTypeface(Typeface.DEFAULT_BOLD)
        season.setTextColor(yellow)
        season.gravity = Gravity.CENTER
        season.setPadding(18, 12, 18, 12)
        season.setBackgroundColor(card)
        content.addView(season)
    }

    private fun buildTabs() {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 28, 0, 24)

        tabAtleta = tab("Atleta")
        tabTempos = tab("Tempos")
        tabTac = tab("TAC")
        tabEvolucao = tab("Evolução")
        tabMais = tab("Mais")

        row.addView(tabAtleta, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tabTempos, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tabTac, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tabEvolucao, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tabMais, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        tabAtleta.setOnClickListener { showAtleta() }
        tabTempos.setOnClickListener { showTempos() }
        tabTac.setOnClickListener { showTac() }
        tabEvolucao.setOnClickListener { showEvolucao() }
        tabMais.setOnClickListener { showMais() }

        content.addView(row)
    }

    private fun showAtleta() {
        clearPage(tabAtleta)
        content.addView(sectionTitle("ATLETA"))
        content.addView(infoCard("Nome", get("nome")))
        content.addView(infoCard("Clube", get("clube")))
        content.addView(infoCard("N.º Identificação / Swimrankings", get("id")))
        content.addView(infoCard("Associação", get("assoc")))
        content.addView(infoCard("Ano de nascimento", get("ano")))
        content.addView(infoCard("Sexo", get("sexo")))
        content.addView(infoCard("Escalão", calcularEscalao(get("ano"))))
        content.addView(infoCard("Resultados relevantes", get("resultados")))
        content.addView(infoCard("Especialidade principal", get("esp")))
    }

    private fun showTempos() {
        clearPage(tabTempos)
        content.addView(sectionTitle("TEMPOS MANUAIS"))

        val prova = input("Prova. Ex: 100 Mariposa")
        val tempo = input("Tempo. Ex: 1:08.42")
        val data = input("Data / competição")

        content.addView(prova)
        content.addView(tempo)
        content.addView(data)

        content.addView(actionButton("➕ Adicionar tempo") {
            val novo = "${prova.text}|${tempo.text}|${data.text}"
            val atual = get("tempos")
            prefs.edit().putString("tempos", if (atual.isBlank()) novo else "$atual;;$novo").apply()
            showTempos()
        })

        content.addView(sectionTitle("REGISTOS"))
        val tempos = get("tempos")
        if (tempos.isBlank()) {
            content.addView(infoCard("Sem tempos", "Ainda não foram adicionados tempos."))
        } else {
            tempos.split(";;").forEach {
                val p = it.split("|")
                if (p.size >= 3) content.addView(infoCard(p[0], "Tempo: ${p[1]}\nData/competição: ${p[2]}"))
            }
        }
    }

    private fun showTac() {
        clearPage(tabTac)
        content.addView(sectionTitle("TAC BASE"))

        val tempos = get("tempos")
        if (tempos.isBlank()) {
            content.addView(infoCard("Estado", "Adiciona primeiro tempos no separador Tempos."))
        } else {
            tempos.split(";;").forEach {
                val p = it.split("|")
                if (p.size >= 2) {
                    val tac = tacBase(p[0])
                    val dif = compararTempo(p[1], tac)
                    content.addView(infoCard(p[0], "Tempo: ${p[1]}\nTAC base: $tac\nEstado: $dif"))
                }
            }
        }

        content.addView(infoCard("Fontes futuras", "TAC reais serão importados da ANDL, Zonais e FPN."))
    }

    private fun showEvolucao() {
        clearPage(tabEvolucao)
        content.addView(sectionTitle("EVOLUÇÃO"))
        content.addView(infoCard("Histórico", get("tempos").ifBlank { "Ainda sem tempos registados." }))
        content.addView(infoCard("Objetivos", "A app irá calcular melhorias, TAC próximos e recordes próximos."))
    }

    private fun showMais() {
        clearPage(tabMais)
        content.addView(sectionTitle("MAIS"))

        content.addView(actionButton("📤 Exportar WhatsApp") {
            val msg =
                "🏊‍♀️ SwimTrack\n" +
                "Atleta: ${get("nome")}\n" +
                "Clube: ${get("clube")}\n" +
                "ID: ${get("id")}\n" +
                "Associação: ${get("assoc")}\n" +
                "Escalão: ${calcularEscalao(get("ano"))}\n\n" +
                "Tempos:\n${formatarTempos()}\n\n" +
                "Fontes: Swimrankings • ANDL • FPN"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, "Partilhar"))
        })

        content.addView(actionButton("✏️ Editar atleta") { showSetupScreen() })

        content.addView(actionButton("🗑 Limpar dados") {
            prefs.edit().clear().apply()
            showSetupScreen()
        })

        content.addView(infoCard("Segurança", "Uso pessoal. Sem login público. Sem servidor externo. Apenas INTERNET."))
        content.addView(infoCard("Disclaimer", disclaimer()))
    }

    private fun formatarTempos(): String {
        val tempos = get("tempos")
        if (tempos.isBlank()) return "Sem tempos registados."
        return tempos.split(";;").joinToString("\n") {
            val p = it.split("|")
            if (p.size >= 3) "- ${p[0]}: ${p[1]} (${p[2]})" else ""
        }
    }

    private fun tacBase(prova: String): String {
        return when {
            prova.contains("100", true) && prova.contains("Mariposa", true) -> "1:07.50"
            prova.contains("200", true) && prova.contains("Livres", true) -> "2:29.00"
            prova.contains("100", true) && prova.contains("Livres", true) -> "1:07.50"
            else -> "Por importar"
        }
    }

    private fun compararTempo(tempo: String, tac: String): String {
        val t = tempoSeg(tempo)
        val v = tempoSeg(tac)
        if (t == null || v == null) return "TAC por importar"
        val dif = t - v
        return if (dif <= 0) "✅ TAC atingido" else "⏳ faltam %.2f s".format(dif)
    }

    private fun tempoSeg(t: String): Double? {
        return try {
            if (t.contains(":")) {
                val p = t.split(":")
                p[0].toDouble() * 60 + p[1].replace(",", ".").toDouble()
            } else {
                t.replace(",", ".").toDouble()
            }
        } catch (e: Exception) {
            null
        }
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

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, 14)
        e.layoutParams = lp
        return e
    }

    private fun get(key: String): String = prefs.getString(key, "") ?: ""

    private fun calcularEscalao(anoTexto: String): String {
        val ano = anoTexto.toIntOrNull() ?: return "Por definir"
        val idade = 2026 - ano
        return when {
            idade <= 12 -> "Infantil"
            idade <= 16 -> "Juvenil"
            idade <= 18 -> "Júnior"
            else -> "Sénior"
        }
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

    private fun tab(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.gravity = Gravity.CENTER
        t.textSize = 12f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(white)
        t.setBackgroundColor(card2)
        t.setPadding(8, 14, 8, 14)
        return t
    }

    private fun clearPage(active: TextView) {
        while (content.childCount > 5) content.removeViewAt(5)
        listOf(tabAtleta, tabTempos, tabTac, tabEvolucao, tabMais).forEach {
            it.setBackgroundColor(card2)
            it.setTextColor(white)
        }
        active.setBackgroundColor(yellow)
        active.setTextColor(bg)
    }

    private fun sectionTitle(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 20f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(yellow)
        t.setPadding(0, 30, 0, 12)
        return t
    }

    private fun infoCard(title: String, body: String): LinearLayout {
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

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, 14)
        c.layoutParams = lp
        return c
    }

    private fun actionButton(text: String, action: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.textSize = 16f
        b.setTypeface(Typeface.DEFAULT_BOLD)
        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(blue)
        b.setPadding(20, 18, 20, 18)
        b.setOnClickListener { action() }

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, 18)
        b.layoutParams = lp
        return b
    }

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Não possui ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Dados previstos: Swimrankings, ANDL e FPN."
    }
}
