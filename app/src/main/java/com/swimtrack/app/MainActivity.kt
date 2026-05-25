package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val azulClaro = Color.rgb(190, 235, 255)
        val azul = Color.rgb(45, 168, 255)
        val azulEscuro = Color.rgb(0, 105, 180)
        val texto = Color.rgb(16, 32, 51)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)

        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.gravity = Gravity.CENTER
        header.setPadding(30, 40, 30, 30)
        header.setBackgroundColor(azulClaro)

        val icon = ImageView(this)
        icon.setImageResource(resources.getIdentifier("swimtrack_icon", "mipmap", packageName))
        icon.layoutParams = LinearLayout.LayoutParams(180, 180)

        val title = TextView(this)
        title.text = "SwimTrack"
        title.textSize = 32f
        title.setTypeface(Typeface.DEFAULT_BOLD)
        title.setTextColor(texto)
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "Gestão pessoal de tempos de natação"
        subtitle.textSize = 16f
        subtitle.setTextColor(texto)
        subtitle.gravity = Gravity.CENTER

        header.addView(icon)
        header.addView(title)
        header.addView(subtitle)

        val scroll = ScrollView(this)
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(24, 24, 24, 24)

        content.addView(card("🏊 Perfil da atleta", "Nome: Constança\nEscalão: automático\nAssociação: ANDL", azulClaro, azulEscuro, texto))
        content.addView(card("⏱ Tempos pessoais", "Registo dos melhores tempos por prova.\nLigação futura ao Swimrankings.", azulClaro, azulEscuro, texto))
        content.addView(card("🎯 TAC", "Comparação com TAC Distritais, Zonais e Nacionais.\nFontes: ANDL e FPN.", azulClaro, azulEscuro, texto))
        content.addView(card("📈 Evolução", "Histórico de melhoria por prova, piscina e época.", azulClaro, azulEscuro, texto))

        content.addView(button("Partilhar resumo WhatsApp", azul) {
            val textoPartilha =
                "🏊‍♀️ SwimTrack\nAtleta: Constança\n\nTempos • TAC • Evolução\n\nFontes previstas: Swimrankings • ANDL • FPN"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, textoPartilha)
            startActivity(Intent.createChooser(intent, "Partilhar"))
        })

        content.addView(card("⚠️ Disclaimer", disclaimer(), azulClaro, azulEscuro, texto))

        scroll.addView(content)
        root.addView(header)
        root.addView(scroll)

        setContentView(root)
    }

    private fun card(titulo: String, corpo: String, fundo: Int, tituloCor: Int, textoCor: Int): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(24, 20, 24, 20)
        card.setBackgroundColor(fundo)

        val t = TextView(this)
        t.text = titulo
        t.textSize = 20f
        t.setTypeface(Typeface.DEFAULT_BOLD)
        t.setTextColor(tituloCor)

        val b = TextView(this)
        b.text = corpo
        b.textSize = 15f
        b.setTextColor(textoCor)
        b.setPadding(0, 10, 0, 0)

        card.addView(t)
        card.addView(b)

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 22)
        card.layoutParams = lp

        return card
    }

    private fun button(textoBotao: String, cor: Int, acao: () -> Unit): Button {
        val b = Button(this)
        b.text = textoBotao
        b.textSize = 16f
        b.setTextColor(Color.WHITE)
        b.setBackgroundColor(cor)
        b.setPadding(16, 16, 16, 16)
        b.setOnClickListener { acao() }
        return b
    }

    private fun disclaimer(): String {
        return "SwimTrack é uma aplicação de uso pessoal e académico destinada ao acompanhamento desportivo de atletas de natação.\n\n" +
                "A aplicação não possui qualquer ligação oficial à FPN, ANDL ou Swimrankings.\n\n" +
                "Os dados poderão ser consultados a partir de fontes públicas: Swimrankings, ANDL e FPN."
    }
}
