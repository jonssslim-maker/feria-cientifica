package com.botdroid.app

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.Random

class BotListenerService : NotificationListenerService() {
    private val handler = Handler(Looper.getMainLooper())
    private val seen = HashMap<String, Long>()
    private val rnd = Random()
    private val pkgs = setOf("com.whatsapp", "com.whatsapp.w4b")

    override fun onListenerDisconnected() {
        try { requestRebind(ComponentName(this, BotListenerService::class.java)) } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in pkgs) return
        if (!Store.enabled(this)) return
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val ex = n.extras ?: return
        if (ex.getBoolean("android.isGroupConversation")) return
        val sender = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        var text = ex.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        val msgs = ex.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (msgs != null && msgs.isNotEmpty()) {
            val last = msgs.last() as? Bundle
            last?.getCharSequence("text")?.toString()?.let { text = it }
        }

        val now = System.currentTimeMillis()
        val key = sbn.key + "|" + text
        synchronized(seen) {
            val t = seen[key]
            if (t != null && now - t < 8000) return
            seen[key] = now
            seen.entries.removeAll { now - it.value > 60000 }
        }

        val reply = Engine.respond(this, sender, text) ?: return
        val found = findReply(n) ?: return
        val delay = 2000L + rnd.nextInt(3000)
        handler.postDelayed({ send(found.first, found.second, reply) }, delay)
    }

    private fun findReply(n: Notification): Pair<Notification.Action, Array<RemoteInput>>? {
        val all = ArrayList<Notification.Action>()
        n.actions?.let { all.addAll(it) }
        all.addAll(Notification.WearableExtender(n).actions)
        for (a in all) {
            val ri = a.remoteInputs
            if (ri != null && ri.isNotEmpty()) return Pair(a, ri)
        }
        return null
    }

    private fun send(a: Notification.Action, ri: Array<RemoteInput>, reply: String) {
        try {
            val intent = Intent()
            val b = Bundle()
            for (r in ri) b.putCharSequence(r.resultKey, reply)
            RemoteInput.addResultsToIntent(ri, intent, b)
            a.actionIntent.send(this, 0, intent)
        } catch (_: PendingIntent.CanceledException) {
        } catch (_: Exception) {
        }
    }
}
