package com.botdroid.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object Store {
    private fun p(c: Context) = c.getSharedPreferences("botdroid", Context.MODE_PRIVATE)

    fun enabled(c: Context) = p(c).getBoolean("enabled", false)
    fun setEnabled(c: Context, v: Boolean) = p(c).edit().putBoolean("enabled", v).apply()
    fun tone(c: Context) = p(c).getString("tone", "amigable") ?: "amigable"
    fun setTone(c: Context, v: String) = p(c).edit().putString("tone", v).apply()

    fun rules(c: Context): MutableList<Rule> {
        val s = p(c).getString("rules", null)
        if (s == null) {
            val d = Defaults.rules()
            saveRules(c, d)
            return d
        }
        val out = mutableListOf<Rule>()
        val arr = JSONArray(s)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val rep = mutableMapOf<String, String>()
            val ro = o.getJSONObject("replies")
            for (k in ro.keys()) rep[k] = ro.getString(k)
            out.add(Rule(o.getString("id"), o.getString("name"), o.getString("keywords"),
                o.getBoolean("greeting"), o.getBoolean("enabled"), rep))
        }
        return out
    }

    fun saveRules(c: Context, list: List<Rule>) {
        val arr = JSONArray()
        for (r in list) {
            val o = JSONObject()
            o.put("id", r.id); o.put("name", r.name); o.put("keywords", r.keywords)
            o.put("greeting", r.greeting); o.put("enabled", r.enabled)
            val ro = JSONObject()
            for ((k, v) in r.replies) ro.put(k, v)
            o.put("replies", ro)
            arr.put(o)
        }
        p(c).edit().putString("rules", arr.toString()).apply()
    }
}
