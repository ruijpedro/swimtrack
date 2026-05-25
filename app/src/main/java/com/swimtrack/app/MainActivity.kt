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

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var content: LinearLayout

    private lateinit var tabAtleta: TextView
    private lateinit var tabImportar: TextView
    private lateinit var tabTempos: TextView
    private lateinit var tabTac: TextView
    private lateinit var tabMais: TextView

    private val PICK_PDF = 9001

    private val bg = Color.rgb(18, 35, 70)
    private val card = Color.rgb(61, 82, 120)
    private val cardDark = Color.rgb(48, 70, 105)
    private val blue = Color.rgb(60, 170, 255)
    private val yellow = Color.rgb(255, 220, 45)
    private val white = Color.WHITE
    private val soft = Color.rgb(210, 225, 240)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PDFBoxResourceLoader.init(applicationContext)

        prefs = getSharedPreferences(
            "swimtrack",
            MODE_PRIVATE
        )

        buildUI()
    }

    private fun buildUI() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setPadding(
            28,
            40,
            28,
            40
        )

        root.setBackgroundColor(bg)

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)

        icon.setImageResource(
            R.mipmap.ic_launcher
        )

        icon.layoutParams =
            LinearLayout.LayoutParams(
                220,
                220
            )

        root.addView(icon)

        root.addView(
            title("SWIMTRACK")
        )

        root.addView(
            subtitle(
                "PERFIL • PDF • TAC • EVOLUÇÃO"
            )
        )

        root.addView(
            topSummary()
        )

        buildTabs(root)

        content = LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        root.addView(content)

        scroll.addView(root)

        setContentView(scroll)

        showAtleta()
    }

    private fun buildTabs(
        root: LinearLayout
    ) {

        val row =
            LinearLayout(this)

        row.orientation =
            LinearLayout.HORIZONTAL

        row.setPadding(
            0,
            22,
            0,
            20
        )

        tabAtleta = tab("ATLETA")
        tabImportar = tab("IMPORTAR")
        tabTempos = tab("TEMPOS")
        tabTac = tab("TAC")
        tabMais = tab("MAIS")

        val tabs = listOf(
            tabAtleta,
            tabImportar,
            tabTempos,
            tabTac,
            tabMais
        )

        for (t in tabs) {

            row.addView(
                t,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

        }

        root.addView(row)

        tabAtleta.setOnClickListener {
            showAtleta()
        }

        tabImportar.setOnClickListener {
            showImportar()
        }

        tabTempos.setOnClickListener {
            showTempos()
        }

        tabTac.setOnClickListener {
            showTac()
        }

        tabMais.setOnClickListener {
            showMais()
        }

    }

    private fun clear(
        active: TextView
    ) {

        content.removeAllViews()

        val tabs = listOf(
            tabAtleta,
            tabImportar,
            tabTempos,
            tabTac,
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

        clear(tabAtleta)

        content.addView(
            section("ATLETA")
        )

        content.addView(
            info(
                "Nome",
                get("nome")
                    .ifBlank {
                        "PEDRO, Constanca Rolim"
                    }
            )
        )

        content.addView(
            info(
                "Ano",
                get("ano")
                    .ifBlank {
                        "2010"
                    }
            )
        )

        content.addView(
            info(
                "País",
                get("pais")
                    .ifBlank {
                        "Portugal"
                    }
            )
        )

        content.addView(
            info(
                "Clube",
                get("clube")
                    .ifBlank {
                        "Bairro dos Anjos / Leiria"
                    }
            )
        )

        content.addView(
            info(
                "Resumo",
                resumoAtleta()
            )
        )

        content.addView(
            button(
                "🌐 ABRIR SWIMRANKINGS"
            ) {

                val id =
                    get("id")
                        .ifBlank {
                            "5631298"
                        }

                val url =
                    "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )

            }
        )

    }

    private fun showImportar() {

        clear(tabImportar)

        content.addView(
            section("IMPORTAR")
        )

        content.addView(
            info(
                "PDF",
                "Importa PDFs Swimrankings, ANDL ou FPN."
            )
        )

        content.addView(
            button(
                "📄 IMPORTAR PDF"
            ) {

                val intent =
                    Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                    )

                intent.addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                intent.type =
                    "application/pdf"

                startActivityForResult(
                    intent,
                    PICK_PDF
                )

            }
        )

    }

    private fun showTempos() {

        clear(tabTempos)

        content.addView(
            section("TEMPOS")
        )

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem dados",
                    "Importa um PDF."
                )
            )

            return

        }

        for (
            linha in
            tempos.split(";;")
        ) {

            val p =
                linha.split("|")

            if (p.size >= 3) {

                content.addView(
                    info(
                        "${p[0]} — ${p[1]}",
                        "Tempo: ${p[2]}"
                    )
                )

            }

        }

    }

    private fun showTac() {

        clear(tabTac)

        content.addView(
            section("TAC")
        )

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem tempos",
                    "Importa primeiro."
                )
            )

            return

        }

        for (
            linha in
            tempos.split(";;")
        ) {

            val p =
                linha.split("|")

            if (p.size >= 3) {

                content.addView(
                    info(
                        "${p[0]} ${p[1]}",
                        compararTac(
                            p[0],
                            p[1],
                            p[2]
                        )
                    )
                )

            }

        }

    }

    private fun showMais() {

        clear(tabMais)

        content.addView(
            section("MAIS")
        )

        content.addView(
            button(
                "🗑 LIMPAR DADOS"
            ) {

                prefs.edit()
                    .clear()
                    .apply()

                buildUI()

            }
        )

        content.addView(
            info(
                "DISCLAIMER",
                disclaimer()
            )
        )

    }

    private fun importarTexto(
        txt: String
    ): Int {

        val resultados =
            mutableListOf<String>()

        val linhas =
            txt.lines()

        for (raw in linhas) {

            val linha =
                raw.trim()

            if (
                linha.contains(
                    " Lap",
                    true
                )
            ) continue

            val prova =
                Regex(
                    "^(50|100|200|400|800|1500)m\\s+[A-Za-zÀ-ÿ]+"
                ).find(linha)
                    ?.value
                    ?.replace("m ", " ")

            val piscina =
                Regex(
                    "\\b(25m|50m)\\b"
                ).find(linha)
                    ?.value

            val tempo =
                Regex(
                    "\\d{1,2}:\\d{2}[.,]\\d{2}|\\d{2}[.,]\\d{2}"
                ).find(linha)
                    ?.value
                    ?.replace(",", ".")

            if (
                prova != null &&
                piscina != null &&
                tempo != null
            ) {

                resultados.add(
                    "$prova|$piscina|$tempo"
                )

            }

        }

        val final =
            resultados
                .distinct()
                .joinToString(";;")

        prefs.edit()
            .putString(
                "tempos",
                final
            )
            .apply()

        return resultados.size

    }

    private fun tac(
        prova: String,
        piscina: String
    ): String {

        return when {

            prova.contains(
                "50 Livres",
                true
            ) &&
                    piscina == "50m"
            -> "31.00"

            prova.contains(
                "100 Livres",
                true
            ) &&
                    piscina == "50m"
            -> "1:07.50"

            prova.contains(
                "200 Livres",
                true
            ) &&
                    piscina == "50m"
            -> "2:29.00"

            else -> ""

        }

    }

    private fun compararTac(
        prova: String,
        piscina: String,
        tempo: String
    ): String {

        val alvo =
            tac(
                prova,
                piscina
            )

        if (alvo.isBlank()) {

            return "TAC por definir"

        }

        val dif =
            tempoSeg(tempo) -
                    tempoSeg(alvo)

        return if (dif <= 0) {

            "✅ TAC atingido"

        } else {

            "⏳ Faltam %.2f s"
                .format(dif)

        }

    }

    private fun resumoAtleta(): String {

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            return "Sem tempos importados."

        }

        return
        "${tempos.split(";;").size} tempos importados."

    }

    private fun tempoSeg(
        t: String
    ): Double {

        return try {

            val v =
                t.replace(",", ".")

            if (v.contains(":")) {

                val p =
                    v.split(":")

                p[0].toDouble() *
                        60.0 +
                        p[1].toDouble()

            } else {

                v.toDouble()

            }

        } catch (e: Exception) {

            99999.0

        }

    }

    private fun resumoTopo(): String {

        val n =
            get("tempos")
                .split(";;")
                .filter {
                    it.isNotBlank()
                }
                .size

        return if (n == 0) {

            "Sem tempos importados"

        } else {

            "$n melhores tempos"

        }

    }

    private fun topSummary(): TextView {

        val t =
            TextView(this)

        t.text =
            resumoTopo()

        t.textSize = 15f

        t.setTextColor(yellow)

        t.gravity =
            Gravity.CENTER

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setPadding(
            18,
            14,
            18,
            14
        )

        t.setBackgroundColor(cardDark)

        return t

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == PICK_PDF &&
            resultCode == RESULT_OK
        ) {

            val uri =
                data?.data ?: return

            try {

                contentResolver
                    .openInputStream(uri)
                    .use { input ->

                        if (input == null) {

                            Toast.makeText(
                                this,
                                "Não foi possível abrir o PDF.",
                                Toast.LENGTH_LONG
                            ).show()

                            return
                        }

                        val doc =
                            PDDocument.load(input)

                        val texto =
                            PDFTextStripper()
                                .getText(doc)

                        doc.close()

                        val total =
                            importarTexto(texto)

                        Toast.makeText(
                            this,
                            "PDF importado: $total tempos encontrados.",
                            Toast.LENGTH_LONG
                        ).show()

                        showTempos()

                    }

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Erro ao importar PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            }

        }

    }

    private fun get(
        key: String
    ): String {

        return prefs
            .getString(
                key,
                ""
            ) ?: ""

    }

    private fun title(
        text: String
    ): TextView {

        val t =
            TextView(this)

        t.text = text

        t.textSize = 34f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(white)

        t.gravity =
            Gravity.CENTER

        return t

    }

    private fun subtitle(
        text: String
    ): TextView {

        val t =
            TextView(this)

        t.text = text

        t.textSize = 14f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(soft)

        t.gravity =
            Gravity.CENTER

        t.setPadding(
            0,
            8,
            0,
            18
        )

        return t

    }

    private fun section(
        text: String
    ): TextView {

        val t =
            TextView(this)

        t.text = text

        t.textSize = 20f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(yellow)

        t.setPadding(
            0,
            30,
            0,
            12
        )

        return t

    }

    private fun tab(
        text: String
    ): TextView {

        val t =
            TextView(this)

        t.text = text

        t.gravity =
            Gravity.CENTER

        t.textSize = 12f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(white)

        t.setBackgroundColor(card)

        t.setPadding(
            8,
            14,
            8,
            14
        )

        return t

    }

    private fun button(
        text: String,
        action: () -> Unit
    ): Button {

        val b =
            Button(this)

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

        lp.setMargins(
            0,
            0,
            0,
            18
        )

        b.layoutParams = lp

        return b

    }

    private fun input(
        hint: String
    ): EditText {

        val e =
            EditText(this)

        e.hint = hint

        e.textSize = 15f

        e.setTextColor(white)

        e.setHintTextColor(soft)

        e.setBackgroundColor(card)

        e.setPadding(
            22,
            16,
            22,
            16
        )

        return e

    }

    private fun inputMulti(
        hint: String
    ): EditText {

        val e =
            input(hint)

        e.minLines = 12

        e.gravity =
            Gravity.TOP

        return e

    }

    private fun info(
        title: String,
        body: String
    ): LinearLayout {

        val c =
            LinearLayout(this)

        c.orientation =
            LinearLayout.VERTICAL

        c.setPadding(
            22,
            18,
            22,
            18
        )

        c.setBackgroundColor(card)

        val t =
            TextView(this)

        t.text = title

        t.textSize = 18f

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setTextColor(white)

        val b =
            TextView(this)

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

        lp.setMargins(
            0,
            0,
            0,
            14
        )

        c.layoutParams = lp

        return c

    }

    private fun disclaimer(): String {

        return
        "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Sem ligação oficial à FPN, ANDL ou Swimrankings."

    }

}
