package com.botdroid.app

import android.content.Context
import java.text.Normalizer
import java.util.Random

object Engine {
    private val rnd = Random()
    private val lastGreet = HashMap<String, Long>()
    private val lastRule = HashMap<String, Pair<String, Long>>()
    private val lastVariant = HashMap<String, String>()
    private const val GREET_COOLDOWN = 6L * 60 * 60 * 1000
    private const val RULE_COOLDOWN = 2L * 60 * 1000

    fun norm(s: String): String {
        var t = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
        t = t.replace(Regex("[^a-z0-9]"), " ")
        t = t.replace(Regex("(.)\\1+"), "$1")
        return t.replace(Regex("\\s+"), " ").trim()
    }

    private fun matches(rule: Rule, padded: String): Boolean =
        rule.keywords.split(",").map { norm(it) }.filter { it.isNotEmpty() }
            .any { padded.contains(" $it") }

    fun match(rules: List<Rule>, text: String): Rule? {
        val padded = " " + norm(text) + " "
        val active = rules.filter { it.enabled }
        return active.firstOrNull { !it.greeting && matches(it, padded) }
            ?: active.firstOrNull { it.greeting && matches(it, padded) }
    }

    private fun pick(rule: Rule, tone: String, sender: String): String? {
        var raw = rule.replies[tone]
        if (raw.isNullOrBlank()) raw = rule.replies.values.firstOrNull { it.isNotBlank() }
        val variants = raw?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: return null
        if (variants.isEmpty()) return null
        val key = sender + "|" + rule.id
        val pool = if (variants.size > 1) variants.filter { it != lastVariant[key] } else variants
        val chosen = pool[rnd.nextInt(pool.size)]
        lastVariant[key] = chosen
        return chosen
    }

    /** Devuelve la respuesta o null (si no hay regla apropiada: no se envía nada). */
    @Synchronized
    fun respond(c: Context, sender: String, text: String, test: Boolean = false): String? {
        val rule = match(Store.rules(c), text) ?: return null
        if (!test) {
            val now = System.currentTimeMillis()
            if (rule.greeting) {
                val g = lastGreet[sender]
                if (g != null && now - g < GREET_COOLDOWN) return null
            } else {
                val l = lastRule[sender]
                if (l != null && l.first == rule.id && now - l.second < RULE_COOLDOWN) return null
            }
            lastGreet[sender] = now
            lastRule[sender] = Pair(rule.id, now)
        }
        return pick(rule, Store.tone(c), sender)
    }
}
