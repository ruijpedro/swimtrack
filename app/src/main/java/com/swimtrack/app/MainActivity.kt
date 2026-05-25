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
    private lateinit var tabImportar: TextView
    private lateinit var tabTempos: TextView
    private lateinit var tabTac: TextView
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
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        root.addView(icon)
        root.addView(title("SWIMTRACK"))
        root.addView(subtitle("PERFIL • SWIMRANKINGS • TAC • EVOLUÇÃO"))

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
        tabImportar = tab("IMPORTAR")
        tabTempos = tab("TEMPOS")
        tabTac = tab("TAC")
        tabMais = tab("MAIS")

        listOf(tabAtleta, tabImportar, tabTempos, tabTac, tabMais).forEach {
            row.addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        root.addView(row)

        tabAtleta.setOnClickListener { showAtleta() }
        tabImportar.setOnClickListener { showImportar() }
        tabTempos.setOnClickListener { showTempos() }
        tabTac.setOnClickListener { showTac() }
        tabMais.setOnClickListener { showMais() }
    }

    private fun clear(active: TextView) {
        content.removeAllViews()
        listOf(tabAtleta, tabImportar, tabTempos, tabTac, tabMais).forEach {
            it.setBackgroundColor(card)
            it.setTextColor(white)
        }
        active.setBackgroundColor(yellow)
        active.setTextColor(bg)
    }

    private fun showAtleta() {
        clear(tabAtleta)

        content.addView(section("ATLETA"))
        content.addView(info("Nome", get("nome").ifBlank { "Por importar" }))
        content.addView(info("Ano", get("ano").ifBlank { "2010" }))
        content.addView(info("País", get("pais").ifBlank { "Portugal" }))
        content.addView(info("Clube", get("clube").ifBlank { "Por importar" }))
        content.addView(info("ID Swimrankings", get("id").ifBlank { "5631298" }))
        content.addView(info("Escalão", "Juvenil"))
        content.addView(info("Resumo", resumoAtleta()))

        content.addView(button("🌐 ABRIR SWIMRANKINGS") {
            val id = get("id").ifBlank { "5631298" }
            val url = "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        })
    }

    private fun showImportar() {
        clear(tabImportar)

        content.addView(section("IMPORTAR SWIMRANKINGS"))
        content.addView(info("Como usar", "1. Abre o perfil Swimrankings\n2. Copia o texto da página\n3. Cola abaixo\n4. Clica em importar"))

        val caixa = inputMulti("Cola aqui o texto completo do Swimrankings")
        content.addView(caixa)

        content.addView(button("📥 IMPORTAR PERFIL + TEMPOS") {
            val texto = caixa.text.toString()
            if (texto.isBlank()) {
                Toast.makeText(this, "Cola primeiro o texto do Swimrankings.", Toast.LENGTH_LONG).show()
            } else {
                importarTexto(texto)
                Toast.makeText(this, "Dados importados.", Toast.LENGTH_LONG).show()
                showTempos()
            }
        })

        content.addView(button("🌐 ABRIR PERFIL") {
            val id = get("id").ifBlank { "5631298" }
            val url = "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        })
    }

    private fun importarTexto(txt: String) {
        val linhas = txt.lines().map { it.trim() }.filter { it.isNotBlank() }

        val nome = linhas.firstOrNull { it.contains("PEDRO", true) || it.contains("Constanca", true) || it.contains("Constança", true) }
            ?: get("nome").ifBlank { "PEDRO, Constanca Rolim" }

        val ano = Regex("\\((\\d{4})").find(txt)?.groupValues?.getOrNull(1) ?: "2010"

        val pais = when {
            txt.contains("POR - Portugal", true) -> "Portugal"
            else -> get("pais")
        }

        val clube = linhas.firstOrNull {
            it.contains("Bairro", true) || it.contains("Leiria", true) || it.contains("Clube", true) || it.contains("Natação", true)
        } ?: get("clube").ifBlank { "Bairro dos Anjos/ Leiria" }

        val tempos = extrairTemposSwimrankings(txt)

        prefs.edit()
            .putString("nome", nome)
            .putString("ano", ano)
            .putString("pais", pais)
            .putString("clube", clube)
            .putString("tempos", tempos)
            .putString("ultima_importacao", "Importação feita com ${contarTempos(tempos)} tempos.")
            .apply()
    }

    private fun extrairTemposSwimrankings(txt: String): String {
        val eventos = listOf(
            "50m Livres", "100m Livres", "200m Livres", "400m Livres", "800m Livres", "1500m Livres",
            "50m Costas", "100m Costas", "200m Costas",
            "50m Bruços", "100m Bruços", "200m Bruços",
            "50m Mariposa", "100m Mariposa", "200m Mariposa",
            "100m Estilos", "200m Estilos", "400m Estilos"
        )

        val resultados = mutableListOf<String>()

        txt.lines().forEach { raw ->
            val linha = raw.trim()
            if (linha.contains(" Lap", true)) return@forEach

            val prova = eventos.firstOrNull { linha.startsWith(it, true) }
            if (prova != null) {
                val regex = Regex(
                    "^(.*?)\\s+(25m|50m)\\s+(\\d{1,2}:\\d{2}\\.\\d{2}|\\d{2}\\.\\d{2})\\s+([0-9\\-]+)\\s+(.+?)\\s+([A-Za-zÁÉÍÓÚáéíóúãõçÇ\\-() ]+)\\s+(.+)$"
                )
                val m = regex.find(linha)

                if (m != null) {
                    val provaLimpa = m.groupValues[1].replace("m ", " ").trim()
                    val piscina = m.groupValues[2]
                    val tempo = m.groupValues[3]
                    val pts = m.groupValues[4]
                    val data = m.groupValues[5].trim()
                    val cidade = m.groupValues[6].trim()
                    val torneio = m.groupValues[7].trim()
                    resultados.add("$provaLimpa|$piscina|$tempo|$pts|$data|$cidade|$torneio")
                } else {
                    val tempo = Regex("\\d{1,2}:\\d{2}\\.\\d{2}|\\d{2}\\.\\d{2}").find(linha)?.value
                    val piscina = Regex("\\b(25m|50m)\\b").find(linha)?.value
                    if (tempo != null && piscina != null) {
                        val provaLimpa = prova.replace("m ", " ")
                        resultados.add("$provaLimpa|$piscina|$tempo|-|Swimrankings|-|-")
                    }
                }
            }
        }

        return escolherMelhores(resultados)
    }

    private fun escolherMelhores(lista: List<String>): String {
        val mapa = mutableMapOf<String, String>()

        lista.forEach {
            val p = it.split("|")
            if (p.size >= 3) {
                val chave = "${p[0]} ${p[1]}"
                val tempo = p[2]
                val antigo = mapa[chave]
                if (antigo == null) {
                    mapa[chave] = it
                } else {
                    val ta = antigo.split("|")[2]
                    if (tempoSeg(tempo) < tempoSeg(ta)) mapa[chave] = it
                }
            }
        }

        return mapa.values.joinToString(";;")
    }

    private fun showTempos() {
        clear(tabTempos)

        content.addView(section("MELHORES TEMPOS"))
        content.addView(info("Última importação", get("ultima_importacao").ifBlank { "Ainda sem importação." }))

        val tempos = get("tempos")
        if (tempos.isBlank()) {
            content.addView(info("Sem dados", "Vai ao separador IMPORTAR e cola o texto do Swimrankings."))
            return
        }

        tempos.split(";;").forEach {
            val p = it.split("|")
            if (p.size >= 7) {
                val titulo = "${p[0]} — ${p[1]}"
                val corpo = "Tempo: ${p[2]}\nPts: ${p[3]}\nData: ${p[4]}\nCidade: ${p[5]}\nTorneio: ${p[6]}"
                content.addView(info(titulo, corpo))
            }
        }
    }

    private fun showTac() {
        clear(tabTac)

        content.addView(section("TAC / OBJETIVOS"))

        val tempos = get("tempos")
        if (tempos.isBlank()) {
            content.addView(info("Sem tempos", "Importa primeiro os tempos."))
            return
        }

        tempos.split(";;").forEach {
            val p = it.split("|")
            if (p.size >= 3) {
                val prova = p[0]
                val piscina = p[1]
                val tempo = p[2]
                content.addView(info("$prova — $piscina", compararTac(prova, piscina, tempo)))
            }
        }

        content.addView(section("ASSISTENTE"))
        content.addView(info("Análise", objetivoMaisProximo()))
    }

    private fun showMais() {
        clear(tabMais)

        content.addView(section("MAIS"))

        content.addView(button("📤 EXPORTAR WHATSAPP") { exportarWhatsApp() })

        content.addView(button("🗑 LIMPAR DADOS") {
            prefs.edit().clear().apply()
            buildUI()
        })

        content.addView(info("Disclaimer", disclaimer()))
    }

    private fun tac(prova: String, piscina: String): String {
        return when {
            prova.contains("50 Livres", true) && piscina == "50m" -> "31.00"
            prova.contains("50 Livres", true) && piscina == "25m" -> "30.50"
            prova.contains("100 Livres", true) && piscina == "50m" -> "1:07.50"
            prova.contains("100 Livres", true) && piscina == "25m" -> "1:05.50"
            prova.contains("200 Livres", true) && piscina == "50m" -> "2:29.00"
            prova.contains("200 Livres", true) && piscina == "25m" -> "2:23.00"
            prova.contains("100 Mariposa", true) && piscina == "25m" -> "1:14.00"
            prova.contains("100 Mariposa", true) && piscina == "50m" -> "1:18.00"
            prova.contains("200 Mariposa", true) && piscina == "25m" -> "2:55.00"
            else -> ""
        }
    }

    private fun compararTac(prova: String, piscina: String, tempo: String): String {
        val alvo = tac(prova, piscina)
        if (alvo.isBlank()) return "TAC por carregar para esta prova."

        val dif = tempoSeg(tempo) - tempoSeg(alvo)
        return if (dif <= 0) {
            "✅ TAC atingido\nTAC: $alvo"
        } else {
            "⏳ Faltam %.2f s\nTAC: $alvo".format(dif)
        }
    }

    private fun objetivoMaisProximo(): String {
        val tempos = get("tempos")
        if (tempos.isBlank()) return "Sem tempos."

        var melhor = ""
        var menor = 99999.0

        tempos.split(";;").forEach {
            val p = it.split("|")
            if (p.size >= 3) {
                val alvo = tac(p[0], p[1])
                if (alvo.isNotBlank()) {
                    val dif = tempoSeg(p[2]) - tempoSeg(alvo)
                    if (dif < menor) {
                        menor = dif
                        melhor = "${p[0]} ${p[1]}"
                    }
                }
            }
        }

        return if (melhor.isBlank()) {
            "Falta carregar TAC para as provas importadas."
        } else if (menor <= 0) {
            "Melhor indicador: $melhor — TAC já atingido."
        } else {
            "Objetivo mais próximo: $melhor — faltam %.2f s.".format(menor)
        }
    }

    private fun resumoAtleta(): String {
        val tempos = get("tempos")
        if (tempos.isBlank()) return "Ainda sem tempos importados."
        return "${contarTempos(tempos)} melhores tempos importados.\n${objetivoMaisProximo()}"
    }

    private fun contarTempos(tempos: String): Int {
        if (tempos.isBlank()) return 0
        return tempos.split(";;").size
    }

    private fun tempoSeg(t: String): Double {
        return try {
            val v = t.replace(",", ".")
            if (v.contains(":")) {
                val p = v.split(":")
                p[0].toDouble() * 60.0 + p[1].toDouble()
            } else {
                v.toDouble()
            }
        } catch (e: Exception) {
            99999.0
        }
    }

    private fun exportarWhatsApp() {
        val msg = "🏊‍♀️ SwimTrack\n\n" +
                "Atleta: ${get("nome")}\n" +
                "Clube: ${get("clube")}\n\n" +
                "Resumo: ${resumoAtleta()}\n\n" +
                "Tempos:\n${formatarTempos()}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(Intent.createChooser(intent, "Partilhar"))
    }

    private fun formatarTempos(): String {
        val tempos = get("tempos")
        if (tempos.isBlank()) return "Sem tempos."

        return tempos.split(";;").joinToString("\n") {
            val p = it.split("|")
            if (p.size >= 3) "- ${p[0]} ${p[1]}: ${p[2]}" else ""
        }
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
        t.setPadding(0, 8, 0, 24)
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

    private fun tab(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.gravity = Gravity.CENTER
        t.textSize = 12f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(white)
        t.setBackgroundColor(card)
        t.setPadding(8, 14, 8, 14)
        return t
    }

    private fun inputMulti(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.textSize = 15f
        e.minLines = 10
        e.gravity = Gravity.TOP
        e.setTextColor(white)
        e.setHintTextColor(soft)
        e.setBackgroundColor(card)
        e.setPadding(22, 16, 22, 16)

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, 18)
        b.layoutParams = lp

        return b
    }

    private fun info(title: String, body: String): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.setPadding(22, 18, 22, 18)
        c.setBackgroundColor(card)

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

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Sem ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Fontes previstas: Swimrankings, ANDL e FPN."
    }
}
