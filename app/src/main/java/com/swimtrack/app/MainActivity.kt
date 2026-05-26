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
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(28, 40, 28, 40)
        root.setBackgroundColor(bg)
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)
        icon.layoutParams = LinearLayout.LayoutParams(220, 220)

        root.addView(icon)
        root.addView(title("SWIMTRACK"))
        root.addView(subtitle("PERFIL • PDF • TAC • EVOLUÇÃO"))
        root.addView(topSummary())

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
        row.setPadding(0, 22, 0, 20)

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

    private fun clear(active: TextView) {
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

        content.addView(section("ATLETA"))

        content.addView(
            info(
                "Nome",
                get("nome").ifBlank { "Por importar" }
            )
        )

        content.addView(
            info(
                "Ano",
                get("ano").ifBlank { "Por importar" }
            )
        )

        content.addView(
            info(
                "País",
                get("pais").ifBlank { "Por importar" }
            )
        )

        content.addView(
            info(
                "Clube",
                get("clube").ifBlank { "Por importar" }
            )
        )

        content.addView(
            info(
                "Escalão estimado",
                escalao(get("ano"))
            )
        )

        content.addView(
            info(
                "Resumo",
                resumoAtleta()
            )
        )

        content.addView(
            button("🌐 ABRIR SWIMRANKINGS") {
                val id = get("id").ifBlank { "5631298" }
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

        content.addView(section("IMPORTAR PDF"))

        content.addView(
            info(
                "PDF Swimrankings",
                "Importa PDF de recordes pessoais. Reconhece piscina curta 25m e piscina longa 50m."
            )
        )

        content.addView(
            info(
                "PDF ANDL / FPN",
                "Reconhece regulamentos com TAC / Tempos de Admissão e guarda a fonte localmente."
            )
        )

        content.addView(
            button("📄 IMPORTAR PDF") {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/pdf"

                startActivityForResult(
                    intent,
                    PICK_PDF
                )
            }
        )

        content.addView(
            button("🧪 COLAR TEXTO PDF / SWIMRANKINGS") {
                showColarTexto()
            }
        )
    }

    private fun showColarTexto() {
        clear(tabImportar)

        content.addView(section("COLAR TEXTO"))

        val caixa = inputMulti(
            "Cola aqui texto do PDF ou do Swimrankings"
        )

        content.addView(caixa)

        content.addView(
            button("📥 IMPORTAR TEXTO") {
                val texto = caixa.text.toString()
                val msg = importarTextoGeral(texto)

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

        content.addView(
            info(
                "Última importação",
                get("ultima_importacao")
                    .ifBlank {
                        "Ainda sem importação."
                    }
            )
        )

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {

            content.addView(
                info(
                    "Sem dados",
                    "Importa o PDF Swimrankings de recordes pessoais."
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

            content.addView(
                info(
                    prova,
                    "Piscina: $piscina\n" +
                            "Tempo: $tempo\n" +
                            "Data: $data\n" +
                            "Cidade: $cidade\n" +
                            "Origem: $origem\n\n" +
                            compararTac(
                                prova,
                                piscina,
                                tempo
                            )
                )
            )
        }
    }

    private fun showTac() {
        clear(tabTac)

        content.addView(
            section("TAC / OBJETIVOS")
        )

        val tacFonte =
            get("tac_fonte")

        if (tacFonte.isNotBlank()) {
            content.addView(
                info(
                    "Regulamento TAC",
                    tacFonte
                )
            )
        }

        val tacTexto =
            get("tac_texto")

        if (tacTexto.isNotBlank()) {
            content.addView(
                info(
                    "Resumo do regulamento importado",
                    tacTexto
                )
            )
        }

        val tempos =
            get("tempos")

        if (tempos.isBlank()) {
            content.addView(
                info(
                    "Sem tempos",
                    "Importa primeiro os tempos da atleta."
                )
            )

            return
        }

        var definidos = 0
        var atingidos = 0

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
                    definidos++

                    if (
                        tempoSeg(p[2]) <=
                        tempoSeg(alvo)
                    ) {
                        atingidos++
                    }
                }

                content.addView(
                    info(
                        "${p[0]} — ${p[1]}",
                        compararTac(
                            p[0],
                            p[1],
                            p[2]
                        )
                    )
                )
            }
        }

        content.addView(
            section("RESUMO")
        )

        content.addView(
            info(
                "TAC atingidos",
                "$atingidos de $definidos TAC definidos."
            )
        )

        content.addView(
            info(
                "Objetivo mais próximo",
                objetivoMaisProximo()
            )
        )
    }

    private fun showMais() {
        clear(tabMais)

        val idInput =
            input(
                "ID Swimrankings",
                get("id").ifBlank {
                    "5631298"
                }
            )

        content.addView(
            section("DEFINIÇÕES")
        )

        content.addView(idInput)

        content.addView(
            button("💾 GUARDAR ID") {
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
            button("📤 EXPORTAR WHATSAPP") {
                exportarWhatsApp()
            }
        )

        content.addView(
            button("🗑 LIMPAR DADOS") {
                prefs.edit()
                    .clear()
                    .apply()

                buildUI()
            }
        )

        content.addView(
            section("SEGURANÇA")
        )

        content.addView(
            info(
                "Modo de uso",
                "Uso pessoal. Sem login. Sem servidor. Sem custos. Dados guardados localmente no telemóvel."
            )
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

                "PDF Swimrankings importado: $total tempos."
            }

            texto.contains(
                "TAC",
                true
            ) ||
                    texto.contains(
                        "Tempos de Admissão",
                        true
                    ) ||
                    texto.contains(
                        "Regulamento",
                        true
                    ) ||
                    texto.contains(
                        "FPN",
                        true
                    ) ||
                    texto.contains(
                        "ANDL",
                        true
                    ) -> {
                importarPdfRegulamentoTac(
                    texto
                )

                "PDF regulamento/TAC importado."
            }

            else -> {
                val total =
                    importarPdfSwimrankings(
                        texto
                    )

                "Importação genérica concluída: $total tempos."
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
                    "Piscina longa",
                    true
                ) ||
                (
                    linha.contains(
                        "(50m)",
                        true
                    ) &&
                    linha.contains(
                        "Piscina",
                        true
                    )
                )
            ) {
                piscinaAtual = "50m"
                continue
            }

            if (
                linha.contains(
                    "Piscina curta",
                    true
                ) ||
                (
                    linha.contains(
                        "(25m)",
                        true
                    ) &&
                    linha.contains(
                        "Piscina",
                        true
                    )
                )
            ) {
                piscinaAtual = "25m"
                continue
            }

            val modalidade =
                estiloDaLinha(linha)

            if (modalidade != null) {

                val item =
                    extrairLinhaTempo(
                        linha,
                        modalidade,
                        piscinaAtual
                    )

                if (item != null) {
                    resultados.add(item)
                }

                continue
            }

            val itemBloco =
                extrairBlocoTabela(
                    linhas,
                    i,
                    piscinaAtual
                )

            if (itemBloco != null) {
                resultados.add(itemBloco)
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
                "Importados ${contarTempos(melhores)} melhores tempos do PDF Swimrankings."
            )
            .apply()

        return contarTempos(melhores)
    }
        private fun importarPerfil(
        texto: String
    ) {
        val linhas =
            texto.lines()
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }

        val linhaPerfil =
            linhas.firstOrNull {
                it.contains(
                    "PEDRO",
                    true
                ) ||
                        it.contains(
                            "POR - Portugal",
                            true
                        )
            } ?: ""

        val nome =
            if (
                linhaPerfil.contains(
                    "PEDRO",
                    true
                )
            ) {
                linhaPerfil
                    .substringBefore("2010")
                    .trim()
                    .ifBlank {
                        "PEDRO, Constanca Rolim"
                    }
            } else {
                get("nome")
                    .ifBlank {
                        "PEDRO, Constanca Rolim"
                    }
            }

        val ano =
            Regex(
                "\\b(20\\d{2}|19\\d{2})\\b"
            )
                .find(linhaPerfil)
                ?.value
                ?: Regex(
                    "\\b(20\\d{2}|19\\d{2})\\b"
                )
                    .find(texto)
                    ?.value
                ?: get("ano")
                    .ifBlank {
                        "2010"
                    }

        val pais =
            if (
                texto.contains(
                    "POR - Portugal",
                    true
                )
            ) {
                "Portugal"
            } else {
                get("pais")
                    .ifBlank {
                        "Portugal"
                    }
            }

        val clube =
            when {
                texto.contains(
                    "Assoc Desp Cult Rec Bairro dos Anjos",
                    true
                ) ->
                    "Assoc Desp Cult Rec Bairro dos Anjos"

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
                "pais",
                pais
            )
            .putString(
                "clube",
                clube
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
                "Brucos ",
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
        val piscina =
            piscinaAtual.ifBlank {
                when {
                    linha.contains(
                        "25m"
                    ) -> "25m"

                    linha.contains(
                        "50m"
                    ) -> "50m"

                    else -> ""
                }
            }

        if (piscina.isBlank()) {
            return null
        }

        val dist =
            Regex(
                "\\b(50|100|200|400|800|1500)m\\b"
            )
                .find(linha)
                ?.value
                ?: return null

        val tempo =
            Regex(
                "\\b\\d{1,2}:\\d{2}[.,]\\d{2}\\b|\\b\\d{2}[.,]\\d{2}\\b"
            )
                .find(linha)
                ?.value
                ?.replace(
                    ",",
                    "."
                )
                ?: return null

        val data =
            Regex(
                "\\b\\d{1,2}\\s+[A-Za-zÀ-ÿ]{3}\\s+\\d{4}\\b"
            )
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

        return "$prova|$piscina|$tempo|$data|$cidade|Swimrankings"
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
            Regex(
                "^(50|100|200|400|800|1500)m$"
            )
                .find(linha)
                ?: return null

        var estilo =
            procurarEstiloAnterior(
                linhas,
                i
            ) ?: return null

        if (estilo == "Brucos") {
            estilo = "Bruços"
        }

        val bloco =
            linhas
                .subList(
                    i,
                    minOf(
                        i + 8,
                        linhas.size
                    )
                )
                .joinToString(" ")

        val tempo =
            Regex(
                "\\b\\d{1,2}:\\d{2}[.,]\\d{2}\\b|\\b\\d{2}[.,]\\d{2}\\b"
            )
                .find(bloco)
                ?.value
                ?.replace(
                    ",",
                    "."
                )
                ?: return null

        val data =
            Regex(
                "\\b\\d{1,2}\\s+[A-Za-zÀ-ÿ]{3}\\s+\\d{4}\\b"
            )
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
                    "Brucos",
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
        if (txt.isBlank()) {
            return "-"
        }

        val conhecidas =
            listOf(
                "Coimbra",
                "Leiria",
                "Tomar",
                "Pombal",
                "Benedita",
                "Condeixa-a-Nova",
                "Badajoz (ESP)",
                "Badajoz"
            )

        for (c in conhecidas) {
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
                        it.split("|")[0]
                    },
                    {
                        it.split("|")[1]
                    }
                )
            )
            .joinToString(";;")
    }

    private fun importarPdfRegulamentoTac(
        texto: String
    ) {
        val fonte =
            when {
                texto.contains(
                    "ANDL",
                    true
                ) -> "ANDL"

                texto.contains(
                    "FPN",
                    true
                ) ||
                        texto.contains(
                            "Federação Portuguesa",
                            true
                        ) -> "FPN"

                else -> "Regulamento"
            }

        val resumo =
            texto.lines()
                .map {
                    it.trim()
                }
                .filter {
                    it.contains(
                        "TAC",
                        true
                    ) ||
                            it.contains(
                                "Tempo",
                                true
                            ) ||
                            it.contains(
                                "Admissão",
                                true
                            ) ||
                            it.contains(
                                "Juvenil",
                                true
                            ) ||
                            it.contains(
                                "Infantil",
                                true
                            ) ||
                            it.contains(
                                "Júnior",
                                true
                            )
                }
                .take(40)
                .joinToString("\n")

        prefs.edit()
            .putString(
                "tac_fonte",
                "$fonte importado"
            )
            .putString(
                "tac_texto",
                resumo
            )
            .apply()
    }

    private fun tac(
        prova: String,
        piscina: String
    ): String {
        return when {
            prova.contains(
                "50 Livres",
                true
            ) && piscina == "50m" -> "31.00"

            prova.contains(
                "50 Livres",
                true
            ) && piscina == "25m" -> "30.50"

            prova.contains(
                "100 Livres",
                true
            ) && piscina == "50m" -> "1:07.50"

            prova.contains(
                "100 Livres",
                true
            ) && piscina == "25m" -> "1:05.50"

            prova.contains(
                "200 Livres",
                true
            ) && piscina == "50m" -> "2:29.00"

            prova.contains(
                "200 Livres",
                true
            ) && piscina == "25m" -> "2:23.00"

            prova.contains(
                "400 Livres",
                true
            ) && piscina == "50m" -> "5:10.00"

            prova.contains(
                "400 Livres",
                true
            ) && piscina == "25m" -> "5:03.00"

            prova.contains(
                "800 Livres",
                true
            ) && piscina == "50m" -> "10:35.00"

            prova.contains(
                "800 Livres",
                true
            ) && piscina == "25m" -> "10:25.00"

            prova.contains(
                "100 Mariposa",
                true
            ) && piscina == "25m" -> "1:14.00"

            prova.contains(
                "100 Mariposa",
                true
            ) && piscina == "50m" -> "1:18.00"

            prova.contains(
                "200 Mariposa",
                true
            ) && piscina == "25m" -> "2:55.00"

            prova.contains(
                "200 Estilos",
                true
            ) && piscina == "25m" -> "2:47.00"

            prova.contains(
                "400 Estilos",
                true
            ) && piscina == "25m" -> "5:55.00"

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
            "✅ TAC atingido\n" +
                    "Tempo: $tempo\n" +
                    "TAC: $alvo\n" +
                    "Margem: %.2f s"
                        .format(
                            kotlin.math.abs(
                                dif
                            )
                        )
        } else {
            "⏳ Faltam %.2f s\n" +
                    "Tempo: $tempo\n" +
                    "TAC: $alvo"
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
            "Não há TAC por atingir na tabela interna ou faltam TAC oficiais."
        } else {
            "$melhor — faltam %.2f s."
                .format(menor)
        }
    }

    private fun resumoAtleta(): String {
        val tempos =
            get("tempos")

        if (tempos.isBlank()) {
            return "Sem tempos importados."
        }

        return "${contarTempos(tempos)} melhores tempos importados.\n${objetivoMaisProximo()}"
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
            "$nome • Sem tempos importados"
        } else {
            "$nome • $n melhores tempos"
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

    private fun exportarWhatsApp() {
        val msg =
            "🏊‍♀️ SwimTrack\n\n" +
                    "Atleta: ${get("nome")}\n" +
                    "Clube: ${get("clube")}\n" +
                    "Escalão: ${escalao(get("ano"))}\n\n" +
                    "Resumo:\n${resumoAtleta()}\n\n" +
                    "Tempos:\n${formatarTempos()}\n\n" +
                    "Objetivo:\n${objetivoMaisProximo()}"

        val intent =
            Intent(
                Intent.ACTION_SEND
            )

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
                    "- ${p[0]} ${p[1]}: ${p[2]}"
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

        e.layoutParams =
            lp

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

        b.layoutParams =
            lp

        return b
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

        b.text =
            if (body.isBlank()) {
                "Por definir"
            } else {
                body
            }

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

        c.layoutParams =
            lp

        return c
    }

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico.\n\n" +
                "Sem ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Importa PDFs Swimrankings, ANDL e FPN para uso local."
    }
}
