package com.botdroid.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var statusTv: TextView
    private lateinit var rulesBox: LinearLayout
    private val toneKeys = listOf("amigable", "natural", "profesional", "comercial")
    private val toneLabels = listOf("Amigable", "Natural", "Profesional", "Comercial")

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun label(t: String, size: Float = 14f, bold: Boolean = false): TextView {
        val v = TextView(this)
        v.text = t; v.textSize = size
        if (bold) v.setTypeface(null, Typeface.BOLD)
        v.setPadding(0, dp(10), 0, dp(4))
        return v
    }

    private fun btn(t: String, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = t
        b.setOnClickListener { onClick() }
        return b
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val sv = ScrollView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(16), dp(16), dp(32))
        sv.addView(root)
        setContentView(sv)

        root.addView(label("BOT DROID", 26f, true))
        statusTv = label("")
        root.addView(statusTv)
        root.addView(btn("Dar acceso a notificaciones") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })

        val sw = Switch(this)
        sw.text = "Automatización activa (apágala para pausar)"
        sw.isChecked = Store.enabled(this)
        sw.setOnCheckedChangeListener { _, c -> Store.setEnabled(this, c) }
        root.addView(sw)

        root.addView(label("Personalidad de respuestas", 18f, true))
        val sp = Spinner(this)
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, toneLabels)
        sp.setSelection(toneKeys.indexOf(Store.tone(this)).coerceAtLeast(0))
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                Store.setTone(this@MainActivity, toneKeys[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        root.addView(sp)

        root.addView(label("Probar", 18f, true))
        val testIn = EditText(this)
        testIn.hint = "Escribe un mensaje de cliente, ej: Holaa, cuánto cuesta?"
        root.addView(testIn)
        val testOut = label("")
        root.addView(btn("Probar respuesta") {
            val r = Engine.respond(this, "prueba", testIn.text.toString(), true)
            testOut.text = r ?: "(No hay regla para este mensaje: no se enviaría nada)"
        })
        root.addView(testOut)

        root.addView(label("Reglas (toca el interruptor para activar/desactivar)", 18f, true))
        rulesBox = LinearLayout(this)
        rulesBox.orientation = LinearLayout.VERTICAL
        root.addView(rulesBox)
        root.addView(btn("Añadir regla") { edit(null) })
        root.addView(btn("Restaurar reglas de ejemplo") {
            AlertDialog.Builder(this).setMessage("Esto reemplaza todas tus reglas por las de ejemplo. ¿Continuar?")
                .setPositiveButton("Sí") { _, _ -> Store.saveRules(this, Defaults.rules()); refresh() }
                .setNegativeButton("No", null).show()
        })
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::statusTv.isInitialized) refresh()
    }

    private fun refresh() {
        val on = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) == true
        statusTv.text = if (on) "Acceso a notificaciones: concedido ✅"
        else "Acceso a notificaciones: falta concederlo ⚠️"
        rulesBox.removeAllViews()
        for (rule in Store.rules(this)) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            val s = Switch(this)
            s.text = rule.name
            s.isChecked = rule.enabled
            s.setOnCheckedChangeListener { _, c ->
                val l = Store.rules(this)
                l.find { it.id == rule.id }?.enabled = c
                Store.saveRules(this, l)
            }
            row.addView(s, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(btn("Editar") { edit(rule) })
            rulesBox.addView(row)
        }
    }

    private fun edit(existing: Rule?) {
        val r = existing ?: Rule(System.currentTimeMillis().toString(), "", "", false, true, mutableMapOf())
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(16), dp(8), dp(16), dp(8))

        fun field(hint: String, value: String, multi: Boolean): EditText {
            val e = EditText(this)
            e.hint = hint
            e.setText(value)
            if (multi) {
                e.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                e.minLines = 2
            }
            box.addView(e)
            return e
        }

        val name = field("Nombre de la regla", r.name, false)
        val kw = field("Palabras clave separadas por coma", r.keywords, true)
        val gr = CheckBox(this)
        gr.text = "Es un saludo (solo se usa si no hay otra regla)"
        gr.isChecked = r.greeting
        box.addView(gr)
        val fields = toneKeys.indices.map { i ->
            field("Respuesta ${toneLabels[i]} (una variante por línea)", r.replies[toneKeys[i]] ?: "", true)
        }

        val sc = ScrollView(this)
        sc.addView(box)
        val b = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Nueva regla" else "Editar regla")
            .setView(sc)
            .setPositiveButton("Guardar") { _, _ ->
                r.name = name.text.toString().ifBlank { "Regla" }
                r.keywords = kw.text.toString()
                r.greeting = gr.isChecked
                toneKeys.forEachIndexed { i, k -> r.replies[k] = fields[i].text.toString().trim() }
                val list = Store.rules(this)
                val idx = list.indexOfFirst { it.id == r.id }
                if (idx >= 0) list[idx] = r else list.add(0, r)
                Store.saveRules(this, list)
                refresh()
            }
            .setNegativeButton("Cancelar", null)
        if (existing != null) {
            b.setNeutralButton("Eliminar") { _, _ ->
                val list = Store.rules(this)
                list.removeAll { it.id == r.id }
                Store.saveRules(this, list)
                refresh()
            }
        }
        b.show()
    }
}
