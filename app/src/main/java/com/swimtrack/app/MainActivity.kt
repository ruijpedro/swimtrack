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
    private lateinit var root: LinearLayout

    private val bg = Color.rgb(18, 35, 70)
    private val card = Color.rgb(61, 82, 120)
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
        root.setPadding(32, 40, 32, 40)
        root.setBackgroundColor(bg)
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)
        icon.layoutParams = LinearLayout.LayoutParams(200, 200)

        root.addView(icon)
        root.addView(title("SWIMTRACK"))
        root.addView(subtitle("PERFIL • TEMPOS • EVOLUÇÃO • TAC"))

        if (get("perfil_ok").isBlank()) {
            showSetup()
        } else {
            showDashboard()
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showSetup() {
        val nome = input("Nome da atleta")
        val clube = input("Clube")
        val id = input("ID Swimrankings / FPN", "5631298")
        val assoc = input("Associação", "ANDL")
        val ano = input("Ano de nascimento")
        val sexo = input("Sexo")
        val especialidade = input("Especialidade principal")

        root.addView(section("CONFIGURAÇÃO INICIAL"))
        listOf(nome, clube, id, assoc, ano, sexo, especialidade).forEach { root.addView(it) }

        root.addView(button("💾 Guardar perfil") {
            prefs.edit()
                .putString("nome", nome.text.toString())
                .putString("clube", clube.text.toString())
                .putString("id", id.text.toString())
                .putString("assoc", assoc.text.toString())
                .putString("ano", ano.text.toString())
                .putString("sexo", sexo.text.toString())
                .putString("especialidade", especialidade.text.toString())
                .putString("perfil_ok", "sim")
                .apply()
            buildScreen()
        })
    }

    private fun showDashboard() {
        root.addView(section("ATLETA"))
        root.addView(info("Nome", get("nome")))
        root.addView(info("Clube", get("clube")))
        root.addView(info("ID Swimrankings", get("id")))
        root.addView(info("Associação", get("assoc")))
        root.addView(info("Escalão", calcularEscalao()))
        root.addView(info("Especialidade", get("especialidade")))

        root.addView(button("🌐 Abrir perfil Swimrankings") {
            val url = "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=${get("id")}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        })

        root.addView(section("ATUALIZAR TEMPOS"))
        val texto = inputMulti("Colar aqui texto copiado do Swimrankings")
        root.addView(texto)

        root.addView(button("📥 Importar / Atualizar evolução") {
            val novos = extrairTempos(texto.text.toString())
            if (novos.isBlank()) {
                Toast.makeText(this, "Não encontrei tempos no texto colado.", Toast.LENGTH_LONG).show()
            } else {
                val atuais = get("tempos")
                val combinado = combinarTempos(atuais, novos)
                prefs.edit().putString("tempos", combinado).apply()
                buildScreen()
            }
        })

        root.addView(section("TEMPOS GUARDADOS"))
        val tempos = get("tempos")
        if (tempos.isBlank()) {
            root.addView(info("Sem tempos", "Abre o Swimrankings, copia o texto dos resultados e cola acima."))
        } else {
            tempos.split(";;").forEach {
                val p = it.split("|")
                if (p.size >= 2) {
                    root.addView(info(p[0], "Melhor tempo: ${p[1]}\nTAC: ${compararTac(p[0], p[1])}"))
                }
            }
        }

        root.addView(section("ASSISTENTE"))
        root.addView(info("Análise automática", analisarEvolucao()))

        root.addView(button("📤 Exportar WhatsApp") {
            exportarWhatsApp()
        })

        root.addView(button("✏️ Editar perfil") {
            prefs.edit().remove("perfil_ok").apply()
            buildScreen()
        })

        root.addView(button("🗑 Limpar tudo") {
            prefs.edit().clear().apply()
            buildScreen()
        })

        root.addView(section("DISCLAIMER"))
        root.addView(info("Aviso", disclaimer()))
    }

    private fun extrairTempos(txt: String): String {
        val provas = listOf(
            "50 Freestyle" to "50 Livres",
            "100 Freestyle" to "100 Livres",
            "200 Freestyle" to "200 Livres",
            "400 Freestyle" to "400 Livres",
            "800 Freestyle" to "800 Livres",
            "1500 Freestyle" to "1500 Livres",
            "50 Butterfly" to "50 Mariposa",
            "100 Butterfly" to "100 Mariposa",
            "200 Butterfly" to "200 Mariposa",
            "50 Backstroke" to "50 Costas",
            "100 Backstroke" to "100 Costas",
            "200 Backstroke" to "200 Costas",
            "50 Breaststroke" to "50 Bruços",
            "100 Breaststroke" to "100 Bruços",
            "200 Breaststroke" to "200 Bruços",
            "100 Medley" to "100 Estilos",
            "200 Medley" to "200 Estilos",
            "400 Medley" to "400 Estilos",
            "50 Livres" to "50 Livres",
            "100 Livres" to "100 Livres",
            "200 Livres" to "200 Livres",
            "100 Mariposa" to "100 Mariposa",
            "200 Mariposa" to "200 Mariposa"
        )

        val resultados = mutableListOf<String>()
        val linhas = txt.lines().map { it.trim() }.filter { it.isNotBlank() }

        for (i in linhas.indices) {
            for ((origem, pt) in provas) {
                if (linhas[i].contains(origem, true)) {
                    val bloco = linhas.subList(i, minOf(i + 8, linhas.size)).joinToString(" ")
                    val tempo = Regex("\\d{1,2}:\\d{2}[.,]\\d{2}|\\d{2}[.,]\\d{2}")
                        .find(bloco)?.value?.replace(",", ".")
                    if (tempo != null) resultados.add("$pt|$tempo")
                }
            }
        }

        return resultados.distinct().joinToString(";;")
    }

    private fun combinarTempos(atuais: String, novos: String): String {
        val mapa = mutableMapOf<String, String>()

        fun adicionar(lista: String) {
            if (lista.isBlank()) return
            lista.split(";;").forEach {
                val p = it.split("|")
                if (p.size >= 2) {
                    val prova = p[0]
                    val tempo = p[1]
                    val antigo = mapa[prova]
                    if (antigo == null || tempoSeg(tempo) < tempoSeg(antigo)) {
                        mapa[prova] = tempo
                    }
                }
            }
        }

        adicionar(atuais)
        adicionar(novos)

        return mapa.entries.joinToString(";;") { "${it.key}|${it.value}" }
    }

    private fun tac(prova: String): String {
        return when {
            prova.contains("50 Livres", true) -> "0:31.00"
            prova.contains("100 Livres", true) -> "1:07.50"
            prova.contains("200 Livres", true) -> "2:29.00"
            prova.contains("100 Mariposa", true) -> "1:07.50"
            prova.contains("200 Mariposa", true) -> "2:30.00"
            else -> "Por importar"
        }
    }

    private fun compararTac(prova: String, tempo: String): String {
        val alvo = tac(prova)
        val t = tempoSeg(tempo)
        val a = tempoSeg(alvo)
        if (a <= 0.0) return "TAC por importar"
        val dif = t - a
        return if (dif <= 0) "✅ TAC atingido" else "⏳ faltam %.2f s".format(dif)
    }

    private fun analisarEvolucao(): String {
        val tempos = get("tempos")
        if (tempos.isBlank()) return "Ainda não há tempos suficientes para análise."

        var melhorProva = ""
        var menorDif = 9999.0

        tempos.split(";;").forEach {
            val p = it.split("|")
            if (p.size >= 2) {
                val alvo = tempoSeg(tac(p[0]))
                val atual = tempoSeg(p[1])
                if (alvo > 0) {
                    val dif = atual - alvo
                    if (dif < menorDif) {
                        menorDif = dif
                        melhorProva = p[0]
                    }
                }
            }
        }

        return if (melhorProva.isBlank()) {
            "Os tempos foram guardados. Falta carregar TAC oficiais para análise completa."
        } else if (menorDif <= 0) {
            "Melhor ponto forte: $melhorProva. TAC já atingido."
        } else {
            "Objetivo mais próximo: $melhorProva. Faltam %.2f s para o TAC.".format(menorDif)
        }
    }

    private fun tempoSeg(t: String): Double {
        return try {
            val v = t.replace(",", ".")
            if (v.contains(":")) {
                val p = v.split(":")
                p[0].toDouble() * 60 + p[1].toDouble()
            } else {
                v.toDouble()
            }
        } catch (e: Exception) {
            99999.0
        }
    }

    private fun calcularEscalao(): String {
        val ano = get("ano").toIntOrNull() ?: return "Por definir"
        val idade = 2026 - ano
        return when {
            idade <= 12 -> "Infantil"
            idade <= 16 -> "Juvenil"
            idade <= 18 -> "Júnior"
            else -> "Sénior"
        }
    }

    private fun exportarWhatsApp() {
        val msg =
            "🏊‍♀️ SwimTrack\n\n" +
            "Atleta: ${get("nome")}\n" +
            "Clube: ${get("clube")}\n" +
            "Escalão: ${calcularEscalao()}\n\n" +
            "Tempos:\n${formatarTempos()}\n\n" +
            "Análise:\n${analisarEvolucao()}"

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
            if (p.size >= 2) "- ${p[0]}: ${p[1]} — ${compararTac(p[0], p[1])}" else ""
        }
    }

    private fun get(key: String): String = prefs.getString(key, "") ?: ""

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
        e.setBackgroundColor(card)
        e.setPadding(22, 16, 22, 16)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, 14)
        e.layoutParams = lp
        return e
    }

    private fun inputMulti(hint: String): EditText {
        val e = input(hint)
        e.minLines = 6
        e.gravity = Gravity.TOP
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
                "Não possui ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Dados consultados a partir de fontes públicas: Swimrankings, ANDL e FPN."
    }
}
