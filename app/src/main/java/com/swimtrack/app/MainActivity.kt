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

import androidx.core.content.FileProvider

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var content: LinearLayout

    private lateinit var tabAtleta: TextView
    private lateinit var tabImportar: TextView
    private lateinit var tabTempos: TextView
    private lateinit var tabTac: TextView
    private lateinit var tabEvolucao: TextView
    private lateinit var tabMais: TextView

    private val PICK_PDF = 9001

    private val bg = Color.rgb(18, 35, 70)
    private val card = Color.rgb(61, 82, 120)
    private val cardDark = Color.rgb(48, 70, 105)

    private val blue = Color.rgb(60, 170, 255)
    private val green = Color.rgb(30, 170, 80)
    private val red = Color.rgb(190, 60, 60)

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
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(28, 40, 28, 40)
        root.setBackgroundColor(bg)
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)
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
                "SWIMRANKINGS • TAC • EVOLUÇÃO"
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

        val row = LinearLayout(this)
        row.orientation =
            LinearLayout.HORIZONTAL

        row.setPadding(
            0,
            22,
            0,
            20
        )

        tabAtleta =
            tab("ATLETA")

        tabImportar =
            tab("IMPORTAR")

        tabTempos =
            tab("TEMPOS")

        tabTac =
            tab("TAC")

        tabEvolucao =
            tab("EVOLUÇÃO")

        tabMais =
            tab("MAIS")

        val tabs =
            listOf(
                tabAtleta,
                tabImportar,
                tabTempos,
                tabTac,
                tabEvolucao,
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

        tabEvolucao.setOnClickListener {
            showEvolucao()
        }

        tabMais.setOnClickListener {
            showMais()
        }
    }

    private fun clear(
        active: TextView
    ) {

        content.removeAllViews()

        val tabs =
            listOf(
                tabAtleta,
                tabImportar,
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

        clear(tabAtleta)

        content.addView(
            section("ATLETA")
        )

        content.addView(
            info(
                "Nome",
                get("nome")
                    .ifBlank {
                        "Por importar"
                    }
            )
        )

        content.addView(
            info(
                "Ano",
                get("ano")
                    .ifBlank {
                        "Por importar"
                    }
            )
        )

        content.addView(
            info(
                "Clube",
                get("clube")
                    .ifBlank {
                        "Por importar"
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
                "Escalão",
                escalao(
                    get("ano")
                )
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
                "PDF Swimrankings",
                "Importa tempos automaticamente."
            )
        )

        content.addView(
            info(
                "PDF FPN / ANDL",
                "Importa TAC automaticamente."
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

        content.addView(
            button(
                "🧪 COLAR TEXTO"
            ) {
                showColarTexto()
            }
        )
    }
        private fun showColarTexto() {

        clear(tabImportar)

        content.addView(
            section("COLAR TEXTO")
        )

        val caixa =
            inputMulti(
                "Cola aqui texto do Swimrankings ou TAC"
            )

        content.addView(caixa)

        content.addView(
            button(
                "📥 IMPORTAR TEXTO"
            ) {

                val texto =
                    caixa.text.toString()

                val msg =
                    importarTextoGeral(texto)

                Toast.makeText(
                    this,
                    msg,
                    Toast.LENGTH_LONG
                ).show()

                showTempos()
            }
        )
    }

    private fun showTempos() {

        clear(tabTempos)

        content.addView(
            section("RECORDES PESSOAIS")
        )

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem dados",
                    "Importa PDF Swimrankings."
                )
            )

            return
        }

        val lista =
            tempos
                .split(";;")
                .map {
                    it.split("|")
                }
                .filter {
                    it.size >= 6
                }

        mostrarPiscina(
            "PISCINA CURTA 25m",
            "25m",
            lista
        )

        mostrarPiscina(
            "PISCINA LONGA 50m",
            "50m",
            lista
        )
    }

    private fun mostrarPiscina(
        titulo: String,
        piscina: String,
        lista: List<List<String>>
    ) {

        val filtrada =
            lista.filter {
                it[1] == piscina
            }

        if (filtrada.isEmpty()) {
            return
        }

        content.addView(
            section(titulo)
        )

        mostrarEstilo(
            "Livres",
            filtrada
        )

        mostrarEstilo(
            "Costas",
            filtrada
        )

        mostrarEstilo(
            "Bruços",
            filtrada
        )

        mostrarEstilo(
            "Mariposa",
            filtrada
        )

        mostrarEstilo(
            "Estilos",
            filtrada
        )
    }

    private fun mostrarEstilo(
        estilo: String,
        lista: List<List<String>>
    ) {

        val provas =
            lista
                .filter {
                    it[0].contains(
                        estilo,
                        true
                    )
                }
                .sortedBy {

                    Regex("\\d+")
                        .find(it[0])
                        ?.value
                        ?.toIntOrNull()
                        ?: 9999

                }

        if (provas.isEmpty()) {
            return
        }

        content.addView(
            section(
                estilo.uppercase()
            )
        )

        for (p in provas) {

            val prova =
                p[0]

            val piscina =
                p[1]

            val tempo =
                p[2]

            val data =
                p[3]

            val cidade =
                p[4]

            val origem =
                p[5]

            val comparacao =
                compararTac(
                    prova,
                    piscina,
                    tempo
                )

            val qualificado =
                comparacao.contains(
                    "QUALIFICADO",
                    true
                )

            content.addView(
                infoCor(
                    prova,
                    "Piscina: $piscina\n" +
                            "Tempo: $tempo\n" +
                            "Data: $data\n" +
                            "Cidade: $cidade\n" +
                            "Origem: $origem\n\n" +
                            comparacao,
                    if (qualificado)
                        green
                    else
                        red
                )
            )
        }
    }

    private fun showTac() {

        clear(tabTac)

        content.addView(
            section("TAC")
        )

        content.addView(
            info(
                "Fonte",
                get("tac_fonte")
                    .ifBlank {
                        "Sem TAC importados."
                    }
            )
        )

        content.addView(
            info(
                "Resumo",
                get("tac_texto")
                    .ifBlank {
                        "Importa PDF ANDL/FPN."
                    }
            )
        )

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem tempos",
                    "Importa tempos primeiro."
                )
            )

            return
        }

        var qualificados = 0
        var falta = 0

        for (linha in tempos.split(";;")) {

            val p =
                linha.split("|")

            if (p.size >= 3) {

                val txt =
                    compararTac(
                        p[0],
                        p[1],
                        p[2]
                    )

                val ok =
                    txt.contains(
                        "QUALIFICADO",
                        true
                    )

                if (ok)
                    qualificados++
                else
                    falta++

                content.addView(
                    infoCor(
                        "${p[0]} ${p[1]}",
                        txt,
                        if (ok)
                            green
                        else
                            red
                    )
                )
            }
        }

        content.addView(
            section("RESUMO")
        )

        content.addView(
            info(
                "Qualificados",
                qualificados.toString()
            )
        )

        content.addView(
            info(
                "Por qualificar",
                falta.toString()
            )
        )
    }

    private fun showEvolucao() {

        clear(tabEvolucao)

        content.addView(
            section("EVOLUÇÃO")
        )

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem tempos",
                    "Importa tempos Swimrankings."
                )
            )

            return
        }

        for (linha in tempos.split(";;")) {

            val p =
                linha.split("|")

            if (p.size >= 3) {

                val prova =
                    p[0]

                val piscina =
                    p[1]

                val tempo =
                    p[2]

                val tac =
                    tac(
                        prova,
                        piscina
                    )

                if (tac.isNotBlank()) {

                    val dif =
                        tempoSeg(tempo) -
                                tempoSeg(tac)

                    val progresso =
                        if (dif <= 0) {
                            "🟢 QUALIFICADO"
                        } else {
                            "🔴 Faltam %.2f s"
                                .format(dif)
                        }

                    content.addView(
                        infoCor(
                            "$prova $piscina",
                            "Tempo: $tempo\n" +
                                    "TAC: $tac\n\n" +
                                    progresso,
                            if (dif <= 0)
                                green
                            else
                                red
                        )
                    )
                }
            }
        }
    }
        private fun showMais() {

        clear(tabMais)

        val idInput =
            input(
                "ID Swimrankings",
                get("id")
                    .ifBlank {
                        "5631298"
                    }
            )

        content.addView(
            section("DEFINIÇÕES")
        )

        content.addView(idInput)

        content.addView(
            button(
                "💾 GUARDAR ID"
            ) {

                prefs.edit()
                    .putString(
                        "id",
                        idInput.text
                            .toString()
                            .trim()
                    )
                    .apply()

                Toast.makeText(
                    this,
                    "ID guardado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        content.addView(
            button(
                "📄 EXPORTAR PDF"
            ) {
                exportarPdf()
            }
        )

        content.addView(
            button(
                "📤 EXPORTAR WHATSAPP"
            ) {
                exportarWhatsApp()
            }
        )

        content.addView(
            button(
                "🗑 LIMPAR DADOS"
            ) {

                prefs.edit()
                    .clear()
                    .apply()

                recreate()
            }
        )

        content.addView(
            section("DISCLAIMER")
        )

        content.addView(
            info(
                "Aviso",
                disclaimer()
            )
        )
    }

    private fun importarTextoGeral(
        texto: String
    ): String {

        if (texto.isBlank()) {
            return "Texto vazio."
        }

        return when {

            texto.contains(
                "Swimrankings",
                true
            ) ||
                    texto.contains(
                        "Piscina longa",
                        true
                    ) ||
                    texto.contains(
                        "Piscina curta",
                        true
                    ) ||
                    texto.contains(
                        "Recordes pess",
                        true
                    ) -> {

                val total =
                    importarPdfSwimrankings(
                        texto
                    )

                "Importados $total tempos."
            }

            texto.contains(
                "TABELA DE TEMPOS",
                true
            ) ||
                    texto.contains(
                        "TAC",
                        true
                    ) ||
                    texto.contains(
                        "Livres",
                        true
                    ) -> {

                importarPdfRegulamentoTac(
                    texto
                )

                "TAC importados."
            }

            else -> {

                val total =
                    importarPdfSwimrankings(
                        texto
                    )

                "Importação concluída: $total tempos."
            }
        }
    }

    private fun importarPdfSwimrankings(
        texto: String
    ): Int {

        importarPerfil(texto)

        val resultados =
            mutableListOf<String>()

        var piscinaAtual = ""

        val linhas =
            texto.lines()
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }

        for (i in linhas.indices) {

            val linha =
                linhas[i]

            if (
                linha.contains(
                    "Piscina curta",
                    true
                )
            ) {
                piscinaAtual = "25m"
                continue
            }

            if (
                linha.contains(
                    "Piscina longa",
                    true
                )
            ) {
                piscinaAtual = "50m"
                continue
            }

            val estilo =
                estiloDaLinha(linha)

            if (estilo != null) {

                val item =
                    extrairLinhaTempo(
                        linha,
                        estilo,
                        piscinaAtual
                    )

                if (item != null) {
                    resultados.add(item)
                }

                continue
            }

            val item2 =
                extrairBlocoTabela(
                    linhas,
                    i,
                    piscinaAtual
                )

            if (item2 != null) {
                resultados.add(item2)
            }
        }

        val melhores =
            escolherMelhores(
                resultados
            )

        prefs.edit()
            .putString(
                "tempos",
                melhores
            )
            .putString(
                "ultima_importacao",
                "Importados ${contarTempos(melhores)} tempos."
            )
            .apply()

        return contarTempos(melhores)
    }

    private fun importarPerfil(
        texto: String
    ) {

        val nome =
            when {

                texto.contains(
                    "PEDRO",
                    true
                ) ->
                    "Constança Rolim Pedro"

                else ->
                    get("nome")
            }

        val ano =
            Regex("\\b20\\d{2}\\b")
                .find(texto)
                ?.value
                ?: "2010"

        val clube =
            when {

                texto.contains(
                    "Bairro dos Anjos",
                    true
                ) ->
                    "Bairro dos Anjos / Leiria"

                else ->
                    get("clube")
            }

        prefs.edit()
            .putString(
                "nome",
                nome
            )
            .putString(
                "ano",
                ano
            )
            .putString(
                "clube",
                clube
            )
            .putString(
                "pais",
                "Portugal"
            )
            .apply()
    }

    private fun importarPdfRegulamentoTac(
        texto: String
    ) {

        val linhas =
            texto.lines()
                .map {
                    it.trim()
                        .replace(",", ".")
                }

        val tacs =
            mutableListOf<String>()

        var piscinaAtual = "25m"

        for (linha in linhas) {

            if (
                linha.contains(
                    "P50M",
                    true
                )
            ) {
                piscinaAtual = "50m"
            }

            if (
                linha.contains(
                    "P25M",
                    true
                )
            ) {
                piscinaAtual = "25m"
            }

            val regex =
                Regex(
                    "(\\d+)\\s+(Livres|Costas|Bruços|Mariposa|Estilos)"
                )

            val prova =
                regex.find(linha)

            if (prova != null) {

                val distancia =
                    prova.groupValues[1]

                val estiloNome =
                    prova.groupValues[2]

                val estilo =
                    when (estiloNome) {

                        "Livres" -> "L"
                        "Costas" -> "C"
                        "Bruços" -> "B"
                        "Mariposa" -> "M"
                        "Estilos" -> "E"

                        else -> "?"
                    }

                val tempos =
                    Regex(
                        "\\d{1,2}:\\d{2}\\.\\d{2}|\\d{2}\\.\\d{2}"
                    )
                        .findAll(linha)
                        .map {
                            it.value
                        }
                        .toList()

                for (tempo in tempos) {

                    val chave =
                        "$piscinaAtual|$distancia|$estilo"

                    tacs.add(
                        "$chave|ANDL|$tempo"
                    )
                }
            }
        }

        prefs.edit()
            .putString(
                "tacs",
                tacs.distinct()
                    .joinToString(";;")
            )
            .putString(
                "tac_fonte",
                "ANDL/FPN"
            )
            .putString(
                "tac_texto",
                "Importados ${tacs.size} TAC."
            )
            .apply()
    }
        private fun estiloDaLinha(
        linha: String
    ): String? {

        return when {

            linha.startsWith(
                "Livres ",
                true
            ) -> "Livres"

            linha.startsWith(
                "Costas ",
                true
            ) -> "Costas"

            linha.startsWith(
                "Bruços ",
                true
            ) -> "Bruços"

            linha.startsWith(
                "Mariposa ",
                true
            ) -> "Mariposa"

            linha.startsWith(
                "Estilos ",
                true
            ) -> "Estilos"

            else -> null
        }
    }

    private fun extrairLinhaTempo(
        linha: String,
        estilo: String,
        piscinaAtual: String
    ): String? {

        if (piscinaAtual.isBlank()) {
            return null
        }

        val dist =
            Regex("\\b(50|100|200|400|800|1500)m\\b")
                .find(linha)
                ?.value
                ?: return null

        val tempo =
            Regex("\\b\\d{1,2}:\\d{2}[.,]\\d{2}\\b|\\b\\d{2}[.,]\\d{2}\\b")
                .find(linha)
                ?.value
                ?.replace(",", ".")
                ?: return null

        val data =
            Regex("\\b\\d{1,2}\\s+[A-Za-zÀ-ÿ]{3}\\s+\\d{4}\\b")
                .find(linha)
                ?.value
                ?: "-"

        val cidade =
            extrairCidade(
                linha.substringAfter(
                    data,
                    ""
                )
            )

        val prova =
            "${dist.removeSuffix("m")} $estilo"

        return "$prova|$piscinaAtual|$tempo|$data|$cidade|Swimrankings"
    }

    private fun extrairBlocoTabela(
        linhas: List<String>,
        i: Int,
        piscinaAtual: String
    ): String? {

        if (piscinaAtual.isBlank()) {
            return null
        }

        val linha =
            linhas[i]

        val distMatch =
            Regex("^(50|100|200|400|800|1500)m$")
                .find(linha)
                ?: return null

        val estilo =
            procurarEstiloAnterior(
                linhas,
                i
            ) ?: return null

        val bloco =
            linhas.subList(
                i,
                minOf(
                    i + 8,
                    linhas.size
                )
            ).joinToString(" ")

        val tempo =
            Regex("\\b\\d{1,2}:\\d{2}[.,]\\d{2}\\b|\\b\\d{2}[.,]\\d{2}\\b")
                .find(bloco)
                ?.value
                ?.replace(",", ".")
                ?: return null

        val data =
            Regex("\\b\\d{1,2}\\s+[A-Za-zÀ-ÿ]{3}\\s+\\d{4}\\b")
                .find(bloco)
                ?.value
                ?: "-"

        val cidade =
            extrairCidade(
                bloco.substringAfter(
                    data,
                    ""
                )
            )

        val prova =
            "${distMatch.groupValues[1]} $estilo"

        return "$prova|$piscinaAtual|$tempo|$data|$cidade|Swimrankings"
    }

    private fun procurarEstiloAnterior(
        linhas: List<String>,
        idx: Int
    ): String? {

        for (
            j in idx downTo
                    maxOf(
                        0,
                        idx - 20
                    )
        ) {

            val l =
                linhas[j]

            when {

                l.startsWith(
                    "Livres",
                    true
                ) -> return "Livres"

                l.startsWith(
                    "Costas",
                    true
                ) -> return "Costas"

                l.startsWith(
                    "Bruços",
                    true
                ) -> return "Bruços"

                l.startsWith(
                    "Mariposa",
                    true
                ) -> return "Mariposa"

                l.startsWith(
                    "Estilos",
                    true
                ) -> return "Estilos"
            }
        }

        return null
    }

    private fun extrairCidade(
        txt: String
    ): String {

        val cidades =
            listOf(
                "Coimbra",
                "Leiria",
                "Tomar",
                "Pombal",
                "Benedita",
                "Condeixa-a-Nova",
                "Badajoz",
                "Alcobaça",
                "Caldas da Rainha"
            )

        for (c in cidades) {

            if (
                txt.contains(
                    c,
                    true
                )
            ) {
                return c
            }
        }

        return "-"
    }

    private fun escolherMelhores(
        lista: List<String>
    ): String {

        val mapa =
            mutableMapOf<String, String>()

        for (linha in lista) {

            val p =
                linha.split("|")

            if (p.size >= 3) {

                val chave =
                    "${p[0]}|${p[1]}"

                val antigo =
                    mapa[chave]

                if (
                    antigo == null ||
                    tempoSeg(p[2]) <
                    tempoSeg(
                        antigo.split("|")[2]
                    )
                ) {

                    mapa[chave] =
                        linha
                }
            }
        }

        return mapa.values
            .sortedWith(
                compareBy(
                    {
                        it.split("|")[1]
                    },
                    {
                        ordemEstilo(
                            it.split("|")[0]
                        )
                    },
                    {
                        Regex("\\d+")
                            .find(
                                it.split("|")[0]
                            )
                            ?.value
                            ?.toIntOrNull()
                            ?: 9999
                    }
                )
            )
            .joinToString(";;")
    }

    private fun ordemEstilo(
        prova: String
    ): Int {

        return when {

            prova.contains(
                "Livres",
                true
            ) -> 1

            prova.contains(
                "Costas",
                true
            ) -> 2

            prova.contains(
                "Bruços",
                true
            ) -> 3

            prova.contains(
                "Mariposa",
                true
            ) -> 4

            prova.contains(
                "Estilos",
                true
            ) -> 5

            else -> 9
        }
    }

    private fun tac(
        prova: String,
        piscina: String
    ): String {

        val estilo =
            when {

                prova.contains(
                    "Livres",
                    true
                ) -> "L"

                prova.contains(
                    "Costas",
                    true
                ) -> "C"

                prova.contains(
                    "Bruços",
                    true
                ) -> "B"

                prova.contains(
                    "Mariposa",
                    true
                ) -> "M"

                prova.contains(
                    "Estilos",
                    true
                ) -> "E"

                else -> "?"
            }

        val distancia =
            Regex("\\d+")
                .find(prova)
                ?.value
                ?: "?"

        val chave =
            "$piscina|$distancia|$estilo"

        val lista =
            get("tacs")

        if (lista.isNotBlank()) {

            val encontrados =
                mutableListOf<String>()

            for (linha in lista.split(";;")) {

                val p =
                    linha.split("|")

                if (
                    p.size >= 3 &&
                    p[0] == chave
                ) {

                    encontrados.add(
                        p[2]
                    )
                }
            }

            if (encontrados.isNotEmpty()) {

                return encontrados.minByOrNull {
                    tempoSeg(it)
                } ?: ""
            }
        }

        return ""
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

            "✅ QUALIFICADO\n" +
                    "Tempo: $tempo\n" +
                    "TAC: $alvo\n" +
                    "Margem: %.2f s"
                        .format(
                            kotlin.math.abs(
                                dif
                            )
                        )

        } else {

            "❌ NÃO QUALIFICADO\n" +
                    "Tempo: $tempo\n" +
                    "TAC: $alvo\n" +
                    "Faltam: %.2f s"
                        .format(dif)
        }
    }

    private fun objetivoMaisProximo(): String {

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {
            return "Sem tempos."
        }

        var melhor = ""
        var menor = 99999.0

        for (linha in tempos.split(";;")) {

            val p =
                linha.split("|")

            if (p.size >= 3) {

                val alvo =
                    tac(
                        p[0],
                        p[1]
                    )

                if (alvo.isNotBlank()) {

                    val dif =
                        tempoSeg(p[2]) -
                                tempoSeg(alvo)

                    if (
                        dif > 0 &&
                        dif < menor
                    ) {

                        menor = dif
                        melhor =
                            "${p[0]} ${p[1]}"
                    }
                }
            }
        }

        return if (melhor.isBlank()) {
            "Sem objetivo por atingir."
        } else {
            "$melhor — faltam %.2f s"
                .format(menor)
        }
    }

    private fun tempoSeg(
        t: String
    ): Double {

        return try {

            val v =
                t.replace(
                    ",",
                    "."
                )

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

    private fun contarTempos(
        tempos: String
    ): Int {

        if (tempos.isBlank()) {
            return 0
        }

        return tempos
            .split(";;")
            .filter {
                it.isNotBlank()
            }
            .size
    }

    private fun resumoAtleta(): String {

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {
            return "Sem tempos importados."
        }

        return "${contarTempos(tempos)} tempos importados.\n${objetivoMaisProximo()}"
    }

    private fun escalao(
        anoTexto: String
    ): String {

        val ano =
            anoTexto.toIntOrNull()
                ?: return "Por definir"

        val idade =
            2026 - ano

        return when {

            idade <= 12 -> "Infantil"

            idade <= 16 -> "Juvenil"

            idade <= 18 -> "Júnior"

            else -> "Sénior"
        }
    }

    private fun resumoTopo(): String {

        val nome =
            get("nome")
                .ifBlank {
                    "SwimTrack"
                }

        val n =
            contarTempos(
                get("tempos")
            )

        return if (n == 0) {
            "$nome • Sem tempos"
        } else {
            "$nome • $n tempos"
        }
    }

    private fun exportarPdf() {

        val texto =
            "SWIMTRACK\n\n" +
                    "Atleta: ${get("nome")}\n" +
                    "Clube: ${get("clube")}\n\n" +
                    formatarTempos()

        val file =
            File(
                cacheDir,
                "swimtrack_relatorio.pdf"
            )

        FileOutputStream(file).use {
            it.write(
                texto.toByteArray()
            )
        }

        val uri =
            FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )

        val intent =
            Intent(Intent.ACTION_SEND)

        intent.type =
            "application/pdf"

        intent.putExtra(
            Intent.EXTRA_STREAM,
            uri
        )

        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        startActivity(
            Intent.createChooser(
                intent,
                "Exportar PDF"
            )
        )
    }

    private fun exportarWhatsApp() {

        val msg =
            "🏊‍♀️ SwimTrack\n\n" +
                    "Atleta: ${get("nome")}\n" +
                    "Clube: ${get("clube")}\n\n" +
                    "Resumo:\n${resumoAtleta()}\n\n" +
                    formatarTempos()

        val intent =
            Intent(Intent.ACTION_SEND)

        intent.type =
            "text/plain"

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
            get("tempos")

        if (tempos.isBlank()) {
            return "Sem tempos."
        }

        return tempos
            .split(";;")
            .joinToString("\n") {

                val p =
                    it.split("|")

                if (p.size >= 3) {
                    "- ${p[0]} ${p[1]}: ${p[2]} | ${compararTac(p[0], p[1], p[2])}"
                } else {
                    ""
                }
            }
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
                                "Não foi possível abrir PDF.",
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

                        val msg =
                            importarTextoGeral(texto)

                        Toast.makeText(
                            this,
                            msg,
                            Toast.LENGTH_LONG
                        ).show()

                        showTempos()
                    }

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Erro PDF: ${e.message}",
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

    private fun topSummary(): TextView {

        return label(
            resumoTopo(),
            yellow,
            cardDark,
            15f
        )
    }

    private fun title(
        text: String
    ): TextView {

        return label(
            text,
            white,
            bg,
            34f
        )
    }

    private fun subtitle(
        text: String
    ): TextView {

        return label(
            text,
            soft,
            bg,
            14f
        )
    }

    private fun section(
        text: String
    ): TextView {

        return label(
            text,
            yellow,
            bg,
            20f
        )
    }

    private fun tab(
        text: String
    ): TextView {

        return label(
            text,
            white,
            card,
            12f
        )
    }

    private fun label(
        text: String,
        txtColor: Int,
        bgColor: Int,
        size: Float
    ): TextView {

        val t =
            TextView(this)

        t.text = text
        t.textSize = size
        t.setTextColor(txtColor)
        t.setBackgroundColor(bgColor)

        t.gravity =
            Gravity.CENTER

        t.setTypeface(
            Typeface.DEFAULT_BOLD
        )

        t.setPadding(
            12,
            14,
            12,
            14
        )

        return t
    }

    private fun input(
        hint: String,
        value: String = ""
    ): EditText {

        val e =
            EditText(this)

        e.hint = hint
        e.setText(value)
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

        return b
    }

    private fun info(
        title: String,
        body: String
    ): LinearLayout {

        return infoCor(
            title,
            body,
            card
        )
    }

    private fun infoCor(
        title: String,
        body: String,
        cor: Int
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

        c.setBackgroundColor(cor)

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
        b.setTextColor(white)

        b.setPadding(
            0,
            8,
            0,
            0
        )

        c.addView(t)
        c.addView(b)

        return c
    }

    private fun disclaimer(): String {

        return "SwimTrack é uma aplicação de uso pessoal.\n\n" +
                "Sem ligação oficial à FPN, ANDL ou Swimrankings."
    }
}
