
package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var root: LinearLayout
    private lateinit var webView: WebView
    private lateinit var resultado: TextView
    private lateinit var idInput: EditText

    private val bg = Color.rgb(18, 35, 70)
    private val card = Color.rgb(61, 82, 120)
    private val blue = Color.rgb(60, 170, 255)
    private val yellow = Color.rgb(255, 220, 45)
    private val white = Color.WHITE
    private val soft = Color.rgb(210, 225, 240)

    private var textoPagina = ""

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
        root.addView(subtitle("PERFIL • SWIMRANKINGS • GUARDAR DADOS"))

        idInput = input("ID Swimrankings", prefs.getString("id", "5631298") ?: "5631298")
        root.addView(idInput)

        root.addView(button("🌐 Abrir perfil Swimrankings") {
            abrirPerfil()
        })

        root.addView(button("📥 Importar dados visíveis") {
            importarTextoWebView()
        })

        root.addView(button("💾 Guardar dados importados") {
            guardarDados()
        })

        root.addView(button("🗑 Limpar dados") {
            prefs.edit().clear().apply()
            textoPagina = ""
            buildScreen()
        })

        resultado = textBox("DADOS DO ATLETA", dadosGuardados())
        root.addView(resultado)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.visibility = View.GONE

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Toast.makeText(this@MainActivity, "Página carregada. Agora clica em Importar dados visíveis.", Toast.LENGTH_LONG).show()
            }
        }

        root.addView(webView)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun abrirPerfil() {
        val id = idInput.text.toString().trim()
        prefs.edit().putString("id", id).apply()

        val url = "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"

        webView.visibility = View.VISIBLE
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            900
        )
        webView.loadUrl(url)
    }

    private fun importarTextoWebView() {
        webView.evaluateJavascript(
            "(function(){return document.body.innerText;})();"
        ) { value ->
            textoPagina = value
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\"", "")
                .trim()

            val nome = extrairNome(textoPagina)
            val clube = extrairClube(textoPagina)
            val tempos = extrairTempos(textoPagina)

            resultado.text =
                "DADOS IMPORTADOS\n\n" +
                "Nome: $nome\n" +
                "Clube: $clube\n" +
                "ID: ${prefs.getString("id", "")}\n\n" +
                "TEMPOS:\n$tempos"

            Toast.makeText(this, "Dados lidos da página.", Toast.LENGTH_LONG).show()
        }
    }

    private fun guardarDados() {
        val nome = extrairNome(textoPagina)
        val clube = extrairClube(textoPagina)
        val tempos = extrairTempos(textoPagina)

        prefs.edit()
            .putString("nome", nome)
            .putString("clube", clube)
            .putString("tempos", tempos)
            .apply()

        resultado.text = dadosGuardados()

        Toast.makeText(this, "Dados guardados.", Toast.LENGTH_LONG).show()
    }

    private fun dadosGuardados(): String {
        val nome = prefs.getString("nome", "") ?: ""
        val clube = prefs.getString("clube", "") ?: ""
        val id = prefs.getString("id", "") ?: ""
        val tempos = prefs.getString("tempos", "") ?: ""

        if (nome.isBlank() && tempos.isBlank()) {
            return "DADOS DO ATLETA\n\nAinda sem dados guardados.\n\n1. Abrir perfil Swimrankings\n2. Importar dados visíveis\n3. Guardar dados"
        }

        return "DADOS GUARDADOS\n\n" +
                "Nome: $nome\n" +
                "Clube: $clube\n" +
                "ID: $id\n\n" +
                "TEMPOS:\n$tempos"
    }

    private fun extrairNome(txt: String): String {
        val linhas = txt.lines().map { it.trim() }.filter { it.isNotBlank() }

        for (linha in linhas) {
            if (
                linha.length in 5..60 &&
                !linha.contains("Swimrankings", true) &&
                !linha.contains("Ranking", true) &&
                !linha.contains("Results", true) &&
                !linha.contains("Times", true) &&
                linha.any { it.isLetter() }
            ) {
                return linha
            }
        }

        return "Por identificar"
    }

    private fun extrairClube(txt: String): String {
        val linhas = txt.lines().map { it.trim() }

        for (i in linhas.indices) {
            val l = linhas[i]

            if (l.contains("Club", true) || l.contains("Team", true)) {
                if (i + 1 < linhas.size) return linhas[i + 1]
            }

            if (
                l.contains("CN", true) ||
                l.contains("Sporting", true) ||
                l.contains("Benfica", true) ||
                l.contains("Natação", true) ||
                l.contains("Clube", true)
            ) {
                return l
            }
        }

        return "Por identificar"
    }

    private fun extrairTempos(txt: String): String {
        val linhas = txt.lines().map { it.trim() }.filter { it.isNotBlank() }

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
            "400 Medley" to "400 Estilos"
        )

        val resultados = mutableListOf<String>()

        for (i in linhas.indices) {
            for ((en, pt) in provas) {
                if (linhas[i].contains(en, true)) {
                    val bloco = linhas.subList(i, minOf(i + 8, linhas.size)).joinToString(" ")

                    val tempo = Regex("\\d{1,2}:\\d{2}\\.\\d{2}|\\d{2}\\.\\d{2}")
                        .find(bloco)
                        ?.value

                    if (tempo != null) {
                        resultados.add("$pt — $tempo")
                    }
                }
            }
        }

        return if (resultados.isEmpty()) {
            "Não foi possível extrair tempos automaticamente.\nUsa a página aberta para validar os dados."
        } else {
            resultados.distinct().joinToString("\n")
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

    private fun input(hint: String, value: String = ""): EditText {
        val e = EditText(this)
        e.hint = hint
        e.setText(value)
        e.textSize = 16f
        e.setTextColor(white)
        e.setHintTextColor(soft)
        e.setBackgroundColor(card)
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

    private fun textBox(title: String, body: String): TextView {
        val t = TextView(this)
        t.text = body
        t.textSize = 15f
        t.setTextColor(white)
        t.setBackgroundColor(card)
        t.setPadding(22, 18, 22, 18)

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 20, 0, 14)
        t.layoutParams = lp

        return t
    }
}
