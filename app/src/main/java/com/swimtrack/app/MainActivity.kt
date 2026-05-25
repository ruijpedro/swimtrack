package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var content: LinearLayout
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
        while (content.childCount > 5) {
            content.removeViewAt(5)
        }

        val tabs = listOf(tabAtleta, tabTempos, tabTac, tabEvolucao, tabMais)
        for (t in tabs) {
            t.setBackgroundColor(card2)
            t.setTextColor(white)
        }

        active.setBackgroundColor(yellow)
        active.setTextColor(bg)
    }

    private fun showAtleta() {
        clearPage(tabAtleta)

        content.addView(sectionTitle("ATLETA"))
        content.addView(infoCard("Nome", "Constança"))
        content.addView(infoCard("Clube", "A definir"))
        content.addView(infoCard("N.º Identificação", "A definir"))
        content.addView(infoCard("Associação", "ANDL — Associação de Natação do Distrito de Leiria"))
        content.addView(infoCard("Escalão", "Cálculo automático pela idade"))
        content.addView(infoCard("Resultados relevantes", "A definir após importação do Swimrankings"))
        content.addView(actionButton("📥 Importar dados Swimrankings") {
            Toast.makeText(this, "Ligação Swimrankings será adicionada na próxima fase.", Toast.LENGTH_LONG).show()
        })
    }

    private fun showTempos() {
        clearPage(tabTempos)

        content.addView(sectionTitle("TEMPOS PESSOAIS"))
        content.addView(infoCard("100 Mariposa", "Melhor tempo: por importar\nPiscina: 25m / 50m\nFonte: Swimrankings"))
        content.addView(infoCard("200 Livres", "Melhor tempo: por importar\nPiscina: 25m / 50m\nFonte: Swimrankings"))
        content.addView(infoCard("100 Livres", "Melhor tempo: por importar\nPiscina: 25m / 50m\nFonte: Swimrankings"))
    }

    private fun showTac() {
        clearPage(tabTac)

        content.addView(sectionTitle("TAC"))
        content.addView(infoCard("TAC Distritais", "Fonte prevista: ANDL\nEstado: por importar"))
        content.addView(infoCard("TAC Zonais", "Fonte prevista: regulamentos oficiais\nEstado: por importar"))
        content.addView(infoCard("TAC Nacionais", "Fonte prevista: FPN\nEstado: por importar"))
        content.addView(infoCard("Comparação automática", "A app irá mostrar quanto falta para cada TAC."))
    }

    private fun showEvolucao() {
        clearPage(tabEvolucao)

        content.addView(sectionTitle("EVOLUÇÃO"))
        content.addView(infoCard("Época 2025/2026", "Histórico de tempos ainda por importar."))
        content.addView(infoCard("Melhorias", "A app irá calcular diferenças entre provas e épocas."))
        content.addView(infoCard("Objetivos", "TAC próximos, recordes próximos e provas prioritárias."))
    }

    private fun showMais() {
        clearPage(tabMais)

        content.addView(sectionTitle("MAIS"))
        content.addView(actionButton("📤 Exportar resumo WhatsApp") {
            val msg =
                "🏊‍♀️ SwimTrack\n" +
                "Atleta: Constança\n" +
                "Clube: A definir\n" +
                "Associação: ANDL\n\n" +
                "Tempos • TAC • Evolução\n\n" +
                "Fontes: Swimrankings • ANDL • FPN"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, "Partilhar"))
        })

        content.addView(infoCard("Segurança", "Uso pessoal. Sem login público. Sem servidor externo. Apenas INTERNET."))
        content.addView(infoCard("Disclaimer", disclaimer()))
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

    private fun actionButton(text: String, action: () -> Unit): Button {
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

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Não possui ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Dados previstos: Swimrankings, ANDL e FPN."
    }
}
