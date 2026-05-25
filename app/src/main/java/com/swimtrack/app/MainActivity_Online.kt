// SwimTrack Online Version
// Colocar em:
// app/src/main/java/com/swimtrack/app/MainActivity.kt

package com.swimtrack.app

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.Gravity
import kotlin.concurrent.thread
import java.net.URL

class MainActivity : Activity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("swimtrack", MODE_PRIVATE)

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40,40,40,40)
        root.setBackgroundColor(Color.rgb(18,35,70))
        root.gravity = Gravity.CENTER_HORIZONTAL

        val icon = ImageView(this)
        icon.setImageResource(R.mipmap.ic_launcher)

        val title = TextView(this)
        title.text = "SWIMTRACK"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)

        val subtitle = TextView(this)
        subtitle.text = "Importação Swimrankings"
        subtitle.textSize = 16f
        subtitle.setTextColor(Color.LTGRAY)

        val atletaId = EditText(this)
        atletaId.hint = "ID Swimrankings"
        atletaId.setText(prefs.getString("id",""))
        atletaId.setTextColor(Color.WHITE)
        atletaId.setHintTextColor(Color.LTGRAY)

        val resultado = TextView(this)
        resultado.setTextColor(Color.WHITE)
        resultado.textSize = 16f

        val botao = Button(this)
        botao.text = "IMPORTAR SWIMRANKINGS"
        botao.setBackgroundColor(Color.rgb(60,170,255))
        botao.setTextColor(Color.WHITE)

        botao.setOnClickListener {

            val id = atletaId.text.toString()

            prefs.edit().putString("id", id).apply()

            resultado.text = "A importar dados online..."

            thread {

                try {

                    val url = "https://www.swimrankings.net/index.php?page=athleteDetail&athleteId=$id"

                    val html = URL(url).readText()

                    prefs.edit()
                        .putString("ultimo_html", html.take(3000))
                        .putString("ultimo_url", url)
                        .apply()

                    runOnUiThread {

                        resultado.text =
                            "Ligação online efetuada com sucesso.\n\n" +
                            "Atleta ID: $id\n\n" +
                            "URL:\n$url\n\n" +
                            "Pré-visualização importada."

                    }

                } catch (e: Exception) {

                    runOnUiThread {

                        resultado.text =
                            "Erro online:\n\n${e.message}"

                    }

                }

            }

        }

        root.addView(icon)
        root.addView(title)
        root.addView(subtitle)
        root.addView(atletaId)
        root.addView(botao)
        root.addView(resultado)

        scroll.addView(root)

        setContentView(scroll)
    }
}
